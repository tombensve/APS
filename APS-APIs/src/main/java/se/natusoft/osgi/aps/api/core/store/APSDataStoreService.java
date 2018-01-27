package se.natusoft.osgi.aps.api.core.store;

import se.natusoft.osgi.aps.api.reactive.APSHandler;
import se.natusoft.osgi.aps.api.reactive.APSResult;

/**
 * This defines a data storage service. It says nothing about persistence nor about networking / cluster.
 *
 * It is just a service that stores something under a name, and allows retrieval at least during a running
 * session. Use appropriate properties when publishing implementations so that clients can lookup the correct
 * variant. Use the APS.Service.PersistenceScope = APS.ServicePersistenceScope.None/Session/Clustered/
 * ClusteredSession/Permanent when publishing and looking up service instances.
 *
 * Depending on the underlying implementation the key might need to be a structured key with parts
 * separated by a '.'. For example if the first key returns a map supply a "map.value-in-map" key.
 */
public interface APSDataStoreService {

    /**
     * Stores a value in the store.
     *
     * @param key The value key.
     * @param value The value to store.
     * @param result A result handler that gets to know if the store succeeded or failed. Do note
     *               that there are no generics declaration on APSResult here. This is because
     *               there is no object returned, only success or failure. I first tried 'Void'
     *               and 'void' as generic type to indicate no object, but that turned out to
     *               be a really, really super bad idea :-).
     */
    void store(String key, Object value, APSHandler<APSResult> result);

    /**
     * Retrieves a value from the store.
     *
     * @param key The key for the value.
     * @param handler The handler to receive the value.
     */
    void retrieve(String key, APSHandler<APSResult<Object>> handler);

    /**
     * Removes a value from the store.
     *
     * @param key The key for the value.
     * @param result The handler to receive the value.
     */
    void remove( String key, APSHandler<APSResult<Object>> result );

}
