package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.eventbus.EventBus
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.framework.ServiceRegistration
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider
import se.natusoft.osgi.aps.api.messaging.APSMessagePublisher
import se.natusoft.osgi.aps.api.messaging.APSMessagingException
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.model.APSHandler
import se.natusoft.osgi.aps.model.APSResult
import se.natusoft.osgi.aps.activator.APSActivatorInteraction
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.*

// MessageType will always be Object!
@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@TypeChecked
@OSGiServiceProvider(
        // Possible criteria for client lookups. ex: "(aps-messaging-protocol=vertx-eventbus)" In most cases clients
        // won't care.
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value =
                        "aps-vertx-event-bus-messaging-provider:publisher" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
                @OSGiProperty( name = APS.Messaging.Protocol.Name, value = "vertx-eventbus" ),
                @OSGiProperty( name = APS.Messaging.Persistent, value = APS.FALSE ),
                @OSGiProperty( name = APS.Messaging.Clustered, value = APS.TRUE )
        ]
)
class MessagePublisherProvider<MessageType> extends AddressResolver implements APSMessagePublisher<MessageType> {

    //
    // Private members
    //

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
    private List<ServiceRegistration> svcRegs = [ ]

    /** For logging. */
    @Managed( name = "publisher", loggingFor = "aps-vertx-event-bus-messaging-provider:publisher" )
    private APSLogger logger

    /** Our bundles context. */
    @Managed
    private BundleContext context

    /**
     * This tracks the EventBus. init() will setup an onActiveServiceAvailable callback handler which
     * will provide the eventBus instance.*/
    @OSGiService( additionalSearchCriteria = "(vertx-object=EventBus)", timeout = "30 sec" )
    private APSServiceTracker<EventBus> eventBusTracker
    private EventBus eventBus

    /** Used to delay service registration. */
    @Managed( name = "publisherAI" )
    private APSActivatorInteraction activatorInteraction

    //
    // Methods
    //

    /**
     * This is run by APSActivator when all @Managed & @OSGiService annotated fields have been injected.*/
    @Initializer
    void init() {
        // Yes, what these handlers do could be done directly below in onActiveServiceAvailable {...} instead
        // of changing state. This is however more future safe.
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.READY ) {
            this.activatorInteraction.registerService( MessagePublisherProvider.class, this.context, this.svcRegs )
        }
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.TEMP_UNAVAILABLE ) {
            this.svcRegs.first().unregister()
            this.svcRegs.clear()
        }

        this.eventBusTracker.onActiveServiceAvailable { EventBus service, ServiceReference serviceReference ->
            this.eventBus = service

            //            this.logger.info( ">>>>>> Got EventBus: ${this.eventBus}" )

            this.activatorInteraction.state = APSActivatorInteraction.State.READY
        }
        this.eventBusTracker.onActiveServiceLeaving { ServiceReference service, Class serviceAPI ->
            this.eventBus = null

            //            this.logger.info( "<<<<<< Lost EventBus!" )

            this.activatorInteraction.state = APSActivatorInteraction.State.TEMP_UNAVAILABLE
        }
    }

    /**
     * Publishes a message.
     *
     * @param destination The destination for the message. Will be translated to an address or used as is.
     * @param message The message to publish.
     *
     * @throws APSMessagingException on any failure. Note that this is a RuntimeException!
     */
    @Override
    void publish( String destination, MessageType message ) throws APSMessagingException {
        //        this.logger.error("@@@@@@@@ THREAD: ${Thread.currentThread(  )}")
        //        this.logger.info( "####### In publish(dest, message)!" )
        String address = resolveAddress( destination )

        this.eventBus.publish( address, TypeConv.apsToVertx( message ) )
        //        this.logger.info( "####### Published(NH):${eventBus}" )

    }

    /**
     * Publishes a message receiving a result of success or failure. On Success there
     * can be a result value and on failure there is an Exception describing the failure
     * available. This variant never throws an Exception.
     *
     * Providing this variant is optional. When not supported an APSResult containing an
     * APSUnsupportedException and a success() value of false should be the result. That
     * this is not supported should also be made very clear in the documentation of the
     * providing implementation.
     *
     * @param destination The destination for the message. Will be translated to an address or used as is.
     * @param message The message to publish.
     */
    @Override
    void publish( String destination, MessageType message, APSHandler<APSResult<MessageType>> result ) {
        String address = resolveAddress( destination )

        //        this.logger.error("@@@@@@@@ THREAD: ${Thread.currentThread(  )}")
        try {

            this.eventBus.publish( address, TypeConv.apsToVertx( message ) )

            if ( result != null ) {
                result.handle( APSResult.success( null ) as APSResult<MessageType> )
            }
            else {
                this.logger.warn( "Call made to publish(message, resultHandler) without a result handler!" )
            }
        }
        catch ( Exception e ) {

            if ( result != null ) {
                result.handle( APSResult.failure( e ) )
            }
            else {
                this.logger.error( e.message, e )
            }
        }
    }
}
