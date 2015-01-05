package se.natusoft.osgi.aps.tools;

import org.junit.Test;
import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools;
import se.natusoft.osgi.aps.test.tools.TestBundle;
import se.natusoft.osgi.aps.tools.services.SearchCriteriaProviderTestService;
import se.natusoft.osgi.aps.tools.services.SimpleService;
import se.natusoft.osgi.aps.tools.services.TestService;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("PackageAccessibility")
public class APSActivatorServiceLookupTest {

    @Test
    public void searchCriteriaProviderTest() throws Exception {
        OSGIServiceTestTools testTools = new OSGIServiceTestTools();
        TestBundle testBundle = testTools.createBundle("test-bundle");
        testBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/tools/services/ServicesSetupProviderTestService.class",
                "/se/natusoft/osgi/aps/tools/services/SearchCriteriaProviderTestService.class"
        );

        APSActivator activator = new APSActivator();
        activator.start(testBundle.getBundleContext());

        try {
            // There should now be 3 instances of ServicesSetupProviderTestService, and one of SearchCriteriaProviderTestService.
            // All provides TestService, which is why we need to loop through all services until we find the service with the
            // implementation class we want. That service should have one of the the other class instances injected into it
            // as TestService and will forward getServiceInstanceInfo() to it. Since we filtered on "second" in the search
            // criteria we expect the string "second" to be returned.
            for (ServiceReference serviceReference : testBundle.getRegisteredServices()) {
                TestService ts = (TestService)testBundle.getBundleContext().getService(serviceReference);
                if (ts instanceof SearchCriteriaProviderTestService) {
                    assertEquals("Expected to get 'second' back from getServiceInstanceInfo()!", ts.getServiceInstanceInfo(), "second");
                }
                testBundle.getBundleContext().ungetService(serviceReference);
            }
        }
        finally {
            activator.stop(testBundle.getBundleContext());
        }

    }
}
