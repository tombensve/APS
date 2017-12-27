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
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.pubsub.APSPublisher
import se.natusoft.osgi.aps.api.util.APSMeta
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.core.lib.APSObjectPublisher
import se.natusoft.osgi.aps.net.vertx.api.VertxSubscriber
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

import java.util.concurrent.ExecutorService

// Note to IDEA users: IDEA underlines (in beige color if you are using Darcula theme) all references to classes that are not
// OSGi compatible (no OSGi MANIFEST.MF entries). The underlines you see here is for the Groovy wrapper of Vert.x. It is OK
// since this wrapper gets included in the bundle. The main Vert.x code is OSGi compliant and can be deployed separately.

/**
 * Provides messaging using vertx. In this a clustered event bus.
 *
 * See http://vertx.io/docs/ for more information.
 */
@SuppressWarnings([ "GroovyUnusedDeclaration", "PackageAccessibility" ])
// This is never referenced directly, only through APSMessageService API.
@OSGiServiceProvider(
        // Possible criteria for client lookups. ex: "(${APS.Messaging.Protocol.Name}=vertx-eventbus)" In most cases clients won't care.
        properties = [
                @OSGiProperty(name = APS.Service.Provider, value = "aps-vertx-event-bus-messaging-provider"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Network),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.Messaging),
                @OSGiProperty(name = APS.Messaging.Protocol.Name, value = "vertx-eventbus"),
                @OSGiProperty(name = APS.Messaging.Persistent, value = APS.FALSE),
                @OSGiProperty(name = "consumed", value = "vertx")
        ]
)
@CompileStatic
@TypeChecked
class APSVertxEventBusMessagingProvider extends VertxSubscriber implements APSPublisher {

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

    /** All publishing run on this thread. */
    @Managed
    @ExecutorSvc(type = ExecutorSvc.ExecutorType.Single, name = "aps-vertx-eventbus-messaging-provider",
            parallelism = 1, unConfigurable = true)
    private ExecutorService publisher

    /** The clustered event bus we are communicating over. */
    private synchronized EventBus eventBus = null

    /** Items to publish. */
    private final List<Tuple2<Object, Map<String, String>>> toPublish = [ ]

    /** Sends messages on the bus. */
    private Runnable publishWorker = {
        synchronized ( toPublish ) {
            this.toPublish.each { Tuple2<Object, Map<String, String>> work ->
                String topic = work.second[ APSMeta.TOPIC ]
                if ( topic != null ) {
                    this.eventBus.publish( topic, work.first )
                } else {
                    this.logger.error( "No topic! Message not published!" )
                }
            }
        }
    }

    /** This is used to deliver received messages to consumers. */
    private APSObjectPublisher<Object> receivedMessagesDelivery =
            new APSObjectPublisher<>(context: this.context, consumerQuery: "(consumed=vertx-event-bus-messages)")

    //
    // Constructor
    //

    APSVertxEventBusMessagingProvider() {
        this.onVertxAvailable = { Vertx vertx ->
            this.eventBus = vertx.eventBus()
            this.publisher.submit( this.publishWorker )
        }
    }

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
    }

    /**
     * This gets called when out bundle gets stopped. In this case we need to shut down Vert.x.
     */
    @BundleStop
    void stop() {
        if ( this.eventBus != null ) {
            this.eventBus = null
        }
    }

    //
    // Methods
    //

    /**
     * Publishes data.
     *
     * @param toPublish The data to publish.
     * @param meta Meta data to help the implementation make decisions.
     */
    void publish( Object toPublish, APSMeta meta ) {
        synchronized ( this.toPublish ) {
            this.toPublish << new Tuple2<Object, Map<String, String>>( toPublish, meta )
        }
        if ( this.eventBus != null ) {
            this.publisher.submit( this.publishWorker )
        }
    }

}
