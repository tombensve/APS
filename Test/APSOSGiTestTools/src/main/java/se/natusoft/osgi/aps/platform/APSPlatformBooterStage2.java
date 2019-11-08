package se.natusoft.osgi.aps.platform;

import se.natusoft.osgi.aps.activator.APSActivator;
import se.natusoft.osgi.aps.test.tools.APSOSGIServiceTestTools;

import java.io.File;

public class APSPlatformBooterStage2 {

    @SuppressWarnings("DuplicatedCode")
    public static void boot( File bundleDir) throws Exception {

        APSOSGIServiceTestTools booter = new APSOSGIServiceTestTools();

        //noinspection ConstantConditions
        for ( File bundle  : bundleDir.listFiles() ) {
            if (bundle.getName().endsWith( ".jar" )) {
                //System.out.print( "Deploying '" + bundle + "' ... " );
                booter.deploy( bundle.getName() ).with( new APSActivator() ).fromJar( bundle );
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
