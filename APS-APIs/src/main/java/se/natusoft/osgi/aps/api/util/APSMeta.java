package se.natusoft.osgi.aps.api.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Meta data utility.
 */
public interface APSMeta extends Map<String, String> {

    //
    // Constants
    //

    String STATE = "state";
    String OBJECT_PUBLISHED_STATUS = "object-published";
    String OBJECT_UPDATED_STATUS = "object-updated";
    String OBJECT_REVOKED_STATUS = "object-revoked";

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

