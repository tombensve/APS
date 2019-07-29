package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.framework.ServiceRegistration
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.osgi.aps.activator.APSActivatorInteraction
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider
import se.natusoft.osgi.aps.api.messaging.APSBusRouter
import se.natusoft.osgi.aps.api.messaging.APSMessagingException
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.vertx.util.RecursiveJsonObjectMap
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSLogger

/**
 * Provides and APSBusRouter implementation using Vert.x EventBus for communication.
 *
 * This uses "cluster:" as target id. When calling send(...) however an additional "all:"
 * can be added before address to do a publish rather than send.
 */
@CompileStatic
@TypeChecked
@SuppressWarnings( "unused" )
// Managed by APSActivator IDE can't see that!
@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-vertx-bus-router" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
                @OSGiProperty( name = APS.Messaging.Protocol.Name, value = "vertx-eventbus" ),
                @OSGiProperty( name = APS.Messaging.Persistent, value = APS.FALSE ),
                @OSGiProperty( name = APS.Messaging.Clustered, value = APS.TRUE )
        ]
)
class APSVertxBusRouter implements APSBusRouter {

    private static final String TARGET_ID = "cluster:"

    //
    // Private members
    //

    /** For logging. */
    @Managed( name = "logger", loggingFor = "aps-vertx-bus-router" )
    private APSLogger logger

    /** The current subscriptions */
    private Map<ID, MessageConsumer> subscriptions = [:]

    /**
     * We manage the registration of our service our self through the APSActivatorInteraction API. There will
     * be one ServiceRegistration put in this list since we only have one instance.
     *
     * When services are injected by APSActivator, as service rather than an APSServiceTracker<Service>, then
     * a java.lang.reflect.Proxy:ied version is injected. This proxy uses the APSServiceTrackers active service,
     * allocates it, calls the method, and then releases the service again, and then returns the result. However
     * if the tracker has no found service then the proxy call is put into a List, and when the tracker has
     * received a service then all entries in this list are executed against the service, and the list is then
     * emptied. This allows for calls to be made to a wrapped/proxied service instance even if the real service
     * is not yet available. This however only works for reactive APIs that has no return values, for obvious
     * reasons.
     *
     * The EventBus does not fulfil that requirement. Thereby we remove our self by unregistering us as an
     * OSGi service when we loose the EventBus. Since the API for this service completely to 100% fulfils
     * this requirement, clients of this can still do proxied (and cached) calls while we are away, and they
     * will be executed when we are available again. **Do note** however that the nonBlocking=true attribute
     * must be set on the @OSGiService annotation when used like this.
     *
     * All this of course assumes that a service goes away only because it is being restarted / upgraded, and
     * will rather quickly be available again.
     **/
    protected List<ServiceRegistration> svcRegs = []

    /** Our bundles context. */
    @Managed
    protected BundleContext context

    /**
     * This tracks the EventBus. init() will setup an onActiveServiceAvailable callback handler which
     * will provide the eventBus instance.*/
    @OSGiService( additionalSearchCriteria = "(vertx-object=EventBus)", timeout = "30 sec" )
    protected APSServiceTracker<EventBus> eventBusTracker
    protected EventBus eventBus

    /** Used to delay service registration. */
    @Managed( name = "APSVertxBusRouterAI" )
    protected APSActivatorInteraction activatorInteraction

    //
    // Methods
    //

