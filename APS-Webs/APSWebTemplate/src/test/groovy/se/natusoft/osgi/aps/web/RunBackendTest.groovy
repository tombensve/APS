package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.runtime.APSRuntime
import se.natusoft.osgi.aps.util.APSLogger

import static java.util.concurrent.TimeUnit.HOURS
import static java.util.concurrent.TimeUnit.SECONDS

/**
 * This is basically the same as deploying APSWebManager in APSRuntime along with dependencies.
 *
 * Note that this test is only run if the "run-backend" profile is active! Also note that run
 * never exits!! It should thereby never be run in any build. This isn't really a test but a
 * simple quick way to get the webb upp and running for playing with it.
 */
@CompileStatic
@TypeChecked
class RunBackendTest extends APSRuntime {

    private static APSLogger logger = new APSLogger( APSLogger.PROP_LOGGING_FOR, "WebContentServerTest" )

    private void deploy() {
        deployConfigAndVertxPlusDeps()

        hold() maxTime 2L unit SECONDS go()

        deploy 'aps-web-template' with new APSActivator() from 'APS-Webs/APSWebTemplate/target/classes'

        // Unfortunately we have to wait a while here for the services to completely start up.
        // If you build on a really slow computer, this might not be enough.
        hold() maxTime 3L unit SECONDS go()
    }

    /*
     * This will start the server and let it run for 24 hours. So you will have to kill the process
     * when done. This is for testing that the who boots by opening it in a web browser. Don't expect
     * much of functionality since there is no backend listening to testMessages.
     *
     * You must first remove the comment from @Test. Don't remember to put it back when done. You must also
     * comment out the other test to not run that.
     */

    @Test
    void runBackend() throws Exception {
        deploy(  )
        println "SERVER RUNNING FOR NEXT 24 HOURS ..."
        hold(  ) maxTime 24L unit HOURS go()
    }
}
