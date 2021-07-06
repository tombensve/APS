/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2018-05-26: Created!
 *
 */
package se.natusoft.aps.api.core.store;

import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;

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
