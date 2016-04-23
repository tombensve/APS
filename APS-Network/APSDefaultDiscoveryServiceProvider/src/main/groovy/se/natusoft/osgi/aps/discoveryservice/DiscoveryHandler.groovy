package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService
import se.natusoft.osgi.aps.api.net.discovery.exception.APSDiscoveryException
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.DatagramPacketListener
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequestListener
import se.natusoft.osgi.aps.codedoc.Implements
import se.natusoft.osgi.aps.discoveryservice.config.DiscoveryConfig
import se.natusoft.osgi.aps.exceptions.APSRuntimeException
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

import java.time.LocalDateTime
import java.util.concurrent.ExecutorService

import static se.natusoft.osgi.aps.api.net.discovery.DiscoveryKeys.*

/**
 * Keeps track of the services and handles all communication.
 */
@CompileStatic
@TypeChecked
class DiscoveryHandler implements DatagramPacketListener, StreamedRequestListener, APSConfigChangedListener {

    //
    // Properties
    //

    /** The known local services. */
    Set<Properties> localServices = Collections.synchronizedSet(new HashSet<Properties>())


    /** The known remote services. */
    Set<Properties> remoteServices = Collections.synchronizedSet(new HashSet<Properties>())

    //
    // Members
    //

    private final ReadErrorHandler readErrorHandler = new ReadErrorHandler()

    private URI mcastConnectionPointURI
    private URI tcpReceiverConnectionPointURI
    private List<URI> senderURIs

    @Managed(name = "executor-service")
    private ExecutorService executorService

    //
    // Services
    //

    @OSGiService
    private APSTCPIPService tcpipService

    @OSGiService
    private APSJSONService jsonService

    @Managed(name = "default-logger", loggingFor = "aps-default-discovery-service-provider")
    private APSLogger logger

    //
    // Methods
    //

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Initializer
    public void init() {
        // This has to be done in a separate thread or we risk a deadlock due to using a managed config.
        // *Config.managed.get() will wait for a config to become managed before returning the config,
        // and if this is done before the config service have been started when called from a bundle activator
        // then the whole startup process will hang forever, since the config service will never be started due
        // to this code never returning.

        this.executorService.submit {
            DiscoveryConfig.managed.get().addConfigChangedListener(this)

            // Do initial setup from config.
            apsConfigChanged(null)
        }
    }

    /**
     * Returns a now timestamp as a string.
     */
    private static String getNow() {
        return LocalDateTime.now().toString()
    }

    /**
     * Converts a JSONObject to a byte array.
     * @param jsonObject
     * @return
     */
    private byte[] jsonToBytes(JSONObject jsonObject) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream()

        this.jsonService.writeJSON(byteStream, jsonObject, true)

