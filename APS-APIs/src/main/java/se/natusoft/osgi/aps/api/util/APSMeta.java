package se.natusoft.osgi.aps.api.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Meta data utility.
 *
 * Note that the defined constants are only utilities! They are not required in any way. Whatever is put in an APSMeta
 * instance is up to the user. This whole class is just a utility to put a clearer name on a specific usage of
 * Map&lt;String, String&gt;.
 */
public interface APSMeta extends Map<String, String> {

    //
    // Constants
    //

    /** This is a key for a status entry. */
    String STATUS = "status";

    /** This is a possible status value. */
    String STATUS_PUBLISHED = "published";

    /** This is a possible status values */
    String STATUS_UPDATED = "updated";

    /** This is a possible status value. */
    String STATUS_REVOKED = "revoked";

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
    static APSMeta with(Map<String, String> initial) {
        APSMeta meta = new APSMeta.Provider();
        meta.putAll(initial);
        return meta;
    }

    /**
     * @return A new empty APSMeta object.
     */
    static APSMeta empty() {
        return new APSMeta.Provider();
    }

    //
    // Default implementation
    //

    /**
     * Provides a default implementation of the APSMeta interface.
     */
    class Provider extends LinkedHashMap<String, String> implements APSMeta {

        public APSMeta add(String key, String value) {
            put(key, value);
            return this;
        }
    }
}

