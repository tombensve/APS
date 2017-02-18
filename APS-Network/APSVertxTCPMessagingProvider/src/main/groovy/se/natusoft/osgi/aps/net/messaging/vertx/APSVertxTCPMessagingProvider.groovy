/* 
 * 
 * PROJECT
 *     Name
 *         APS Vertx TCP Messaging Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides messaging over TCP/IP using Vert.x Net service.
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
package se.natusoft.osgi.aps.net.messaging.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.buffer.Buffer
import io.vertx.groovy.core.eventbus.MessageConsumer
import io.vertx.groovy.core.net.NetClient
import io.vertx.groovy.core.net.NetServer
import io.vertx.groovy.core.net.NetSocket
import org.osgi.framework.BundleContext
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.api.net.messaging.service.APSSubscriber
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.vertx.api.APSVertxService
import se.natusoft.osgi.aps.net.messaging.vertx.api.APSVertxTCPMessagingOptions
import se.natusoft.osgi.aps.tools.APSActivatorInteraction
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.APSSimplePool
import se.natusoft.osgi.aps.tools.annotation.activator.*
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException
import se.natusoft.osgi.aps.tools.tracker.WithServiceException

// Note to IDEA users: IDEA underlines (in beige color if you are using Darcula theme) all references to classes that are not
// OSGi compatible (no OSGi MANIFEST.MF entries). The underlines you see here is for the Groovy wrapper of Vert.x. It is OK
// since this wrapper gets included in the bundle. The main Vert.x code is OSGi compliant and can be deployed separately.

/**
 * Provides messaging using vertx. In this case TCP clients and servers. Servers receives requests to subscriber and publishes
 * response. Clients publishes requests, and subscribe to response.
 *
 * Clients and servers are setup from configuration. The topic used determines what is what. There can be client topics
 * or server topics. It helps to have good, clear topics.
 *
 * See http://vertx.io/docs/ for more information on vert.x.
 *
 * This service implements TCP servers and clients using the APSMessageService API. I admit that this is a bit contrived,
 * but my intention is that code should in an as small way as possible be aware of the communication and what protocols
 * are used. Its only about sending or receiving blocks of data. This specific service uses TCP sockets to communicate.
 *
 * To make this implementation wise and also user wise simpler the API looks slightly different that standard pub/sub
 * APIs in that subscribers receive in addition to topic and message an APSPublisher instance. The APSMessageService
 * extends this interface. This supplied instance should be used to deliver replies when the subscriber is a server
 * type of subscriber. For client type subscriber the received message is a reply and the APSPublisher will throw
 * an APSMessagingException if it is called.
 */
@SuppressWarnings( "GroovyUnusedDeclaration" ) // This is never referenced directly, only through APSMessageService API.
@OSGiServiceProvider(
        // Possible criteria for client lookups. ex: "(${APS.Messaging.Protocol.Name}=vertx-eventbus)" In most cases clients won't care.
        properties = [
                @OSGiProperty( name = APS.Service.Provider,        value = "aps-vertx-tcp-messaging-provider" ),
                @OSGiProperty( name = APS.Service.Category,        value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function,        value = APS.Value.Service.Function.Messaging ),
                @OSGiProperty( name = APS.Messaging.Protocol.Name, value = "TCP" ),
                @OSGiProperty( name = APS.Messaging.Persistent,    value = APS.FALSE )
        ],
        threadStart = true // Required since we are putting ourselves in a blocking situation when we are calling
                           // APSVertxService which might be deployed after us and bundle activations are not threaded!
)
@CompileStatic
@TypeChecked
class APSVertxTCPMessagingProvider extends APSMessageService.AbstractAPSMessageService implements APSMessageService {

    //
    // Private Members
    //

    /** Our bundle context. */
    @Managed
    BundleContext context

    /** For logging. */
    @Managed(loggingFor = "aps-vertx-tcp-messaging-provider")
    private APSLogger logger

    /** Will delay registration of service until state == READY */
    @Managed
    private APSActivatorInteraction activatorInteraction

    @OSGiService( timeout = "5 sec" )
    private APSVertxService vertxService

    @OSGiService(timeout = "5 sec")
    private APSServiceTracker<APSVertxTCPMessagingOptions> tcpMessagingOptionsTracker

    /** The listeners of this service. */
    private Map<String, List<APSSubscriber>> listeners = [ : ]

    /** We have one consumer per topic towards Vert.x. If we have more that one listener on a topic we handle that internally. */
    private Map<String, MessageConsumer> consumers = [ : ]

