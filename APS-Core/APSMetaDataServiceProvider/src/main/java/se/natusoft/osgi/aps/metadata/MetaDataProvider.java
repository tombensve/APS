package se.natusoft.osgi.aps.metadata;

import se.natusoft.osgi.aps.api.core.meta.MetaData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides an implementation of MetaData backed by a synchronized HashMap.
 */
public class MetaDataProvider implements MetaData {

    //
    // Private Members
    //

    /** Our data store. */
    private Map<String, String> data = Collections.synchronizedMap(new HashMap<>());

    //
    // Methods
    //

    /**
     * Returns the value of a key.
     *
     * @param key The key to get value for.
     */
    @Override
    public String get(String key) {
        return this.data.get(key);
    }

    /**
     * Set/update a key with a new value.
     *
     * @param key   The key to set or update.
     * @param value The new value to provide.
     */
    @Override
    public void put(String key, String value) {
        this.data.put(key, value);
    }

    /**
     * Remove a value.
     *
     * @param key The value to remove:s key.
     */
    @Override
    public String remove(String key) {
        return this.data.remove(key);
    }

    /**
     * Returns a list of all keys.
     */
    @Override
    public Set<String> getKeys() {
        return this.data.keySet();
    }
}
