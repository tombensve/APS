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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.test.tools.internal.ServiceRegistry;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;

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

    private List<BundleManager> bundleManagers = new LinkedList<>();

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
     *
     * @param bundle The bundle to remove.
     */
    public void removeBundle(TestBundle bundle) {
        this.bundles.remove(bundle);
        this.bundleByName.remove(bundle.getSymbolicName());
        this.bundleById.remove(bundle.getBundleId());
    }

    /**
     * Removes a created bundle.
     *
     * @param bundleContext The context of the bundle to remove.
     */
    public void removeBundle(TestBundleContext bundleContext) {
        removeBundle((TestBundle)bundleContext.getBundle());
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

    /**
     * Shuts down all bundles started with deployBundle(...).
     */
    public void shutdown() {
        Collections.reverse(this.bundleManagers);
        for (BundleManager bm : this.bundleManagers) {
            bm.shutdown();
        }
        this.bundleManagers = new LinkedList<>();
    }

    /**
     * Test deploys a bundle using a BundleActivator.
     *
     * Usage:
     *
     *     BundleContext ctx = deployBundle("test-bundle).with(new APSActivator()).from("proj root relative bundle root path.");
     *     BundleContext ctx = deployBundle("test-bundle).with(new APSActivator()).from(new File("proj root relative bundle root path."));
     *     BundleContext ctx = deployBundle("test-bundle).with(new APSActivator()).from("group", "artifact", "version");
     *     BundleContext ctx = deployBundle("test-bundle).with(new APSActivator()).from("bundle content path", ...);
     *
     * @param name The name of the bundle to create and deploy.
     *
     * @return An intermediate BundleManager that handles the with() and from() giving you a BundleContext in the end.
     *
     * @throws IOException
     */
    public BundleManager deploy(String name) throws IOException {
        BundleManager bm = new BundleManager(name);
        this.bundleManagers.add(bm);
        return bm;
    }

    public static interface WithBundle {
        public void run(BundleContext bundleContext);
    }

    /**
     * Runs a piece of code as part of a temporary bundle.
     *
     * @param name The name of the bundle.
     * @param withBundle The code to run.
     *
     * @throws Exception
     */
    public void withNewBundle(String name, WithBundle withBundle) throws Exception {
        TestBundle bundle = createBundle(name);

        withBundle.run(bundle.getBundleContext());

        removeBundle(bundle);
    }

    /**
     * Inner class to support primitive DSL. Looks better when called from Groovy where you can skip chars like '.' and '()' :-).
     */
    public class BundleManager {

        private TestBundle bundle = null;
        private BundleActivator activator = null;
        private boolean started = false;

        /**
         * Creates the BundleManager instance.
         *
         * @param name The name of the bundle managed.
         */
        public BundleManager(String name) {
            this.bundle = createBundle(name);
        }

        /**
         * Private support method that actually starts the bundle using its BundleActivator.
         *
         * @return itself.
         * @throws Exception
         */
        private BundleManager start() throws Exception {
            if (this.activator == null) {
                throw new Exception("Activator has not been provided! Add an 'with new MyActivator()'");
            }
            this.activator.start(this.bundle.getBundleContext());
            this.started = true;
            return this;
        }

        /**
         * Provides a BundleActivator to use for starting the bundle. This call only saves the activator, it does
         * not call it yet.
         *
         * @param bundleActivator The BundleActivator to provide.
         * @return itself
         */
        public BundleManager withActivator(BundleActivator bundleActivator) {
            this.activator = bundleActivator;
            return this;
        }

        /**
         * Supplies a Callable that creates and returns a subclass of APSConfig. If a static ManagedConfig instance
         * is found in the returned config object then it is setup.
         *
         * @param config A Callable that should setup configuration and return it.
         * @return itself
         * @throws Exception
         */
        public BundleManager withAPSConfig(Callable<APSConfig> config) throws Exception {
            if (this.started) throw new Exception("Config must be provided earlier in command line!");
            APSConfig apsConfig = config.call();
            for (Field field : apsConfig.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true); // This should not be needed!
                    Object managed = field.get(apsConfig);
                    if (managed != null && ManagedConfig.class.isAssignableFrom(managed.getClass())) {
                        ManagedConfig managedConfig = (ManagedConfig)managed;
                        //noinspection unchecked
                        managedConfig.serviceProviderAPI.setConfigInstance(apsConfig);
                        managedConfig.serviceProviderAPI.setManaged();
                    }
                }
            }

            return this;
        }

        /**
         * Provides bundle content by reading maven artifact.
         *
         * @param group The artifact group
         * @param artifact The artifact.
         * @param version The artifact version
         * @return itself
         * @throws Exception
         */
        public BundleManager from(String group, String artifact, String version) throws Exception {
            this.bundle.loadEntryPathsFromMaven(group, artifact, version);
            return start();
        }

        /**
         * Provides bundle content by scanning files under a root directory.
         *
         * @param dirScan The root diretory to scan.
         * @return itself
         * @throws Exception
         */
        public BundleManager from(String dirScan) throws Exception {
            this.bundle.loadEntryPathsFromDirScan(dirScan);
            return start();
        }

        /**
         * Provides bundle content by scanning files under a root directory using a File object to specify root.
         *
         * @param dirScan The root to start scanning at.
         * @return itself
         * @throws Exception
         */
        public BundleManager from(File dirScan) throws Exception {
            this.bundle.loadEntryPathsFromDirScan(dirScan);
            return start();
        }

        /**
         * Provides bundle content by providing content paths as an array.
         *
         * @param paths The paths to provide.
         * @return itself
         * @throws Exception
         */
        public BundleManager using(String[] paths) throws Exception {
            this.bundle.addEntryPaths(paths);
            return start();
        }

        /**
         * Terminates this builder and returns a BundleContext representing the result.
         */
        public BundleContext asContext() {
            return this.bundle.getBundleContext();
        }

        /**
         * Terminates this builder and returns a Bundle representing the result.
         */
        public Bundle asBundle() {
            return this.bundle;
        }

        /**
         * This is saved internally, and on OSGiServiceTestTools.shutdown() this is called for all saved instances.
         */
        public void shutdown() {
            try {
                this.activator.stop(this.bundle.getBundleContext());
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
            removeBundle(this.bundle);
            this.started = false;
        }
    }
}
