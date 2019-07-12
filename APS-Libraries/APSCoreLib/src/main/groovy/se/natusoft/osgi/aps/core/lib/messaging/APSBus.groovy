package se.natusoft.osgi.aps.core.lib.messaging

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.BundleContext
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.NotUsed
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.docutations.Reactive
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
 *
 * Note that even if there are no APSBusRouter services published, the APSLocalInMemoryBus,
 * which is a router, is always available. It however only reacts on targets with target id
 * "local:".
 *
 * To make other buses available through APSBus:
 * - Implement APSBusRouter and publish as OSGi service.
 *
 * @See APSBusRouter
 */
@SuppressWarnings( "unused" )
@CompileStatic
@TypeChecked
class APSBus {

    //
    // Private Members
    //

    /** The currently known bus routers */
    @Nullable
    private APSServiceTracker<APSBusRouter> routerTracker = null

    //
    // Constructors
    //

    /**
     * Creates a new APSBus tracking all deployed bus routers.
     *
     * @param bundleContext The bundle context of the caller.
     */
    APSBus( @NotNull BundleContext bundleContext ) {

        this.routerTracker = new APSServiceTracker<>( bundleContext, APSBusRouter.class )
        this.routerTracker.start(  )
    }

    /**
     * This constructor will not track published APSBusRouter services. It will have only
     * one rotuer, APSLocalInMemoryBus. This is intended for testing.
     */
    @SuppressWarnings( "WeakerAccess" )
    APSBus() {}

    //
    // Methods
    //

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

            APSLocalInMemoryBus.ROUTER.send( _target, message, resultHandler )

            if ( this.routerTracker != null ) {
                this.routerTracker.withAllAvailableServices() { APSBusRouter apsBusRouter, @NotUsed Object[] args ->
                    apsBusRouter.send( target, message, resultHandler )
                }
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

        APSLocalInMemoryBus.ROUTER.subscribe( id, target, resultHandler, messageHandler )

        if ( this.routerTracker != null ) {
            this.routerTracker.withAllAvailableServices() { APSBusRouter apsBusRouter, @NotUsed Object[] args ->
                apsBusRouter.subscribe( id, target, resultHandler, messageHandler )
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

        APSLocalInMemoryBus.ROUTER.unsubscribe( subscriberId )

        if ( this.routerTracker != null ) {
            this.routerTracker.withAllAvailableServices() {
                APSBusRouter apsBusRouter, Object[] args ->

                    apsBusRouter.unsubscribe( subscriberId )
            }
        }
    }

    @Reactive
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

    void cleanup() {
        this.routerTracker.stop(  )
    }
}
