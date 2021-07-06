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
import io.vertx.core.AsyncResult
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.framework.ServiceRegistration
import se.natusoft.aps.activator.APSActivatorInteraction
import se.natusoft.aps.activator.annotation.*
import se.natusoft.osgi.aps.api.messaging.APSMessage
import se.natusoft.osgi.aps.api.messaging.APSMessageSender

import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.exceptions.APSException
import se.natusoft.osgi.aps.net.vertx.util.RecursiveJsonObjectMap
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.util.APSLogger

@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@APSPlatformServiceProvider(
        properties = [
                @APSPlatformServiceProperty( name = APS.Service.Provider, value = "aps-vertx-event-bus-messaging-provider:sender" ),
                @APSPlatformServiceProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @APSPlatformServiceProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
                @APSPlatformServiceProperty( name = APS.Messaging.Protocol.Name, value = "vertx-eventbus" ),
                @APSPlatformServiceProperty( name = APS.Messaging.Persistent, value = APS.FALSE ),
                @APSPlatformServiceProperty( name = APS.Messaging.Clustered, value = APS.TRUE )
        ],
        serviceAPIs = [ APSMessageSender.class ]
)
class MessageSenderProvider implements APSMessageSender {

    //
    // Private Members
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

    /** For logging. */
    @Managed( name = "sender", loggingFor = "aps-vertx-event-bus-messaging-provider:sender" )
    private APSLogger logger

    /** Our bundles context. */
    @Managed
    private BundleContext context

    /**
     * This tracks the EventBus. init() will setup an onActiveServiceAvailable callback handler which
     * will provide the eventBus instance.*/
    @APSPlatformService( additionalSearchCriteria = "(vertx-object=EventBus)", timeout = "30 sec" )
    private APSServiceTracker<EventBus> eventBusTracker
    private EventBus eventBus

    /** Used to delay service registration. */
    @Managed( name = "senderAI" )
    private APSActivatorInteraction activatorInteraction

    /** The current subscriber. */
    private APSHandler<APSMessage> reply

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
            this.activatorInteraction.registerService( MessageSenderProvider.class, this.context, this.svcRegs )
            this.logger.info( "Registered MessageSenderProvider as service!" )
        }
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.TEMP_UNAVAILABLE ) {
            this.svcRegs.first().unregister() // We only have one instance.
            this.svcRegs.clear()
            this.logger.info( "Unregistered MessageSenderProvider as service!" )
        }

        this.eventBusTracker.onActiveServiceAvailable { EventBus service, ServiceReference serviceReference ->
            this.logger.debug( "Received eventbus!" )

            this.eventBus = service

            this.activatorInteraction.state = APSActivatorInteraction.State.READY
        }
        this.eventBusTracker.onActiveServiceLeaving { ServiceReference service, Class serviceAPI ->
            this.logger.debug( "Lost eventbus!" )

            this.eventBus = null

            this.activatorInteraction.state = APSActivatorInteraction.State.TEMP_UNAVAILABLE
        }
    }

    /**
     * Sends a message.
     *
     * @param destination The destination to send to.
     * @param message The message to send.
     */
    private void doSend( String destination, Map<String, Object> message ) {

        String address = destination

        if ( this.reply != null ) {

            // For replyable messages only send is valid. We still need to filter address.
            if ( address.startsWith( "all:" ) ) {

                throw new APSException(
                        "A reply handler has been provided and the destination implies a publish! " +
                                "This is an impossible combination."
                )
            }

            this.eventBus.send( address, new JsonObject( message ) ) { AsyncResult<Message<Map<String, Object>>> reply ->

                if ( reply.succeeded() ) {

                    Map<String, Object> msg = new RecursiveJsonObjectMap( reply.result().body() as JsonObject )
                    this.reply.handle( new APSMessageProvider( message: msg, vertxMsg: reply
                            .result() ) )
                }
            }
        }
        else {

            if ( address.startsWith( "all:" ) ) {
                address = address.substring( 4 )
                this.logger.debug( "Publishing to address: " + address )
                this.eventBus.publish( address, new JsonObject( message ) )
            }
            else {
                this.logger.debug( "Sending to address: " + address )
                this.eventBus.send( address, new JsonObject( message ) )
            }
        }
    }

    /**
     * Sends a message receiving a result of success or failure. On Success there
     * can be a result value and on failure there is an Exception describing the failure
     * available.
     *
     * If result is null then an APSException will be thrown instead on error.
     *
     * @param message The message to send.
     */
    @Override
    void send( String destination, Map<String, Object> message, APSHandler<APSResult> result ) {

        try {
            doSend( destination, message )

            if ( result != null ) {
                result.handle( APSResult.success( null ) )
            }
        }
        catch ( Exception e ) {
            if ( result != null ) {
                result.handle( APSResult.failure( e ) )
            }
            else {
                this.logger.error( e.message, e )

                // Yes, in Groovy all exceptions are unchecked! But non Groovy code might call this
                // so make sure we do throw a runtime exception, which all APS exceptions are.
                if ( e instanceof APSException ) {
                    throw e
                }
                else {
                    throw new APSException( e.message, e )
                }
            }
        }
    }

    /**
     * This must be called before send(...). send will use the last supplied reply subscriber.
     *
     * @param reply the subscriber to receive reply.
     */
    @Override
    APSMessageSender replyTo( APSHandler<APSMessage> _reply ) {
        this.reply = _reply

        this
    }

}
