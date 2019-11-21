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
package se.natusoft.osgi.aps.runtime;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import se.natusoft.osgi.aps.activator.APSActivator;
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService;
import se.natusoft.osgi.aps.exceptions.APSException;
import se.natusoft.osgi.aps.runtime.internal.ServiceRegistry;

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
 *     deploy 'aps-vertx-provider' with new APSActivator() from('se.natusoft.osgi.aps','aps-vertx-provider','1.0.0')
 *
 *     deploy 'aps-config-manager' with new APSActivator() from 'APS-Core/APSConfigManager/target/classes'
 *
 *     deploy 'moon-whale-service' with new APSActivator() from 'APS-Core/APSConfigManager/target/test-classes'
 *
 * ## Java Example
 *
 *     deploy("aps-vertx-provider").with(new APSActivator()).from("se.natusoft.osgi.aps","aps-vertx-provider","1.0.0");
 *
 *     deploy( "aps-config-manager").with( new APSActivator() ).from( "APS-Core/APSConfigManager/target/classes");
 *
 *     deploy( "moon-whale-service").with( new APSActivator() ).from( "APS-Core/APSConfigManager/target/test-classes");
 */
@SuppressWarnings({ "WeakerAccess", "SpellCheckingInspection" })
public class APSRuntime {
    //
    // Private Members
    //

    private long idGen = 0;

    private ServiceRegistry serviceRegistry = new ServiceRegistry();

    private List<APSBundle> bundles = new LinkedList<>();

    private Map<String, APSBundle> bundleByName = new HashMap<>();

    private Map<Long, APSBundle> bundleById = new HashMap<>();

    private List<BundleBuilder> bundleBuilders = new LinkedList<>();

    //
    // Methods
    //

    /**
     * Send a bundle event to bundles.
     *
     * @param bundle The bundle the event is about.
     * @param type   The type of the event.
     */
    private void bundleEvent( Bundle bundle, int type ) {
        for ( APSBundle apsBundle : this.bundles ) {
            ( (APSBundleContext) apsBundle.getBundleContext() ).bundleEvent( bundle, type );
        }
    }

    /**
     * Creates a new TestBundle.
     *
     * @param symbolicName The symbolic name of the bundle to create.
     */
    public APSBundle createBundle( String symbolicName ) {
        APSBundle bundle = new APSBundle( ++idGen, symbolicName, this.serviceRegistry );
        this.bundles.add( bundle );
        this.bundleByName.put( symbolicName, bundle );
        this.bundleById.put( bundle.getBundleId(), bundle );

        for ( APSBundle apsBundle : this.bundles ) {
            ( (APSBundleContext) apsBundle.getBundleContext() ).bundleEvent( bundle, BundleEvent.INSTALLED );
        }

        return bundle;
    }

    /**
     * Removes a created bundle.
     *
     * @param bundle The bundle to remove.
     */
    public void removeBundle( APSBundle bundle ) {
        this.bundles.remove( bundle );
        this.bundleByName.remove( bundle.getSymbolicName() );
        this.bundleById.remove( bundle.getBundleId() );

        for ( APSBundle apsBundle : this.bundles ) {
            ( (APSBundleContext) apsBundle.getBundleContext() ).bundleEvent( bundle, BundleEvent.UNINSTALLED );
        }
    }

    /**
     * Removes a created bundle.
     *
     * @param bundleContext The context of the bundle to remove.
     */
    @SuppressWarnings( "unused" )
    public void removeBundle( APSBundleContext bundleContext ) {
        removeBundle( (APSBundle) bundleContext.getBundle() );
    }

    /**
     * Returns all created bundles.
     */
    public List<APSBundle> getBundles() {
        return this.bundles;
    }

    /**
     * Returns a specific bundle by its symbolic name.
     *
     * @param name The name of the bundle to get.
     */
    @SuppressWarnings( "unused" )
    public APSBundle getBundleBySymbolicName( String name ) {
        return this.bundleByName.get( name );
    }

    /**
     * Returns a specific bundle by its id.
     *
     * @param id The id of the bundle to get.
     */
    @SuppressWarnings( "unused" )
    public APSBundle getBundleById( long id ) {
        return this.bundleById.get( id );
    }

