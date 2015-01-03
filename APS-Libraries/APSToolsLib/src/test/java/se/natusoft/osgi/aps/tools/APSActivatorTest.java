package se.natusoft.osgi.aps.tools;

import org.junit.Test;
import static org.junit.Assert.*;

import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools;
import se.natusoft.osgi.aps.test.tools.TestBundle;
import se.natusoft.osgi.aps.tools.services.TestService;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("PackageAccessibility")
public class APSActivatorTest {

    @Test
    public void simpleTest() throws Exception {
        OSGIServiceTestTools testTools = new OSGIServiceTestTools();
        TestBundle testBundle = testTools.createBundle("test-bundle");
        testBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/tools/services/OSGiService.class",
                "/se/natusoft/osgi/aps/tools/services/TestService.class"
        );

        APSActivator activator = new APSActivator();
        activator.start(testBundle.getBundleContext());

        try {
            assertTrue("There should only be one registered service!", testBundle.getRegisteredServices().length == 1);
            Object service = testBundle.getBundleContext().getService(testBundle.getRegisteredServices()[0]);
            assertNotNull("Service should not be null!", service);
            System.out.println("Service: " + service);
            assertEquals("This service should be of type OSGiService!", service.getClass(),
                    se.natusoft.osgi.aps.tools.services.OSGiService.class);
        }
        finally {
            activator.stop(testBundle.getBundleContext());
        }
    }

    @Test
    public void serviceSetupProviderTest() throws Exception {
        OSGIServiceTestTools testTools = new OSGIServiceTestTools();
        TestBundle testBundle = testTools.createBundle("test-bundle");
        testBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/tools/services/ServicesSetupProviderTestService.class"
        );

        APSActivator activator = new APSActivator();
        activator.start(testBundle.getBundleContext());

        try {
            assertTrue("There should be 3 registered service instances!", testBundle.getRegisteredServices().length == 3);

            List<String> validValues = new LinkedList<>();
            validValues.add("first");
            validValues.add("second");
            validValues.add("third");

            TestService service = (TestService)testBundle.getBundleContext().getService(testBundle.getRegisteredServices()[0]);
            assertNotNull("Service should not be null!", service);
            System.out.println(service.getServiceInstanceInfo());
            validValues.remove(service.getServiceInstanceInfo());

            service = (TestService)testBundle.getBundleContext().getService(testBundle.getRegisteredServices()[1]);
            assertNotNull("Service should not be null!", service);
            System.out.println(service.getServiceInstanceInfo());
            validValues.remove(service.getServiceInstanceInfo());

            service = (TestService)testBundle.getBundleContext().getService(testBundle.getRegisteredServices()[2]);
            assertNotNull("Service should not be null!", service);
            System.out.println(service.getServiceInstanceInfo());
            validValues.remove(service.getServiceInstanceInfo());

            assertTrue("Not all 'validValues' where checked off!", validValues.size() == 0);
        }
        finally {
            activator.stop(testBundle.getBundleContext());
        }

    }
}
