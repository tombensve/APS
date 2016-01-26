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
package se.natusoft.osgi.aps.test.tools;

import org.osgi.framework.*;
import se.natusoft.osgi.aps.test.tools.internal.ServiceRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;

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
    @SuppressWarnings("unused")
    public void addServiceInstance(Object service) {
        Properties properties = new Properties();
        properties.setProperty(Constants.OBJECTCLASS, service.getClass().getInterfaces()[0].getName());
        this.serviceRegistry.registerService(new TestServiceRegistration(service.getClass().getName(), new TestServiceReference(this.bundleContext, properties), this), service, service.getClass().getInterfaces()[0]);
    }

    /**
     * Supplies manifest headers.
     *
     * @param headers The headers to set.
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
     * This provides simulated content by reading the content list of a real jar in the local maven repository.
     *
     * To be **very clear** this does not load nor use the actual files in the specified jar! It just provides
     * the paths that the real jar has. When running the real jar must be available on the classpath. The specified
     * jar is again **not added** to the classpath! So any jar provided here must either be the jar of the project
     * being tested or be added as a test dependency.
     *
     * @param group The group id of the jar artifact.
     * @param artifact The artifact name.
     * @param version The version of the artifact.
     */
    public void loadEntryPathsFromMaven(String group, String artifact, String version) throws IOException {
        loadEntryPathsFromMaven(group, artifact, version, "");
    }

    /**
     * This provides simulated content by reading the content list of a real jar in the local maven repository.
     *
     * To be **very clear** this does not load nor use the actual files in the specified jar! It just provides
     * the paths that the real jar has. When running the real jar must be available on the classpath. The specified
     * jar is again **not added** to the classpath! So any jar provided here must either be the jar of the project
     * being tested or be added as a test dependency.
     *
     * @param group The group id of the jar artifact.
     * @param artifact The artifact name.
     * @param version The version of the artifact.
     * @param classifier The classifier of the  jar. Can be null or blank.
     */
    public void loadEntryPathsFromMaven(String group, String artifact, String version, String classifier) throws IOException {
        if (classifier == null || classifier.trim().isEmpty()) {
            classifier = "";
        }
        else {
            classifier = "-" + classifier.trim();
        }

        File jarFile = new File(System.getProperty("user.home"));
        jarFile = new File(jarFile, ".m2/repository");
        jarFile = new File(jarFile, group.replace('.','/'));
        jarFile = new File(jarFile, artifact);
        jarFile = new File(jarFile, version);
        jarFile = new File(jarFile, artifact + "-" + version + classifier + ".jar");

        if (!jarFile.exists()) throw new IllegalArgumentException("GAV '" + group + ":" + artifact + ":" + version + classifier +
                " does not exist!");

        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream().forEach(jarEntry -> TestBundle.this.entryPaths.add(jarEntry.getName()));
        }
    }

    /**
     * Loads entry paths by doing a file scan at the specified root.
     *
     * @param relPath A path that is relative to the top maven parent project.
     */
    public void loadEntryPathsFromDirScan(String relPath) {
        loadEntryPathsFromDirScan(new File(new MavenRootFile().getRoot(), relPath));
    }

    /**
     * Loads entry paths by doing a file scan at the specified root.
     *
     * @param root The root of the file scan.
     */
    public void loadEntryPathsFromDirScan(File root) {
        new DirScanner(root).stream().forEach(path -> TestBundle.this.entryPaths.add(path));
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
    public void start(int options) throws BundleException {}

    /**
     * Does nothing.
     */
    @Override
    public void start() throws BundleException {}

    /**
     * Does nothing.
     */
    @Override
    public void stop(int options) throws BundleException {}

    /**
     * Does nothing.
     */
    @Override
    public void stop() throws BundleException {}

    /**
     * Does nothing.
     */
    @Override
    public void update(InputStream input) throws BundleException {}

    /**
     * Does nothing.
     */
    @Override
    public void update() throws BundleException {}

    /**
     * Does nothing.
     */
    @Override
    public void uninstall() throws BundleException {}

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
