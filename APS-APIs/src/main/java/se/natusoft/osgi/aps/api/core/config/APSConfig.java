package se.natusoft.osgi.aps.api.core.config;

import se.natusoft.osgi.aps.model.APSHandler;

import java.util.Map;

/**
 * This represents a JSON configuration using Map & List to represent a JSON Object and JSON Array.
 *
 * "Struct path"s are paths that are separated by dots ('.') where the first part is a key in a Map
 * and the part after the dot is a key in the object returned for the first key, and so on. For List
 * objects and index in the form of '.[i].'. Note that the index is a path "part" in itself.
 *
 * Maps are used to represent JSON structure and the "Struct path"s are just a way to provide a
 * reference to values within the structure.
 *
 * There is a "StructMap" class in aps-core-lib that can be used to handle these "struct path"s
 * in implementations. Also if you make a "struct path" reference to a Map and not a end value
 * then this result can be wrapped with a StructMap and from there be accessed just like the
 * lookup path, but relative to this map.
 */
@SuppressWarnings("unused")
public interface APSConfig extends Map<String, Object> {

    /** Start of message service target address to subscribe to for configuration. Actual config id is added to this. */
    String APS_CONFIG_AVAILABLE_ADDRESS_START = "aps.config.available.";

    /** Messages on this address informs that cluster conf have been updated.  */
    String CLUSTER_CONFIG_REFRESH_ADDRESS = "aps.config.refresh";

    /**
     * Calls the provided handler for each value path in the map.
     *
     * This provides paths to all values available in the structure.
     *
     * @param pathHandler The handler to call with value paths.
     */
    void withStructPath(APSHandler<String> pathHandler);

    /**
     * Looks up the value of a specified struct Path. Null or blank will return the whole root config Map.
     *
     * @param structPath The structPath to lookup.
     * @param valueHandler The handler receiving the looked up value.
     */
    void lookup(String structPath, APSHandler<Object> valueHandler);

    /**
     * provides a new value.
     *
     * @param structPath The value path.
     * @param value The value.
     */
    void provide( String structPath, Object value );
}
