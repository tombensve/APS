package se.natusoft.osgi.aps.test.tools;

import org.osgi.framework.*;
import se.natusoft.osgi.aps.test.tools.internal.ServiceRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * This is the starting point. Create one of this first. It provides an implementation of Bundle.
 *
 * You are required to add information to the bundle instance to set it up for testing.
 */
public class TestBundle implements Bundle {
    //
    // Private Members
    //

    private ServiceRegistry serviceRegistry;
    private Dictionary headers;
    private long id;
    private TestBundleContext bundleContext = new TestBundleContext(this);
    private Version version = new Version(1,2,3);
    private String symbolicName;
    private List<String> entryPaths = new LinkedList<>();

    //
    // Constructors
    //

    /**
     * Creates a new TestBundle.
     *
     * @param id The id of the bundle.
     * @param symbolicName The symbolic name of the bundle.
     * @param serviceRegistry The common service registry.
     */
    public TestBundle(long id, String symbolicName, ServiceRegistry serviceRegistry) {
        this.id = id;
        this.symbolicName = symbolicName;
        this.serviceRegistry = serviceRegistry;
    }

    //
    // Methods
    //

    ServiceRegistry getServiceRegistry() {
        return this.serviceRegistry;
    }

    /**
     * A quickie method for providing a service instance.
     *
     * @param service The service to provide.
     */
    public void addServiceInstance(Object service) {
        Properties properties = new Properties();
        properties.setProperty(Constants.OBJECTCLASS, service.getClass().getInterfaces()[0].getName());
        this.serviceRegistry.registerService(new TestServiceRegistration(service.getClass().getName(), new TestServiceReference(this.bundleContext, properties), this), service);
    }

    /**
     * Supplies manifest headers.
     *
     * @param headers The headers to set.
     */
    public void setHeaders(Dictionary headers) {
        this.headers = headers;
    }

    /**
     * Changes the bundle version. Default version is 1.2.3.
     *
     * @param version The new version to set.
     */
    public void setVersion(Version version) {
        this.version = version;
    }

    /**
     * Since this is not a real Bundle with real content, you have to specify the simulated test content
     * you want to make available to getEntryPath().
     *
     * Paths should always start with '/'!
     *
     * @param entryPaths A List of paths to add.
     */
    public void setEntryPaths(List<String> entryPaths) {
        this.entryPaths = entryPaths;
    }

    /**
     * Since this is not a real Bundle with real content, you have to specify the simulated test content
     * you want to make available to getEntryPath().
     *
     * Paths should always start with '/'!
     *
     * @param paths A varargs of Strings, one for each path.
     */
    public void addEntryPaths(String... paths) {
        Collections.addAll(this.entryPaths, paths);
    }

    /**
     * Currently not supported. Returns 0.
     */
    @Override
    public int getState() {
        return 0;
    }

    /**
     * Does nothing.
     */
    @Override
    public void start(int options) throws BundleException {

    }

    /**
     * Does nothing.
     */
    @Override
    public void start() throws BundleException {

    }

    /**
     * Does nothing.
     */
    @Override
    public void stop(int options) throws BundleException {

    }

    /**
     * Does nothing.
     */
    @Override
    public void stop() throws BundleException {

    }

    /**
     * Does nothing.
     */
    @Override
    public void update(InputStream input) throws BundleException {

    }

    /**
     * Does nothing.
     */
    @Override
    public void update() throws BundleException {

    }

    /**
     * Does nothing.
     */
    @Override
    public void uninstall() throws BundleException {

    }

    /**
     * Returns the added headers.
     */
    @Override
    public Dictionary getHeaders() {
        return this.headers;
    }

    /**
     * Returns the bundle id provided at creation.
     */
    @Override
    public long getBundleId() {
        return this.id;
    }

    /**
     * Currently just returns "/tmp". Not sure if that is more useful than "not supported".
     */
    @Override
    public String getLocation() {
        return "/tmp";
    }

    /**
     * Returns all registered services.
     */
    @Override
    public ServiceReference[] getRegisteredServices() {
        return this.serviceRegistry.getRegisteredServices();
    }

    /**
     * Currently this returns the same as getRegisteredServices()!
     */
    @Override
    public ServiceReference[] getServicesInUse() {
        return this.serviceRegistry.getServicesInUse();
    }

    /**
     * Not supported. Always returns true.
     */
    @Override
    public boolean hasPermission(Object permission) {
        return true;
    }

    /**
     * Just returns the named resource from current ClassLoader.
     *
     * @param name Resource to get.
     */
    @Override
    public URL getResource(String name) {
        return getClass().getClassLoader().getResource(name);
    }

    /**
     * Returns any added headers.
     *
     * @param locale This is completely ignored.
     */
    @Override
    public Dictionary getHeaders(String locale) {
        return this.headers;
    }

    /**
     * Returns the symbolic name provided at construction.
     */
    @Override
    public String getSymbolicName() {
        return this.symbolicName;
    }

    /**
     * Loads the specified call using the current ClassLoader.
     *
     * @param name Name of class to load.
     * @throws ClassNotFoundException
     */
    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith("/") || name.startsWith(".")) {
            name = name.substring(1);
        }
        return getClass().getClassLoader().loadClass(name);
    }

    /**
     * Returns the named resources by using the current ClassLoader.
     *
     * @param name Resource name to get.
     *
     * @throws IOException
     */
    @Override
    public Enumeration getResources(String name) throws IOException {
        return getClass().getClassLoader().getResources(name);
    }

    /**
     * Returns the entry paths added.
     *
     * @param path Only returns paths starting with this.
     *
     */
    @Override
    public Enumeration getEntryPaths(String path) {
        Vector<String> paths = new Vector<>();
        for (String entryPath : this.entryPaths) {
            if (entryPath.startsWith(path)) {
                paths.add(entryPath);
            }
        }
        return paths.elements();
    }

    /**
     * Returns an entry matching those provided by setEntryPaths(...) or addEntryPath(...), but in URL form.
     * @param path The start path of the entry to get.
     */
    @Override
    public URL getEntry(String path) {
        Enumeration enumeration = getEntryPaths(path);
        if (!enumeration.hasMoreElements()) {
            return null;
        }
        try {
            return new URL("file:" + enumeration.nextElement().toString());
        }
        catch (MalformedURLException mfe) {
            return null;
        }
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    /**
     * Currently not supported. Will just return getEntryPaths(path).
     *
     */
    @Override
    public Enumeration findEntries(String path, String filePattern, boolean recurse) {
        return getEntryPaths(path);
    }

    /**
     * Returns the BundleContext which in this case is an TestBundleContext.
     */
    @Override
    public BundleContext getBundleContext() {
        return this.bundleContext;
    }

    /**
     * Not supported! Returns null!
     */
    @Override
    public Map getSignerCertificates(int signersType) {
        return null;
    }

    /**
     * Returns 1.2.3 unless another version have been set.
     */
    @Override
    public Version getVersion() {
        return this.version;
    }
}