    /**
     * This is run by APSActivator when all @Managed & @OSGiService annotated fields have been injected.
     */
    @SuppressWarnings( "DuplicatedCode" )
    @Initializer
    void init() {
        // Yes, what these handlers do could be done directly below in onActiveServiceAvailable {...} instead
        // of changing state. This is however more future safe.
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.READY ) {
            this.activatorInteraction.registerService( APSVertxBusRouter.class, this.context, this.svcRegs )
            this.logger.debug( ">>>> Registered ${ APSVertxBusRouter.class.simpleName } as service!" )
        }
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.TEMP_UNAVAILABLE ) {
            this.svcRegs.first().unregister() // We only have one instance.
            this.svcRegs.clear()
            this.logger.debug( ">>>> Unregistered ${ APSVertxBusRouter.class.simpleName } as service!" )
        }

        this.eventBusTracker.onActiveServiceAvailable { EventBus service, ServiceReference serviceReference ->
            this.logger.debug( ">>>> received eventbus!" )

            this.eventBus = service

            this.activatorInteraction.state = APSActivatorInteraction.State.READY
        }
        this.eventBusTracker.onActiveServiceLeaving { ServiceReference service, Class serviceAPI ->
            this.logger.debug( ">>>> Lost eventbus!" )

            this.eventBus = null

            this.activatorInteraction.state = APSActivatorInteraction.State.TEMP_UNAVAILABLE
        }
    }

    /**
     * This checks if provided target is valid and if so proceeds with the operation.
     *
     * @param target Target to validate.
     * @param go Closure to call on valid target.
     */
    private static void validTarget( String target, Closure go ) {
        if ( target.startsWith( TARGET_ID ) ) {
            target = target.substring( TARGET_ID.length() )

            go.call( target )
        }
        else if ( target.startsWith( "all:" ) ) {
            target = target.substring( 4 )

            go.call( target )
        }
        // Note that since APSBus will call all APSBusRouter implementations found, receiving
        // and invalid target is nothing strange. We should only react on those that we recognize.
    }

    /**
     * Sends a message.
     *
     * @param target The target to send to. In this case it should start with cluster: and what comes after
     *               that is taken as the Vert.x EventBus address. If the target starts with
     *               "cluster:all:" then the "all:" part is also removed and the messages is published
     *               rather than sent.
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler The handler to call with result of operation. Can be null!
     */
    @Override
    void send( @NotNull String target, @NotNull Map<String, Object> message, @Optional @Nullable APSHandler<APSResult> resultHandler ) {

        validTarget( target ) { String realTarget ->

            if ( realTarget.startsWith( "all:" ) ) {

                String pubTarget = realTarget.substring( 4 )
                this.eventBus.publish( pubTarget, new JsonObject( message ) )
            }
            else {
                this.eventBus.send( realTarget, new JsonObject( message ) )
            }

            resultHandler.handle( APSResult.success( null ) )
        }
    }

    /**
     * Subscribes to messages to a target.
     *
     * @param id A unique ID to associate subscription with. Also used to unsubscribe.
     * @param target The target to subscribe to.
     * @param resultHandler The result of the subscription.
     * @param messageHandler The handler to call with messages sent to target.
     */
    @Override
    void subscribe( @NotNull ID id, @NotNull String target, @Optional @Nullable APSHandler<APSResult> resultHandler, @NotNull APSHandler<Map<String, Object>> messageHandler ) {

        validTarget( target ) { String realTarget ->

            MessageConsumer consumer = this.eventBus.consumer( target ) { Message<JsonObject> msg ->

                Map<String, Object> message = new RecursiveJsonObjectMap( msg.body() )
                messageHandler.handle( message )
            }

            if ( consumer == null ) { // Not sure this can actually happen ...
                resultHandler.handle( APSResult.failure( new APSMessagingException( "Failed to get MessageConsumer!" ) ) )
            }
            else {
                // Cannot use "this.subscriptions" reference since we are within a
                // closure. Groovy does not support the java variant of <Class>.this.ref.
                // The value is available to the closure, you just cannot make an absolute
                // failsafe reference to it.
                subscriptions[ id ] = consumer
            }
        }
    }

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    @Override
    void unsubscribe( @NotNull ID subscriberId ) {
        MessageConsumer consumer = this.subscriptions[ subscriberId ]
        consumer.unregister()
    }

    @BundleStop
    void cleanup() {
        // This is not an excuse for clients to not clean up after themselves! And this will not be done
        // until we shut down.
        this.subscriptions.keySet().each { ID key ->
            this.subscriptions[ key ].unregister()
        }
        this.subscriptions.clear()
    }

}
