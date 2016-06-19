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
 *         2016-06-12: Created!
 *         
 */
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
