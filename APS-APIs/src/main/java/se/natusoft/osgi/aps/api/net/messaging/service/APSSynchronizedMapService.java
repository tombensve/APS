package se.natusoft.osgi.aps.api.net.messaging.service;

import java.util.List;
import java.util.Map;

/**
 * This service makes a synchronized named map available.
 */
public interface APSSynchronizedMapService {

    /**
     * Returns a named map into which objects can be stored with a name.
     *
     * If the named map does not exists it should be created and an empty map be returned.
     */
    Map<String, String> getNamedMap(String name);

    /**
     * Returns the available names.
     */
    List<String> getAvailableNamedMaps();

    /**
     * Returns true if the implementation supports persistence for stored objects. If false is returned
     * objects are in memory only.
     */
    boolean supportsNamedMapPersistence();

}
