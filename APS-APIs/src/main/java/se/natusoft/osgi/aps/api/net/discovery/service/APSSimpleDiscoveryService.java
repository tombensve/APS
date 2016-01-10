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


import se.natusoft.osgi.aps.api.net.discovery.exception.APSDiscoveryException;
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;

import java.util.Set;

/**
 * A network service discovery.
 *
 * There a many such services available in general, a bit less from a java perspective, but the intention
 * with this is not to compete with any of the others, but to provide an extremely simple way to discover
 * remote services in an as simple an primitive way as possible. Basically a way to have multiple hosts
 * running APS based code find each other easily, may it be by simple configuration or by multicast or
 * TCP, or wrapping some other service.
 */
public interface APSSimpleDiscoveryService {

    /**
     * Returns all discovered services, both locally registered and remotely discovered.
     */
    Set<ServiceDescription> getAllDiscoveredServices();

    /**
     * Returns all discovered services with the specified id.
     *
     * @param serviceId The id of the service to get.
     * @param version The version of the service to get.
     */
    Set<ServiceDescription> getDiscoveredService(String serviceId, String version);

    /**
     * Returns all discovered services, both locally registered and remotely discovered.
     */
    Set<ServiceDescription> getAllLocalServices();

    /**
     * Returns all discovered services with the specified id.
     *
     * @param serviceId The id of the service to get.
     * @param version The version of the service to get.
     */
    Set<ServiceDescription> getLocalService(String serviceId, String version);

    /**
     * Publishes a local service. This will announce it to other known APSSimpleDiscoveryService instances.
     *
     * @param service The description of the servcie to publish.
     *
     * @throws APSDiscoveryException on problems to publish (note: this is a runtime exception!).
     */
    void publishService(ServiceDescription service) throws APSDiscoveryException;

    /**
     * Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this
     * service is no longer available.
     *
     * @param service The service to unpublish.
     *
     * @throws APSDiscoveryException on problems to publish (note: this is a runtime exception!).
     */
    void unpublishService(ServiceDescription service) throws APSDiscoveryException;
}
