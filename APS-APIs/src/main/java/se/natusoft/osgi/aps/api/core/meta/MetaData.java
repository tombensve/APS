package se.natusoft.osgi.aps.api.core.meta;

import java.util.Set;

/**
 * This represents a specific set of meta data for one specific owner.
 *
 * So why not just use java.util.Map interface ? Map contains far to much stuff not needed here.
 * I want this to be a very primitive and simple interface (I'm after all planning to implement it :-)).
 */
public interface MetaData {

    /**
     * Returns the value of a key.
     *
     * @param key The key to get value for.
     */
    String get(String key);

    /**
     * Set/update a key with a new value.
     *
     * @param key The key to set or update.
     * @param value The new value to provide.
     */
    void put(String key, String value);

    /**
     * Remove a value.
     *
     * @param key The value to remove:s key.
     */
    String remove(String key);

    /**
     * Returns a list of all keys.
     */
    Set<String> getKeys();
}
