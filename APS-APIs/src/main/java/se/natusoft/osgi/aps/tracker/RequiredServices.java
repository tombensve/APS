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
 *         2011-10-22: Created!
 *
 */
package se.natusoft.osgi.aps.tracker;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a set of concurrently required serviceReferences.
 */
public class RequiredServices {
    //
    // Private Members
    //

    /** Holds the service references. */
    private Map<Class, ServiceReference> serviceReferences = new HashMap<Class, ServiceReference>();

    /** A temporary map of looked up services. */
    private Map<Class, Object> services = null;

    //
    // Constructors
    //

    /**
     * Creates a new RequiredServices instance.
     */
    public RequiredServices() {}

    //
    // Methods
    //

    /**
     * Puts a service into the required serviceReferences container.
     *
     * @param serviceAPI The API class of the service.
     * @param service The actual service.
     */
    public void putService(Class serviceAPI, ServiceReference service) {
        this.serviceReferences.put(serviceAPI, service);
    }

    /**
     * Removes a service from this container.
     *
     * @param serviceAPI The service to remove.
     */
    public void removeService(Class serviceAPI) {
        this.serviceReferences.remove(serviceAPI);
    }

    /**
     * Returns true if the container contains the specified service.
     *
     * @param serviceAPI The service to check for.
     */
    public boolean hasService(Class serviceAPI) {
        return this.serviceReferences.containsKey(serviceAPI);
    }

    /**
     * Gets a service reference by its API class.
     *
     * @param serviceAPI The API class of the service to get.
     */
    public ServiceReference getServiceReference(Class serviceAPI) {
        return this.serviceReferences.get(serviceAPI);
    }

    /**
     * Gets a service by its API class.
     *
     * @param serviceAPI The API class of the service to get.
     */
    public <Service> Service getService(Class<Service> serviceAPI) {
        return (Service)this.services.get(serviceAPI);
    }

    /**
     * Returns the number of serviceReferences currently in this container.
     */
    public int getServiceCount() {
        return this.serviceReferences.size();
    }

    /**
     * Looks up all services instances from the references. After this call getService(Class) will work.
     *
     * @param context The BundleContext to use to lookup the real service instance.
     */
    public void loadServices(BundleContext context) {
        this.services = new HashMap<Class, Object>();
        for (Class key : this.serviceReferences.keySet()) {
            ServiceReference serviceReference = this.serviceReferences.get(key);
            this.services.put(key, context.getService(serviceReference));
        }
    }

    /**
     * Releases all service instances looked up by `loadServices()`. After this `getService(Class)` will not work.
     *
     * @param context The BundleContext to use to release the service instance with.
     */
    public void releaseServices(BundleContext context) {
        for (Class key : this.serviceReferences.keySet()) {
            ServiceReference serviceReference = this.serviceReferences.get(key);
            context.ungetService(serviceReference);
        }
        this.services = null;
    }
}
