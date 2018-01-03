package se.natusoft.osgi.aps.api.core.config;

import java.util.Map;

/**
 * This represents a JSON configuration using Map & List to represent a JSON Object and JSON Array.
 */
public interface APSConfig extends Map<String, Object> {

    /**
     * This gets called when the configuration is available.
     *
     * @param handler The handler to call when there is actual config data in the map.
     */
    void onConfigReady(Runnable handler);
}
