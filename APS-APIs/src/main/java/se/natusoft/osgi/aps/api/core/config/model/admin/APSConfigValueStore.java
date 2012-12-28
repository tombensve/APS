/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.0
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-02-17: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.config.model.admin;

import java.util.Set;

/**
 * The actual configuration values are stored in an implementation of this.
 */
public interface APSConfigValueStore {

    /**
     * Returns a configuration value by its key.
     *
     * @param key The key of the configuration value to get.
     *
     * @return The configuration value for the key or null if key has not value.
     */
    String getConfigValue(String key);

    /**
     * Sets a configuration value for a key.
     *
     * @param key The key to set the configuration value for.
     * @param value The value to set.
     */
    void setConfigValue(String key, String value);

    /**
     * Removes a configuration value by this key.
     *
     * @param key The key to remove.
     *
     * @return The removed value.
     */
    String removeConfigValue(String key);

    /**
     * @return All configuration keys stored.
     */
    Set<String> getKeys();

    /**
     * Replaces the values of this configuration box with the values of the specified configuration values box.
     *
     * @param configurationValues The configuration values to take new values from.
     */
    void replaceWithValuesFrom(APSConfigValueStore configurationValues);
}
