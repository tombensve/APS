package se.natusoft.osgi.aps.fortest.service;

import se.natusoft.osgi.aps.fortest.pub.ForTestService;
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@OSGiServiceProvider
public class ForTestServiceProvider implements ForTestService {

    private Map<String, String> params = new HashMap<>();

    /**
     * Tests receiving HTTP GET parameters as a map when declaring one Map<String,String> parameter.
     * I think this only works with the JSONREST protocol.
     *
     * @param params These should contain the HTTP GET request parameters.
     */
    @Override
    public String paramsTest(Map<String, String> params) {
        this.params = params;
        return getParams();
    }

    /**
     * This should be called on a HTTP GET call without any method specified.
     */
    @Override
    public String getParams() {
        StringBuilder sb = new StringBuilder();
        for (String key : this.params.keySet()) {
            String value = this.params.get(key);
            sb.append(key);
            sb.append("=");
            sb.append(value);
            sb.append("; ");
        }

        return sb.toString();
    }

    /**
     * Tests HTTP PUT call without any method specified.
     */
    @Override
    public String putParam(String name, String value) {
        this.params.put(name, value);
        return getParams();
    }

    /**
     * Tests HTTP DELETE without any method specified.
     */
    @Override
    public String deleteParam(String name) {
        this.params.remove(name);
        return getParams();
    }

}
