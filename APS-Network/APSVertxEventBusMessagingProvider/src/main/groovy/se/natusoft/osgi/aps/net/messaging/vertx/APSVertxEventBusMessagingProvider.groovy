package se.natusoft.osgi.aps.net.messaging.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.eventbus.EventBus
import io.vertx.groovy.core.eventbus.Message
import io.vertx.groovy.core.eventbus.MessageConsumer
import org.osgi.framework.BundleContext
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.tools.APSActivatorInteraction
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

// Note to IDEA users: IDEA underlines (in beige color if you are using Darcula theme) all references to classes that are not
// OSGi compatible (no OSGi MANIFEST.MF entries). The underlines you see here is for the Groovy wrapper of Vert.x. It is OK
// since this wrapper gets included in the bundle. The main Vert.x code is OSGi compliant and can be deployed separately.

/**
 * Provides messaging using vertx. In this case only a clustered event bus!
 */
@SuppressWarnings( "GroovyUnusedDeclaration" ) // This is never referenced directly, only through APSMessageService API.
@OSGiServiceProvider(
        // Possible criteria for client lookups. ex: "(${APS.Messaging.Protocol.Name}=vertx-eventbus)" In most cases clients won't care.
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-vertx-event-bus-messaging-provider" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
                @OSGiProperty( name = APS.Messaging.Protocol.Name, value = "vertx-eventbus" )
        ]
)
@CompileStatic
@TypeChecked
class APSVertxEventBusMessagingProvider implements APSMessageService {

    //
    // Private Members
    //

    /** Our bundle context. */
    @Managed
    BundleContext context

    /** For logging. */
    @Managed(loggingFor = "aps-vertx-messaging-provider[event bus]")
    private APSLogger logger

    /** This is used to interact with APSActivator, which is our BundleActivator. This forces delayed registration of service. */
    @Managed
    private APSActivatorInteraction interaction

    /** The listeners of this service. */
    private Map<String, List<APSMessageService.Listener>> listeners = [ : ]

    /** We have one consumer per topic towards Vert.x. If we have more that one listener on a topic we handle that internally. */
    private Map<String, MessageConsumer> consumers = [ : ]

    /** The root of all Vert.x! */
    private Vertx vertx = null

    /** The clustered event bus we are communicating over. */
    private EventBus eventBus = null

    //
    // Initializer
    //

    /**
     * Setup. This is called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.start( this.context ) // Connect to OSGi log service if available.

        Map<String, Object> options = [ workerPoolSize:40 ] as HashMap < String, Object >

        Vertx.clusteredVertx( options, { AsyncResult < Vertx > res ->
            if ( res.succeeded() ) {
                this.vertx = res.result()
                this.eventBus = this.vertx.eventBus()

                // Notify APSActivator that we are ready to work. APSActivator will register this as service with OSGi on this state.
                this.interaction.state = APSActivatorInteraction.State.READY

                this.logger.info "Vert.x cluster started successfully!"
            }
            else {
                this.interaction.state = APSActivatorInteraction.State.STARTUP_FAILED
                this.logger.error "Vert.x cluster failed to start: ${res.cause()}, shutting down bundle!"
                this.context.bundle.stop()
            }
        })
    }

    /**
     * This gets called when out bundle gets stopped. In this case we need to shut down Vert.x.
     */
    @BundleStop
    void stop() {
        if ( this.eventBus != null ) {
            this.eventBus = null
        }

        if ( this.vertx != null ) {
            this.vertx.close { AsyncResult res ->

                if ( res.succeeded() ) {
                    this.logger.info "Vert.x successfully shut down!"
                }
                else {
                    this.logger.error "Vert.x failed to shut down! [${res.cause()}]"
                }
            }
        }
    }

    //
    // Methods
    //

    /**
     * Returns a List of topic listeners, creating an empty if none exists.
     *
     * @param topic The topic to get listeners for.
     */
    private @NotNull List<APSMessageService.Listener> getListenersForTopic(@NotNull String topic ) {
        List<APSMessageService.Listener> topicListeners = this.listeners [ topic ]

        if ( topicListeners == null ) {
            topicListeners = new LinkedList<>()
            this.listeners [ topic ] = topicListeners
        }

        return topicListeners
    }

    /**
     * Sends a message to the destination.
     *
     * @param topic The destination to send message.
     * @param message The message to send.
     * @param reply If the underlying message mechanism supports replies to specific messages such will be delivered to
     *              this listener. Can be null.
     */
    @Override
    void publish( @NotNull String topic, @NotNull Object message, @NotNull APSMessageService.Receivers receivers ) {
        switch ( receivers ) {
            case APSMessageService.Receivers.ALL:
                this.eventBus.publish topic, message
                break

            case APSMessageService.Receivers.ONE:
                this.eventBus.send topic, message, { AsyncResult < Message > res ->
                    if ( res.succeeded() ) {
                        this.listeners [ topic ] ?. each { APSMessageService.Listener listener ->
                            listener.messageReceived res.result().body()
                        }
                    }
                }
                break
        }
    }

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param topic The endpoint to listen to.
     * @param listener The listener to call with received messages.
     */
    @Override
    void subscribe( @NotNull String topic, @NotNull APSMessageService.Listener listener ) {
        List<APSMessageService.Listener> topicListeners = getListenersForTopic topic
        boolean newTopic = topicListeners.size() == 0

        if ( !topicListeners.contains( listener ) ) {
            topicListeners.add listener
        }

        // We only need to do this once for each topic.
        if (newTopic) {
            MessageConsumer consumer = this.consumers [ topic ]

            if (consumer == null) {
                consumer = this.eventBus.consumer( topic ).handler { Message message ->

                    getListenersForTopic message.address() each { APSMessageService.Listener toListener ->
                        toListener.messageReceived message.body()
                    }
                }
                this.consumers [ topic ] = consumer
            }
        }
    }

    /**
     * Removes a listener for a source.
     *
     * @param topic The endpoint to remove listener for.
     * @param listener The listener to remove.
     */
    @Override
    void unsubscribe( @NotNull String topic, @NotNull APSMessageService.Listener listener ) {
        List<APSMessageService.Listener> topicListeners = getListenersForTopic topic
        topicListeners.remove listener

        if ( topicListeners.isEmpty() ) {
            MessageConsumer consumer = this.consumers[ topic ]
            if ( consumer == null ) throw new APSMessagingException("There is no consumer for topic '${topic}!")

            consumer.unregister { AsyncResult res ->

                if ( res.succeeded() ) {
                    this.logger.info "Unregistered '${topic}' on all nodes!"
                }
                else {
                    this.logger.error "Failed to unregister '${topic}' on all nodes! [${res.cause()}]"
                }
            }

            // Doesn't look like it is possible to close a MessageConsumer! When calling this.eventBus.consumer(...) however
            // a new consumer will be crated! So better keep the one we have.
        }
    }

}
