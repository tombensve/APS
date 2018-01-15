package se.natusoft.osgi.aps.api.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility for providing Map\<String, String\> properties.
 *
 * Note that the defined constants are only utilities! They are not required in any way. Whatever is put in an APSProperties
 * instance is up to the user. This whole class is just a utility to put a clearer name on a specific usage of
 * Map&lt;String, String&gt;.
 */
@SuppressWarnings("unused")
public interface APSProperties extends Map<String, String> {

    //
    // Constants
    //

    /** Key for a topic. */
    String TOPIC = "topic";

    //
    // Methods
    //

    /**
     * For static initialization with values.
     *
     * @param initial The initial Map to set.
     */
    static APSProperties with(Map<String, String> initial) {
        APSProperties properties = new APSProperties.Provider();
        properties.putAll(initial);
        return properties;
    }

    /**
     * @return A new empty APSMeta object.
     */
    static APSProperties empty() {
        return new APSProperties.Provider();
    }

    //
    // Default implementation
    //

    /**
     * Provides a default implementation of the APSMeta interface.
     */
    class Provider extends LinkedHashMap<String, String> implements APSProperties {

        public APSProperties add(String key, String value) {
            put(key, value);
            return this;
        }
    }
}

