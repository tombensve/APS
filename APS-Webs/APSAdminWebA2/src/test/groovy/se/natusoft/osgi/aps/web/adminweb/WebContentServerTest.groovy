package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger

import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@TypeChecked
class WebContentServerTest extends OSGIServiceTestTools {

    private static APSLogger logger = new APSLogger( APSLogger.PROP_LOGGING_FOR, "WebContentServerTest" )

//    public static Consumer.Consumed<Vertx> vertx = null

    @Test
    void testWebContentServer() throws Exception {
        // Most of the unfamiliar constructs here are provided by OSGiServiceTestTools and groovy DSL features.

        println "==============================================================================="
        println " DO NOTE: All the RED colored output comes from Hazelcast! It is not something "
        println " that have failed! Hazelcast have just chosen this color for their log output! "
        println "==============================================================================="

        // NOTE: You can move these 3 deploys in any order, and it will still work!

        // We deploy this service not to test it, but to add one more client to VertxProvider and verify
        // that it can failure more than one. It didn't at first :-).
        deploy 'web-content-server' with new APSActivator() using '/se/natusoft/osgi/aps/web/adminweb/WebContentServer.class'

        deploy 'sockJS-event-bus-bridge' with new APSActivator() using '/se/natusoft/osgi/aps/web/adminweb/SockJSEventBusBridge.class'

        deploy 'aps-vertx-provider' with new APSActivator() from 'se.natusoft.osgi.aps', 'aps-vertx-provider', '1.0.0'

        try {
//            Thread verifyThread = Thread.start {
                logger.info "Waiting 10 seconds for server to come up."
                hold() maxTime 10L unit SECONDS go()

                logger.info "Calling server ..."

                logger.info "no file"
                getAndVerify( "", "index.html" )
                logger.info "index.html"
                getAndVerify( "index.html", "index.html" )
                logger.info "adminweb-bundle.js"
                getAndVerify( "adminweb-bundle.js", "adminweb-bundle.js" )
                logger.info "app/components/apsadminweb/apsadminweb-tpl.html"
                getAndVerify(
                        "app/components/apsadminweb/apsadminweb-tpl.html",
                        "app/components/apsadminweb/apsadminweb-tpl.html"
                )
                try {
                    getAndVerify( "nonexistent", null )
                    throw new Exception( "A FileNotFoundException was expected!" )
                }
                // You don't get an HTTP status from java.net.URL! It throws exceptions instead.
                catch ( FileNotFoundException ignore ) {
                    logger.info "Correctly got a FileNotFoundException for 'nonexistent'!"
                }
//            }
//            synchronized (verifyThread) {
//                verifyThread.wait( 10000 )
//            }
        }
        catch ( Exception e ) {
            logger.error(e.message, e)
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
            URL url = new URL( "http://localhost:9080/apsadminweb/${ serverFile }" )
            String fileFromServer = loadFromStream( url.openStream() )
            if ( localFile != null ) {
                String fileFromLocal =
                        loadFromStream( System.getResourceAsStream( "/webContent/${ localFile }" ) )
                assert fileFromServer == fileFromLocal
                logger.info "${ serverFile } served correctly!"
            }
        }
        catch ( FileNotFoundException fnfe ) {
            throw new FileNotFoundException( "Failed to serve file: ${ serverFile } ! [${ fnfe.message }]" )
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
