/*
 *
 * PROJECT
 *     Name
 *         APSOSGiTestTools
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides tools for testing OSGi services.
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
 *         2015-01-23: Created!
 *
 */
package se.natusoft.osgi.aps.test.tools.internal;

import org.osgi.framework.*;
import se.natusoft.osgi.aps.test.tools.TestServiceRegistration;

import java.util.*;

/**
 * Handles all registered services for a TestBundle.
 */
public class ServiceRegistry {

    /**
     * Holds both listener and its filter.
     */
    private static class ListenerEntry {
        public ServiceListener listener;
        public String filter;

        public ListenerEntry() {}
        public ListenerEntry(ServiceListener listener, String filter) {
            this.listener = listener;
            this.filter = filter;
        }
    }

    //
    // Private Members
    //

    /** Holds all listeners */
    private Map<String/*service API*/, List<ListenerEntry>> serviceListenerMap = new HashMap<>();

    /** Holds registered services. */
    private Map<TestServiceRegistration, Object> services = Collections.synchronizedMap(new HashMap<TestServiceRegistration, Object>());

    //
    // Methods
    //

    /**
     * Utility to send events to registered listeners.
     *
     * @param serviceRegistration The service registration to send event about.
     * @param event The event to send.
     * @param serviceAPI Used to find the listeners to send to.
     */
    private void sendListenerEvents(TestServiceRegistration serviceRegistration, int event, String serviceAPI) {
        try {
            List<ListenerEntry> listeners = this.serviceListenerMap.get(serviceAPI);
            if (listeners != null) {
                for (ListenerEntry listenerEntry : listeners) {
                    Filter svcFilter = FrameworkUtil.createFilter(listenerEntry.filter);
                    if (svcFilter.match(serviceRegistration.getReference())) {
                        listenerEntry.listener.serviceChanged(new ServiceEvent(event, serviceRegistration.getReference()));
                    }
                }
            }
            if (this.serviceListenerMap.containsKey("all")) {
                for (ListenerEntry listenerEntry : this.serviceListenerMap.get("all")) {
                    Filter svcFilter = FrameworkUtil.createFilter(listenerEntry.filter);
                    if (svcFilter.match(serviceRegistration.getReference())) {
                        listenerEntry.listener.serviceChanged(new ServiceEvent(event, serviceRegistration.getReference()));
                    }
                }
            }
        }
        catch (InvalidSyntaxException ise) {
            throw new IllegalArgumentException("Bad filter syntax!", ise);
        }
    }

    /**
     * Registers a service.
     *
     * @param serviceRegistration The internal TestServiceRegistration implementation of ServiceRegistration.
     * @param service The service instance.
     * @param serviceAPI The service API class.
     */
    public void registerService(TestServiceRegistration serviceRegistration, Object service, Class serviceAPI) {
        this.services.put(serviceRegistration, service);

        sendListenerEvents(serviceRegistration, ServiceEvent.REGISTERED, serviceAPI.getName());
    }

    /**
     * Unregister a service.
     *
     * @param serviceRegistration The internal TestServiceRegistration implementation of ServiceRegistration.
     */
    public void unregisterService(TestServiceRegistration serviceRegistration) {
        this.services.remove(serviceRegistration);

        sendListenerEvents(serviceRegistration, ServiceEvent.UNREGISTERING, serviceRegistration.getServiceName());
    }

    /**
     * Adds service listeners which are connected to their service API class.
     *
     * @param listener The listener to add.
     * @param filter An optional filter for the service.
     *
     * @throws InvalidSyntaxException
     */
    public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
        int ix = filter.indexOf(Constants.OBJECTCLASS);
        String filter2 = filter.substring(ix + Constants.OBJECTCLASS.length() + 1);
        String[] filterParts = filter2.split("[ )]");
        String serviceClass = filterParts[0];
        List<ListenerEntry> listenerEntries = this.serviceListenerMap.get(serviceClass);
        if (listenerEntries == null) {
            listenerEntries = new LinkedList<>();
            this.serviceListenerMap.put(serviceClass, listenerEntries);
        }
        ListenerEntry entry = new ListenerEntry(listener, filter);
        listenerEntries.add(entry);
    }

    /**
     * Adds service listeners without filters to the "all" key rather than service API class.
     *
     * @param listener The listener to add.
     */
    public void addServiceListener(ServiceListener listener) {
        List<ListenerEntry> listenerEntries = this.serviceListenerMap.get("all");
        if (listenerEntries == null) {
            listenerEntries = new LinkedList<>();
            this.serviceListenerMap.put("all", listenerEntries);
        }
        listenerEntries.add(new ListenerEntry(listener, null));
    }

    /**
     * Removes a service listener independent of how it is keyed.
     *
     * @param listener The listener to remove.
     */
    public void removeServiceListener(ServiceListener listener) {
        for (Map.Entry<String, List<ListenerEntry>> entry : this.serviceListenerMap.entrySet()) {
            for (ListenerEntry listenerEntry : entry.getValue()) {
                if (listener == listenerEntry.listener) {
                    this.serviceListenerMap.remove(entry.getKey());
                    break;
                }
            }
        }
    }

    /**
     * Alias for getAllServiceReferences(...).
     *
     * @param clazz The service API class to get ServiceReferences for.
     * @param filter The additional filter for the ServiceReferences to get.
     */
    public ServiceReference[] getServiceReferences(String clazz, String filter)  {
        return getAllServiceReferences(clazz, filter);
    }

    /**
     * Returns all ServiceReferences based on service API class and additional filter.
     *
     * @param clazz The service API class to get ServiceReferences for.
     * @param filter The additional filter for the ServiceReferences to get.
     */
    public ServiceReference[] getAllServiceReferences(String clazz, String filter) {
        try {
            Filter svcFilter = FrameworkUtil.createFilter(filter);
            List<ServiceReference> refs = new ArrayList<>();
            for (Map.Entry<TestServiceRegistration, Object> entry : this.services.entrySet()) {
                if (svcFilter.match(entry.getKey().getReference())) {
                    refs.add(entry.getKey().getReference());
                }
            }
            ServiceReference[] refsArray = new ServiceReference[refs.size()];
            return refs.toArray(refsArray);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException("Failed to parse search filter!", e);
        }
    }

    /**
     * Returns a specific ServiceReference bases on service API class or null if not found.
     *
     * @param clazz The service API class to get ServiceReference for.
     */
    public ServiceReference getServiceReference(String clazz) {
        ServiceReference[] refs = getAllServiceReferences(clazz, "");
        if (refs != null && refs.length > 0) {
            return refs[0];
        }
        return null;
    }

    /**
     * Returns service object using its reference.
     *
     * @param reference The reference to the service to get.
     */
    public Object getService(ServiceReference reference) {
        for (Map.Entry<TestServiceRegistration, Object> entry : this.services.entrySet()) {
            if (entry.getKey().getReference() == reference) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Returns the ServiceReference objects of all registered services.
     */
    public ServiceReference[] getRegisteredServices() {
        List<ServiceReference> refs = new ArrayList<>();
        for (Map.Entry<TestServiceRegistration, Object> entry : this.services.entrySet()) {
            refs.add(entry.getKey().getReference());
        }
        ServiceReference[] refsArray = new ServiceReference[refs.size()];
        return refs.toArray(refsArray);
    }

    /**
     * Currently returns the same as getRegisteredServices!
     */
    public ServiceReference[] getServicesInUse() {
        return getRegisteredServices();
    }

}
