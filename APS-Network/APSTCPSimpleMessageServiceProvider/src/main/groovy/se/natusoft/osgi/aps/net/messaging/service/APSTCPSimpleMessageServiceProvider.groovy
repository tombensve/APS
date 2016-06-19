/* 
 * 
 * PROJECT
 *     Name
 *         APS TCP Simple Message Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides a direct TCP based message service that is not persistent. This service makes use of
 *         the TCPIPService.
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
 *         2016-06-19: Created!
 *         
 */
package se.natusoft.osgi.aps.net.messaging.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.Implements
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessage
import se.natusoft.osgi.aps.api.net.messaging.service.APSSimpleMessageService
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequest
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequestListener
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.messaging.config.ServiceConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * This provides the APSSimpleMessageService over TCP.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@OSGiServiceProvider(properties = [
        @OSGiProperty(name = APS.SERVICE_PROVIDER, value = "aps-tcp-simple-message-service-provider"),
        @OSGiProperty(name = APS.SERVICE_CATEGORY, value = APS.Messaging.SERVICE_CATEGORY),
        @OSGiProperty(name = APS.SERVICE_FUNCTION, value = APS.Messaging.SERVICE_FUNCTION),
        @OSGiProperty(name = APS.Messaging.SERVICE_CATEGORY, value = APS.TRUE),
        @OSGiProperty(name = APS.Messaging.SERVICE_FUNCTION, value = APS.TRUE),
        @OSGiProperty(name = APS.Messaging.PERSISTENT, value = "false"),
        @OSGiProperty(name = APS.Messaging.MULTIPLE_RECEIVERS, value = "true")
])
@CompileStatic
@TypeChecked
class APSTCPSimpleMessageServiceProvider implements APSSimpleMessageService, APSConfigChangedListener, StreamedRequestListener {
    //
    // Constants
    //

    private static final String DISCOVERY_ENTRY_ID = "aps-tcp-simple-message-service"
    private static final String DISCOVERY_ENTRY_VERSION = "1.0.0"

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-tcp-simple-message-service-provider")
    APSLogger logger

    @OSGiService
    private APSTCPIPService tcpipService

    @OSGiService
    private APSSimpleDiscoveryService discoveryService

    /** Client listeners */
    private Map<String/*topic*/, List<APSSimpleMessageService.MessageListener>> msgListeners = new HashMap<>()

    /** Resolved send to connection points. */
    private List<URI> senders = null

    /** A little flag marking all senders as setup. Client calls will be paused until this happens. */
    private boolean sendersAvailable = false

    /** The connection point to listen to. */
    private URI recvConnectionPoint = null

    /** For registering with the discovery service. */
    private Properties sd = null

    /** For sending more efficiently. */
    private ExecutorService threadPool = null

    //
    // Methods
    //

    @BundleStart(thread = true)
    void init() {
        this.threadPool = Executors.newFixedThreadPool(10)

        setupReceiver()
        setupSenders()

        registerWithDiscoveryService()
    }

    @BundleStop
    void exit() {
        shutdownReceiver()
        unregisterWithDiscoveryService()

        this.threadPool.shutdown()
    }

    private void registerWithDiscoveryService() {
        if (ServiceConfig.managed.get().registerWithDiscoveryService.boolean) {
            this.sd = new Properties()
            this.sd.host = InetAddress.getLocalHost().hostName
            this.sd.port = this.recvConnectionPoint.port
            this.sd.protocol = "TCP"
            this.sd.name = DISCOVERY_ENTRY_ID
            this.sd.version = DISCOVERY_ENTRY_VERSION
            this.sd.apsURI = "tcp://${sd.host}:${sd.port}"

            this.discoveryService.publishService(this.sd)
        }
    }

    private void unregisterWithDiscoveryService() {
        if (this.sd != null && ServiceConfig.managed.get().registerWithDiscoveryService.boolean) {
            this.discoveryService.unpublishService(this.sd)
            this.sd = null
        }
    }

    private void setupReceiver() {
        shutdownReceiver()
        this.recvConnectionPoint = new URI(ServiceConfig.managed.get().listenConnectionPointUrl.string)
        this.tcpipService.setStreamedRequestListener(this.recvConnectionPoint, this)
    }

    private void shutdownReceiver() {
        if (this.recvConnectionPoint != null) {
            this.tcpipService.removeStreamedRequestListener(this.recvConnectionPoint, this)
        }
    }

