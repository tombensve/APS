/*
 *
 * PROJECT
 *     Name
 *         APS VertX Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This service provides configured Vertx instances allowing multiple services to use the same Vertx instance.
 *         
 *         This service also provides for multiple instances of VertX by associating an instance with a name. Everyone
 *         asking for the same name will get the same instance.
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
 *         2018-05-28: Created!
 *
 */
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
import se.natusoft.osgi.aps.activator.APSActivatorInteraction
import se.natusoft.osgi.aps.activator.annotation.*
import se.natusoft.osgi.aps.api.messaging.APSMessage
import se.natusoft.osgi.aps.api.messaging.APSMessageSubscriber
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.exceptions.APSIOException
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.net.vertx.util.RecursiveJsonObjectMap
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSLogger

@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@TypeChecked
@OSGiServiceProvider(
        // Possible criteria for client lookups. ex: "(aps-messaging-protocol=vertx-eventbus)" In most cases clients
        // won't care.
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value =
                        "aps-vertx-event-bus-messaging-provider:subscriber" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
                @OSGiProperty( name = APS.Messaging.Protocol.Name, value = "vertx-eventbus" ),
                @OSGiProperty( name = APS.Messaging.Persistent, value = APS.FALSE ),
                @OSGiProperty( name = APS.Messaging.Clustered, value = APS.TRUE )
        ]
)
class MessageSubscriberProvider implements APSMessageSubscriber {

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
    private List<ServiceRegistration> svcRegs = []

    /** The logger for this class. */
    @Managed( name = "subscriber", loggingFor = "aps-vertx-eventbus-messaging:subscriber" )
    private APSLogger logger

    /** Our bundles context. */
    @Managed
    private BundleContext context

    /**
     * This tracks the EventBus. init() will setup an onActiveServiceAvailable callback handler which
     * will provide the eventBus instance.
     **/
    @OSGiService( additionalSearchCriteria = "(vertx-object=EventBus)", timeout = "30 sec" )
    private APSServiceTracker<EventBus> eventBusTracker
    private EventBus eventBus

    /** Used to delay service registration. */
    @Managed( name = "subscriberAI" )
    private APSActivatorInteraction activatorInteraction

    /** Active subscribers are stored here. */
    private Map<ID, MessageConsumer> subscribers = [:]

    //
    // Startup / shutdown
    //

    /**
     * This is run by APSActivator when all @Managed & @OSGiService annotated fields have been injected.
     **/
    @Initializer
    void init() {
        // Yes, what these handlers do could be done directly below in onActiveServiceAvailable {...} instead
        // of changing state. This is however more future safe.
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.READY ) {
            this.activatorInteraction.registerService( MessageSubscriberProvider.class, this.context, this.svcRegs )
        }
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.TEMP_UNAVAILABLE ) {
            this.svcRegs.first().unregister()
            this.svcRegs.clear()
        }

        this.eventBusTracker.onActiveServiceAvailable { EventBus service, ServiceReference serviceReference ->
            this.eventBus = service

            this.activatorInteraction.state = APSActivatorInteraction.State.READY
        }
        this.eventBusTracker.onActiveServiceLeaving { ServiceReference service, Class serviceAPI ->
            this.eventBus = null

            this.activatorInteraction.state = APSActivatorInteraction.State.TEMP_UNAVAILABLE
        }
    }

    //
    // Methods
    //

    /**
     * Adds a subscriber.
     *
     * @param destination The destination to subscribe to.
     *                    This is up to the implementation, but it is strongly recommended that
     *                    this is a name that will be looked up in some configuration for the real
     *                    destination, by the service rather than have the client pass a value from
     *                    its configuration.
     * @param subscriptionId A unique id for this subscription. Use the same to unsubscribe.
     * @param result The result of the call callback. Will always return success. Can be null.
     * @param handler The subscription handler.
     */
    @Override
    void subscribe( @NotNull String destination, @NotNull ID subscriptionId, @Nullable APSHandler<APSResult> result,
                    @NotNull APSHandler<APSMessage> handler ) {
        //        this.logger.error( "@@@@@@@@ THREAD: ${Thread.currentThread()}" )

        String address = destination

        MessageConsumer consumer = this.eventBus.consumer( address ) { Message<JsonObject> msg ->

            Map<String, Object> message = new RecursiveJsonObjectMap( msg.body() as JsonObject )

            handler.handle( new APSMessageProvider( message: message, vertxMsg: msg ) )

        }

        if ( consumer == null ) {
            if ( result != null ) {
                result.handle( APSResult.failure( new APSIOException( "Failed to create consumer!" ) ) )
            }
        }
        else {
            this.subscribers[ subscriptionId ] = consumer

            if ( result != null ) {
                result.handle( APSResult.success( null ) )
            }
        }
    }

    /**
     * Cancel a subscription.
     *
     * @param subscriptionId The same id as passed to subscribe.
     * @param result The result of the call. Can be null.
     */
    @Override
    void unsubscribe( @NotNull ID subscriptionId, @Nullable APSHandler<APSResult> result ) {
        //        this.logger.error( "@@@@@@@@ THREAD: ${Thread.currentThread()}" )

        MessageConsumer consumer = this.subscribers[ subscriptionId ]

        if ( consumer != null ) {
            consumer.unregister()

            if ( result != null ) {
                result.handle( APSResult.success( null ) )
            }
        }
        else {
            Exception e = new APSValidationException( "No subscription with id '${ subscriptionId }' exists!" )
            if ( result != null ) {
                result.handle( APSResult.failure( e ) )
            }
            else {
                throw e
            }
        }
    }
}
