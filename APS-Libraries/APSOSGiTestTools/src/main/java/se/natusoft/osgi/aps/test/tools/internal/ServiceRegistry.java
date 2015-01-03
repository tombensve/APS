package se.natusoft.osgi.aps.test.tools.internal;

import org.osgi.framework.*;
import se.natusoft.osgi.aps.test.tools.TestServiceRegistration;

import java.util.*;

/**
 * Handles all registered services for a TestBundle.
 */
public class ServiceRegistry {
    //
    // Private Members
    //

    private Map<String, List<ServiceListener>> serviceListenerMap = new HashMap<>();

    private Map<TestServiceRegistration, Object> services = new HashMap<>();

    //
    // Methods
    //

    public void registerService(TestServiceRegistration serviceRegistration, Object service) {
        this.services.put(serviceRegistration, service);

        List<ServiceListener> listeners = this.serviceListenerMap.get(service.getClass().getName());
        if (listeners != null) {
            for (ServiceListener listener : listeners) {
                listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, serviceRegistration.getReference()));
            }
        }
        if (this.serviceListenerMap.containsKey("all")) {
            for (ServiceListener listener : this.serviceListenerMap.get("all")) {
                listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, serviceRegistration.getReference()));
            }
        }
    }

    public void unregisterService(TestServiceRegistration serviceRegistration) {
        this.services.remove(serviceRegistration);
        List<ServiceListener> listeners = this.serviceListenerMap.get(serviceRegistration.getServiceName());
        if (listeners != null) {
            for (ServiceListener listener : listeners) {
                listener.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, serviceRegistration.getReference()));
            }
        }
        if (this.serviceListenerMap.containsKey("all")) {
            for (ServiceListener listener : this.serviceListenerMap.get("all")) {
                listener.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, serviceRegistration.getReference()));
            }
        }
    }

    public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
        int ix = filter.indexOf(Constants.OBJECTCLASS);
        filter = filter.substring(ix + Constants.OBJECTCLASS.length() + 1);
        String[] filterParts = filter.split("[ )]");
        String serviceClass = filterParts[0];
        List<ServiceListener> listeners = this.serviceListenerMap.get(serviceClass);
        if (listeners == null) {
            listeners = new LinkedList<>();
            this.serviceListenerMap.put(serviceClass, listeners);
        }
        listeners.add(listener);
    }

    public void addServiceListener(ServiceListener listener) {
        List<ServiceListener> listeners = this.serviceListenerMap.get("all");
        if (listeners == null) {
            listeners = new LinkedList<>();
            this.serviceListenerMap.put("all", listeners);
        }
        listeners.add(listener);
    }

    public void removeServiceListener(ServiceListener listener) {
        for (Map.Entry<String, List<ServiceListener>> entry : this.serviceListenerMap.entrySet()) {
            for (ServiceListener listenerEntry : entry.getValue()) {
                if (listener == listenerEntry) {
                    this.serviceListenerMap.remove(entry.getKey());
                    break;
                }
            }
        }
    }

    public ServiceReference[] getServiceReferences(String clazz, String filter)  {
        return getAllServiceReferences(clazz, filter);
    }

    public ServiceReference[] getAllServiceReferences(String clazz, String filter) {
        List<ServiceReference> refs = new ArrayList<>();
        for (Map.Entry<TestServiceRegistration, Object> entry : this.services.entrySet()) {
            if (entry.getValue().getClass().getName().equals(clazz)) {
                refs.add(entry.getKey().getReference());
            }
        }
        ServiceReference[] refsArray = new ServiceReference[refs.size()];
        return refs.toArray(refsArray);
    }

    public ServiceReference getServiceReference(String clazz) {
        ServiceReference[] refs = getAllServiceReferences(clazz, "");
        if (refs != null && refs.length > 0) {
            return refs[0];
        }
        return null;
    }

    public Object getService(ServiceReference reference) {
        for (Map.Entry<TestServiceRegistration, Object> entry : this.services.entrySet()) {
            if (entry.getKey().getReference() == reference) {
                return entry.getValue();
            }
        }

        return null;
    }

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
     * @return
     */
    public ServiceReference[] getServicesInUse() {
        return getRegisteredServices();
    }

}
