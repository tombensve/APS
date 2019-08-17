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
import groovy.transform.TypeChecked
import io.vertx.amqpbridge.AmqpBridge
import io.vertx.core.AsyncResult
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.eventbus.MessageProducer
import io.vertx.core.json.JsonObject
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.osgi.aps.api.messaging.APSBusRouter
import se.natusoft.osgi.aps.net.vertx.util.RecursiveJsonObjectMap
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSLogger

/**
 * Provides and APSBusRouter implementation using Vert.x EventBus for communication.
 *
 * This uses "amqp:" as target id.
 */
@CompileStatic
@TypeChecked
@SuppressWarnings( "unused" )
class APSAmqpBridgeBusRouter implements APSBusRouter {

    private static final String TARGET_ID = "amqp:"

    //
    // Private members
    //

    /** For logging. */
    APSLogger logger

    /** The bridge to use for messaging. */
    AmqpBridge amqpBridge

    private Map<String, MessageProducer> producers = [:]

    private Map<String, MessageConsumer> consumers = [:]

    /** The current subscriptions */
    private Map<ID, MessageConsumer> subscriptions = [:]

    //
    // Methods
    //

    /**
     * This checks if provided target is valid and if so proceeds with the operation.
     *
     * @param target Target to validate.
     * @param go Closure to call on valid target.
     */
    @SuppressWarnings( "DuplicatedCode" )
    private static void validTarget( String target, Closure go ) {
        if ( target.startsWith( TARGET_ID ) ) {
            target = target.substring( TARGET_ID.length() )

            go.call( target )
        }
        // Can't decide if this is an acceptable or very bad idea.
//        else if ( target.startsWith( "all:" ) ) {
//            target = target.substring( 4 )
//
//            go.call( target )
//        }
        // Note that since APSBus will call all APSBusRouter implementations found, receiving
        // and invalid target is nothing strange. We should only react on those that we recognize.
    }

    /**
     * Sends a message.
     *
     * @param target The target to send to. In this case it should start with amqp: and what comes after
     *               that is taken as the AMQP address.
     *
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler The handler to call with result of operation. Can be null!
     */
    @Override
    void send( @NotNull String target, @NotNull Map<String, Object> message, @Optional @Nullable APSHandler<APSResult> resultHandler ) {

        validTarget( target ) { String realTarget ->

            try {

                MessageProducer messageProducer = producers[ realTarget ]
                if ( messageProducer == null ) {
                    messageProducer = this.amqpBridge.createProducer( realTarget )
                    producers[ realTarget ] = messageProducer
                }

                messageProducer.write( new JsonObject( message ) ) { AsyncResult res ->
                    if ( res.failed() ) {
                        logger.error( "Failed to send AMQP message!", res.cause() )

                        if ( resultHandler != null ) resultHandler.handle( APSResult.failure( res.cause() ) )
                    }
                }

                if ( resultHandler != null ) resultHandler.handle( APSResult.success( null ) )
            }
            catch ( IllegalStateException ise ) {
                this.logger.error( "Failed to send AMQP message!", ise )

                if ( resultHandler != null ) resultHandler.handle( APSResult.failure( ise ) )
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
    void subscribe( @NotNull ID id, @NotNull String target, @Optional @Nullable APSHandler<APSResult> resultHandler, @NotNull APSHandler<Map<String, Object>> messageHandler ) {

        validTarget( target ) { String realTarget ->

            try {
                MessageConsumer consumer = consumers[ realTarget ]
                if ( consumer == null ) {
                    consumer = this.amqpBridge.createConsumer( realTarget )
                    consumers[ realTarget ] = consumer
                }

                consumer.handler() { Message<JsonObject> msg ->
                    messageHandler.handle( new RecursiveJsonObjectMap( msg.body() ) )
                }

                subscriptions[ id ] = consumer

                if ( resultHandler != null ) resultHandler.handle( APSResult.success( null ) )
            }
            catch ( IllegalStateException ise ) {
                if ( resultHandler != null ) resultHandler.handle( APSResult.failure( ise ) )
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
        MessageConsumer consumer = this.subscriptions[ subscriberId ]
        consumer.unregister()
    }

    /**
     * @return true if the implementation is a required, non optional provider.
     */
    @Override
    boolean required() {
        return false
    }

    void shutdown() {
        // This is not an excuse for clients to not clean up after themselves! And this will not be done
        // until we shut down.
        this.subscriptions.keySet().each { ID key ->
            this.subscriptions[ key ].unregister()
        }
        this.subscriptions.clear()

        this.consumers.each { String address, MessageConsumer consumer ->
            consumer.unregister()
        }
        this.consumers.clear()

        this.producers.each { String address, MessageProducer producer ->

            producer.close()
            producer.end()
        }
        this.producers.clear()
    }

}
