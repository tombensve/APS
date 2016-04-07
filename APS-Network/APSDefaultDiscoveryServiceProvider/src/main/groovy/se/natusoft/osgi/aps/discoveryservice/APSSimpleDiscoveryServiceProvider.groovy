package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.Filter
import org.osgi.framework.FrameworkUtil
import se.natusoft.docutations.Implements
import se.natusoft.osgi.aps.api.net.discovery.exception.APSDiscoveryException
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescriptionProvider
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService
import se.natusoft.osgi.aps.discoveryservice.config.DiscoveryConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*
import se.natusoft.osgi.aps.tools.util.DictionaryView
import se.natusoft.osgi.aps.tools.util.LoggingRunnable

import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
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
    @ExecutorSvc(parallelism = 10, type = ExecutorSvc.ExecutorType.FixedSize)
    private ExecutorService executorService

    /** The known local services. */
    private Set<Properties> localServices = Collections.synchronizedSet(new HashSet<Properties>())

    @Managed(name = "refresh-thread-pool")
    @ExecutorSvc(parallelism = 2, type = ExecutorSvc.ExecutorType.Scheduled)
    private ScheduledExecutorService refreshThreadPool = null

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
        this.refreshThreadPool.scheduleAtFixedRate(this.reAnnounceTask, 180, 180, TimeUnit.SECONDS)

        this.refreshThreadPool.scheduleAtFixedRate(this.clearExpiredTask, 120, 120, TimeUnit.SECONDS)

        // IDEA bug!
        //noinspection GroovyAccessibility
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
    private void addConfiguredServices() {
        this.logger.info("Adding configured services ...")
        for (DiscoveryConfig.ManualServiceEntry manualServiceEntry : DiscoveryConfig.managed.get().manualServiceEntries) {
            ServiceDescription sd = new ServiceDescriptionProvider();
            sd.description = manualServiceEntry.description.string
            sd.serviceHost = manualServiceEntry.host.string
            sd.serviceId = manualServiceEntry.serviceId.string
            sd.servicePort = manualServiceEntry.port.int
            sd.networkProtocol = manualServiceEntry.networkProtocol.string
            sd.serviceProtocol = manualServiceEntry.serviceProtocol.string
            sd.serviceURL = manualServiceEntry.url.string
            sd.classifier = manualServiceEntry.classifier.string
            sd.contentType = manualServiceEntry.contentType.string
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
     * Finds services by doing an LDAP-format query on the java bean fields of the ServiceDescription bean.
     * This is exactly the same query syntax as used by OSGi to find services based on published properties.
     *
     * @param ldapServiceDescriptionJavaBeanPropertyQuery The LDAP-format query of the ServiceDescription bean properties.
     */
    @Override
    Set<ServiceDescription> getDiscoveredServices(String ldapServiceDescriptionJavaBeanPropertyQuery) {
        Filter sdPropsFilter = FrameworkUtil.createFilter(ldapServiceDescriptionJavaBeanPropertyQuery);
        return this.discoverer.remoteServices.findAll { ServiceDescription serviceDescription ->
            DictionaryView dictView = new DictionaryView(ServiceDescription.class, serviceDescription, this.logger)
            sdPropsFilter.match(dictView)
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
     * Finds services by doing an LDAP-format query on the java bean fields of the ServiceDescription bean.
     * This is exactly the same query syntax as used by OSGi to find services based on published properties.
     *
     * @param ldapServiceDescriptionJavaBeanPropertyQuery The LDAP-format query of the ServiceDescription bean properties.
     */
    @Override
    Set<ServiceDescription> getLocalService(String ldapServiceDescriptionJavaBeanPropertyQuery) {
        Filter sdPropsFilter = FrameworkUtil.createFilter(ldapServiceDescriptionJavaBeanPropertyQuery);
        return this.localServices.findAll { ServiceDescription serviceDescription ->
            DictionaryView dictView = new DictionaryView(ServiceDescription.class, serviceDescription, this.logger)
            sdPropsFilter.match(dictView)
        }
    }

    /**
     * Publishes a local service. This will announce it to other known APSSimpleDiscoveryService instances.
     *
     * @param service The description of the service to publish.
     *
     * @throws APSDiscoveryException on problems to publish (note: this is a runtime exception!).
     */
    @Override
    @Implements(APSSimpleDiscoveryService.class)
    void publishService(Properties serviceProps) throws APSDiscoveryException {
        this.localServices.add(serviceProps)
        this.discoverer.sendUpdate(service, HB_ADD)
    }

    /**
     * Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this
     * service is no longer available.
     *
     * @param service The service to unpublish.
     *
     * @throws APSDiscoveryException on problems to publish (note: this is a runtime exception!).
     */
    @Override
    @Implements(APSSimpleDiscoveryService.class)
    void unpublishService(ServiceDescription service) throws APSDiscoveryException {
        this.localServices.remove(service)
        this.discoverer.sendUpdate(service, HB_REMOVE)
    }
}
