package se.natusoft.osgi.aps.api.core.config;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This represents a JSON configuration using Map & List to represent a JSON Object and JSON Array.
 */
public interface APSConfig extends Map<String, Object> {

    /**
     * Calls the provided handler for each value path in the map.
     *
     * @param pathHandler The handler to call with value paths.
     */
    void withStructPath(Consumer<String> pathHandler);


    /**
     * Returns all struct paths as a List.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    List<String> getStructPaths();

    /**
     * Looks up the value of a specified struct Path.
     *
     * @param structPath The structPath to lookup.
     *
     * @return The value or null.
     */
    Object lookup(String structPath);

    /**
     * This gets called when the configuration is available.
     *
     * @param handler The handler to call when there is actual config data in the map.
     */
    void onConfigReady(Runnable handler);
}
