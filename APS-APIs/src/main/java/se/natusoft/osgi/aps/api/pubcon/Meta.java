package se.natusoft.osgi.aps.api.pubcon;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Meta data utility.
 */
public class Meta extends LinkedHashMap<String, String> implements Map<String, String> {

    public Meta add(String key, String value) {
        put(key, value);
        return this;
    }
}
