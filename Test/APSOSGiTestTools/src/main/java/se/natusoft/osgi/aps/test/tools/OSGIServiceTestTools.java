/*
 *
 * PROJECT
 *     Name
 *         APS OSGi Test Tools
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
import org.osgi.framework.BundleEvent;
import se.natusoft.osgi.aps.test.tools.internal.ServiceRegistry;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * This is the entry point to using the OSGi service test tools.
 *
 * In the most common case, let your unit test class extend this and then just call deploy("name") which
 * will return a BundleBuilder.
 *
 * The below examples uses the maven GAV reference which looks in ~/.m2/repository. There is also a
 * from variant that takes a root path and then scans it for content. Instead of 'from' it is also
 * possible to use 'using' which takes an array of string bundle content paths. When test is run these
 * must also be available in JUnit classpath for the test. This does no classloading, it only used
 * the JUnit setup classpath. Using true OSGi bundle classloading here would make things very much
 * more complicated. maven-bundle-plugin have already validated imports and exports so doing so again
 * seems overkill.
 *
 * ## Groovy Example
 *
 *             deploy 'aps-vertx-provider' with new APSActivator() from(
 *                 'se.natusoft.osgi.aps',
 *                 'aps-vertx-provider',
 *                 '1.0.0'
 *            )
 *
 *            deploy 'aps-config-manager' with new APSActivator() from 'APS-Core/APSConfigManager/target/classes'
 *
 *            deploy 'moon-whale-service' with new APSActivator() from 'APS-Core/APSConfigManager/target/test-classes'
 *
 * ## Java Example
 *
 *             deploy("aps-vertx-provider").with(new APSActivator()).from(
 *                 "se.natusoft.osgi.aps",
 *                 "aps-vertx-provider",
 *                 "1.0.0"
 *             );
 *
 *            deploy( "aps-config-manager").with( new APSActivator() ).from( "APS-Core/APSConfigManager/target/classes");
 *
 *            deploy( "moon-whale-service").with( new APSActivator() ).from( "APS-Core/APSConfigManager/target/test-classes");
 *
 */
@SuppressWarnings("WeakerAccess")
public class OSGIServiceTestTools {
    //
    // Private Members
    //

    private long idGen = 0;

    private ServiceRegistry serviceRegistry = new ServiceRegistry();

    private List<TestBundle> bundles = new LinkedList<>();

    private Map<String, TestBundle> bundleByName = new HashMap<>();

    private Map<Long, TestBundle> bundleById = new HashMap<>();

    private List<BundleBuilder> bundleBuilders = new LinkedList<>();

    //
    // Methods
    //

    /**
     * Send a bundle event to bundles.
     *
     * @param bundle The bundle the event is about.
     * @param type The type of the event.
     */
    private void bundleEvent(Bundle bundle, int type) {
        for (TestBundle testBundle : this.bundles) {
            ((TestBundleContext)testBundle.getBundleContext()).bundleEvent( bundle, type );
        }
    }

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

        for (TestBundle testBundle : this.bundles) {
            ((TestBundleContext)testBundle.getBundleContext()).bundleEvent( bundle, BundleEvent.INSTALLED );
        }

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

