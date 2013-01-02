/* 
 * 
 * PROJECT
 *     Name
 *         APS Discovery Service Provider
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         This is a simple discovery service to discover other services on the network.
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
package se.natusoft.osgi.aps.discovery.model;

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;

import java.util.*;

/**
 * Holds a set of ServiceDescriptionProvider object.
 */
public class ServiceDescriptions {
    //
    // Private Members
    //

    /** How old a service description can get before being evicted. */
    public static final long EVICT_AGE = 1000 * 60 * 10;

    /** Holds all unique entries. */
    private Map<ServiceDescription, Long> allUnique = new HashMap<ServiceDescription, Long>();

    /** Holds entries by their service id. */
    private Map<String, List<ServiceDescription>> byID = new HashMap<String, List<ServiceDescription>>();

    //
    // Constructors
    //

    /**
     * Creates a new ServiceDescriptions instance.
     */
    public ServiceDescriptions() {}

    //
    // Methods
    //

    /**
     * Returns true if the specified service description is held by this object.
     *
     * @param serviceDescription The service description to check for.
     */
    public synchronized boolean hasServiceDescription(ServiceDescription serviceDescription) {
        return this.allUnique.containsKey(serviceDescription);
    }

    /**
     * Returns true if there are any services with the specified service id and version.
     *
     * @param serviceId The id of the service.
     * @param version The version of the service.
     */
    public synchronized boolean hasService(String serviceId, String version) {
        return this.byID.containsKey(serviceId + version);
    }

    /**
     * Adds a new service description if it is not already added.
     *
     * @param serviceDescription The service description to add.
     */
    public synchronized void addServiceDescription(ServiceDescription serviceDescription) {
        if (!this.allUnique.containsKey(serviceDescription)) {
            List<ServiceDescription> descriptions = this.byID.get(serviceDescription.getServiceId() + serviceDescription.getVersion());
            if (descriptions == null) {
                descriptions = new ArrayList<ServiceDescription>();
                this.byID.put(serviceDescription.getServiceId() + serviceDescription.getVersion(), descriptions);
            }
            descriptions.add(serviceDescription);
        }
        this.allUnique.put(serviceDescription, new Date().getTime());
    }

    /**
     * Removes a service description.
     *
     * @param serviceDescription The service description to remove.
     */
    public synchronized void removeServiceDescription(ServiceDescription serviceDescription) {
        if (this.allUnique.containsKey(serviceDescription)) {
            this.allUnique.remove(serviceDescription);
            List<ServiceDescription> descriptions = this.byID.get(serviceDescription.getServiceId() + serviceDescription.getVersion());
            descriptions.remove(serviceDescription);
        }
    }

    /**
     * Returns all held service descriptions.
     */
    public synchronized Set<ServiceDescription> getAllServiceDescriptions() {
        return this.allUnique.keySet();
    }


    /**
     * Returns all service descriptions with the specified service id.
     *
     * @param serviceId The service id to get service descriptions for.
     * @param version The version to get service descriptions for.
     */
    public synchronized List<ServiceDescription> getServiceDescriptions(String serviceId, String version) {
        return this.byID.get(serviceId + version);
    }

    /**
     * Evicts to old entries.
     */
    public synchronized void evictOld() {
        long now = new Date().getTime();
        List<ServiceDescription> toEvict = new LinkedList<ServiceDescription>();
        for (ServiceDescription sd : this.allUnique.keySet()) {
            long sdTime = this.allUnique.get(sd);
            if (now > (sdTime + EVICT_AGE)) {
                toEvict.add(sd);
            }
        }
        for (ServiceDescription sd : toEvict) {
            removeServiceDescription(sd);
        }
    }
}
