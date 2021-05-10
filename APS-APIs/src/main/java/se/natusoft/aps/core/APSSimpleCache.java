package se.natusoft.aps.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A very trivial object cache.
 */
public class APSSimpleCache {

    private static Map<Object, Object> CACHE = new LinkedHashMap<>();

    public static void cache(Object key, Object value) {
        CACHE.put(key, value);
    }

    public static void remove(Object key) {
        CACHE.remove( key );
    }

    public static Object lookup(Object key) {
        return CACHE.get(key);
    }
}
