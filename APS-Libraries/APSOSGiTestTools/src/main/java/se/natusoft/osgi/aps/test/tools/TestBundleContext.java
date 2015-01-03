package se.natusoft.osgi.aps.test.tools;

import org.osgi.framework.*;

import java.io.File;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Properties;

/**
 * This is a BundleContext implementation intended for unit testing OSGi services.
 */
public class TestBundleContext implements BundleContext {

    //
    // Private Members
    //

    private TestBundle bundle;

    private Properties props = new Properties();

    //
    // Constructors
    //

    /**
     * Creates a new TestBundleContext.
     *
     * @param bundle The contexts Bundle.
     */
    public TestBundleContext(TestBundle bundle) {
        this.bundle = bundle;
    }

    //
    // Methods
    //

    /**
     * Provides properties for the bundle. By default the Bundle properties are empty.
     *
     * @param props The properties to provide.
     */
    public void setProperties(Properties props) {
        this.props = props;
    }

    /**
     * Returns the Bundles properties. There is an empty Properties instance available by default.
     */
    public Properties getProperties() {
        return this.props;
    }

    //
    // BundleContext Methods
    //

    @Override
    public String getProperty(String key) {
        return this.props.getProperty(key);
    }

    @Override
    public Bundle getBundle() {
        return this.bundle;
    }

    /**
     * Not supported!
     */
    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        throw new BundleException("installBundle(...) is not supported!");
    }

    /**
     * Not supported!
     */
    @Override
    public Bundle installBundle(String location) throws BundleException {
        throw new BundleException("installBundle(...) is not supported!");
    }

    /**
     * Not supported!
     */
    @Override
    public Bundle getBundle(long id) {
        return this.bundle;
    }

    /**
     * Not supported!
     */
    @Override
    public Bundle[] getBundles() {
        return new Bundle[] {this.bundle};
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
        this.bundle.getServiceRegistry().addServiceListener(listener, filter);
    }

    @Override
    public void addServiceListener(ServiceListener listener) {
        this.bundle.getServiceRegistry().addServiceListener(listener);
    }

    @Override
    public void removeServiceListener(ServiceListener listener) {
        this.bundle.getServiceRegistry().removeServiceListener(listener);
    }

    /**
     * Not supported!
     */
    @Override
    public void addBundleListener(BundleListener listener) {
        throw new RuntimeException("Not yet supported!");
    }

    /**
     * Not supported!
     */
    @Override
    public void removeBundleListener(BundleListener listener) {
        throw new RuntimeException("Not yet supported!");
    }

    /**
     * Not supported!
     */
    @Override
    public void addFrameworkListener(FrameworkListener listener) {
        throw new RuntimeException("Not yet supported!");
    }

    /**
     * Not supported!
     */
    @Override
    public void removeFrameworkListener(FrameworkListener listener) {
        throw new RuntimeException("Not yet supported!");
    }

    @Override
    public ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties) {
        return registerService(clazzes[0], service, properties);
    }

    @Override
    public ServiceRegistration registerService(String clazz, Object service, Dictionary properties) {
        TestServiceRegistration sr = new TestServiceRegistration(clazz, new TestServiceReference(this), this.bundle);
        this.bundle.getServiceRegistry().registerService(sr, service);
        return sr;
    }

    @Override
    public ServiceReference[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        return this.bundle.getServiceRegistry().getServiceReferences(clazz, filter);
    }

    @Override
    public ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        return this.bundle.getServiceRegistry().getAllServiceReferences(clazz, filter);
    }

    @Override
    public ServiceReference getServiceReference(String clazz) {
        return this.bundle.getServiceRegistry().getServiceReference(clazz);
    }

    @Override
    public Object getService(ServiceReference reference) {
        return this.bundle.getServiceRegistry().getService(reference);
    }

    /**
     * Does nothing!
     */
    @Override
    public boolean ungetService(ServiceReference reference) {
        return true;
    }

    /**
     * Not supported!
     */
    @Override
    public File getDataFile(String filename) {
        throw new RuntimeException("Not supported!");
    }

    /**
     * Not supported!
     */
    @Override
    public Filter createFilter(String filter) throws InvalidSyntaxException {
        throw new RuntimeException("Not supported!");
    }
}
