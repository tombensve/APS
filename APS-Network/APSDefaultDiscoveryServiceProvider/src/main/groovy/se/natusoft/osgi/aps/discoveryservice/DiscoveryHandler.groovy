package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.net.discovery.exception.APSDiscoveryException
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescriptionProvider
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.DatagramPacketListener
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequestListener
import se.natusoft.osgi.aps.discoveryservice.config.DiscoveryConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.util.LoggingRunnable

import java.time.LocalDateTime

import static ReadWriteTools.HB_ADD

/**
 * Keeps track of the services and handles all communication.
 */
@CompileStatic
@TypeChecked
class DiscoveryHandler implements DatagramPacketListener, StreamedRequestListener, APSConfigChangedListener {

    //
    // Properties
    //

    /** The known remote services. */
    Set<ServiceDescription> remoteServices = Collections.synchronizedSet(new HashSet<ServiceDescription>())

    //
    // Members
    //

    private URI mcastConnectionPointURI
    private URI tcpReceiverConnectionPointURI
    private List<URI> senderURIs

    @Managed(name = "executor-service")
    private DiscoveryExecutorService executorService

    //
    // Services
    //

    @OSGiService
    private APSTCPIPService tcpipService

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

        // IDEA bug.
        //noinspection GroovyAccessibility
        this.executorService.submit(new LoggingRunnable(this.logger) {
            @Override
            void doRun() {
                DiscoveryConfig.managed.get().addConfigChangedListener(DiscoveryHandler.this)

                // Do initial setup from config.
                apsConfigChanged(null)
            }
        })
    }

    /**
     * Event listener callback when event occurs.
     *
     * @param event information about the event.
     */
    @Override
    synchronized void apsConfigChanged(APSConfigChangedEvent event) {
        // Do note that we ignore "event" since we don't care what has changed, we only remove and add listeners
        // based on previous and new config. The APSConfigChangedEvent currently has an obvious flaw that prevents
        // its usefulness: only one singe configId can be provided!

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
        if (this.executorService != null) {
            this.executorService.shutdown()
        }
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
    void sendUpdate(ServiceDescription serviceDescription, byte headerByte) throws IOException {
        if (this.mcastConnectionPointURI != null) {
            this.executorService.submit(new MCastSendTask(
                    mcastConnectionPoint: this.mcastConnectionPointURI,
                    headerByte: headerByte,
                    serviceDescription: serviceDescription,
                    tcpipService: this.tcpipService,
                    logger: this.logger
            ))
        }

        if (this.senderURIs != null && !this.senderURIs.empty) {
            this.senderURIs.each { URI sendURI ->
                this.executorService.submit(new TCPSendTask(
                        tcpSendConnectionPoint: sendURI,
                        headerByte: headerByte,
                        serviceDescription: serviceDescription,
                        tcpipService: this.tcpipService,
                        logger: this.logger
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
    void dataBlockReceived(URI receivePoint, DatagramPacket packet) {
        ServiceDescriptionProvider serviceDescription = new ServiceDescriptionProvider()
        byte headerByte = ReadWriteTools.fromBytes(serviceDescription, packet.data)

        // Since this is a HashSet which only contains one copy of each entry, any previous entry have to be removed
        // for a new entry to be added. So no matter if this is only a remove or an add we have to start with remove.
        this.remoteServices.remove(serviceDescription)

        if (headerByte == HB_ADD) {
            serviceDescription.setLastUpdated(LocalDateTime.now())
            this.remoteServices.add(serviceDescription)
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
    void requestReceived(URI receivePoint, InputStream requestStream, OutputStream responseStream) throws IOException {
        ServiceDescriptionProvider serviceDescription = new ServiceDescriptionProvider()
        ObjectInputStream ooStream = new ObjectInputStream(requestStream)
        byte headerByte = ReadWriteTools.readServiceDescription(serviceDescription, ooStream)

        // Since this is a HashSet which only contains one copy of each entry, any previous entry have to be removed
        // for a new entry to be added. So no matter if this is only a remove or an add we have to start with remove.
        this.remoteServices.remove(serviceDescription)

        if (headerByte == HB_ADD) {
            serviceDescription.setLastUpdated(LocalDateTime.now())
            this.remoteServices.add(serviceDescription)
        }
    }

    /**
     * Removed external services that are over 3.2 minutes old.
     */
    synchronized void cleanExpired() {
        LocalDateTime validTime = LocalDateTime.now().minusMinutes(3)

        List<ServiceDescription> toRemove = new LinkedList<>()
        this.remoteServices.each { ServiceDescription sd ->
            if (sd.lastUpdated.isBefore(validTime)) {
                toRemove.add(sd)
                this.logger.info("Cleanup - Removing: " + sd)
            }
        }

        toRemove.each { ServiceDescription sd ->
            this.remoteServices.remove(sd)
        }
    }

}

