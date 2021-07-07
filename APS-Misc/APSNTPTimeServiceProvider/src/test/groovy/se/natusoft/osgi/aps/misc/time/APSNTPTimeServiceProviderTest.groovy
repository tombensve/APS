package se.natusoft.osgi.aps.misc.time

import org.junit.Test
import org.osgi.framework.BundleContext
import se.natusoft.aps.activator.APSActivator
import se.natusoft.aps.api.misc.time.APSTimeService
import se.natusoft.osgi.aps.runtime.APSRuntime
import se.natusoft.aps.tracker.APSServiceTracker
import se.natusoft.aps.util.APSLogger

import java.time.Instant
import java.util.concurrent.TimeUnit

class APSNTPTimeServiceProviderTest extends APSRuntime {

    private APSLogger logger = new APSLogger()

    @Test
    void testNTPTimeService() throws Exception {

        // For now we have to make sure we are running Vert.x clustered. This will not work with
        // unclustered vertx instance.
        String vertxClustered = System.getProperty( "aps.vertx.clustered" )
        if (vertxClustered != null && vertxClustered == "false") {
            return
        }

        deployConfigAndVertxPlusDeps(  )

        // The actual code to test

        deploy 'aps-ntp-time-service-provider' with new APSActivator() from 'APS-Misc/APSNTPTimeServiceProvider/target/classes'

        try {
            with_new_bundle 'test-exec-bundle', { BundleContext context ->

                APSServiceTracker<APSTimeService> timeServiceTracker =
                        new APSServiceTracker<>(context, APSTimeService.class, "2 sec")
                timeServiceTracker.start()

                // To make sure deployment is done and that tracker has picked up service.
                hold() maxTime 2 unit TimeUnit.SECONDS

                APSTimeService apsTimeService = timeServiceTracker.allocateService()
                println "Local time: ${Instant.ofEpochMilli(new Date().time)}"
                println "Remote time: ${apsTimeService.time}"

                // There really isn't much we can validate here!
                assert apsTimeService.time != null
                assert apsTimeService.lastTimeUpdate != null

                timeServiceTracker.stop()

            }
        }
        finally {
            hold() maxTime 2 unit TimeUnit.SECONDS go()

            shutdown()
        }


    }
}
