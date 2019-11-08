package se.natusoft.osgi.aps;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class APSPlatformBooter {

    @SuppressWarnings("FieldCanBeLocal")
    private static URLClassLoader dependenciesClassLoader;
    @SuppressWarnings("FieldCanBeLocal")
    private static URLClassLoader bundlesClassLoader;

    @SuppressWarnings("DuplicatedCode")
    public static void main( String... args ) throws Exception {

        String depsDir = null;
        String bundlesDir = null;

        int param = 0;
        while ( param < args.length ) {
            String arg = args[ param++ ];

            if ( arg.equals( "--dependenciesDir" ) ) {
                depsDir = args[ param++ ];
            } else if ( arg.equals( "--bundlesDir" ) ) {
                bundlesDir = args[ param++ ];
            }
        }

        String errMsg = "";
        if ( depsDir == null ) {
            errMsg += "--bundlesDir <dir> must be specified!\n";
        }
        if ( bundlesDir == null ) {
            errMsg += "--dependenciesDir <dir> must be specified!\n";
        }

        if ( errMsg.length() > 0 ) throw new Exception( errMsg );

        ArrayList<URL> jarUrls = new ArrayList<>();

        //noinspection ConstantConditions
        File dependencyDir = new File( depsDir );
        //noinspection ConstantConditions
        for ( File jarFile : dependencyDir.listFiles() ) {
            if ( jarFile.getName().endsWith( ".jar" ) ) {
                jarUrls.add( jarFile.toURI().toURL() );
                System.out.println( "Added dependency jar: " + jarFile.toURI().toURL() );
            }
        }

        // noinspection ConstantConditions
        @SuppressWarnings("DuplicatedCode")
        File bundleDir = new File( bundlesDir );
        //noinspection ConstantConditions,DuplicatedCode
        for ( File jarFile : bundleDir.listFiles() ) {
            if ( jarFile.getName().endsWith( ".jar" ) ) {
                jarUrls.add( jarFile.toURI().toURL() );
                System.out.println( "Added bundle jar: " + jarFile.toURI().toURL() );
            }
        }

        jarUrls.trimToSize();
        URL[] _jarUrls = new URL[ jarUrls.size() ];
        jarUrls.toArray( _jarUrls );
        bundlesClassLoader = new URLClassLoader( _jarUrls, APSPlatformBooter.class.getClassLoader() );

        Thread.currentThread().setContextClassLoader( bundlesClassLoader );

        Class test = bundlesClassLoader.loadClass( "se.natusoft.osgi.aps.activator.APSActivator" );
        System.out.println("" + test);

        Class stage2 = bundlesClassLoader.loadClass( "se.natusoft.osgi.aps.platform.APSPlatformBooterStage2" );

        @SuppressWarnings("unchecked")
        Method bootMethod = stage2.getMethod( "boot", java.io.File.class );

        bootMethod.invoke( null, bundleDir );
    }
}
