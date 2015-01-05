package se.natusoft.osgi.aps.tools;

import org.junit.Test;
import static org.junit.Assert.*;

import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools;
import se.natusoft.osgi.aps.test.tools.TestBundle;
import se.natusoft.osgi.aps.tools.services.SearchCriteriaProviderTestService;
import se.natusoft.osgi.aps.tools.services.SimpleService;
import se.natusoft.osgi.aps.tools.services.TestService;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("PackageAccessibility")
public class APSActivatorServiceRegTest {

    @Test
    public void simpleTest() throws Exception {
        OSGIServiceTestTools testTools = new OSGIServiceTestTools();
        TestBundle testBundle = testTools.createBundle("test-bundle");
        testBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/tools/services/SimpleService.class",
                "/se/natusoft/osgi/aps/tools/services/TestService.class"
        );

        APSActivator activator = new APSActivator();
        activator.start(testBundle.getBundleContext());

        try {
            assertTrue("There should only be one registered service!", testBundle.getRegisteredServices().length == 1);
            TestService service = (TestService)testBundle.getBundleContext().getService(testBundle.getRegisteredServices()[0]);
            assertNotNull("The managed logger field was not injected!", ((SimpleService)service).getLogger());
            assertNotNull("Service should not be null!", service);
            System.out.println("Service: " + service);
            assertEquals("This service should be of type OSGiService!", service.getClass(),
                    SimpleService.class);
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
            assertEquals("'instance' property does not match!", service.getServiceInstanceInfo(),
                    testBundle.getRegisteredServices()[0].getProperty("instance"));
            validValues.remove(service.getServiceInstanceInfo());
            testBundle.getBundleContext().ungetService(testBundle.getRegisteredServices()[0]);

            service = (TestService)testBundle.getBundleContext().getService(testBundle.getRegisteredServices()[1]);
            assertNotNull("Service should not be null!", service);
            System.out.println(service.getServiceInstanceInfo());
            assertEquals("'instance' property does not match!", service.getServiceInstanceInfo(),
                    testBundle.getRegisteredServices()[1].getProperty("instance"));
            validValues.remove(service.getServiceInstanceInfo());
            testBundle.getBundleContext().ungetService(testBundle.getRegisteredServices()[1]);

            service = (TestService)testBundle.getBundleContext().getService(testBundle.getRegisteredServices()[2]);
            assertNotNull("Service should not be null!", service);
            System.out.println(service.getServiceInstanceInfo());
            assertEquals("'instance' property does not match!", service.getServiceInstanceInfo(),
                    testBundle.getRegisteredServices()[2].getProperty("instance"));
            validValues.remove(service.getServiceInstanceInfo());
            testBundle.getBundleContext().ungetService(testBundle.getRegisteredServices()[2]);

            assertTrue("Not all 'validValues' where checked off!", validValues.size() == 0);
        }
        finally {
            activator.stop(testBundle.getBundleContext());
        }
    }
}