    /** Servers per topic. */
    private Map<String, List<NetServer>> activeServers = [ : ]

    /** Clients per topic. */
    private Map<String, APSSimplePool<Tuple2<NetClient, NetSocket>>> activeClients = [ : ]

    /** A map between topics and URIs for clients. */
    private Map<String, List<URI>> clientTopicURIMap = [ : ]

    /** The topic : URI mappings config. */
    private Map<String , String> topicURIConfig = null

    /** The root of all Vert.x! */
    private Vertx vertx = null

    //
    // Initializer
    //

    /**
     * Setup. This is called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.connectToLogService( this.context ) // Connect to OSGi log service if available. APSLogger does not use a timeout when
                                                        // tracking the LogService so it will fail immediately if service is not available,
                                                        // so there is no risk of blocking (which there is when timeout is used).

        this.vertxService.useGroovyVertX APSVertxService.DEFAULT_INST , { AsyncResult<Vertx> result ->

            if ( result.succeeded() ) {
                this.vertx = result.result()

                this.setupReceive()

                this.logger.info "Vert.x cluster started successfully!"

                this.activatorInteraction.state = APSActivatorInteraction.State.READY
            }

            else {
                this.logger.error "Vert.x cluster failed to start: ${result.cause().message}, shutting down bundle!" , result.cause()
                this.activatorInteraction.state = APSActivatorInteraction.State.STARTUP_FAILED

                this.context.bundle.stop()
            }
        }
    }

    /**
     * This gets called when out bundle gets stopped. In this case we need to shut down Vert.x.
     */
    @BundleStop
    void stop() {
        this.activeServers.each { String topic , List<NetServer> servers ->

            servers.each { NetServer server ->

                server.close { AsyncResult result ->

                    if ( result.succeeded() ) {
                        this.logger.info "Server '${server}' closed successfully!"
                    }
                    else {
                        this.logger.error "Servier '${server}' failed to close!", result.cause()
                    }
                }
            }
        }
        this.activeServers.clear()
        this.activeServers = null

        this.activeClients.each { String topic, APSSimplePool<Tuple2<NetClient, NetSocket>> clientPool ->
            clientPool.close()
            clientPool.allPooledEntries.each { Tuple2<NetClient, NetSocket> clientPublisher ->
                try {
                    clientPublisher.second.close()
                    clientPublisher.first.close()
                }
                catch (RuntimeException re) {
                    this.logger.error "Failed to close client!", re
                }
            }
        }
        this.activeClients.clear()
        this.activeClients = null

        if ( this.vertx != null ) {
            try {
                this.vertxService.releaseGroovyVertX APS.DEFAULT
            }
            catch ( APSNoServiceAvailableException ignore ) {
                // We don't want a stacktrace with this log since that make this seem more serious than it really is.
                // This happens on shutdown and APSVertxService shuts down before this service. We are on out way down,
                // and if APSVertxService went down before us it already released/disconnected us.
                this.logger.warn "Failed to disconnect Vertx instance from APSVertxService! This most probably means " +
                        "that the APSVertxService has shut down faster than this service. This is OK in that case."
            }
        }

        this.logger.disconnectFromLogService this.context
    }

    //
    // Methods
    //

    /**
     * Returns the mapping between topics and uris.
     */
    private Map<String , String> getTopicURIMapping() {
        if ( this.topicURIConfig == null) {
            this.topicURIConfig = new LinkedHashMap<>()
            try {
                this.tcpMessagingOptionsTracker.waitForService( 10000 )
                this.tcpMessagingOptionsTracker.withAllAvailableServices { APSVertxTCPMessagingOptions options, Object[] ignore ->
                    if (options.topicToURIMapping != null) {
                        this.topicURIConfig.putAll options.topicToURIMapping
                    }
                }
            }
            catch (WithServiceException wse) {
                this.logger.error "Failed to get options for aps-vertx-tcp-messaging-provider!" , wse
            }
        }

        return this.topicURIConfig
    }

