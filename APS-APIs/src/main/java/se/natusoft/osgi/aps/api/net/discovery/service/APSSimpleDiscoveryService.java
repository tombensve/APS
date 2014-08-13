/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2011-10-16: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.discovery.service;


import se.natusoft.osgi.aps.api.net.discovery.exception.APSDiscoveryPublishException;
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;

import java.util.List;

/**
 * A network service discovery.
 */
public interface APSSimpleDiscoveryService {

    /**
     * Returns all remotely discovered services.
     */
    public List<ServiceDescription> getRemotelyDiscoveredServices();

    /**
     * Returns the locally registered services.
     */
    public List<ServiceDescription> getLocallyRegisteredServices();

    /**
     * Returns all known services, both locally registered and remotely discovered.
     */
    public List<ServiceDescription> getAllServices();
    
    /**
     * Returns all discovered services with the specified id.
     *
     * @param serviceId The id of the service to get.
     * @param version The version of the service to get.
     */
    public List<ServiceDescription> getService(String serviceId, String version);

    /**
     * Publishes a local service. This will announce it to other known APSSimpleDiscoveryService instances.
     *
     * @param service The description of the servcie to publish.
     *
     * @throws APSDiscoveryPublishException on problems to publish (note: this is a runtime exception!).
     */
    public void publishService(ServiceDescription service) throws APSDiscoveryPublishException;

    /**
     * Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this
     * service is no longer available.
     *
     * @param service The service to unpublish.
     *
     * @throws APSDiscoveryPublishException on problems to publish (note: this is a runtime exception!).
     */
    public void unpublishService(ServiceDescription service) throws APSDiscoveryPublishException;
}
