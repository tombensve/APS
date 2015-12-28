package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.Implements
import se.natusoft.osgi.aps.api.net.discovery.exception.APSDiscoveryPublishException
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescriptionProvider
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService
import se.natusoft.osgi.aps.discoveryservice.config.DiscoveryConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider
import se.natusoft.osgi.aps.tools.util.LoggingRunnable

import javax.xml.ws.Service
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

import static ReadWriteTools.HB_ADD
import static ReadWriteTools.HB_REMOVE

/**
 * Provides an implementation of APSSimpleDiscoveryService that uses Multicast, TCP, and manual config entries
 * to receive and announce services. What is in the end supported depends on how it is configured.
 */
@OSGiServiceProvider(properties = [
        @OSGiProperty(name = "service-provider", value = "aps-default-discovery-service-provider")
])
@CompileStatic
@TypeChecked
@SuppressWarnings("GroovyUnusedDeclaration")
class APSSimpleDiscoveryServiceProvider implements APSSimpleDiscoveryService {

    //
    // Members
    //

    @Managed
    private DiscoveryHandler discoverer

    @Managed(name = "default-logger", loggingFor = "aps-default-discovery-service-provider")
    private APSLogger logger

    @Managed(name = "executor-service")
    private DiscoveryExecutorService executorService

    /** The known local services. */
    private Set<ServiceDescription> localServices = Collections.synchronizedSet(new HashSet<ServiceDescription>())

    private ScheduledThreadPoolExecutor refreshThreadPool = null

    private Runnable reAnnounceTask = new Runnable() {
        @Override
        void run() {
            try {
                APSSimpleDiscoveryServiceProvider.this.reAnnounce()
            }
            catch (Exception e) {
                APSSimpleDiscoveryServiceProvider.this.logger.error("'reAnnounceTask' failed: " + e.getMessage(), e)
            }
        }
    }

    private Runnable clearExpiredTask = new Runnable() {
        @Override
        void run() {
            try {
                APSSimpleDiscoveryServiceProvider.this.discoverer.cleanExpired()
            }
            catch (Exception e) {
                APSSimpleDiscoveryServiceProvider.this.logger.error("'clearExpiredTask' failed: " + e.getMessage(), e)
            }
        }
    }

    //
    // Methods
    //

    /**
     * Initializes the service. This is called after all injections have been done!
     */
    @Initializer
    void init() {
        this.refreshThreadPool = new ScheduledThreadPoolExecutor(2)

        this.refreshThreadPool.scheduleAtFixedRate(this.reAnnounceTask, 180, 180, TimeUnit.SECONDS)

        this.refreshThreadPool.scheduleAtFixedRate(this.clearExpiredTask, 120, 120, TimeUnit.SECONDS)

        this.executorService.submit(new LoggingRunnable(this.logger) {
            @Override
            void doRun() {
                APSSimpleDiscoveryServiceProvider.this.addConfiguredServices()
            }
        })
    }

    /**
     * This gets called on bundle stop by APSActivator.
     */
    @BundleStop
    void shutdown() {
        if (this.refreshThreadPool != null) {
            this.refreshThreadPool.shutdownNow()
        }
        this.discoverer.shutdown()
    }

    /**
     * Adds services provided through configuration.
     */
    void addConfiguredServices() {
        this.logger.info("Adding configured services ...")
        for (DiscoveryConfig.ManualServiceEntry manualServiceEntry : DiscoveryConfig.managed.get().manualServiceEntries) {
            ServiceDescription sd = new ServiceDescriptionProvider();
            sd.description = manualServiceEntry.description.string
            sd.serviceHost = manualServiceEntry.host.string
            sd.serviceId = manualServiceEntry.serviceId.string
            sd.servicePort = manualServiceEntry.port.int
            sd.serviceProtocol = ServiceDescription.Protocol.valueOf(manualServiceEntry.protocol.string)
            sd.serviceURL = manualServiceEntry.url.string
            sd.version = manualServiceEntry.version.string
            publishService(sd)
            this.logger.info("  Added service: " + sd.toString())
        }
        this.logger.info("Done adding configured services!")
    }

    /**
     * Reannounces both published local services and last hours unpublished local services.
     */
    private synchronized void reAnnounce() {
        this.localServices.each { ServiceDescription sd ->
            publishService(sd)
        }
    }

    /**
     * Returns all discovered services, both locally registered and remotely discovered.
     */
    @Override
    @Implements(APSSimpleDiscoveryService.class)
    Set<ServiceDescription> getAllDiscoveredServices() {
        return this.discoverer.remoteServices
    }

    /**
     * Returns all discovered services with the specified service id and version.
     *
     * @param serviceId The id of the service to get.
     * @param version The version of the service to get.
     */
    @Override
    @Implements(APSSimpleDiscoveryService.class)
    Set<ServiceDescription> getDiscoveredService(String serviceId, String version) {
        return this.discoverer.remoteServices.findAll { ServiceDescription serviceDescription ->
            serviceDescription.version == version && serviceDescription.serviceId == serviceId
        }
    }

    /**
     * Returns all discovered services, both locally registered and remotely discovered.
     */
    @Override
    @Implements(APSSimpleDiscoveryService.class)
    Set<ServiceDescription> getAllLocalServices() {
        return this.localServices
    }

    /**
     * Returns all discovered services with the specified id.
     *
     * @param serviceId The id of the service to get.
     * @param version The version of the service to get.
     */
    @Override
    @Implements(APSSimpleDiscoveryService.class)
    Set<ServiceDescription> getLocalService(String serviceId, String version) {
        return this.localServices.findAll { ServiceDescription serviceDescription ->
            serviceDescription.version == version && serviceDescription.serviceId == serviceId
        }
    }

    /**
     * Publishes a local service. This will announce it to other known APSSimpleDiscoveryService instances.
     *
     * @param service The description of the servcie to publish.
     *
     * @throws APSDiscoveryPublishException on problems to publish (note: this is a runtime exception!).
     */
    @Override
    @Implements(APSSimpleDiscoveryService.class)
    void publishService(ServiceDescription service) throws APSDiscoveryPublishException {
        this.localServices.add(service)
        this.discoverer.sendUpdate(service, HB_ADD)
    }

    /**
     * Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this
     * service is no longer available.
     *
     * @param service The service to unpublish.
     *
     * @throws APSDiscoveryPublishException on problems to publish (note: this is a runtime exception!).
     */
    @Override
    @Implements(APSSimpleDiscoveryService.class)
    void unpublishService(ServiceDescription service) throws APSDiscoveryPublishException {
        this.localServices.remove(service)
        this.discoverer.sendUpdate(service, HB_REMOVE)
    }
}
