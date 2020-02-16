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
import groovy.transform.TypeChecked
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider
import se.natusoft.osgi.aps.api.messaging.APSBusRouter
import se.natusoft.osgi.aps.api.messaging.APSMessagingException
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.core.lib.ValidTarget
import se.natusoft.osgi.aps.exceptions.APSInvalidException
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSLogger

import java.util.concurrent.ConcurrentHashMap

/**
 * A very simple little bus that will call handler of any subscriber subscribing to same target
 * as sending to. No threading is done by this class, but subscription handlers that does not
 * handle the call quickly should submit their handling to an ExecutionService backed by a thread
 * pool.
 */
@CompileStatic
@TypeChecked
@SuppressWarnings( [ "UnnecessaryQualifiedReference", "unused" ] )
@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-local-in-memory-bus" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Communication ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
        ]
)
class APSLocalInMemoryBusRouter implements APSBusRouter {

    private static final String TARGET_ID = "local:"

    //
    // Private Members
    //

    private static final String SUPPORTED_TARGET = "local"

    private Map<String /*target*/, Map<ID, List<APSHandler<Map<String, Object>>>>> subscribers =
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

            Map<ID, List<APSHandler<Map<String, Object>>>> subs =
                    this.subscribers.computeIfAbsent( address ) { Map<String, Object> byId ->
                        new ConcurrentHashMap<>()
                    }

            if ( !subs.isEmpty() ) {

                subs.each { ID id, List<APSHandler<Map<String, Object>>> handlers ->

                    handlers.each { APSHandler<Map<String, Object>> handler ->

                        try {
                            handler.handle( message )
                        }
                        catch ( Exception e ) {
                            this.logger.error( "Message handler threw illegal exception!", e )
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

            this.subscribers.computeIfAbsent( address ) { new ConcurrentHashMap<>() }
                    .computeIfAbsent( id ) { new LinkedList<>() }
                    .add( messageHandler )

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
        this.subscribers.each { String target, Map<ID, List<APSHandler<Map<String, Object>>>> value ->

            value.remove( subscriberId )
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
