package se.natusoft.osgi.aps.test.tools;

import se.natusoft.osgi.aps.test.tools.internal.ServiceRegistry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is the entry point to using the OSGi service test tools.
 */
public class OSGIServiceTestTools {
    //
    // Private Members
    //

    private long idGen = 0;

    private ServiceRegistry serviceRegistry = new ServiceRegistry();

    private List<TestBundle> bundles = new LinkedList<>();

    private Map<String, TestBundle> bundleByName = new HashMap<>();

    private Map<Long, TestBundle> bundleById = new HashMap<>();

    //
    // Methods
    //

    /**
     * Creates a new TestBundle.
     *
     * @param symbolicName The symbolic name of the bundle to create.
     */
    public TestBundle createBundle(String symbolicName) {
        TestBundle bundle = new TestBundle(++idGen, symbolicName, this.serviceRegistry);
        this.bundles.add(bundle);
        this.bundleByName.put(symbolicName, bundle);
        this.bundleById.put(bundle.getBundleId(), bundle);
        return bundle;
    }

    /**
     * Removes a created bundle.
     * @param bundle
     */
    public void removeBundle(TestBundle bundle) {
        this.bundles.remove(bundle);
        this.bundleByName.remove(bundle.getSymbolicName());
        this.bundleById.remove(bundle.getBundleId());
    }

    /**
     * Returns all created bundles.
     */
    public List<TestBundle> getBundles() {
        return this.bundles;
    }

    /**
     * Returns a specific bundle by its symbolic name.
     *
     * @param name The name of the bundle to get.
     */
    public TestBundle getBundleBySymbolicName(String name) {
        return this.bundleByName.get(name);
    }

    /**
     * Returns a specific bundle by its id.
     *
     * @param id The id of the bundle to get.
     */
    public TestBundle getBundleById(long id) {
        return this.bundleById.get(id);
    }
}
