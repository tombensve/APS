package se.natusoft.osgi.aps.test.tools;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation of ServiceReference for testing.
 */
public class TestServiceReference implements ServiceReference {
    //
    // Private Members
    //

    private TestBundleContext bundleContext = null;

    private List<Bundle> usingBundles = new ArrayList<>();

    //
    // Constructors
    //

    /**
     * Creates a new ServiceReference.
     *
     * @param bundleContext The context of the bundle the service belongs to.
     */
    public TestServiceReference(TestBundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    //
    // Methods
    //

    /**
     * Adds "using" bundle that will be returned by getUsingBundles().
     *
     * @param usingBundle The "using" bundle to add.
     */
    public void addUsingBundle(Bundle usingBundle) {
        this.usingBundles.add(usingBundle);
    }

    @Override
    public Object getProperty(String key) {
        return this.bundleContext.getProperty(key);
    }

    @Override
    public String[] getPropertyKeys() {
        String[] propKeys = new String[this.bundleContext.getProperties().stringPropertyNames().size()];
        return this.bundleContext.getProperties().stringPropertyNames().toArray(propKeys);
    }

    @Override
    public Bundle getBundle() {
        return this.bundleContext.getBundle();
    }

    @Override
    public Bundle[] getUsingBundles() {
        Bundle[] bundles = new Bundle[this.usingBundles.size()];
        return this.usingBundles.toArray(bundles);
    }


    /**
     * Always returns false.
     */
    @Override
    public boolean isAssignableTo(Bundle bundle, String className) {
        return false;
    }

    /**
     * Always returns 0.
     */
    @Override
    public int compareTo(Object reference) {
        return 0;
    }
}
