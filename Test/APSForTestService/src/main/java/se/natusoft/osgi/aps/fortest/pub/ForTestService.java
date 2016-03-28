package se.natusoft.osgi.aps.fortest.pub;

import java.util.Map;

public interface ForTestService {

    String paramsTest(Map<String, String> params);

    String getParams();

    String putParam(String name, String value);

    String deleteParam(String name);
}
