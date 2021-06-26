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
 *         2019-08-17: Created!
 *
 */
package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import io.vertx.core.AsyncResult
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
import se.natusoft.osgi.aps.activator.annotation.APSPlatformServiceProperty
import se.natusoft.osgi.aps.activator.annotation.APSPlatformService
import se.natusoft.osgi.aps.activator.annotation.APSPlatformServiceProvider
import se.natusoft.osgi.aps.api.messaging.APSBusRouter
import se.natusoft.osgi.aps.api.messaging.APSMessagingException
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.core.lib.ValidTarget
import se.natusoft.osgi.aps.net.vertx.util.RecursiveJsonObjectMap
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSLogger
import static se.natusoft.osgi.aps.util.APSExecutor.*

/**
 * Provides and APSBusRouter implementation using Vert.x EventBus for communication.
 *
 * This uses "cluster:" as target id. When calling send(...) however an additional "all:"
 * can be added before address to do a publish rather than send.
 */
@CompileStatic
@SuppressWarnings( "unused" )
// Managed by APSActivator IDE can't see that!
@APSPlatformServiceProvider(
        properties = [
                @APSPlatformServiceProperty( name = APS.Service.Provider, value = "aps-vertx-bus-router" ),
                @APSPlatformServiceProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @APSPlatformServiceProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
                @APSPlatformServiceProperty( name = APS.Messaging.Protocol.Name, value = "vertx-eventbus" ),
                @APSPlatformServiceProperty( name = APS.Messaging.Persistent, value = APS.FALSE ),
                @APSPlatformServiceProperty( name = APS.Messaging.Clustered, value = APS.TRUE )
        ]
)
class APSVertxBusRouter implements APSBusRouter {

    private static final String SUPPORTED_TARGET_IDS = "cluster MessageService msvc MessageClient mclnt"

    //
    // Private members
    //

    /** For logging. */
    @Managed( name = "aps-vertx-bus-router-logger", loggingFor = "aps-vertx-bus-router" )
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
     **/
    protected List<ServiceRegistration> svcRegs = []

    /** Our bundles context. */
    @Managed
    protected BundleContext context

    /**
     * This tracks the EventBus. init() will setup an onActiveServiceAvailable callback handler which
     * will provide the eventBus instance.*/
    @APSPlatformService( additionalSearchCriteria = "(vertx-object=EventBus)", timeout = "30 sec" )
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
    @Initializer
    void init() {
        // Yes, what these handlers do could be done directly below in onActiveServiceAvailable {...} instead
        // of changing state. This is however more future safe.
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.READY ) {
            activatorInteraction.registerService( APSVertxBusRouter.class, context, svcRegs )
            logger.debug( ">>>> Registered ${ APSVertxBusRouter.class.simpleName } as service!" )
        }
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.TEMP_UNAVAILABLE ) {
            svcRegs.first().unregister() // We only have one instance.
            svcRegs.clear()
            logger.debug( ">>>> Unregistered ${ APSVertxBusRouter.class.simpleName } as service!" )
        }

        this.eventBusTracker.onActiveServiceAvailable { EventBus service, ServiceReference serviceReference ->

            logger.debug( ">>>> received eventbus!" )

            eventBus = service

            activatorInteraction.state = APSActivatorInteraction.State.READY
        }
        this.eventBusTracker.onActiveServiceLeaving { ServiceReference service, Class serviceAPI ->
            logger.debug( ">>>> Lost eventbus!" )

            eventBus = null

            activatorInteraction.state = APSActivatorInteraction.State.TEMP_UNAVAILABLE
        }
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
    boolean send( @NotNull String target, @NotNull Map<String, Object> message, @Optional @Nullable APSHandler<APSResult> resultHandler ) {

        this.logger.debug( "§§§§ Sending to target -> '${target}'" )

        return ValidTarget.onValid(SUPPORTED_TARGET_IDS, true, target ) { String realTarget ->

            def internalResultHandler = { AsyncResult res ->
                if ( res.succeeded() ) {
                    resultHandler.handle( APSResult.success( null ) )
                }
                else {
                    resultHandler.handle( APSResult.failure( res.cause() ) )
                }
            }

            // Note that we have to check passed target here, not realTarget, which will have the "all:" part
            // removed! The realTarget will be valid for the publisher and sender.
            if ( target.startsWith( "all:" ) ) {

                eventBus.publisher( realTarget ).write( new JsonObject( message ) ) { AsyncResult res ->

                    internalResultHandler( res )
                }.close()
            }
            else {
                eventBus.sender( realTarget ).write( new JsonObject( message ) ) { AsyncResult res ->

                    internalResultHandler( res )
                }.close()
            }
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
    boolean subscribe( @NotNull ID id, @NotNull String target, @Optional @Nullable APSHandler<APSResult> resultHandler, @NotNull APSHandler<Map<String, Object>> messageHandler ) {

        return ValidTarget.onValid(SUPPORTED_TARGET_IDS, false, target ) { String realTarget ->

            MessageConsumer consumer = eventBus.consumer( realTarget ) { Message<JsonObject> msg ->

                Map<String, Object> message = new RecursiveJsonObjectMap( msg.body() )
                // Call message handler on APS thread rather than Vert.x thread.
                concurrent { messageHandler.handle( message ) }
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

                resultHandler.handle( APSResult.success( null ) )
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
        MessageConsumer consumer = this.subscriptions.remove subscriberId
        if ( consumer != null ) consumer.unregister()
    }

    /**
     * @return true if the implementation is a required, non optional provider.
     */
    @Override
    boolean required() {
        return true
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
