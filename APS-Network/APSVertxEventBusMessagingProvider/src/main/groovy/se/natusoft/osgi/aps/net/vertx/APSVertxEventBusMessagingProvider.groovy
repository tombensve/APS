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
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.pubsub.APSPubSubService
import se.natusoft.osgi.aps.api.pubsub.APSPublisher
import se.natusoft.osgi.aps.api.pubsub.APSSender
import se.natusoft.osgi.aps.api.reactive.APSAsyncValue
import se.natusoft.osgi.aps.api.reactive.APSHandler
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.core.lib.Actions
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

import java.util.concurrent.ExecutorService

/**
 * Provides messaging using vertx event bus.
 *
 * This implementation is non blocking. If the Vertx instance and thus the EventBus is not available due to being on
 * its way up or being restarted any publish or send calls will be added as Closure objects to a list and will be
 * executed as soon as Vertx/EventBus becomes available.
 */
// This is never referenced directly, only through APSMessageService API.
@SuppressWarnings([ "GroovyUnusedDeclaration", "PackageAccessibility" ])
@OSGiServiceProvider(
        // Possible criteria for client lookups. ex: "(${APS.Messaging.Protocol.Name}=vertx-eventbus)" In most cases clients won't care.
        properties = [
                @OSGiProperty(name = APS.Service.Provider, value = "aps-vertx-event-bus-messaging-provider"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Network),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.Messaging),
                @OSGiProperty(name = APS.Messaging.Protocol.Name, value = "vertx-eventbus"),
                @OSGiProperty(name = APS.Messaging.Persistent, value = APS.FALSE),
        ]
)
@CompileStatic
@TypeChecked
class APSVertxEventBusMessagingProvider implements APSPubSubService<Map<String, Object>> {

    //
    // Constants
    //

    //
    // Private Members
    //

    /** Our bundle context. */
    @Managed
    BundleContext context

    /** For logging. */
    @Managed(loggingFor = "aps-vertx-event-bus-messaging-provider")
    private APSLogger logger

    /**
     * The event bus we will be using for sending messages.
     *
     * Note that we can set nonBlocking = true due to EventBus API d
     */
    @OSGiService(additionalSearchCriteria = "(vertx-object=EventBus)", timeout = "30 sec", nonBlocking = true)
    private EventBus eventBus

    /** All publishing run on this thread. */
    @Managed
    @ExecutorSvc(type = ExecutorSvc.ExecutorType.Single, name = "aps-vertx-eventbus-messaging-provider",
            parallelism = 1, unConfigurable = true)
    private ExecutorService publisher

    /** Keeps track of subscribers to messages by address. */
    private Map<String, List<UUID>> subscribers = [ : ]

    /** Saves handlers by their unique id. */
    private Map<UUID, APSHandler<APSAsyncValue<Map<String, Object>>>> idToHandler = [ : ]

    /** Maps id to address to make things easier ... */
    private Map<UUID, String> idToAddress = [ : ]

    //
    // Initializer
    //

    /**
     * Setup. This is called after all injections are done.
     */
    @SuppressWarnings("PackageAccessibility")
    @Initializer
    void init() {
        this.logger.connectToLogService( this.context )
    }

    /**
     * This gets called when out bundle gets stopped. In this case we need to shut down Vert.x.
     */
    @BundleStop
    void stop() {
    }

    //
    // Methods
    //

    /**
     * Returns a publisher to publish with.
     *
     * @param params Meta data for the publisher.
     */
    @Override
    APSPublisher publisher( Map params ) {
        return new Publisher( meta: params, getEventBus: { this.eventBus } )
    }

    /**
     * Returns a sender to send with. Depending on implementation the APSSender instance returned can possibly
     * be an APSReplyableSender that allows for providing a subscriber for a reply to the sent message.
     *
     * @param params Meta data for the sender.
     */
    @Override
    APSSender sender( Map params ) {
        return new Sender( meta: params, getEventBus: { this.eventBus } )
    }

    /**
     * Adds a subscriber.
     *
     * @param subscriber The subscriber to add.
     * @param params Meta data. This depends on the implementation. Can possibly be null when not used. For example
     *                   if there is a need for an address or topic put it in the meta data.
     */
    @Override
    Object subscribe( Map<String, String> params, APSHandler<APSAsyncValue<Map<String, Object>>> handler ) {
        UUID subId = UUID.randomUUID()
        String address = params[ ADDRESS ]
        if ( this.subscribers[ address ] == null ) {
            List<UUID> subIds = [ subId ]
            this.subscribers[ address ] = subIds

            this.idToHandler[ subId ] = handler
            this.idToAddress[ subId ] = address

            this.eventBus.consumer( address ) { Message<JsonObject> msg ->
                Map<String, Object> message = msg.body().map

                this.subscribers[ address ].each { UUID subHandlerId ->
                    APSHandler<APSAsyncValue<Map<String, Object>>> callSubHandler = this.idToHandler[ subHandlerId ]
                    callSubHandler.handle( new APSAsyncValue.Provider( message ) )
                }
            }
        }

        subId
    }

    /**
     * Removes a subscriber.
     *
     * @param subscriptionId The id of the subscriber to remove.
     */
    @Override
    synchronized void unsubscribe( Object subscriptionId ) {
        UUID id = (UUID) subscriptionId

        String address = this.idToAddress[ id ]
        List<UUID> subIds = this.subscribers[ address ]
        if ( subIds != null ) {
            subIds.remove( id )
        }
        this.idToHandler.remove( id )
        this.idToAddress.remove( id )

    }
}

