package se.natusoft.osgi.aps.json;

import org.junit.Test;
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools;
import se.natusoft.osgi.aps.tools.APSServiceTracker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class JSONMapTest extends OSGIServiceTestTools {

    @Test
    public void mapTest() throws Throwable {

        deploy("json-service-bundle").with(new APSJSONServiceActivator()).from("APS-Misc/APSJSONServiceProvider/target/classes");

        try {
            with_new_bundle("map-test-bundle", bundleContext -> {

                APSServiceTracker<APSJSONExtendedService> jsonSvcTracker =
                        new APSServiceTracker<>(bundleContext, APSJSONExtendedService.class, "5 seconds");
                jsonSvcTracker.start();

                APSJSONExtendedService jsonSvc = jsonSvcTracker.getWrappedService();

                Properties props = new Properties();
                props.setProperty("name", "Test props.");
                props.setProperty("desc", "For testing Map and properties to JSONObject.");
                props.setProperty("qaz", "wsx");

                Map<String, Serializable> inMap = new HashMap<>();
                inMap.put("action", "Test");
                inMap.put("data", props);

                JSONObject jsonObject = jsonSvc.createJSONObject();
                jsonObject.fromMap(inMap);

                assertEquals("Test", jsonObject.getValue("action").toString());
                JSONObject propsObject = (JSONObject)jsonObject.getValue("data");
                assertEquals("Test props.", propsObject.getValue("name").toString());
                assertEquals("For testing Map and properties to JSONObject.", propsObject.getValue("desc").toString());
                assertEquals("wsx", propsObject.getValue("qaz").toString());

                jsonSvc.writeJSON(System.out, jsonObject);
                System.out.println();

                Map<String, Object> asMap = jsonObject.toMap();

                assertEquals("Test", asMap.get("action"));
                Map<String, Object> dataMap = (Map<String, Object>)asMap.get("data");

                Properties mProps = new Properties();
                mProps.putAll(dataMap);

                assertEquals("Test props.", mProps.getProperty("name"));
                assertEquals("For testing Map and properties to JSONObject.", mProps.getProperty("desc"));
                assertEquals("wsx", mProps.getProperty("qaz"));

                jsonSvcTracker.stop(bundleContext);
            });
        }
        finally {
            shutdown();
        }

    }
}
