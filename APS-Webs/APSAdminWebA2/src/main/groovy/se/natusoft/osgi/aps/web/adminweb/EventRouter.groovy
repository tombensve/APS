package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.reactivex.disposables.Disposable
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.eventbus.EventBus
import io.vertx.groovy.core.eventbus.Message
import io.vertx.groovy.core.eventbus.MessageConsumer
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.reactive.ObjectConsumer
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider
import static se.natusoft.osgi.aps.web.adminweb.Constants.*

/**
 * This service listens to both public Vertx event-bus messages, and internal 'LocalEventBus' messages.
 * It routes specific messages from one to the other.
 *
 * This application is 100% event driven! Certain events are handled internally on the server side, while others
 * are forwarded to the Vertx cluster event-bus, which are also received by clients connecting to the event-bus
 * via the SockJS event-bus bridge. The clients are of course also producers of events to be handled by the server
 * side.
 *
 * So this web application is very odd compared to how such usually work by using REST requests. This specific web
 * application is very simple in what it does so much here is overkill, but this serves as a "proof of concept" for
 * a completely event driven web application.
 *
 * An internal and a public event bus are used so that local, internal messages aren't unnecessarily sent out on the
 * network. Local services only uses the local bus, it is only this router that manages to and from messages to the
 * public event-bus.
 *
 * The message structure looks like this:
 *
 *     {
 *         "type": "service" / ...,
 *         "classifier": "public" / "private",
 *         "action": "(action)"
 *         ...
 *     }
 *
 * ### Suppressed Warnings
 *
 * This applies to IDEA and probably other IDEs too.
 *
 * __GroovyUnusedDeclaration__
 *
 * There are complains that this class is not used, this because it is managed by APSActivator and the IDE
 * cannot see the code that creates and manages this instance.
 *
 * __PackageAccessibility__
 *
 * This is an OSGi issue. OSGi imports and exports packages, and to be deployable a jar must contain a
 * valid MANIFEST.MF with OSGi keys for imports, exports, etc. Must 3rd party jars do contain a valid
 * OSGi MANIFEST.MF exporting all packages of the jar sp that they can just be dropped into an OSGi
 * container and have their classpath be made available to all other code running in the container.
 *
 * The Groovy Vertx wrapper code does not contain a valid OSGi MANIFEST.MF. I have solved this by having
 * the aps-vertx-provider bundle include the Groovy Vertx wrapper, and export all packages of that
 * dependency. So as long as the aps-vertx-provider is deployed the Groovy Vertx wrapper code will
 * also be available runtime. IDEA however does not understand this. It does not figure out the
 * exported dependency from aps-vertx-provider either. So it sees code that is not OSGi compatible
 * and used in the code without including the dependency jar in the bundle, and complains about
 * that. But since in reality this code will be available at runtime I just hide these incorrect
 * warnings.
 */
@SuppressWarnings(["GroovyUnusedDeclaration", "PackageAccessibility"])
@CompileStatic
@TypeChecked
@OSGiServiceProvider( properties = [ @OSGiProperty( name = "consumed", value = "vertx" ) ] )
class EventRouter implements ObjectConsumer<Vertx> , Constants {

    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed(loggingFor = "aps-admin-web-a2:event-router")
    private APSLogger logger

    /** Our local in application event bus, implemented by an 'RxJava PublishSubject. */
    @Managed
    private LocalEventBus localBus

    /** A Vertx instance. Received in onObjectAvailable(...). */
    private ObjectConsumer.ObjectHolder<Vertx> vertx

    /** Our public event-bus. */
    private EventBus eventBus

    /** Consumer of public event-bus messages. */
    private MessageConsumer eventConsumer

    /** Consumer of local bus messages. */
    private Disposable localConsumer

    //
    // Initializer
    //

    /**
     * Setup. This is called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.connectToLogService( this.context )
    }

    //
    // Methods
    //

    /**
     * Specific options for the consumer.
     */
    @Override
    Properties consumerOptions() { return null }

