package se.natusoft.osgi.aps.platform;

import se.natusoft.osgi.aps.activator.APSActivator;
import se.natusoft.osgi.aps.runtime.APSRuntime;

import java.io.File;

/**
 * The APSPlatformBooter maven module contains a class with a main method that
 * creates an URLClassLoader and then adds all jars in dependenciesDir and bundlesDir,
 * and then via reflection creates an instance of this class and calls boot method with
 * the bundleDir as a File object. So at this point everything is on the classpath and
 * this just uses the APSRuntime deploy DSL API to deploy each bundle.
 *
 * In other words this is the entry point for running an APS service/backend.
 *
 * Note: If no bundle creates a daemon thread then the JVM process will die when
 * done! There is a short wait at the end to give threads a little extra time to get
 * going.
 */
public class APSPlatformBooterStage2 {

    @SuppressWarnings("DuplicatedCode")
    public static void boot( File bundleDir) throws Exception {

        APSRuntime runtime = new APSRuntime();

        //noinspection ConstantConditions
        for ( File bundle  : bundleDir.listFiles() ) {
            if (bundle.getName().endsWith( ".jar" )) {
                //System.out.print( "Deploying '" + bundle + "' ... " );
                runtime.deploy( bundle.getName() ).with( new APSActivator() ).fromJar( bundle );
                //System.out.println( "OK!" );
            }
            else {
                System.err.println("Ignoring unknown file: " + bundle);
            }
        }

        System.out.println("\nDeploy thread running! Giving bundles time to start ...\n");
        Thread.sleep( 1000 * 10 );
        System.out.println("\nIf no bundle have started a daemon by now, this process will exit!\n");

    }
}
