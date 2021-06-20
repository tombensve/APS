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
package se.natusoft.aps.core.service

import groovy.transform.CompileStatic
import se.natusoft.aps.api.core.platform.service.APSLogService
import se.natusoft.aps.api.messaging.APSBus
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.NotUsed
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional

import java.time.Instant

//import static se.natusoft.osgi.aps.util.APSExecutor.concurrent
//import static se.natusoft.osgi.aps.util.APSTools.waitFor

/**
 * This is a simple bus API that is used by creating an instance and passing a BundleContext.
 *
 * All calls will be passed to all the APSBusRouter implementations tracked. Also see the
 * javadoc for that interface.
 */
@SuppressWarnings( [ "unused", "PackageAccessibility" ] )
@CompileStatic

class APSBusProvider implements APSBus {

    //
    // Private Members
    //

    @Managed( loggingFor = "aps-bus-provider" )
    private APSLogService logger

    private List<ServiceRegistration> svcRegs = []

    @Managed
    private BundleContext context

    @Managed
    private APSActivatorInteraction activatorInteraction

    /** The currently known bus routers */

    private APSServiceTracker<APSBusRouter> routerTracker

    //
    // Constructors
    //


    /**
     * This constructor will not track published APSBusRouter services. It will have only
     * one rotuer, APSLocalInMemoryBus. This is intended for testing.
     */
    @SuppressWarnings( "WeakerAccess" )
    APSBusProvider() {}

    //
    // Methods
    //

    @Initializer
    void init() {

        this.activatorInteraction.state = APSActivatorInteraction.State.TEMP_UNAVAILABLE

        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.READY ) {

            this.activatorInteraction.registerService( APSBusProvider.class, this.context, this.svcRegs )
        }