    private void setupSenders() {
        this.senders = new LinkedList<>()

        ServiceConfig.managed.get().sendConnectionPointUrls.each { APSConfigValue value ->
            def connPoint = value.string
            if (!connPoint.trim().endsWith("#async")) {
                connPoint = connPoint.trim() + "#async"
            }
            this.senders.add(new URI(connPoint))
        }

        if (ServiceConfig.managed.get().lookInDiscoveryService.boolean) {
            try {
                def unique = [:] as Map<String, String>
                this.discoveryService.getServices("&((name=${DISCOVERY_ENTRY_ID})(version=${DISCOVERY_ENTRY_VERSION}))").each
                { Properties sd ->

                    def hostPort = "${sd.host}:${sd.port}" as String
                    def discoveredAddress = InetAddress.getByName("${sd.host}")

                    if (!unique.containsKey(hostPort) && discoveredAddress != InetAddress.localHost) {
                        this.senders.add(new URI("tcp://${hostPort}#async"))
                        unique.put(hostPort, hostPort)
                    }
                }
            }
            catch (Exception e) {
                logger.error("Failed discovery service call!", e)
            }
        }

        // Notify possible client sender in other thread that was faster than this startup.
        this.sendersAvailable = true
        synchronized (this) {
            notifyAll()
        }
    }

    /**
     * Creates a new APSMessage.
     */
    @Implements(APSSimpleMessageService.class)
    @Override
    APSMessage createMessage() {
        new APSMessage.Provider()
    }

    /**
     * Adds a listener for types.
     *
     * @param target The topic to listen to.
     * @param listener The listener to add.
     */
    @Implements(APSSimpleMessageService.class)
    @Override
    void addMessageListener(String target, APSSimpleMessageService.MessageListener listener) {
        List<APSSimpleMessageService.MessageListener> listeners = this.msgListeners.get(target)
        if (listeners == null) {
            listeners = new LinkedList<>()
            this.msgListeners.put(target, listeners)
        }
        listeners.add(listener)
    }

    /**
     * Removes a messaging listener.
     *
     * @param target The topic to stop listening to.
     * @param listener The listener to remove.
     */
    @Implements(APSSimpleMessageService.class)
    @Override
    void removeMessageListener(String target, APSSimpleMessageService.MessageListener listener) {
        List<APSSimpleMessageService.MessageListener> listeners = this.msgListeners.get(target)
        if (listeners != null) {
            listeners.remove(listener)
            if (listeners.isEmpty()) {
                this.msgListeners.remove(target)
            }
        }
    }

    /**
     * Sends a message.
     *
     * @param target The target of the message.
     * @param message The message to send.
     *
     * @throws APSMessagingException on failure.
     */
    @Implements(APSSimpleMessageService.class)
    @Override
    void sendMessage(String target, APSMessage message) throws APSMessagingException {
        APSMessagingException msgException = new APSMessagingException("Send not entirely successful! Failing recipients: ")

        ProtocolMessage msg = new ProtocolMessage(target: target, data: message.bytes)

        def futures = [] as List<Future>

        // Make sure the service is initialized before continuing. A client can possible be faster than the service
        // startup!
        if (!this.sendersAvailable) {
            synchronized (this) {
                wait(5000) // We will be notified as soon as they are available! This number is just a timeout if things go due south.
            }
        }

        this.senders.each { URI senderCP ->

            futures << this.threadPool.submit({
                try {
                    this.tcpipService.sendStreamedRequest(senderCP, new StreamedRequest() {
                        @Override
                        void sendRequest(URI sendPoint, OutputStream requestStream, InputStream responseStream) throws IOException {
                            msg >> requestStream
                        }
                    })
                }
                catch (IOException ioe) {
                    msgException.addToMessage("(${senderCP})")
                    msgException.addCause(ioe)
                }
            } as Runnable)

        }

        // Wait for all to finish before we check if any failed.
        futures.each { Future future -> future.get() }

        if (msgException.hasCauses()) {
            throw msgException
        }
    }


    /**
     * Event listener callback when event occurs.
     *
     * @param event information about the event.
     */
    @Implements(APSConfigChangedListener.class)
    public synchronized void apsConfigChanged(APSConfigChangedEvent event) {
        setupReceiver()
        setupSenders()
        unregisterWithDiscoveryService()
        registerWithDiscoveryService()
    }

    /**
     * Listeners of requests should implement this.
     *
     * @param receivePoint The receive-point the listener was registered with.
     * @param requestStream This contains the request data. DO NOT CLOSE THIS STREAM!
     * @param responseStream If receive-point is marked as async then this will be null, otherwise a
     *                       response should be written to this. DO NOT CLOSE THIS STREAM.
     */
    @Implements(StreamedRequestListener.class)
    void requestReceived(URI receivePoint, InputStream requestStream, OutputStream responseStream) throws IOException {
        ProtocolMessage msg = new ProtocolMessage() << requestStream
        this.msgListeners.get(msg.target)?.each { APSSimpleMessageService.MessageListener listener ->
            try {
                APSMessage message = createMessage()
                message.bytes = msg.data
                listener.messageReceived(msg.target, message)
            }
            catch (Throwable t) {
                this.logger.error("Failed to call listener: ${listener}", t)
            }
        }
    }
}
