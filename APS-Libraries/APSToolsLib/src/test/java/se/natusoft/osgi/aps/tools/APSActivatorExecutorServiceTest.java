package se.natusoft.osgi.aps.tools;

import org.junit.Test;
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools;
import se.natusoft.osgi.aps.tools.services.TestService;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class APSActivatorExecutorServiceTest extends OSGIServiceTestTools {


    @Test
    public void testExecutorService() throws Throwable {

        deploy("exec-svc-bundle").with(new APSActivator()).using(new String[]
                {"/se/natusoft/osgi/aps/tools/services/APSActivatorExecutorServiceTestSvc.class"});

        try {
            with_new_bundle("test-verify-bundle", bundleContext -> {

                APSServiceTracker<TestService> tsTracker =
                        new APSServiceTracker<>(bundleContext, TestService.class, "20 seconds");
                tsTracker.start();

                TestService svc = tsTracker.allocateService();

                    assertEquals("OK", svc.getServiceInstanceInfo());

                tsTracker.releaseService();

                tsTracker.stop(bundleContext);
            });
        }
        finally {
            shutdown();
        }

    }

}
