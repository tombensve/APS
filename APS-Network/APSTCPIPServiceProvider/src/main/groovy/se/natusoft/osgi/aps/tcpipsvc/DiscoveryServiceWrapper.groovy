package se.natusoft.osgi.aps.tcpipsvc

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.Managed

/**
 * This wraps APSSimpleDiscoveryService and handles that it may not be available.
 */
class DiscoveryServiceWrapper {

    @Managed
    private APSServiceTracker<APSSimpleDiscoveryService> discoverySvcTracker

    public boolean isDiscoveryAvailable() {
        this.discoverySvcTracker.hasTrackedService()
    }

    /**
     * Tries to lookup a service with the local APSSimpleDiscoveryService if available.
     *
     * @param id The service id to look for.
     *
     * @return a ServiceDescription or null if none found or service not available.
     */
    public ServiceDescription getServiceWithId(String id) {
        if (!isDiscoveryAvailable()) return null

        id = id.trim()

        APSSimpleDiscoveryService discoveryService = this.discoverySvcTracker.allocateService()
        ServiceDescription sd = discoveryService.allDiscoveredServices.find() { ServiceDescription svcd ->
            svcd.serviceId.trim() == id
        }
        if (sd == null) {
            sd = discoveryService.allLocalServices.find() { ServiceDescription svcd ->
                svcd.serviceId.trim() == id
            }
        }
        this.discoverySvcTracker.releaseService()

        sd
    }
}
