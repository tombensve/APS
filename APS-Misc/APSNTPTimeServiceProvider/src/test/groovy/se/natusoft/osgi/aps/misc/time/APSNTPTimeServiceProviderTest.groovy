package se.natusoft.osgi.aps.misc.time;

import org.junit.Test
import static org.junit.Assert.*;
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.misc.time.APSTimeService
import se.natusoft.osgi.aps.misc.time.config.NTPConfig
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.tracker.APSServiceTracker

import java.time.Instant

public class APSNTPTimeServiceProviderTest extends OSGIServiceTestTools {

    @Test
    public void testNTPTimeService() throws Exception {
        deploy 'aps-ntp-time-service-provider' with new APSActivator() with {
            NTPConfig ntpConfig =  new NTPConfig()
            ntpConfig.ntpServers = new TestConfigValue(value: "0.pool.ntp.org,1.pool.ntp.org")

            NTPConfig.get = ntpConfig

            ntpConfig
        } from 'APS-Misc/APSNTPTimeServiceProvider/target/classes'

        try {
            with_new_bundle 'test-exec-bundle', { BundleContext context ->

                // If test fails due to not getting a time from NTP server then you might want to increase this wait.
                Thread.sleep(500)

                APSServiceTracker<APSTimeService> timeServiceTracker =
                        new APSServiceTracker<>(context, APSTimeService.class, "10 sec")
                timeServiceTracker.start()

                APSTimeService apsTimeService = timeServiceTracker.allocateService()
                println "Local time: ${Instant.ofEpochMilli(new Date().time)}"
                println "Remote time: ${apsTimeService.time}"

                // There really isn't much we can validate here!
                assertNotNull(apsTimeService.lastTimeUpdate)

                timeServiceTracker.stop()

            }
        }
        finally {
            shutdown()
        }


    }
}
