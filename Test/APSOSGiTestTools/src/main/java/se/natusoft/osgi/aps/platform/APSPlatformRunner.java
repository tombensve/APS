package se.natusoft.osgi.aps.platform;

import se.natusoft.osgi.aps.activator.APSActivator;
import se.natusoft.osgi.aps.test.tools.APSOSGIServiceTestTools;

import java.io.File;

public class APSPlatformRunner extends APSOSGIServiceTestTools {

    public static void main( String... args ) throws Exception {
        if ( args.length > 2 ) throw new Exception( "Bad arguments! Allowed: --bundleDir <dir>" );

        APSPlatformRunner inst = new APSPlatformRunner();

        File bundleDir = null;
        if ( args[ 0 ].equals( "--bundleDir" ) ) {
            if ( args.length < 2 ) throw new Exception( "--bundleDir must have an argument!" );

            bundleDir = new File( args[ 1 ] );
        } else {

            bundleDir = new File( "." );
        }

        // bundleDir will always be set here, so IDEA warning is wrong!
        //noinspection ConstantConditions
        for ( File bundle : bundleDir.listFiles() ) {

            if (bundle.getName().endsWith( ".jar" )) {
                inst.deploy( bundle.getName() ).with( new APSActivator() ).fromJar( bundle );
            }
            else {
                System.err.println("Ignoring unknown file: " + bundle);
            }
        }

        System.out.println("\nDeploy thread running! Giving bundles time to start ...\n");
        Thread.sleep( 1000 * 20 );
        System.out.println("\nIf no bundle have started a daemon by now, this process will exit!\n");
    }
}
