package se.natusoft.osgi.aps.core.lib.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration
import se.natusoft.docutations.*
import se.natusoft.osgi.aps.activator.APSActivatorInteraction
import se.natusoft.osgi.aps.activator.annotation.*
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.api.messaging.APSBus
import se.natusoft.osgi.aps.api.messaging.APSBusRouter
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.APSUUID
import se.natusoft.osgi.aps.types.ID

/**
 * This is a simple bus API that is used by creating an instance and passing a BundleContext.
 *
 * All calls will be passed to all the APSBusRouter implementations tracked. Also see the
 * javadoc for that interface.
 */
@SuppressWarnings( "unused" )
@CompileStatic
@TypeChecked
@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-bus-provider" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Communication ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
        ]
)
class APSBusProvider implements APSBus {

    //
    // Private Members
    //

    private List<ServiceRegistration> svcRegs = []

    @Managed
    private BundleContext context

    @Managed
    private APSActivatorInteraction activatorInteraction

    /** The currently known bus routers */
    @OSGiService( serviceAPI = APSBusRouter.class )
    private APSServiceTracker<APSBusRouter> routerTracker

    //
    // Constructors
    //


    /**
     * This constructor will not track published APSBusRouter services. It will have only
     * one rotuer, APSLocalInMemoryBus. This is intended for testing.
     */
    @SuppressWarnings( "WeakerAccess" )
    APSBusProvider() {}

    //
    // Methods
    //

    @Initializer
    void init() {

        this.activatorInteraction.state = APSActivatorInteraction.State.TEMP_UNAVAILABLE

        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.READY ) {

            this.activatorInteraction.registerService( APSBusProvider.class, this.context, this.svcRegs )
        }

        // Make sure this service is not made available until we have at least one bus router.
        Thread.start {
            while ( this.routerTracker.trackedServiceCount == 0 ) {
                Thread.sleep( 1000 )
            }

            this.activatorInteraction.state = APSActivatorInteraction.State.READY
        }
    }

    /**
     * Cleanup on shutdown since we manage our own service registration.
     */
    @BundleStop
    void shutdown() {
        if ( !this.svcRegs.empty ) this.svcRegs.first().unregister()
    }

    /**
     * Sends a message.
     *
     * @param target The target to send to. Note that for send you can give multiple, comma separated
     *        targets to send same message to multiple places.
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler Receives the success or failure of the call.
     */
    @Reactive
    void send( @NotNull String target, @NotNull Map<String, Object> message,
               @Optional @Nullable APSHandler<APSResult> resultHandler ) {

        target.split( "," ).each { String _target ->

            this.routerTracker.withAllAvailableServices() { APSBusRouter apsBusRouter, @NotUsed Object[] args ->
                apsBusRouter.send( _target.trim(), message, resultHandler )
            }
        }
    }

    /**
     * Subscribes to messages to a target.
     *
     * @param id A unique ID to associate subscription with. Also used to unsubscribe.
     * @param target The target to subscribe to.
     * @param messageHandler The handler to call with messages sent to target.
     */
    @Reactive
    void subscribe( @NotNull ID id, @NotNull String target, @Optional @Nullable APSHandler<APSResult> resultHandler,
                    @NotNull APSHandler<Map<String, Object>> messageHandler ) {

        target.split( "," ).each { String _target ->

            this.routerTracker.withAllAvailableServices() { APSBusRouter apsBusRouter, @NotUsed Object[] args ->
                apsBusRouter.subscribe( id, _target.trim(), resultHandler, messageHandler )
            }
        }
    }


    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    @Reactive
    void unsubscribe( @NotNull ID subscriberId ) {

        this.routerTracker.withAllAvailableServices() {
            APSBusRouter apsBusRouter, Object[] args ->

                apsBusRouter.unsubscribe( subscriberId )
        }
    }

    /**
     * Sends a message and expects to get a response message back.
     *
     * This is not forwarded to a APSBusRouter! This is locally implemented
     * and does the following:
     *
     * - Generates a unique reply address.
     * - Subscribes to address.
     *   - After reply message is received and forwarded to handler, the
     *     message subscription is unsubscribed.
     * - Updates message header.replyAddress with address
     * - Sends message.
     *
     * This should theoretically work for any APSBusRouter implementation. For
     * some it might not make sense however.
     *
     * @param target The target to send to.
     * @param message The message to send.
     * @param resultHandler optional handler to receive result of send.
     * @param responseMessage A message that is a response of the sent message.
     */
    @Reactive
    void request( @NotNull String target, @NotNull Map<String, Object> message,
                  @Nullable @Optional APSHandler<APSResult> resultHandler,
                  @NotNull APSHandler<Map<String, Object>> responseMessage ) {

        try {
            String replyAddr = "local:" + new APSUUID().toString()
            message[ 'header' ][ 'replyAddress' ] = replyAddr

            ID subID = new APSUUID()

            this.subscribe( subID, replyAddr ) { APSResult res ->

                if ( resultHandler != null ) {
                    resultHandler.handle( res )
                }

            } { Map<String, Object> reply ->

                try {
                    responseMessage.handle( reply )
                }
                finally {
                    this.unsubscribe( subID )
                }
            }

            send( target, message, resultHandler )
        }
        catch ( Exception e ) {
            if ( resultHandler != null ) {
                resultHandler.handle( APSResult.failure( e ) )
            }
        }

    }

}
