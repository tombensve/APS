package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.Filter
import org.osgi.framework.FrameworkUtil
import se.natusoft.docutations.Implements
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.net.discovery.exception.APSDiscoveryException
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService
import se.natusoft.osgi.aps.discoveryservice.config.DiscoveryConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

import java.util.concurrent.TimeUnit

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

    @Schedule(delay = 3L, repeat = 3L, timeUnit = TimeUnit.MINUTES)
    private Runnable reAnnounceTask = {
        try {
            this.reAnnounce()
        }
        catch (Exception e) {
            this.logger.error("'reAnnounceTask' failed: " + e.getMessage(), e)
        }
    }

    @Schedule(delay = 2L, repeat = 2L, timeUnit = TimeUnit.MINUTES)
    private Runnable clearExpiredTask = {
        try {
            this.discoverer.cleanExpired()
        }
        catch (Exception e) {
            this.logger.error("'clearExpiredTask' failed: " + e.getMessage(), e)
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
        // This needs to be done in parallel not to cause deadlock!
        Thread.start {
            try {
                this.addConfiguredServices()
            }
            catch (Exception e) {
                this.logger.error(e.getMessage(), e)
            }
        }
    }

    /**
     * This gets called on bundle stop by APSActivator.
     */
    @BundleStop
    void shutdown() {
        this.discoverer.shutdown()
    }

    /**
     * Adds services provided through configuration.
     */
    private void addConfiguredServices() {
        this.logger.info("Adding configured services ...")
        DiscoveryConfig.managed.get().manualServiceEntries.each { DiscoveryConfig.ManualServiceEntry manualServiceEntry ->
            Properties props = new Properties()
            manualServiceEntry.propertyList.each { APSConfigValue configValue ->
                String[] nameValue = configValue.string.split("=")
                props.setProperty(nameValue[0].trim(), nameValue[1].trim())
            }
            publishService(props)
        }
        this.logger.info("Done adding configured services!")
    }

    /**
     * Reannounces local services.
     */
    private synchronized void reAnnounce() {
        this.discoverer.localServices.each { Properties sd ->
            publishService(sd)
        }
    }

    /**
     * Finds services by doing an LDAP-format query on the service description properties.
     * This is exactly the same query syntax as used by OSGi to find services based on published properties.
     *
     * @param propertyQuery The LDAP-format query of the ServiceDescription bean properties.
     */
    @Override
    Set<Properties> getServices(String propertyQuery) {
        Filter sdPropsFilter = FrameworkUtil.createFilter(propertyQuery)
        Set<Properties> remoteSvcs = this.discoverer.remoteServices.findAll { Properties serviceDescription ->
            sdPropsFilter.match(serviceDescription)
        }
        Set<Properties> localSvcs = this.discoverer.localServices.findAll { Properties serviceDescription ->
            sdPropsFilter.match(serviceDescription)
        }
        remoteSvcs.addAll(localSvcs)

        return remoteSvcs
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
    void publishService(Properties serviceDescription) throws APSDiscoveryException {
        if (this.discoverer.localServices.add(serviceDescription)) {
            this.discoverer.sendUpdate(DiscoveryAction.ADD, serviceDescription)
        }
    }

    /**
     * Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this
     * service is no longer available.
     *
     * @param serviceProps The same service properties used to publish service.
     *
     * @throws APSDiscoveryException on problems to publish (note: this is a runtime exception!).
     */
    void unpublishService(Properties serviceDescription) throws APSDiscoveryException {
        this.discoverer.localServices.remove(serviceDescription)
        this.discoverer.sendUpdate(DiscoveryAction.REMOVE, serviceDescription)
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
    void unpublishService(String unpublishFilter) throws APSDiscoveryException {

        Filter sdPropsFilter = FrameworkUtil.createFilter(unpublishFilter)
        Set<Properties> localSvcs = this.discoverer.localServices.findAll { Properties serviceDescription ->
            sdPropsFilter.match(serviceDescription)
        }

        localSvcs.each { Properties serviceDescription ->
            unpublishService(serviceDescription)
        }
    }

}
