package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescriptionProvider
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.TCPListener
import se.natusoft.osgi.aps.api.net.tcpip.UDPListener
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
class DiscoveryHandler implements UDPListener, TCPListener, APSConfigChangedListener {
    //
    // Properties
    //

    /** The known remote services. */
    Set<ServiceDescription> remoteServices = Collections.synchronizedSet(new HashSet<ServiceDescription>())

    //
    // Members
    //

    private String _mcastConfig
    private String _tcpReceiverConfig

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

        // The following noinspection is required due to a bug in IDEA!
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

        this._mcastConfig = DiscoveryConfig.managed.get().mcastDiscoveryConfigName.string
        if (this._mcastConfig != null && this._mcastConfig.empty) {
            this._mcastConfig = null
        }
        else {
            this.tcpipService.addUDPListener(mcastConfig, this)
            this.logger.info("Added multicast listener for named config: " + this._mcastConfig)
        }

        this._tcpReceiverConfig = DiscoveryConfig.managed.get().tcpReceiverConfigName.string
        if (this._tcpReceiverConfig != null && this._tcpReceiverConfig.empty) {
            this._tcpReceiverConfig = null
        }
        else {
            this.tcpipService.setTCPRequestListener(tcpReceiverConfig, this)
            this.logger.info("Set request listener for named config: " + this._tcpReceiverConfig)
        }

        this.logger.info("Setup according to new config!")
    }

    void cleanup() {
        if (this._mcastConfig != null) {
            this.tcpipService.removeUDPListener(this._mcastConfig, this)
            this.logger.info("Removed UDP listener for named config: " + this._mcastConfig)
        }
        if (this._tcpReceiverConfig != null) {
            this.tcpipService.removeTCPRequestListener(this._tcpReceiverConfig)
            this.logger.info("Removed TCP listener for named config: " + this._tcpReceiverConfig)
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

    synchronized String getMcastConfig() {
        return this._mcastConfig
    }

    synchronized String getTcpReceiverConfig() {
        return this._tcpReceiverConfig
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
        if (this.mcastConfig != null) {
            this.executorService.submit(new MCastSendTask(
                    mcastConfig: this.mcastConfig,
                    headerByte: headerByte,
                    serviceDescription: serviceDescription,
                    tcpipService: this.tcpipService,
                    logger: this.logger
            ))
        }

        if (!DiscoveryConfig.managed.get().tcpPublishToConfigNames.empty) {
            DiscoveryConfig.managed.get().tcpPublishToConfigNames.each { APSConfigValue tcpSvcConfig ->
                this.executorService.submit(new TCPSendTask(
                        tcpSendConfig: tcpSvcConfig.string,
                        headerByte: headerByte,
                        serviceDescription: serviceDescription,
                        tcpipService: this.tcpipService,
                        logger: this.logger
                ))
            }
        }
    }

    /**
     * Received multicast data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param dataGramPacket The received datagram.
     */
    @Override
    void udpDataReceived(String name, DatagramPacket dataGramPacket) {
        ServiceDescriptionProvider serviceDescription = new ServiceDescriptionProvider()
        byte headerByte = ReadWriteTools.fromBytes(serviceDescription, dataGramPacket.data)

        if (headerByte == HB_ADD) {
            serviceDescription.setLastUpdated(LocalDateTime.now())
            // Since this is a HashSet which only contains one copy of each entry, any previous entry have to be removed
            // for a new entry to be added.
            this.remoteServices.remove(serviceDescription)
            this.remoteServices.add(serviceDescription)
        }
        else {
            this.remoteServices.remove(serviceDescription)
        }
    }

    /**
     * Receives a TCP request stream.
     *
     * @param name The name of a configuration specifying address and port that the request comes from.
     * @param address The address of the request.
     * @param tcpReqStream The request stream.
     * @param tcpRespStream The response stream.
     *
     * @throws IOException
     */
    @Override
    void tcpRequestReceived(String name, InetAddress address, InputStream tcpReqStream, OutputStream tcpRespStream) throws IOException {
        ServiceDescriptionProvider serviceDescription = new ServiceDescriptionProvider()
        ObjectInputStream ooStream = new ObjectInputStream(tcpReqStream)
        byte headerByte = ReadWriteTools.readServiceDescription(serviceDescription, ooStream)
        if (headerByte == HB_ADD) {
            // Since this is a HashSet which only contains one copy of each entry, any previous entry have to be removed
            // for a new entry to be added.
            this.remoteServices.remove(serviceDescription)
            this.remoteServices.add(serviceDescription)
        }
        else {
            this.remoteServices.remove(serviceDescription)
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

