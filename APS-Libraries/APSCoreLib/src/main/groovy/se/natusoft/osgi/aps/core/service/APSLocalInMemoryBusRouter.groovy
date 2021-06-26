/*
 *
 * PROJECT
 *     Name
 *         APS Core Lib
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         This library is made in Groovy and thus depends on Groovy, and contains functionality that
 *         makes sense for Groovy, but not as much for Java.
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
package se.natusoft.osgi.aps.core.service

import groovy.transform.CompileStatic
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.APSPlatformServiceProperty
import se.natusoft.osgi.aps.activator.annotation.APSPlatformServiceProvider
import se.natusoft.osgi.aps.api.messaging.APSBusRouter
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.core.lib.ValidTarget
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSLogger

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

import static se.natusoft.osgi.aps.util.APSExecutor.concurrent

/**
 * A very simple little bus that will call the handler of any subscriber subscribing to same
 * target as sending to. No networking nor any threading is done by this class. Subscription
 * handlers that does not handle the call quickly should submit their handling to an
 * ExecutionService backed by a thread pool like APSExecutor.
 *
 * On the fronted I have made a very similar in memory bus. There it is used to talk between
 * components. On the backend it does not have a clear use, other that being very useful in
 * tests, if you don't want to start up a Vert.x cluster by deploying the aps-vertx-provider.
 * The APSBusTest uses this bus since it wants to test the APSBusProvider functionality, not
 * network messaging. For testing message driven services this bus works fine. APSVertxProvider
 * will of course start Vert.x, but it has no choice! Other code sending and receiving messages
 * do have a choice and can avoid starting a network cluster.
 */
@CompileStatic
@SuppressWarnings( [ "UnnecessaryQualifiedReference", "unused" ] )
@APSPlatformServiceProvider(
        properties = [
                @APSPlatformServiceProperty( name = APS.Service.Provider, value = "aps-local-in-memory-bus" ),
                @APSPlatformServiceProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Communication ),
                @APSPlatformServiceProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
        ]
)
class APSLocalInMemoryBusRouter implements APSBusRouter {

    //
    // Private Members
    //

    private static final String SUPPORTED_TARGET = "local"

    private Map<String /*target*/, Map<ID, Queue<APSHandler<Map<String, Object>>>>> subscribers =
            new ConcurrentHashMap<>()

    @Managed( loggingFor = "APSLocalInMemoryBus" )
    private APSLogger logger

    APSLocalInMemoryBusRouter() {
    }

    //
    // Methods
    //

    /**
     * Sends a message.
     *
     * @param target The target to send to. How to interpret this is up to implementation.
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler The handler to call with result of operation. Can be null!
     */
    @Override
    boolean send( @NotNull String target, @NotNull Map<String, Object> message,
                  @Nullable APSHandler<APSResult> resultHandler ) {

        return ValidTarget.onValid( SUPPORTED_TARGET, target ) { String address ->

            // First tried computeIfAbsent, but it does not seem to play well with Groovy Closures.
            Map<ID, Queue<APSHandler<Map<String, Object>>>> addressSubscribers = subscribers[ address ]
            if ( addressSubscribers == null ) {
                addressSubscribers = new ConcurrentHashMap<>()
                subscribers[ address ] = addressSubscribers
            }

            if ( !addressSubscribers.isEmpty() ) {

                addressSubscribers.each { ID id, Queue<APSHandler<Map<String, Object>>> handlers ->

                    handlers.each { APSHandler<Map<String, Object>> handler ->

                        concurrent {
                            try {
                                handler.handle( message )
                            }
                            catch ( Exception e ) {
                                this.logger.error( "Message handler threw illegal exception!", e )
                            }
                        }
                    }
                }

                if ( resultHandler != null ) {
                    resultHandler.handle( APSResult.success( null ) )
                }
            }
            else if ( resultHandler != null ) {

                resultHandler.handle( APSResult.failure( new APSValidationException( "No subscribers!" ) ) )
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
    boolean subscribe( @NotNull ID id, @NotNull String target,
                       @Nullable @Optional APSHandler<APSResult> resultHandler,
                       @NotNull APSHandler<Map<String, Object>> messageHandler ) {

        return ValidTarget.onValid( SUPPORTED_TARGET, target ) { String address ->

            Map<ID, Queue<APSHandler<Map<String, Object>>>> addressSubscribers = subscribers[ address ]
            if ( addressSubscribers == null ) {
                addressSubscribers = new ConcurrentHashMap<>()
                subscribers[ address ] = addressSubscribers
            }

            Queue<APSHandler<Map<String, Object>>> handlers = addressSubscribers[ id ]
            if ( handlers == null ) {
                handlers = new ConcurrentLinkedQueue<>()
                addressSubscribers[ id ] = handlers
            }
            handlers << messageHandler

            if ( resultHandler != null ) {

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

        // Note that we don't have a target here, and thus tries to remove from all targets.
        // The subscriberId should be unique so only one will be removed.
        this.subscribers.each { String address, Map<ID, Queue<APSHandler<Map<String, Object>>>> subscriptionHandlers ->

            subscriptionHandlers.remove( subscriberId )
        }
    }

    /**
     * @return true if the implementation is a required, non optional provider.
     */
    @Override
    boolean required() {
        return true
    }
}
