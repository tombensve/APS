package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.util.APSLogger

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.MINUTES
import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@TypeChecked
class APSWebBooterTest extends OSGIServiceTestTools {

    private static APSLogger logger = new APSLogger( APSLogger.PROP_LOGGING_FOR, "WebContentServerTest" )

//    public static Consumer.Consumed<Vertx> vertx = null

    @Test
    void testWebContentServer() throws Exception {
        // Most of the unfamiliar constructs here are provided by OSGiServiceTestTools and groovy DSL features.

        println "==============================================================================="
        println " DO NOTE: that Hazelcast is used under the surface by Vert.x. Hazelcasts log   "
        println " output are done in red. This is not an error.                                 "
        println "==============================================================================="

        // NOTE: You can move these 3 deploys in any order, and it will still work!

        deployConfigAndVertxPlusDeps()

        hold() maxTime 2L unit SECONDS go()

        deploy 'aps-web-manager' with new APSActivator() from 'APS-Webs/APSAdminWebA2/target/classes'

        // Unfortunately we have to wait a while here for the services to completely start up.
        // If you build on a really slow computer, this might not be enough.
        hold() maxTime 3L unit SECONDS go()

        try {

            logger.info "Calling server ..."

            logger.info "no file"
            getAndVerify( "", "index.html" )

            logger.info "index.html"
            getAndVerify( "index.html", "index.html" )

            logger.info "service-worker.js"
            getAndVerify( "service-worker.js", "service-worker.js" )

            try {
                getAndVerify( "nonexistent", null )
                throw new Exception( "A FileNotFoundException was expected!" )
            }
            // You don't get an HTTP status from java.net.URL! It throws exceptions instead.
            catch ( FileNotFoundException ignore ) {
                logger.info "Correctly got a FileNotFoundException for 'nonexistent'!"
            }
        }
        catch ( Exception e ) {
            logger.error( e.message, e )
            shutdown()
            throw e
        }
        finally {
            shutdown()
            hold() maxTime 1 unit SECONDS go() // Give Vertx time to shut down.
        }
    }

    private static void getAndVerify( String serverFile, String localFile ) throws Exception {
        try {
            URL url = new URL( "http://localhost:8880/aps/${ serverFile }" )
            logger.debug( "URL: '${ url }'" )
            String fileFromServer = loadFromStream( url.openStream() )
            if ( localFile != null ) {
                String fileFromLocal =
                        loadFromStream( System.getResourceAsStream( "/webContent/${ localFile }" ) )

                logger.info( "server: ${ fileFromServer }" )
                logger.info( "local: ${ fileFromLocal }" )

                assert fileFromServer == fileFromLocal
                logger.info "${ serverFile } served correctly!"
            }
        }
        catch ( FileNotFoundException fnfe ) {
            //fnfe.printStackTrace( System.err )
            throw new FileNotFoundException( "Failed to serve file! serverFile:'${ serverFile }', localFile:'${ localFile }' ! [${ fnfe.message }]" )
        }
    }

    private static String loadFromStream( InputStream readStream ) {
        StringBuilder sb = new StringBuilder()
        BufferedReader reader = new BufferedReader( new InputStreamReader( readStream ) )

        String line = reader.readLine()
        while ( line != null ) {
            sb.append( line )
            sb.append( '\n' )
            line = reader.readLine()
        }

        reader.close()

        return sb.toString()
    }
}
