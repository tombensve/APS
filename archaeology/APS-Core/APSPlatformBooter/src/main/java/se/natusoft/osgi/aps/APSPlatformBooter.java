package se.natusoft.osgi.aps;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class APSPlatformBooter {

    @SuppressWarnings("DuplicatedCode")
    public static void main( String... args ) throws Exception {

        String depsDir = null;
        String bundlesDir = null;
        List<String> bundleOrder = null;

        int param = 0;
        while ( param < args.length ) {
            String arg = args[ param++ ];

            switch ( arg ) {
                case "--dependenciesDir":
                    depsDir = args[ param++ ];
                    break;
                case "--bundlesDir":
                    bundlesDir = args[ param++ ];
                    break;
                case "--order":
                    bundleOrder = new LinkedList<>( Arrays.asList( args[ param++ ].split( "," ) ) );
                    break;
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

        File dependencyDir = new File( depsDir );
        //noinspection ConstantConditions
        for ( File jarFile : dependencyDir.listFiles() ) {
            if ( jarFile.getName().endsWith( ".jar" ) ) {
                jarUrls.add( jarFile.toURI().toURL() );
                System.out.println( "Added dependency jar: " + jarFile.toURI().toURL() );
            }
        }

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
        URLClassLoader bundlesClassLoader = new URLClassLoader( _jarUrls, APSPlatformBooter.class.getClassLoader() );

        Thread.currentThread().setContextClassLoader( bundlesClassLoader );

        Class<?> stage2 = bundlesClassLoader.loadClass( "se.natusoft.osgi.aps.platform.APSPlatformBooterStage2" );

        Method bootMethod = stage2.getMethod( "boot", java.io.File.class, java.util.List.class );

        bootMethod.invoke( null, bundleDir, bundleOrder );
    }
}