        for (TestBundle testBundle : this.bundles) {
            ((TestBundleContext)testBundle.getBundleContext()).bundleEvent( bundle, BundleEvent.UNINSTALLED );
        }
    }

    /**
     * Removes a created bundle.
     *
     * @param bundleContext The context of the bundle to remove.
     */
    @SuppressWarnings("unused")
    public void removeBundle(TestBundleContext bundleContext) {
        removeBundle((TestBundle) bundleContext.getBundle());
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
    @SuppressWarnings("unused")
    public TestBundle getBundleBySymbolicName(String name) {
        return this.bundleByName.get(name);
    }

    /**
     * Returns a specific bundle by its id.
     *
     * @param id The id of the bundle to get.
     */
    @SuppressWarnings("unused")
    public TestBundle getBundleById(long id) {
        return this.bundleById.get(id);
    }

    /**
     * Shuts down all bundles started with deployBundle(...).
     */
    public void shutdown() {
        Collections.reverse(this.bundleBuilders);
        this.bundleBuilders.forEach(BundleBuilder::shutdown);
        this.bundleBuilders = new LinkedList<>();
    }

    /**
     * Test deploys a bundle using a BundleActivator.
     * <p>
     * Usage:
     * <p>
     * BundleContext ctx = deployBundle("test-bundle).with(new APSActivator()).from("proj root relative bundle root path.");
     * BundleContext ctx = deployBundle("test-bundle).with(new APSActivator()).from(new File("proj root relative bundle root path."));
     * BundleContext ctx = deployBundle("test-bundle).with(new APSActivator()).from("group", "artifact", "version");
     * BundleContext ctx = deployBundle("test-bundle).with(new APSActivator()).from("bundle content path", ...);
     *
     * @param name The name of the bundle to create and deploy.
     * @return An intermediate BundleManager that handles the with() and from() giving you a BundleContext in the end.
     */
    public BundleBuilder deploy(String name) {
        BundleBuilder bm = new BundleBuilder(name);
        this.bundleBuilders.add(bm);
        return bm;
    }

    /**
     * Undeploys a bundle.
     *
     * @param name The name of the bundle to undeploy.
     */
    @SuppressWarnings("unused")
    public void undeploy(String name) {
        BundleBuilder bb = this.bundleBuilders.stream().filter(b -> b.getName().equals(name)).findFirst().orElse(null);
        if (bb != null) {
            this.bundleBuilders.remove(bb);
            bb.shutdown();
        }
    }

    /**
     * API to implement for passing to withNewBundle.
     */
    public interface WithBundle {
        void run(BundleContext bundleContext) throws Throwable;
    }

    /**
     * Runs a piece of code as part of a temporary bundle.
     *
     * @param name       The name of the bundle.
     * @param withBundle The code to run.
     * @throws Throwable Any exception is forwarded.
     */
    public void with_new_bundle(String name, WithBundle withBundle) throws Throwable {
        TestBundle bundle = createBundle(name);

        withBundle.run(bundle.getBundleContext());

        removeBundle(bundle);
    }

    /**
     * Provides a delay so that concurrent things can finish before checking results.
     *
     * @param milliseconds The number of milliseconds to wait.
     * @throws InterruptedException If interrupted.
     */
    public void delay(int milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }

    /**
     * Provides a delay so that concurrent things can finish before checking results.
     *
     * @param delay A string. 'long' ==> 10000ms, 'very small' ==> 500ms, anything else ==> 1000ms.
     * @throws InterruptedException if interrupted.
     */
    public void delay(String delay) throws InterruptedException {
        int ms = 1000;
        if (delay.startsWith("long")) {
            ms = 10000;
        } else if (delay.startsWith("very small")) {
            ms = 500;
        }
        delay(ms);
    }

    /**
     * Waits for a condition with a timeout using creative synonyms to reserved words ...
     * <p>
     * Assuming: static import java.util.concurrent.TimeUnit.*;
     * <p>
     * Usage Java (Java 8+: A lambda could possible be used as Callable):
     * * hold().whilst(new Callable&lt;Boolean&gt;(){...}).maxTime(30L).unit(SECONDS).go();
     * * hold().until(new Callable&lt;Boolean&gt;(){...}).maxTime(30L).unit(SECONDS).go();
     * <p>
     * Usage Groovy:
     * * hold().whilst { this.something == null } maxTime 30L unit SECONDS go()
     * * hold().until { this.something != null } macTime 10L unit SECONDS go()
     */
    public Wait hold() {
        return new Wait();
    }

    public class Wait {
        private long maxTime = 5;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private Callable<Boolean> condition;

        public Wait whilst(Callable<Boolean> condition) {
            this.condition = condition;
            return this;
        }

        public Wait until(Callable<Boolean> condition) {
            this.condition = () -> !condition.call();
            return this;
        }

        public Wait maxTime(long time) {
            this.maxTime = time;
            return this;
        }

        public Wait unit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public void go() {
            try {
                if (this.condition != null) {
                    int maxCount = (int) (TimeUnit.MILLISECONDS.convert(this.maxTime, this.timeUnit) / 200);
                    int count = 0;
                    while (this.condition.call()) {
                        synchronized (this) {
                            wait(200);
                        }
                        ++count;
                        if (count > maxCount) break;
                    }
                } else {
                    Thread.sleep(TimeUnit.MILLISECONDS.convert(this.maxTime, this.timeUnit));
                }
            } catch (Exception ignore) {}
        }
    }

    /**
     * Inner class to support primitive DSL. Looks better when called from Groovy where you can skip chars like '.' and '()' :-).
     */
    public class BundleBuilder {

        private TestBundle bundle;
        private BundleActivator activator = null;
//        private boolean started = false;
        private String name;

        /**
         * Creates the BundleManager instance.
         *
         * @param name The name of the bundle managed.
         */
        public BundleBuilder(String name) {
            this.name = name;
            this.bundle = createBundle(name);
        }

        /**
         * Returns the name of the Bundle this BundleBuilder represents.
         */
        private String getName() {
            return this.name;
        }

        /**
         * Private support method that actually starts the bundle using its BundleActivator.
         *
         * @return itself.
         * @throws Exception Any exceptions are forwarded.
         */
        private BundleBuilder start() throws Exception {
            if (this.activator == null) {
                throw new IllegalStateException("Activator has not been provided! Add an 'with new MyActivator()'");
            }
            this.activator.start(this.bundle.getBundleContext());

            bundleEvent( bundle, BundleEvent.STARTED );

            return this;
        }

        public BundleBuilder with(BundleActivator bundleActivator) {
            return with_activator(bundleActivator);
        }

        public BundleBuilder with(ClassLoader bundleClassLoader) {
            this.bundle.setBundleClassLoader(bundleClassLoader);
            return this;
        }

        /**
         * Provides a BundleActivator to use for starting the bundle. This call only saves the activator, it does
         * not call it yet.
         *
         * @param bundleActivator The BundleActivator to provide.
         * @return itself
         */
        public BundleBuilder with_activator(BundleActivator bundleActivator) {
            this.activator = bundleActivator;
            return this;
        }

        /**
         * Provides bundle content by reading maven artifact.
         *
         * @param group    The artifact group
         * @param artifact The artifact.
         * @param version  The artifact version
         * @return itself
         * @throws Exception Forwards exceptions
         */
        public BundleBuilder from(String group, String artifact, String version) throws Exception {
            this.bundle.loadEntryPathsFromMaven(group, artifact, version);
            return start();
        }

        /**
         * Provides bundle content by scanning files under a root directory.
         *
         * @param dirScan The root diretory to scan.
         * @return itself
         * @throws Exception Forwards exceptions
         */
        public BundleBuilder from(String dirScan) throws Exception {
            this.bundle.loadEntryPathsFromDirScan(dirScan);
            return start();
        }

        /**
         * Provides bundle content by scanning files under a root directory using a File object to specify root.
         *
         * @param dirScan The root to start scanning at.
         * @return itself
         * @throws Exception Forwards exceptions
         */
        public BundleBuilder from(File dirScan) throws Exception {
            this.bundle.loadEntryPathsFromDirScan(dirScan);
            return start();
        }

        /**
         * Provides bundle content by providing content paths as an array.
         *
         * @param paths The paths to provide.
         * @return itself
         * @throws Exception Forwards exceptions
         */
        public BundleBuilder using(String[] paths) throws Exception {
            for (String path : paths) {
                if (!path.startsWith( "/" )) {
                    path = "/" + path;
                }
                this.bundle.addEntryPaths( new BundleEntryPath( path ) );
            }
            return start();
        }

        /**
         * Terminates this builder and returns a BundleContext representing the result.
         */
        @SuppressWarnings("unused")
        public BundleContext as_context() {
            return this.bundle.getBundleContext();
        }

        /**
         * Terminates this builder and returns a Bundle representing the result.
         */
        @SuppressWarnings("unused")
        public Bundle as_bundle() {
            return this.bundle;
        }

        /**
         * This is saved internally, and on OSGiServiceTestTools.shutdown() this is called for all saved instances.
         */
        public void shutdown() {
            try {
                this.activator.stop(this.bundle.getBundleContext());
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

            bundleEvent( bundle, BundleEvent.STOPPED );

            removeBundle(this.bundle);
        }
    }
}
