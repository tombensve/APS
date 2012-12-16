/* 
 * 
 * PROJECT
 *     Name
 *         APS Discovery Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This is a simple discovery service to discover other services on the network.
 *         It supports both multicast and UDP connections.
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
package se.natusoft.osgi.aps.discovery.service.provider;

import se.natusoft.osgi.aps.api.core.platform.model.PlatformDescription;
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService;
import se.natusoft.osgi.aps.discovery.model.ServiceDescriptions;
import se.natusoft.osgi.aps.discovery.service.event.DiscoveryEventListener;
import se.natusoft.osgi.aps.discovery.service.event.DiscoveryEventProvidingBase;
import se.natusoft.osgi.aps.discovery.service.net.ObjectContainer;

import java.util.LinkedList;
import java.util.List;

/**
 * The discovery service Implementation.
 */
public class APSSimpleDiscoveryServiceProvider extends DiscoveryEventProvidingBase implements APSSimpleDiscoveryService, DiscoveryEventListener {
    //
    // Private Members
    //

    /** The services published locally by clients of this service. */
    private ServiceDescriptions locallyPublishedServices = null;

    /** The services published on other APSSimpleDiscoveryService instances and announced to us. */
    private ServiceDescriptions remotelyPublishedServices = null;

    /** The description of the platform. */
    private ObjectContainer<PlatformDescription> platformDescription = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSSimpleDiscoveryServiceProvider instance.
     *
     * @param platformDescription A description of the platform.
     */
    public APSSimpleDiscoveryServiceProvider(ObjectContainer<PlatformDescription> platformDescription) {
        this.platformDescription = platformDescription;
    }

    //
    // Methods
    //

    /**
     * Sets the container of services published locally by clients of this service.
     *
     * @param locallyPublishedServices The locally published services container.
     */
    public void setLocallyPublishedServices(ServiceDescriptions locallyPublishedServices) {
        this.locallyPublishedServices = locallyPublishedServices;
    }

    /**
     * Sets the container or services published on other APSSimpleDiscoveryService instances and announced to us.
     *
     * @param remotelyPublishedServices The remotely published services container.
     */
    public void setRemotelyPublishedServices(ServiceDescriptions remotelyPublishedServices) {
        this.remotelyPublishedServices = remotelyPublishedServices;
    }

    /**
     * Adds an event listener for local discovery events.
     *
     * @param discoveryEventListener The listener to add.
     */
    public void addLocalDiscoveryEventListener(DiscoveryEventListener discoveryEventListener) {
        super.addDiscoveryEventListener(discoveryEventListener);
    }

    /**
     * Removes a previously added event listener.
     *
     * @param discoveryEventListener The event listener to remove.
     */
    public void removeLocalDiscoveryEventListener(DiscoveryEventListener discoveryEventListener) {
        super.removeDiscoveryEventListener(discoveryEventListener);
    }

    /**
     * Unpublishes all local services.
     */
    public void unpublishAllLocal() {
        for (ServiceDescription serviceDescription : this.locallyPublishedServices.getAllServiceDescriptions()) {
            super.fireLeavingEvent(serviceDescription);
        }
    }

    //
    // DiscoveryEventListener Implementation Methods
    //

    /**
     * Announces that a new service is available.
     *
     * @param serviceDescription The description of the new service.
     */
    @Override
    public synchronized void serviceAvailable(ServiceDescription serviceDescription) {
        // We will se our own remotely published services due to multicast.
        if (!this.locallyPublishedServices.hasServiceDescription(serviceDescription)) {
            this.remotelyPublishedServices.addServiceDescription(serviceDescription);
        }
    }

    /**
     * Announces that an old service is leaving.
     *
     * @param serviceDescription The description of the leaving service.
     */
    @Override
    public synchronized void serviceLeaving(ServiceDescription serviceDescription) {
        // We will se our own remotely published services due to multicast.
        if (!this.locallyPublishedServices.hasServiceDescription(serviceDescription)) {
            this.remotelyPublishedServices.removeServiceDescription(serviceDescription);
        }
    }

    //
    // APSSimpleDiscoveryService Implementation Methods
    //

    /**
     * Returns all remotely discovered services.
     */
    @Override
    public List<ServiceDescription> getRemotelyDiscoveredServices() {
        return this.remotelyPublishedServices.getAllServiceDescriptions();
    }

    /**
     * Returns the locally registered services.
     */
    public List<ServiceDescription> getLocallyRegisteredServices() {
        return this.locallyPublishedServices.getAllServiceDescriptions();
    }

    /**
     * Returns all known services, both locally registered and remotely discovered.
     */
    public List<ServiceDescription> getAllServices() {
        List<ServiceDescription> allServices = new LinkedList<ServiceDescription>();
        allServices.addAll(getRemotelyDiscoveredServices());
        allServices.addAll(getLocallyRegisteredServices());
        return allServices;
    }

    /**
     * Returns all discovered services with the specified id.
     *
     * @param serviceId The id of the service to get.
     * @param version The version of the service to get.
     */
    @Override
    public List<ServiceDescription> getService(String serviceId, String version) {
        return this.remotelyPublishedServices.getServiceDescriptions(serviceId, version);
    }

    /**
     * Publishes a local service. This will announce it to other known APSSimpleDiscoveryService instances.
     *
     * @param serviceDescription The description of the servcie to publish.
     */
    @Override
    public void publishService(ServiceDescription serviceDescription) {
        serviceDescription.internalSetPlatformDescription(this.platformDescription.get());
        this.locallyPublishedServices.addServiceDescription(serviceDescription);
        super.fireAvailableEvent(serviceDescription);
    }

    /**
     * Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this
     * service is no longer available.
     *
     * @param serviceDescription The service to unpublish.
     */
    @Override
    public void unpublishService(ServiceDescription serviceDescription) {
        serviceDescription.internalSetPlatformDescription(this.platformDescription.get());
        this.locallyPublishedServices.removeServiceDescription(serviceDescription);
        super.fireLeavingEvent(serviceDescription);
    }

}
