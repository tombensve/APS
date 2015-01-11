package se.natusoft.osgi.aps.test.tools;

import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.UUID;

/**
 * Provides a ServiceRegistration implementation for testing.
 */
public class TestServiceRegistration implements ServiceRegistration {
    //
    // Private Members
    //

    private UUID id = UUID.randomUUID();

    private String serviceName;
    private ServiceReference serviceReference;
    private TestBundle bundle;

    //
    // Constructors
    //

    /**
     * Creates a new TestServiceRegistration instance.
     *
     * @param serviceName The name of the registered service.
     * @param serviceReference The reference of the registered service.
     */
    public TestServiceRegistration(String serviceName, ServiceReference serviceReference, TestBundle bundle) {
        this.serviceName = serviceName;
        this.serviceReference = serviceReference;
        this.bundle = bundle;
    }

    //
    // Methods
    //

    /**
     * Returns the name of the registered service.
     */
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public ServiceReference getReference() {
        return this.serviceReference;
    }

    /**
     * Does nothing!
     */
    @Override
    public void setProperties(Dictionary properties) {
    }

    /**
     * Unregisters this service registration.
     */
    @Override
    public void unregister() {
        this.bundle.getServiceRegistry().unregisterService(this);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean equals(TestServiceRegistration testServiceRegistration) {
        return this.id.equals(testServiceRegistration.id);
    }
}