        return byteStream.toByteArray()
    }


    private JSONObject bytesToJSON(byte[] bytes) {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes)
        try {
            return this.jsonService.readJSON(byteStream, this.readErrorHandler) as JSONObject
        }
        finally {
            byteStream.close()
        }
    }

    /**
     * Event listener callback when event occurs.
     *
     * @param event information about the event.
     */
    @Override
    @Implements(APSConfigChangedListener.class)
    synchronized void apsConfigChanged(APSConfigChangedEvent event) {
        // Do note that we ignore "event" since we don't care what has changed, we only remove and add listeners
        // based on previous and new config. The APSConfigChangedEvent currently has an obvious flaw that prevents
        // its usefulness: only one singe configId can be provided! A task for this have been created: #aps-67.

        this.logger.info("Config changed - reconfiguring ...")

        cleanup()
        this.logger.info("Old setup cleaned ...")

        try {
            String mcastConnectionPoint = DiscoveryConfig.managed.get().multicastConnectionPoint.string
            if (mcastConnectionPoint != null && mcastConnectionPoint.empty) {
                this.mcastConnectionPointURI = null
            } else {
                this.mcastConnectionPointURI = new URI(mcastConnectionPoint)
                this.tcpipService.addDataPacketListener(this.mcastConnectionPointURI, this)
                this.logger.info("Added multicast listener for named config: " + this.mcastConnectionPointURI)
            }
        }
        catch (URISyntaxException use) {
            throw new APSDiscoveryException("Bad configuration! 'multicastConnectionPoint' must follow URI syntax!", use)
        }

        try {
            String tcpReceiverConnectionPoint = DiscoveryConfig.managed.get().tcpReceiverConnectionPoint.string
            if (tcpReceiverConnectionPoint != null && tcpReceiverConnectionPoint.empty) {
                this.tcpReceiverConnectionPointURI = null
            } else {
                this.tcpReceiverConnectionPointURI = new URI(tcpReceiverConnectionPoint)
                this.tcpipService.setStreamedRequestListener(this.tcpReceiverConnectionPointURI, this)
                this.logger.info("Set request listener for named config: " + this.tcpReceiverConnectionPointURI)
            }
        }
        catch (URISyntaxException use) {
            throw new APSDiscoveryException("Bad configuration! 'tcpReceiverConnectionPoint' must follow URI syntax!", use)
        }

        if (!DiscoveryConfig.managed.get().tcpPublishToConnectionPoints.empty) {
            try {
                this.senderURIs = new LinkedList<>()
                DiscoveryConfig.managed.get().tcpPublishToConnectionPoints.each { APSConfigValue tcpSvcConfig ->
                    this.senderURIs.add(new URI(tcpSvcConfig.string))
                }
            }
            catch (URISyntaxException use) {
                throw new APSDiscoveryException("Bad configuration! 'tcpPublishToConnectionPoints' must follow URI syntax!", use)
            }
        }

        this.logger.info("Setup according to new config!")
    }

    synchronized void cleanup() {
        if (this.mcastConnectionPointURI != null) {
            this.tcpipService.removeDataPacketListener(this.mcastConnectionPointURI, this)
            this.logger.info("Removed UDP listener for named config: " + this.mcastConnectionPointURI)
        }
        if (this.tcpReceiverConnectionPointURI != null) {
            this.tcpipService.removeStreamedRequestListener(this.tcpReceiverConnectionPointURI, this)
            this.logger.info("Removed TCP listener for named config: " + this.tcpReceiverConnectionPointURI)
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    @BundleStop
    synchronized void shutdown() {
        this.logger.info("Shutting down ...")
        cleanup()
        this.logger.info("Shutdown complete!")
    }

    /**
     * Sends off an update to other nodes.
     *
     * @param serviceDescription The service description to send.
     * @param header byte.
     *
     * @throws IOException The unavoidable ...
     */
    void sendUpdate(DiscoveryAction action, Properties serviceDescription) throws IOException {
        JSONObject serviceUpdate = this.jsonService.createJSONObject()
        serviceUpdate.fromMap([
                action: action.name(),
                serviceDescription: serviceDescription
        ])

        byte[] update = jsonToBytes(serviceUpdate)

        if (this.mcastConnectionPointURI != null) {
            this.executorService.submit(new MCastSendTask(
                    mcastConnectionPoint: this.mcastConnectionPointURI,
                    tcpipService: this.tcpipService,
                    logger: this.logger,
                    serviceInfo: update
            ))
        }

        if (this.senderURIs != null && !this.senderURIs.empty) {
            this.senderURIs.each { URI sendURI ->
                this.executorService.submit(new TCPSendTask(
                        tcpSendConnectionPoint: sendURI,
                        tcpipService: this.tcpipService,
                        logger: this.logger,
                        serviceInfo: update
                ))
            }
        }
    }

    /**
     * This is called whenever a new data block is received.
     *
     * @param receivePoint The receive-point this data came from.
     * @param packet The actual data received.
     */
    @Override
    @Implements(DatagramPacketListener.class)
    void dataBlockReceived(URI receivePoint, DatagramPacket packet) {

        Map<String, Object> jsonReq = bytesToJSON(packet.data).toMap()
        Properties serviceDescription = new Properties()
        serviceDescription.putAll(jsonReq.serviceDescription as Map<String, String>)

        // Since multicast are received by everyone including the sender we check that the
        // received service is not available among the local services.
        if (!this.localServices.contains(serviceDescription)) {

            // Since this is a HashSet which only contains one copy of each entry, any previous entry have to be removed
            // for a new entry to be added. So no matter if this is only a remove or an add we have to start with remove.
            this.remoteServices.remove(serviceDescription)

            DiscoveryAction action = DiscoveryAction.from(jsonReq.action)
            if (action == DiscoveryAction.ADD) {
                serviceDescription.setProperty(LAST_UPDATED, now)
                this.remoteServices.add(serviceDescription)
            }
        }
    }

    /**
     * Listeners of requests should implement this.
     *
     * @param receivePoint The receive-point the listener was registered with.
     * @param requestStream This contains the request data. DO NOT CLOSE THIS STREAM!
     * @param responseStream If receive-point is marked as async then this will be null, otherwise a
     *                       response should be written to this. DO NOT CLOSE THIS STREAM.
     */
    @Override
    @Implements(StreamedRequestListener.class)
    void requestReceived(URI receivePoint, InputStream requestStream, OutputStream responseStream) throws IOException {

        JSONValue json = this.jsonService.readJSON(requestStream, this.readErrorHandler)
        if (JSONObject.class.isAssignableFrom(json.getClass())) {
            throw new IOException("Received unexpected JSON! [${json}]")
        }

        Map<String, Object> jsonReq = (json as JSONObject).toMap()
        Properties serviceDescription = new Properties()
        serviceDescription.putAll(jsonReq.serviceDescription as Map<String, String>)


        // Since this is a HashSet which only contains one copy of each entry, any previous entry have to be removed
        // for a new entry to be added. So no matter if this is only a remove or an add we have to start with remove.
        this.remoteServices.remove(serviceDescription)

        DiscoveryAction action = DiscoveryAction.from(jsonReq.action)
        if (action == DiscoveryAction.ADD) {
            serviceDescription.setProperty(LAST_UPDATED, now)
            this.remoteServices.add(serviceDescription)
        }
    }

    /**
     * Removed external services that are over 3 minutes old.
     */
    synchronized void cleanExpired() {
        LocalDateTime validTime = LocalDateTime.now().minusMinutes(3)

        List<Properties> toRemove = new LinkedList<>()
        this.remoteServices.each { Properties sd ->
            LocalDateTime lastUpdated = LocalDateTime.parse(sd.getProperty(LAST_UPDATED))
            if (lastUpdated?.isBefore(validTime)) {
                toRemove.add(sd)
                this.logger.info("Cleanup - Removing: " + sd)
            }
        }

        toRemove.each { Properties sd ->
            this.remoteServices.remove(sd)
        }
    }

    //
    // Inner Classes
    //

    public class ReadErrorHandler implements JSONErrorHandler {

        /**
         * Warns about something.
         *
         * @param message The warning message.
         */
        @Override
        @Implements(JSONErrorHandler.class)
        void warning(String message) {
            logger.warn(message)
        }

        /**
         * Indicate failure.
         *
         * @param message The failure message.
         * @param cause The cause of the failure. Can be null!
         *
         * @throws RuntimeException This method must throw a RuntimeException.
         */
        @Override
        @Implements(JSONErrorHandler.class)
        void fail(String message, Throwable cause) throws RuntimeException {
            logger.error("Failed to parse received JSON! (${message})", cause)
            throw new APSRuntimeException(message, cause)
        }
    }
}

