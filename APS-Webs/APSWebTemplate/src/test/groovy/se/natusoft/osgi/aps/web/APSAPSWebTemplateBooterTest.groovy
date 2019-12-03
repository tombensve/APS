package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.runtime.APSRuntime
import se.natusoft.osgi.aps.util.APSLogger

import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@TypeChecked
class APSAPSWebTemplateBooterTest extends APSRuntime {

    private static APSLogger logger = new APSLogger( APSLogger.PROP_LOGGING_FOR, "WebContentServerTest" )

    private void deploy() {
        deployConfigAndVertxPlusDeps()

        hold() maxTime 2L unit SECONDS go()

        deploy 'aps-web-manager' with new APSActivator() from 'APS-Webs/APSWebTemplate/target/classes'

        // Unfortunately we have to wait a while here for the services to completely start up.
        // If you build on a really slow computer, this might not be enough.
        hold() maxTime 3L unit SECONDS go()
    }

    @Test
    void testWebContentServer() throws Exception {
        // Most of the unfamiliar constructs here are provided by OSGiServiceTestTools and groovy DSL features.

        println "==============================================================================="
        println " DO NOTE: that Hazelcast is used under the surface by Vert.x. Hazelcasts log   "
        println " output are done in red. This is not an error.                                 "
        println "==============================================================================="

        deploy()

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
            hold() maxTime 1 unit SECONDS go() // Give Vert.x time to shut down.
        }
    }

    private static void getAndVerify( String serverFile, String localFile ) throws Exception {
        // Since System.getResourceAsStream() no longer works like before we need to resolve the path to
        // our maven project to locate the source files the http running at localhost:8880 now serves
        // to compare then with what is downloaded. We do that by finding the path of our test class!
        // I really wish there were a better way of doing this!! Relative to CWD will produce different
        // result depending on where build is started. THIS REALLY SUCKS!
        File projRoot = new File(
                APSAPSWebTemplateBooterTest.class.getResource(
                        "APSAPSWebTemplateBooterTest.class"
                ).toString().substring( 5 )
        ).parentFile.parentFile.parentFile.parentFile.parentFile.parentFile.parentFile.parentFile

        logger.debug( "##################### ${ projRoot } #########################" )

        logger.debug( "CWD: ${ new File( "." ).absolutePath }" )
        try {
            URL url = new URL( "http://localhost:8880/apsweb/${ serverFile }" )
            logger.debug( "URL: '${ url }'" )
            String fileFromServer = loadFromStream( url.openStream() )
            if ( localFile != null ) {
                String localPath = "${ projRoot }/src/main/resources/webContent/${ localFile }"

                String fileFromLocal =
                        loadFromStream( new FileInputStream( localPath ) )

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
