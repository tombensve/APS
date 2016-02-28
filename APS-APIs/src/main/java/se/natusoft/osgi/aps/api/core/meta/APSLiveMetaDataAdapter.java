package se.natusoft.osgi.aps.api.core.meta;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Help class for providing live meta data. All that is left to implement is the get() method, and
 * providing the constructor with the valid keys provided.
 */
@SuppressWarnings("unused")
public abstract class APSLiveMetaDataAdapter implements MetaData {

    //
    // Private Members
    //

    private Set<String> keys = new LinkedHashSet<>();

    //
    // Constructors
    //

    /**
     * Creates a new APSLiveMetaDataAdapter.
     *
     * @param keys The keys provided by this MetaData.
     */
    public APSLiveMetaDataAdapter(String... keys) {
        Collections.addAll(this.keys, keys);
    }

    //
    // Methods
    //

    /**
     * Set/update a key with a new value.
     *
     * @param key   The key to set or update.
     * @param value The new value to provide.
     */
    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("This MetaData instance does not allow updates!");
    }

    /**
     * Remove a value.
     *
     * @param key The value to remove:s key.
     */
    @Override
    public String remove(String key) {
        throw new UnsupportedOperationException("This MetaData instance does not allow updates!");
    }

    /**
     * Returns a list of all keys.
     */
    @Override
    public Set<String> getKeys() {
        return this.keys;
    }
}