    /**
     * Shuts down all bundles started with deployBundle(...).
     */
    public void shutdown() {
        Collections.reverse( this.bundleBuilders );
        this.bundleBuilders.forEach( BundleBuilder::shutdown );
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
    public BundleBuilder deploy( String name ) {
        BundleBuilder bm = new BundleBuilder( name );
        this.bundleBuilders.add( bm );
        return bm;
    }

    /**
     * Undeploys a bundle.
     *
     * @param name The name of the bundle to undeploy.
     */
    @SuppressWarnings( "unused" )
    public void undeploy( String name ) {
        BundleBuilder bb = this.bundleBuilders.stream().filter( b -> b.getName().equals( name ) ).findFirst().orElse( null );
        if ( bb != null ) {
            this.bundleBuilders.remove( bb );
            bb.shutdown();
        }
    }

    // ---- Helpers for unit tests ---- //

    @SuppressWarnings("unused")
    public void deployConfigAndVertxPlusDeps()  throws Exception {
        deployConfigAndVertxPlusDeps( null );
    }

    public static final String VERTX_DEPLOYER = "vertxDeployer";
    public static final String DATASTORE_SERVICE_DEPLOYER = "dataStoreServiceDeployer";
    public static final String BUS_MESSAGING_DEPLOYER = "busMessagingDeployer";

    @SuppressWarnings("unused")
    public static Map<String, Runnable> vertxDeployer( Map<String, Runnable> depMap, Runnable deployer) {
        if (depMap == null) depMap = new LinkedHashMap<>(  );
        depMap.put(VERTX_DEPLOYER, deployer);
        return depMap;
    }

    @SuppressWarnings("unused")
    public static Map<String, Runnable> dataStoreServiceDeployer( Map<String, Runnable> depMap, Runnable deployer) {
        if (depMap == null) depMap = new LinkedHashMap<>(  );
        depMap.put(DATASTORE_SERVICE_DEPLOYER, deployer);
        return depMap;
    }

    @SuppressWarnings("unused")
    public static Map<String, Runnable> busMessagingDeployer( Map<String, Runnable> depMap, Runnable deployer) {
        if (depMap == null) depMap = new LinkedHashMap<>(  );
        depMap.put(BUS_MESSAGING_DEPLOYER, deployer);
        return depMap;
    }


    /**
     * Deploys the aps-config-manager and all its dependencies. This is needed to be able to
     * deploy a bundle that makes use of aps-config-manager to get its configuration.
     *
     * I've decided to deploy actual services instead of faking configuration in test.
     *
     * @throws Exception on any failure to deploy.
     */
    public void deployConfigAndVertxPlusDeps( Map<String, Runnable> altDeployers) throws Exception {

        System.setProperty( APSFilesystemService.CONF_APS_FILESYSTEM_ROOT, "target/config" );

        deploy( "aps-config-manager" ).with( new APSActivator() ).from(
                "se.natusoft.osgi.aps",
                "aps-config-manager",
                "1.0.0"
        );

        deploy( "aps-core-lib" ).with( new APSActivator() ).from(
                "se.natusoft.osgi.aps",
                "aps-core-lib",
                "1.0.0"
        );

        Runnable vertxDeployer = altDeployers != null ? altDeployers.get("vertxDeployer") : null;
        System.err.println("vertxDeployer: " + vertxDeployer);
        if (vertxDeployer == null) {
            deploy( "aps-vertx-provider" ).with( new APSActivator() ).from(
                    "se.natusoft.osgi.aps",
                    "aps-vertx-provider",
                    "1.0.0"
            );
        }
        else {
            vertxDeployer.run();
        }

        hold().maxTime( 2 ).unit( TimeUnit.SECONDS ).go();

        Runnable dataStoreServiceDeployer = altDeployers != null ? altDeployers.get("dataStoreServiceDeployer") : null;
        if (dataStoreServiceDeployer == null) {
            deploy( "aps-vertx-cluster-datastore-service-provider" ).with( new APSActivator() ).from(
                    "se.natusoft.osgi.aps",
                    "aps-vertx-cluster-datastore-service-provider",
                    "1.0.0"
            );
        }
        else {
            dataStoreServiceDeployer.run();
        }

        deploy( "aps-filesystem-service-provider" ).with( new APSActivator() ).from(
                "se.natusoft.osgi.aps",
                "aps-filesystem-service-provider",
                "1.0.0"
        );

    }

    // ---- End helpers for unit test ---- //

    /**
     * API to implement for passing to withNewBundle.
     */
    public interface WithBundle {
        void run( BundleContext bundleContext ) throws Throwable;
    }

    /**
     * Runs a piece of code as part of a temporary bundle.
     *
     * @param name       The name of the bundle.
     * @param withBundle The code to run.
     * @throws Throwable Any exception is forwarded.
     */
    public void with_new_bundle( String name, WithBundle withBundle ) throws Throwable {
        APSBundle bundle = createBundle( name );

        withBundle.run( bundle.getBundleContext() );

        removeBundle( bundle );
    }

    /**
     * Provides a delay so that concurrent things can finish before checking results.
     *
     * @param milliseconds The number of milliseconds to wait.
     * @throws InterruptedException If interrupted.
     */
    public void delay( int milliseconds ) throws InterruptedException {
        Thread.sleep( milliseconds );
    }

    /**
     * Provides a delay so that concurrent things can finish before checking results.
     *
     * @param delay A string. 'long' ==> 10000ms, 'very small' ==> 500ms, anything else ==> 1000ms.
     * @throws InterruptedException if interrupted.
     */
    public void delay( String delay ) throws InterruptedException {
        int ms = 1000;
        if ( delay.startsWith( "long" ) ) {
            ms = 10000;
        }
        else if ( delay.startsWith( "very small" ) ) {
            ms = 500;
        }
        delay( ms );
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

    public static class Wait {
        private boolean exceptionOnTimeout = false;
        private long maxTime = 5;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private Callable<Boolean> condition;

        public Wait whilst( Callable<Boolean> condition ) {
            this.condition = condition;
            return this;
        }

        @SuppressWarnings("unused")
        public Wait until( Callable<Boolean> condition ) {
            this.condition = () -> !condition.call();
            return this;
        }

        public Wait maxTime( long time ) {
            this.maxTime = time;
            return this;
        }

        public Wait unit( TimeUnit timeUnit ) {
            this.timeUnit = timeUnit;
            return this;
        }

        @SuppressWarnings("unused")
        public Wait exceptionOnTimeout( boolean exceptionOnTimeout ) {
            this.exceptionOnTimeout = exceptionOnTimeout;
            return this;
        }

        public void go() throws APSException {

            try {
                if ( this.condition != null ) {
                    int maxCount = ( int ) ( TimeUnit.MILLISECONDS.convert( this.maxTime, this.timeUnit ) / 200 );
                    int count = 0;
                    while ( this.condition.call() ) {
                        synchronized ( this ) {
                            wait( 200 );
                        }
                        ++count;
                        if ( count > maxCount ) {
                            if (this.exceptionOnTimeout) {
                                throw new APSException( "The current hold() timed out!" );
                            }
                            else {
                                System.err.println( "WARNING: APSOSGiServiceTestTools.hold() exited due to timeout!!" );
                            }
                            break;
                        }
                    }
                }
                else {
                    Thread.sleep( TimeUnit.MILLISECONDS.convert( this.maxTime, this.timeUnit ) );
                }
            } catch ( Exception mostlyIgnore ) {
                if (mostlyIgnore.getClass() == APSException.class && mostlyIgnore.getMessage().equals( "The current hold() timed out!" )) {
                    throw (APSException)mostlyIgnore;
                }

                System.err.println("WARNING: The following Exception did occur, but will not break test!");
                mostlyIgnore.printStackTrace( System.err );
            }
        }
    }

    /**
     * Inner class to support primitive DSL. Looks better when called from Groovy where you can skip chars like '.' and '()' :-).
     */
    public class BundleBuilder {

        private APSBundle bundle;
        private BundleActivator activator = null;
        //        private boolean started = false;
        private String name;

        /**
         * Creates the BundleManager instance.
         *
         * @param name The name of the bundle managed.
         */
        public BundleBuilder( String name ) {
            this.name = name;
            this.bundle = createBundle( name );
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
            if ( this.activator == null ) {
                throw new IllegalStateException( "Activator has not been provided! Add an 'with new MyActivator()'" );
            }
            this.activator.start( this.bundle.getBundleContext() );

            bundleEvent( bundle, BundleEvent.STARTED );

            return this;
        }

        public BundleBuilder with( BundleActivator bundleActivator ) {
            return with_activator( bundleActivator );
        }

        public BundleBuilder with( ClassLoader bundleClassLoader ) {
            this.bundle.setBundleClassLoader( bundleClassLoader );
            return this;
        }

        /**
         * Provides a BundleActivator to use for starting the bundle. This call only saves the activator, it does
         * not call it yet.
         *
         * @param bundleActivator The BundleActivator to provide.
         * @return itself
         */
        public BundleBuilder with_activator( BundleActivator bundleActivator ) {
            this.activator = bundleActivator;
            return this;
        }

        /**
         * Provides a MANIFEST.MF for test bundle.
         *
         * @param testClass The test class requesting the MANIFEST.MF. It will be loaded using this
         *                  class ClassLoader.
         *
         * @return itself.
         */
        public BundleBuilder manifest_from( Class testClass) {
            manifest_from( testClass, null );
            return this;
        }

        /**
         * Provides a MANIFEST.MF for test bundle.
         *
         * @param testClass The test class requesting the MANIFEST.MF. It will be loaded using this
         *                  class ClassLoader.
         * @param name The name of the MANIFEST.MF file to use. Defaults to MANIFEST.MF if null or blank.
         *
         * @return itself.
         */
        @SuppressWarnings("UnusedReturnValue")
        public BundleBuilder manifest_from( Class testClass, String name) {
            if (name == null || name.equals( "" )) {
                name = "MANIFEST.MF";
            }
            String _name = "/forTest/META-INF/" + name;
            this.bundle.loadManifest( testClass.getResourceAsStream( _name ) );

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
        public BundleBuilder from( String group, String artifact, String version ) throws Exception {
            this.bundle.loadEntryPathsFromMaven( group, artifact, version );
            return start();
        }

        /**
         * Provides bundle content by reading a jar file.
         *
         * @param file The jar file to read.
         * @return itself.
         * @throws Exception Forwards exception.
         */
        @SuppressWarnings("UnusedReturnValue") // This is upp to the caller, not this API!
        public BundleBuilder fromJar( File file) throws Exception {
            this.bundle.loadEntryPathsFromJar( file );
            return start();
        }

        /**
         * Provides bundle content by scanning files under a root directory.
         *
         * @param dirScan The root diretory to scan.
         * @return itself
         * @throws Exception Forwards exceptions
         */
        public BundleBuilder from( String dirScan ) throws Exception {
            this.bundle.loadEntryPathsFromDirScan( dirScan );
            return start();
        }

        /**
         * Provides bundle content by scanning files under a root directory using a File object to specify root.
         *
         * @param dirScan The root to start scanning at.
         * @return itself
         * @throws Exception Forwards exceptions
         */
        public BundleBuilder from( File dirScan ) throws Exception {
            this.bundle.loadEntryPathsFromDirScan( dirScan );
            return start();
        }

        /**
         * Provides bundle content by providing content paths as an array.
         *
         * @param paths The paths to provide.
         * @return itself
         * @throws Exception Forwards exceptions
         */
        public BundleBuilder using( String[] paths ) throws Exception {
            for ( String path : paths ) {
/*
                if ( !path.startsWith( "/" ) ) {
                    path = "/" + path;
                }
*/
                this.bundle.addEntryPath( path );
            }
            return start();
        }

        /**
         * Terminates this builder and returns a BundleContext representing the result.
         */
        @SuppressWarnings( "unused" )
        public BundleContext as_context() {
            return this.bundle.getBundleContext();
        }

        /**
         * Terminates this builder and returns a Bundle representing the result.
         */
        @SuppressWarnings( "unused" )
        public Bundle as_bundle() {
            return this.bundle;
        }

        /**
         * This is saved internally, and on OSGiServiceTestTools.shutdown() this is called for all saved instances.
         */
        public void shutdown() {
            try {
                this.activator.stop( this.bundle.getBundleContext() );
            } catch ( Exception e ) {
                e.printStackTrace( System.err );
            }

            bundleEvent( bundle, BundleEvent.STOPPED );

            removeBundle( this.bundle );
        }
    }
}
