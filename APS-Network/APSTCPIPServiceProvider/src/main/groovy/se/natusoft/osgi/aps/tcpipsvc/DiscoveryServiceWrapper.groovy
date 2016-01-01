/* 
 * 
 * PROJECT
 *     Name
 *         APS TCPIP Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides an implementation of APSTCPIPService. This service does not provide any security of its own,
 *         but makes use of APSTCPSecurityService, and APSUDPSecurityService when available and configured for
 *         security.
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
 *         2016-01-01: Created!
 *         
 */
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
    @SuppressWarnings("SpellCheckingInspection")
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