    /**
     * Helper called from initializer. Sets up both clients and servers from configuration.
     */
    private void setupReceive() {

        topicURIMapping.each { String topic, String s_uris ->
            try {
                s_uris.split(",").each { String s_uri ->

                    URI uri = new URI( s_uri.trim() )

                    this.logger.info "TOPIC: ${topic}  URI: ${uri}"
                    // In this case we create a server that listens on connections and calls a subscriber to respond
                    // to the received "request". The APSPublisher passed to the subscriber should be used to send
                    // a response.
                    if ( "in" == uri.fragment?.toLowerCase() ) {

                        List<NetServer> servers = activeServers [ topic ]
                        if ( servers == null ) {
                            servers = []
                            activeServers [ topic ] = servers
                        }

                        // If '.times' are red marked, then you are probably using IDEA. This is legal groovy and compiles fine.
                        getInstances( uri ).times {

                            NetServer server = createServer().connectHandler { NetSocket socket ->

                                socket.handler { Buffer buffer ->
                                    handleSubscribers topic , buffer
                                }

                            }

                            server.listen uri.port , uri.host , { AsyncResult<NetServer> result ->
                                if (result.succeeded()) {
                                    this.logger.info("Server listening on ${uri.host}:${uri.port}!")
                                }
                                else {
                                    this.logger.error("Failed to listen to ${uri.host}:${uri.port}!")
                                }
                            }
                            servers << server

                        }
                    }

                    else {
                        if ( "out" != uri.fragment?.toLowerCase() ) {
                            this.logger.error "Bad URI spec for topic '${topic}'! fragment (#) can only be 'in' or 'out'."
                        }
                    }
                }
            }
            catch ( URISyntaxException use ) {
                this.logger.error "Bad URI spec for topic '${topic}'!", use
            }
        }
    }

    /**
     * Handles a received message that gets forwarded to subscribers.
     *
     * @param topic The topic the server belongs to.
     * @param buffer The received request data.
     * @param publisher Should be passed on to subscribers for publishing replies to the request.
     */
    private void handleSubscribers(String topic, Buffer buffer ) {

        if ( this.listeners.containsKey( topic ) ) {

            this.listeners [ topic ] .each { APSSubscriber subscriber ->
                subscriber.subscription topic , buffer
            }
        }
    }

    /**
     * Returns the number of instances of a service to create. This information is taken from the URI query inst=n. Default is 1.
     *
     * @param uri The URI to get the number of instances from.
     */
    private static int getInstances( URI uri ) {
        int instances = 1
        String[] queries = uri.query?.split "&"

        if ( queries != null ) {

            String[] query = queries[ 0 ].split "="

            if ( query != null && query[ 0 ] == "inst" ) {
                instances = Integer.valueOf query[ 1 ]
            }
        }

        return instances
    }

    /**
     * Creates a NetServer.
     */
    private NetServer createServer() {
        Map<String, Object> serverOptions = new LinkedHashMap<>()
        try {
            this.tcpMessagingOptionsTracker.withAllAvailableServices { APSVertxTCPMessagingOptions options, Object[] ignore ->
                if ( options.serverOptions != null ) {
                    serverOptions.putAll options.serverOptions
                }
            }
        }
        catch ( APSNoServiceAvailableException ignore ) {
            serverOptions = new LinkedHashMap<>()
        }

        this.vertx.createNetServer serverOptions
    }

    /**
     * Creates a NetClient.
     */
    private NetClient createClient() {
        Map<String, Object> clientOptions = new LinkedHashMap<>()
        try {
            this.tcpMessagingOptionsTracker.waitForService(3000)
            this.tcpMessagingOptionsTracker.withAllAvailableServices { APSVertxTCPMessagingOptions options, Object[] ignore ->
                if ( options.clientOptions != null ) {
                    clientOptions.putAll options.clientOptions
                }
            }
        }
        catch ( APSNoServiceAvailableException ignore ) {
            clientOptions = new LinkedHashMap<>()
        }

        this.vertx.createNetClient clientOptions

    }

    /**
     * Returns a List of topic listeners, creating an empty if none exists.
     *
     * @param topic The topic to get listeners for.
     */
    private @NotNull List<APSSubscriber> getSubscribersForTopic(@NotNull String topic ) {
        List<APSSubscriber> topicListeners = this.listeners [ topic ]

        if ( topicListeners == null ) {
            topicListeners = new LinkedList<>()
            this.listeners [ topic ] = topicListeners
        }

        return topicListeners
    }

