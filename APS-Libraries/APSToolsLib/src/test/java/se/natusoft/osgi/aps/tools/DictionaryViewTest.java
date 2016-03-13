package se.natusoft.osgi.aps.tools;

import org.junit.Test;
import static org.junit.Assert.*;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescriptionProvider;
import se.natusoft.osgi.aps.tools.util.DictionaryView;

/**
 * Test of DictionaryView.
 */
public class DictionaryViewTest {

    @Test
    public void testDictionaryView() throws Exception {
        ServiceDescriptionProvider sd = new ServiceDescriptionProvider();
        sd.setVersion("1.2.3");
        sd.setServiceId("test-service");
        sd.setServiceHost("localhost");
        sd.setServicePort(6789);
        sd.setNetworkProtocol("TCP");
        sd.setServiceProtocol("HTTP");
        sd.setClassifier("public-utilities");
        sd.setContentType("application/json");
        sd.setDescription("A dummy test service descriptor for a fictional service.");

        DictionaryView dv = new DictionaryView(ServiceDescription.class, sd);

        assertEquals("1.2.3", dv.get("version"));
        assertEquals("test-service", dv.get("serviceId"));
        assertEquals("localhost", dv.get("serviceHost"));
        assertEquals("6789", dv.get("servicePort"));
        assertEquals("TCP", dv.get("networkProtocol"));
        assertEquals("HTTP", dv.get("serviceProtocol"));
        assertEquals("public-utilities", dv.get("classifier"));
        assertEquals("application/json", dv.get("contentType"));
        assertEquals("A dummy test service descriptor for a fictional service.", dv.get("description"));

        // Any property not part of the bean should always return null.
        assertNull(dv.get("qwerty"));

        Filter filter = FrameworkUtil.createFilter("(&(&(version=1.2.3)(servicePort=6789))(!(networkProtocol=UDP)))");
        assertTrue(filter.match(dv));
        filter = FrameworkUtil.createFilter("(&(&(version=1.2.3)(servicePort=6789))(!(networkProtocol=TCP)))");
        assertFalse(filter.match(dv));
    }
}