        concurrent {

            // Due to the possibility of this being published before all bus routers are
            // available, we make sure we have all of the bus routers before we make our
            // self available.
            //
            // This is done by having each bus router implementation added, one per line to
            //
            //     aps/bus/routers
            //
            // file (without package) of the bundle/jar containing the implementation(s).
            // "routers" is a text file without extension.

            List<String> busRouters = resolveBusRouters()

            this.logger.info( "Discovering APSBusRouter providers ..." )

            busRouters.each { String router ->
                this.logger.info( "Found bus router: ${ router }" )
            }
            this.logger.info( "Total of ${ busRouters.size() } bus routers found!" )

            waitFor { this.routerTracker.trackedServiceCount >= busRouters.size() }

            this.activatorInteraction.state = APSActivatorInteraction.State.READY
        }
    }

    /**
     * Returns List of entries in aps/bus/routers files findable as resources. Each entry in such
     * files should provide a name of a bus router. The name is actually only used in logs, but
     * helps troubleshooting, so provide clear names.
     *
     * @return A List of names of bus routers.
     */
    private List<String> resolveBusRouters() {

        Map<String, String> busRouters = [:]
        this.getClass().getClassLoader().getResources( "aps/bus/routers" ).each { URL url ->

            BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream() ) )
            reader.lines().each { String line ->
                if ( line.trim().length() > 0 ) {
                    busRouters[ line ] = line
                }
            }

            reader.close()
        }

        List<String> routers = []
        busRouters.keySet().each { String router ->
            routers << router.trim()
        }

        routers
    }

    /**
     * Cleanup on shutdown since we manage our own service registration.
     */
    @BundleStop
    void shutdown() {
        if ( !this.svcRegs.empty ) {
            this.svcRegs.first().unregister()
        }
    }

    private static boolean validateBaseMessageStructure( Map<String, Object> message ) {
        if ( message[ 'aps' ] != null && message[ 'content' ] != null ) {
            true
        }
        else {
            false
        }
    }

    private static validationFail( APSHandler<APSResult<?>> resultHandler ) {
        resultHandler.handle(
                APSResult.failure(
                        new APSValidationException( "Bad message structure! 'aps' and 'content' keys need to be in " +
                                "root!" )
                )
        )
    }

    /**
     * Sends a message.
     *
     * @param target The target to send to.
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler Receives the success or failure of the call.
     */
    void send( @NotNull String target, @NotNull Map<String, Object> message,
               @Optional @Nullable APSHandler<APSResult<?>> resultHandler ) {

        if ( validateBaseMessageStructure( message ) ) {

            boolean valid = false

            // It is fully possible for more than one router to act on this, but usually only one will
            // handle the message. Do note that if multiple routers acts on this then there will be
            // multiple calls to the resultHandler. To be clear APS by default has no bus routers that
            // acts on same targets, but it is fully possible to create such. This code is not trying
            // to block that in any way. If it is a good idea to do so is another discussion ...
            this.routerTracker.withAllAvailableServices() { APSBusRouter apsBusRouter, @NotUsed Object[] args ->

                // Note that absBusRouter.send(...) actually returns true/false. If true it means that
                // the router handled the message. This is actually used to determine if none of the
                // routers handled the message so that we ca produce an error then.
                if ( apsBusRouter.send( target.trim(), message, resultHandler ) ) {
                    valid = true
                }
            }

            if ( !valid ) {
                resultHandler.handle(
                        APSResult.failure( new APSMessagingException( "No routers accepted target '${ target }'!" ) )
                )
            }
        }
        else {
            validationFail( resultHandler )
        }
    }

    /**
     * Subscribes to messages to a target.
     *
     * @param id A unique ID to associate subscription with. Also used to unsubscribe.
     * @param target The target to subscribe to.
     * @param messageHandler The handler to call with messages sent to target.
     */
    void subscribe( @NotNull ID id, @NotNull String target, @Optional @Nullable APSHandler<APSResult<?>>
            resultHandler,
                    @NotNull APSHandler<Map<String, Object>> messageHandler ) {

        boolean valid = false

        this.routerTracker.withAllAvailableServices() { APSBusRouter apsBusRouter, @NotUsed Object[] args ->

            if ( apsBusRouter.subscribe( id, target.trim(), resultHandler, messageHandler ) ) {
                valid = true
            }
        }

        if ( !valid ) {
            resultHandler.handle( APSResult.failure( new APSMessagingException( "No routers accepted target!" ) ) )
        }
    }

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    void unsubscribe( @NotNull ID subscriberId ) {

        this.routerTracker.withAllAvailableServices() {
            APSBusRouter apsBusRouter, Object[] args ->

                apsBusRouter.unsubscribe( subscriberId )
        }
    }

    /**
     * Allow overriding default timeout with system property.
     *
     * @return The timeout in seconds.
     */
    private static apsRequestTimeout() {
        int timeout = 60
        String defaultTimeoutStr = System.getProperty( "aps.request.timeout" )
        if (defaultTimeoutStr != null) {
            try {
                timeout = Integer.valueOf( defaultTimeoutStr )
                println "APSBusProvider: Overriding request timeout with ${timeout} seconds!"
            }
            catch ( NumberFormatException nfe ) {
                nfe.printStackTrace( System.err )
            }
        }

        timeout
    }

    /**
     * Sends a message and expects to get a response message back.
     *
     * This is not forwarded to a APSBusRouter! This is locally implemented
     * and does the following:
     *
     * - Generates a unique reply address.
     * - Subscribes to address.
     *   - After reply message is received and forwarded to handler, the
     *     message subscription is unsubscribed.
     * - Updates message header.replyAddress with address
     * - Sends message.
     *
     * This should theoretically work for any APSBusRouter implementation. For
     * some it might not make sense however.
     *
     * @param target The target to send to.
     * @param message The message to send.
     * @param resultHandler optional handler to receive result of send.
     * @param responseMessage A message that is a response of the sent message.
     */
    void request( @NotNull String target, @NotNull Map<String, Object> message,
                  @Nullable @Optional APSHandler<APSResult<?>> resultHandler,
                  @NotNull APSHandler<Map<String, Object>> responseMessage ) {
        request( target, message, apsRequestTimeout(  ) as int, resultHandler, responseMessage )
    }

    /**
     * Sends a message and expects to get a response message back.
     *
     * This is not forwarded to a APSBusRouter! This is locally implemented
     * and does the following:
     *
     * - Generates a unique reply address.
     * - Subscribes to address.
     *   - After reply message is received and forwarded to handler, the
     *     message subscription is unsubscribed.
     * - Updates message header.replyAddress with address
     * - Sends message.
     *
     * This should theoretically work for any APSBusRouter implementation. For
     * some it might not make sense however.
     *
     * Note that due to different services and clients might not start in the optimal order
     * a client might try to send a request to a service that is not yet listening to
     * messages. To solve that in an easy way request(...) do wait up to 30 seconds for
     * a reply, and while waiting it repeats the request message every 5 seconds. This
     * situation should only happen at startup!
     *
     * @param target The target to send to.
     * @param message The message to send.
     * @param timeOutSec The number of seconds to wait for a reply before failing.
     * @param resultHandler optional handler to receive result of send.
     * @param responseHandler A handler for message that is a response of the sent message.
     */
    void request( @NotNull String target, @NotNull Map<String, Object> message, int timeOutSec,
                  @Nullable @Optional APSHandler<APSResult<?>> resultHandler,
                  @NotNull APSHandler<Map<String, Object>> responseHandler ) {

        if ( validateBaseMessageStructure( message ) ) {

            String replyTarget = "${ target.split( ":" )[ 0 ] }:" + new APSUUID().toString()
            message[ 'aps' ][ 'replyTarget' ] = replyTarget

            ID replySubscriptionId = new APSUUID()

            // We are not on different threads, but this value is also passed to
            // sendRequest(...) and will be modified in this method further down
            // in reply, so having value boxed so that value change reflects in
            // sendRequest(...) is a requirement.
            SyncedValue<Boolean> keepSending = new SyncedValue<>( true )

            subscribe( replySubscriptionId, replyTarget ) { APSResult subRes ->

                if ( subRes.success() ) {

                    // Subscription on reply successful so send request.
                    sendRequest( target, message, timeOutSec, keepSending, resultHandler )
                }
                else {
                    resultHandler.handle( subRes )
                }

            } { Map<String, Object> reply ->
                // Reply subscription receiver

                keepSending.value = false
                this.unsubscribe( replySubscriptionId )
                responseHandler.handle( reply )
            }

        }
        else {
            validationFail( resultHandler )
        }

    }

    /**
     * Continuation of request after success of reply subscription.
     *
     * @param target The target to send to.
     * @param message The message to send.
     * @param timeoutSec The number of seconds to wait for a reply before failing.
     * @param keepSending As long as this flag is true and timeout has not been reached, this method
     *                    will keep resending message at 2 second intervals.
     * @param resultHandler The handler to call on success and failure with the result.
     */
    private void sendRequest( @NotNull String target, @NotNull Map<String, Object> message, int timeOutSec,
                              SyncedValue<Boolean> keepSending, APSHandler<APSResult<?>> resultHandler ) {

        Instant timeOut = Instant.now().plusSeconds( timeOutSec )

        // We keep sending every 2 seconds until a reply or timeout. The point of this is that
        // the sender might be up and running and sending before the receiver is ready to listen.
        // This gives the receiver time to catch up. More than one send will basically only happen
        // on startup or if a receiver is redeployed and thus temporarily unavailable and non receiving.
        // This is a very simple, primitive way of handling this. This worked on first try. My previous
        // attempt at a more complex service directory did not (Somehow the "keep it simple" rule
        // always seem to win!).
        //
        // DO NOTE THAT ANY SERVICE TAKING MORE THAN 3 SECONDS TO EXECUTE (INCLUDING NETWORK TIME)
        // WILL CAUSE PROBLEMS! On the other side if a service takes more than 2 seconds to respond
        // you probably have more serious problems than this.
        Exception sendFail = null
        Sporadic.until { keepSending.value && Instant.now().isBefore( timeOut ) }.
                interval( 2 ).
                exec {
                    System.err.println "################## DOING SEND! ##################"
                    System.err.println "Target: ${ target }"
                    send( target, message ) { APSResult<?> sendRes ->
                        if ( sendRes.success() ) {
                            sendFail = null
                            keepSending.value = false
                        }
                        else {
                            sendFail = sendRes.failure()
                        }
                    }
                }

        if ( sendFail != null ) {
            logger.error( "Failed to send message! ", sendFail )
            resultHandler.handle( APSResult.failure( sendFail ) )
        }
        else {
            if ( !keepSending.value || Instant.now().isBefore( timeOut ) ) {
                resultHandler.handle( APSResult.success( null ) )
            }
            else {
                resultHandler.handle(
                        APSResult.failure(
                                new APSMessagingException( "Timed out waiting for reply to request!" )
                        )
                )
            }
        }
    }

    /**
     * Replies to a received message.
     *
     * @param replyTo The received message to reply to.
     * @param reply The reply.
     * @param resultHandler The result of sending reply.
     */
    void reply( @NotNull Map<String, Object> replyTo, @NotNull Map<String, Object> reply,
                @Nullable APSHandler<APSResult<?>> resultHandler ) {

        if ( validateBaseMessageStructure( reply ) ) {
            String replyTarget = replyTo[ 'aps' ][ 'replyTarget' ]
            if ( replyTarget != null ) {
                send( replyTarget, reply, resultHandler )
            }
            else {
                if ( resultHandler != null ) {
                    resultHandler.handle(
                            APSResult.failure(
                                    new APSValidationException( "No {aps: {replyAddress: ... }} found! in replyTo!" )
                            )
                    )
                }
            }
        }
        else {
            validationFail( resultHandler )
        }
    }
}