    /**
     * Support method to read configuration and setup mappings between topic and URIs.
     *
     * @param topic The topic to setup mappings for.
     */
    private void setupClientURIMappings( String topic ) {
        String uris = this.topicURIMapping [ topic ]

        if ( uris != null ) {
            uris.split(",").each { String configURI ->
                try {
                    URI uri = new URI(configURI)

                    if ("out" == uri.fragment?.toLowerCase()) {

                        List<URI> clientURIs = this.clientTopicURIMap [ topic ]
                        if ( clientURIs == null ) {
                            clientURIs = new LinkedList<>()
                            this.clientTopicURIMap.put topic , clientURIs
                        }

                        clientURIs << uri
                    }
                }
                catch (URISyntaxException use) {
                    this.logger.error "Bad URI spec for topic '${topic}'!", use
                }
            }
        }
    }

    /**
     * Sends a message to the destination.
     *
     * Valid properties:
     *
     *      none
     *
     * @param topic The destination to send message.
     * @param message The message to send.
     * @param props Implementation specific properties.
     */
    @Override
    void publish( @NotNull String topic , @NotNull Object message , @Nullable Properties props ) {

        if (!this.clientTopicURIMap.containsKey( topic )) {
            setupClientURIMappings topic
        }

        List<URI> uris = this.clientTopicURIMap [ topic ]
        if ( uris == null ) {
            throw new APSMessagingException( "Topic '${topic}' is undefined!" )
        }

        uris.each { URI uri ->

            int insts = getInstances( uri )

            APSSimplePool<Tuple2<NetClient, NetSocket>> clientPool = this.activeClients [ topic ]

            if ( clientPool == null ) {
                clientPool = new APSSimplePool<>()
                this.activeClients.put topic , clientPool
            }

            if ( clientPool.availableSize == 0 ) {

                NetClient client = createClient()

                client.connect uri.port, uri.host, { AsyncResult<NetSocket> result ->

                    if ( result.succeeded() ) {
                        NetSocket socket = result.result()

                        Tuple2<NetClient, NetSocket> t2 = new Tuple2<>( client , socket )

                        this.logger.info "Net client '${topic}' to ${uri.host}:${uri.port} started!"

                        doPublish socket , message

                        if ( clientPool.poolSize < insts ) {
                            Tuple2<NetClient, NetSocket> entry = new Tuple2<>( client , socket )
                            clientPool.add entry

                            this.logger.info "New client was added to the pool!"
                        }

                        else {
                            client.close()
                            this.logger.warn "client pool already has the max of ${insts} clients. This was created because they " +
                                    "all were busy. If this happens a lot you should increase pool size (#insts=n on URI)."
                        }
                    }

                    else {
                        this.logger.error "Net client '${topic}' to ${uri.host}:${uri.port} failed: ${result.cause().message}",
                                result.cause()
                    }
                }
            }

            else {
                Tuple2<NetClient, NetSocket> clientSocket = clientPool.allocate()
                doPublish clientSocket.second , message
                clientPool.release clientSocket
            }
        }
    }


    /**
     * Sends a message to one destination.
     *
     * @param socket The socket to write to.
     * @param message The message to send. What is allowed here depends on the provider.
     */
    private static void doPublish( @NotNull NetSocket socket , @NotNull Object message ) {

        if ( String.class.isAssignableFrom( message.class ) || GString.class.isAssignableFrom( message.class )) {
            socket.write message as String
        }

        else if ( Buffer.class.isAssignableFrom( message.class ) ) {
            socket.write message as Buffer
        }

        else if ( Byte.class.isAssignableFrom( message.class ) && message.class.isArray() ) {
            Buffer repBuff = Buffer.buffer()

            byte[] msgBytes = message as byte[]
            (msgBytes).each { byte b ->
                repBuff.appendByte b
            }
            socket.write repBuff
        }

        else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream( baos )
            oos.writeObject message
            oos.close()
            baos.close()

            Buffer repBuff = Buffer.buffer()
            baos.toByteArray().each { byte b -> repBuff.appendByte b }
            socket.write repBuff
        }
    }


    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * Valid properties:
     *
     *      none
     *
     * @param topic The endpoint to listen to.
     * @param listener The listener to call with received messages.
     * @param props Implementation specific properties.
     */
    @Override
    void subscribe( @NotNull String topic , @NotNull APSSubscriber listener , Properties props ) {
        List<APSSubscriber> topicListeners = getSubscribersForTopic topic

        if ( !topicListeners.contains( listener ) ) {
            topicListeners.add listener
        }
    }


    /**
     * Removes a listener for a source.
     *
     * @param topic The endpoint to remove listener for.
     * @param listener The listener to remove.
     */
    @Override
    void unsubscribe( @NotNull String topic , @NotNull APSSubscriber listener ) {
        getSubscribersForTopic( topic )?.remove listener
    }

}

