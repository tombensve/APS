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