    /**
     * Called with requested object type when available.
     *
     * @param object The received object.
     */
    @SuppressWarnings("PackageAccessibility")
    @Override
    void onObjectAvailable( ObjectConsumer.ObjectHolder<Vertx> vertx ) {
        this.vertx = vertx

        this.eventBus = this.vertx.use().eventBus()

        // Handles public events
        this.eventConsumer = eventBus.consumer( BusEvent._Address.PUBLIC ).handler { Message message ->
            routePublicBusEvents(message)
        }

        // Handle local events
        this.localConsumer = this.localBus.consume { Map<String, Object> localEvent ->
            routeLocalBusEvents(localEvent)
        }

    }

    /**
     * This takes pubic messages from client(s) and routes specific messages to the local bus for servicing
     * client requests.
     *
     * @param eventMessage A received public messages. These can be both from client(s) and other service instances.
     */
    @SuppressWarnings( "PackageAccessibility" )
    private void routePublicBusEvents( Message eventMessage ) {
        // Convert from JSON string to a Map<String, Object> which can be used almost like client side JSON by Groovy.
        Map<String, Object> event
        if ( JsonObject.class.isAssignableFrom( eventMessage.body().class ) ) {
            event = (eventMessage.body() as JsonObject).map
        }
        else {
            event = new JsonObject( eventMessage.body().toString() ).map
        }

        if ( event [ 'type' ] == "service" ) {
            this.localBus.send(event)

            if ( event [ 'reply' ] != null ) {
                Map<String, Object> reply = event[ 'reply' ] as Map<String, Object>
                JsonObject replyJson = new JsonObject(reply)
                if ( eventMessage.replyAddress() != null && !eventMessage.replyAddress().empty ) {
                    eventMessage.reply(replyJson)
                }
                else {
                    this.eventBus.send( BusEvent._Address.PUBLIC , replyJson )
                }
            }
        }
    }

    /**
     * Routes messages sent on local bus, intended for client to the public bus. All other messages are ignored.
     *
     * @param event The event to route. This event is local to the application.
     */
    private void routeLocalBusEvents( Map<String, Object> event ) {
        if ( BusEvent._Address.PUBLIC == event [ BusEvent.Address ] && BusEvent._Classifier.PUBLIC == event [ BusEvent.Classifier ] ) {
            this.eventBus.send( BusEvent._Address.PUBLIC , new JsonObject( event ) )
        }
    }

    /**
     * Unregisters ourself as consumer on event bus.
     */
    private void stopEventConsumption() {
        if ( this.eventConsumer != null ) {
            this.eventConsumer.unregister { AsyncResult res ->

                if ( res.succeeded() ) {
                    this.logger.info "Unregistered 'aps-admin-web' event consumer!"
                    this.eventConsumer = null
                }
                else {
                    this.logger.error "Failed to unregister 'aps-admin-web' event consuemr! [${res.cause()}]"
                }
            }
        }
        if ( this.localConsumer != null ) {
            this.localConsumer.dispose()
            this.localConsumer = null
        }
    }

    /**
     * Called when there is a failure to deliver requested object.
     */
    @Override
    void onObjectUnavailable() {
        this.logger.error "Failed to setup local event router due to no Vertx instance available!"
    }

    /**
     * Called if/when a previously made available Vertx object is no longer valid.
     *
     * Do note that we do not need to shut down due to this! We can just wait for a new Vertx instance to be delivered.
     */
    @Override
    void onObjectRevoked() {
        this.logger.error "Vertx instance have been revoked!"
        stopEventConsumption()
        this.vertx = null
    }

    /**
     * Now we are really shutting down.
     */
    @BundleStop
    void shutdown() {
        this.logger.info("Shutting down!")
        stopEventConsumption()
        this.vertx = null
        this.eventBus = null
    }
}
