/* 
 * 
 * PROJECT
 *     Name
 *         APS Vertx Event Bus Messaging Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides an implementation of APSMessageService using Vert.x event bus.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2017-01-01: Created!
 *         
 */
package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.eventbus.EventBus
import io.vertx.groovy.core.eventbus.Message
import io.vertx.groovy.core.eventbus.MessageConsumer
import org.osgi.framework.BundleContext
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.api.net.messaging.service.APSSubscriber
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.vertx.api.APSVertxService
import se.natusoft.osgi.aps.tools.APSActivatorInteraction
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

// Note to IDEA users: IDEA underlines (in beige color if you are using Darcula theme) all references to classes that are not
// OSGi compatible (no OSGi MANIFEST.MF entries). The underlines you see here is for the Groovy wrapper of Vert.x. It is OK
// since this wrapper gets included in the bundle. The main Vert.x code is OSGi compliant and can be deployed separately.

/**
 * Provides messaging using vertx. In this a clustered event bus.
 *
 * See http://vertx.io/docs/ for more information.
 */
@SuppressWarnings(["GroovyUnusedDeclaration", "PackageAccessibility"]) // This is never referenced directly, only through APSMessageService API.
@OSGiServiceProvider(
        // Possible criteria for client lookups. ex: "(${APS.Messaging.Protocol.Name}=vertx-eventbus)" In most cases clients won't care.
        properties = [
                @OSGiProperty( name = APS.Service.Provider,        value = "aps-vertx-event-bus-messaging-provider" ),
                @OSGiProperty( name = APS.Service.Category,        value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function,        value = APS.Value.Service.Function.Messaging ),
                @OSGiProperty( name = APS.Messaging.Protocol.Name, value = "vertx-eventbus" ),
                @OSGiProperty( name = APS.Messaging.Persistent,    value = APS.FALSE )

        ]
)
@CompileStatic
@TypeChecked
class APSVertxEventBusMessagingProvider extends APSMessageService.AbstractAPSMessageService implements APSMessageService {

    //
    // Constants
    //

    /** Sending property for sending to only one member. Value is true / false. */
    private static final String VERTX_ONE_RECEIVER = "vertx-one-receiver"

    //
    // Private Members
    //

    /** Our bundle context. */
    @Managed
    BundleContext context

    /** For logging. */
    @Managed(loggingFor = "aps-vertx-event-bus-messaging-provider")
    private APSLogger logger

    /** This is used to interact with APSActivator, which is our BundleActivator. Injecting this forces delayed registration of service. */
    @Managed
    private APSActivatorInteraction interaction

    @OSGiService
    private APSVertxService vertxService

    /** The listeners of this service. */
    private Map<String, List<APSSubscriber>> listeners = [ : ]

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
    @SuppressWarnings("PackageAccessibility")
    @Initializer
    void init() {
        this.logger.connectToLogService( this.context ) // Connect to OSGi log service if available. APSLogger does not use a timeout when
                                                        // tracking the LogService so it will fail immediately if service is not available,
                                                        // so there is no risk of blocking (which there is when timeout is used).

        this.vertxService.useGroovyVertX( APSVertxService.DEFAULT_INST, { AsyncResult<Vertx> result ->
            if ( result.succeeded() ) {
                this.vertx = result.result()
                this.eventBus = this.vertx.eventBus()

                // Notify APSActivator that we are ready to work. APSActivator will register this as service with OSGi on this state.
                this.interaction.state = APSActivatorInteraction.State.READY

                this.logger.info "Vert.x cluster started successfully!"
            }
            else {
                this.interaction.state = APSActivatorInteraction.State.STARTUP_FAILED
                this.logger.error "Vert.x cluster failed to start: ${result.cause()}, shutting down bundle!"
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
            this.vertxService.releaseGroovyVertX( APS.DEFAULT )
            this.logger.disconnectFromLogService( this.context )
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
    private @NotNull List<APSSubscriber> getListenersForTopic(@NotNull String topic ) {
        List<APSSubscriber> topicListeners = this.listeners [ topic ]

        if ( topicListeners == null ) {
            topicListeners = new LinkedList<>()
            this.listeners [ topic ] = topicListeners
        }

        return topicListeners
    }

    /**
     * Sends a message to the destination.
     *
     * Valid properties:
     *
     *      'vertx-one-receiver' : 'true/false'
     *
     * @param topic The destination to send message.
     * @param message The message to send.
     * @param props Implementation specific properties.
     */
    @SuppressWarnings("PackageAccessibility")
    @Override
    void publish(@NotNull String topic, @NotNull Object message, @Nullable Properties props ) {
        if ( props != null && props.getProperty( VERTX_ONE_RECEIVER ) ?. toLowerCase() == "true" ) {
            this.eventBus.send topic, message, { AsyncResult<Message> reply ->
                if ( reply.succeeded() ) {
                    this.listeners [ topic ] ?. each { APSSubscriber listener ->
                        listener.subscription topic , reply.result().body()
                    }
                }
            }
        }
        else {
            this.eventBus.publish topic, message
        }
    }

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * Valid properties:
     *
     *      none
     *
     * @param topic The topic to listen to.
     * @param listener The listener to call with received messages.
     * @param props Implementation specific properties.
     */
    @Override
    void subscribe(@NotNull String topic, @NotNull APSSubscriber listener, Properties props ) {
        List<APSSubscriber> topicListeners = getListenersForTopic topic
        boolean newTopic = topicListeners.size() == 0

        if ( !topicListeners.contains( listener ) ) {
            topicListeners.add listener
        }

        // We only need to do this once for each topic.
        if (newTopic) {
            MessageConsumer consumer = this.consumers [ topic ]

            if (consumer == null) {
                consumer = this.eventBus.consumer( topic ).handler { Message message ->

                    getListenersForTopic message.address() each { APSSubscriber toListener ->
                        toListener.subscription topic , message.body()
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
    @SuppressWarnings("PackageAccessibility")
    @Override
    void unsubscribe(@NotNull String topic, @NotNull APSSubscriber listener ) {
        List<APSSubscriber> topicListeners = getListenersForTopic topic
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
