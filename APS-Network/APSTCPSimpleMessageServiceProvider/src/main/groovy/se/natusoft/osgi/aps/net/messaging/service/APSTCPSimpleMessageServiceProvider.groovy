package se.natusoft.osgi.aps.net.messaging.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.Implements
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.service.APSSimpleMessageService
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequest
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequestListener
import se.natusoft.osgi.aps.api.net.util.TypedData
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
        @OSGiProperty(name = "service-provider", value = "aps-tcp-simple-message-service-provider")
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
        this.tcpipService.removeStreamedRequestListener(this.recvConnectionPoint, this)
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
        if (this.recvConnectionPoint != null) {
            this.tcpipService.removeStreamedRequestListener(this.recvConnectionPoint, this)
        }
        this.recvConnectionPoint = new URI(ServiceConfig.managed.get().listenConnectionPointUrl.string)
        this.tcpipService.setStreamedRequestListener(this.recvConnectionPoint, this)
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
     * Adds a listener for types.
     *
     * @param topic The topic to listen to.
     * @param listener The listener to add.
     */
    @Implements(APSSimpleMessageService.class)
    void addMessageListener(String topic, APSSimpleMessageService.MessageListener listener) {
        List<APSSimpleMessageService.MessageListener> listeners = this.msgListeners.get(topic)
        if (listeners == null) {
            listeners = new LinkedList<>()
            this.msgListeners.put(topic, listeners)
        }
        listeners.add(listener)
    }

    /**
     * Removes a messaging listener.
     *
     * @param topic The topic to stop listening to.
     * @param listener The listener to remove.
     */
    @Implements(APSSimpleMessageService.class)
    void removeMessageListener(String topic, APSSimpleMessageService.MessageListener listener) {
        List<APSSimpleMessageService.MessageListener> listeners = this.msgListeners.get(topic)
        if (listeners != null) {
            listeners.remove(listener)
            if (listeners.isEmpty()) {
                this.msgListeners.remove(topic)
            }
        }
    }

    /**
     * Sends a message.
     *
     * @param topic The topic of the message.
     * @param message The message to send.
     *
     * @throws APSMessagingException on failure.
     */
    @Implements(APSSimpleMessageService.class)
    void sendMessage(String topic, TypedData message) throws APSMessagingException {
        APSMessagingException msgException = new APSMessagingException("Send not entirely successful! Failing recipients: ")

        Message msg = new Message(topic: topic, typedData: message)

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
        Message msg = new Message() << requestStream
        this.msgListeners.get(msg.topic)?.each { APSSimpleMessageService.MessageListener listener ->
            try {
                listener.messageReceived(msg.topic, msg.typedData)
            }
            catch (Throwable t) {
                this.logger.error("Failed to call listener: ${listener}", t)
            }
        }
    }
}
