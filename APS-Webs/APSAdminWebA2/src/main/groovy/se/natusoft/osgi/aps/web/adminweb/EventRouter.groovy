package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonObject
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.net.vertx.api.VertxConsumer
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.LocalEventBus
import se.natusoft.osgi.aps.tools.annotation.activator.*
import se.natusoft.osgi.aps.tools.reactive.Consumer

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
 * See EventDefinition.EVENT_SCHEMA for a definition of the event structure.
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
@OSGiServiceProvider(properties = [
        @OSGiProperty(name = "consumed", value = "vertx")
])
class EventRouter extends VertxConsumer implements Consumer<Vertx>, Constants {

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
    private Consumer.Consumed<Vertx> vertx

    /** Consumer of public event-bus messages. */
    private MessageConsumer eventConsumer

    //
    // Constructor
    //

    EventRouter() {
        this.onVertxAvailable = { Consumer.Consumed<Vertx> vertx ->
            this.vertx = vertx

            // Handles public events
            this.eventConsumer = this.vertx.get().eventBus().consumer(GLOBAL_BUS_ADDRESS).handler { Message message ->
                routePublicBusEvents(message)
            }

            // Handle local events
            this.localBus.onError { Exception e -> this.logger.error("Local bus problem!", e)}
                .onWarning {String message -> this.logger.warn(message)}
                .subscribe("adminweb.general") { Map<String, Object> localEvent ->
                   routeLocalBusEvents(localEvent)
            }
        }

        this.onVertxRevoked = {
            this.vertx = null
            this.logger.error("Vertx just got revoked! This event router will thereby not work until a new Vertx is provided.")
        }
    }

    //
    // Initializer
    //

    /**
     * Setup. This is called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.connectToLogService(this.context)
    }

    //
    // Methods
    //

    /**
     * This takes pubic messages from client(s) and routes specific messages to the local bus for servicing
     * client requests.
     *
     * @param eventMessage A received public messages. These can be both from client(s) and other service instances.
     */
    @SuppressWarnings("PackageAccessibility")
    private void routePublicBusEvents(Message eventMessage) {
        // Convert from JSON string to a Map<String, Object> which can be used almost like client side JSON by Groovy.
        Map<String, Object> event = null
        if (JsonObject.class.isAssignableFrom(eventMessage.body().class)) {
            event = (eventMessage.body() as JsonObject).map
        } else if (String.class.isAssignableFrom(eventMessage.body().class)) {
            event = new JsonObject(eventMessage.body().toString()).map
        } else {
            sendReply(
                    EventDefinition.createError(
                            event,
                            [code: 2, message: "Bad format of message body! Must be JsonObject or String!"] as Map<String, Object>)
                    ,
                    eventMessage
            )
            return
        }

        if (event['type'] == "service") {
            this.localBus.publish(LOCAL_BUS_ADDRESS, event)

            if (event['reply'] != null) {
                sendReply(event['reply'] as Map<String, Object>, eventMessage)
            }
        }
    }

    /**
     * Sends a reply.
     *
     * @param reply The reply to send.
     * @param eventMessage The original event message.
     */
    private static void sendReply(Map<String, Object> reply, Message eventMessage) {
        JsonObject replyJson = new JsonObject(reply)
        if (eventMessage.replyAddress() != null && !eventMessage.replyAddress().empty) {
            eventMessage.reply(replyJson)
        }
    }

    /**
     * Routes messages sent on local bus, intended for client to the public bus. All other messages are ignored.
     *
     * @param event The event to route. This event is local to the application.
     */
    private void routeLocalBusEvents(Map<String, Object> event) {
        if (GLOBAL_BUS_ADDRESS == event[_address_] && CLASSIFIER_PUBLIC == event[_classifier_]) {
            this.vertx.get().eventBus().send(GLOBAL_BUS_ADDRESS, new JsonObject(event))
        }
    }

    /**
     * Unregisters ourself as consumer on event bus.
     */
    private void stopEventConsumption() {
        if (this.eventConsumer != null) {
            this.eventConsumer.unregister { AsyncResult res ->

                if (res.succeeded()) {
                    this.logger.info "Unregistered 'aps-admin-web' event consumer!"
                    this.eventConsumer = null
                } else {
                    this.logger.error "Failed to unregister 'aps-admin-web' event consuemr! [${res.cause()}]"
                }
            }
        }
    }

    /**
     * Now we are really shutting down.
     */
    @BundleStop
    void shutdown() {
        this.logger.info("Shutting down!")
        stopEventConsumption()
        if (this.vertx != null) this.vertx.release()
    }

}
