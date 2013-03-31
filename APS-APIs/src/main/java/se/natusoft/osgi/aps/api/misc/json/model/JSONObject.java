/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.1
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
 *         2012-01-17: Created!
 *         
 */
package se.natusoft.osgi.aps.api.misc.json.model;

import java.util.Set;

/**
 * This class is based on the structure defined on http://www.json.org/.
 *
 * It represents the "object" diagram on the above mentioned web page:
 *
 *                  ________________________________________
 *                 /                                        \
 *     |___ ({) __/_____ (string) ____ (:) ____ (value) _____\___ (}) ____|
 *     |           /                                        \             |
 *                 \__________________ (,) _________________/
 *
 *
 * @see JSONValue
 *
 * @author Tommy Svensson
 */
public interface JSONObject extends JSONValue {
    /**
     * Returns the names of the available child values.
     */
    public Set<JSONString> getValueNames();

    /**
     * Returns the named value.
     *
     * @param name The name of the value to get.
     */
    public JSONValue getValue(JSONString name);

    /**
     * Returns the named value.
     *
     * @param name The name of the value to get.
     */
    public JSONValue getValue(String name);

    /**
     * Adds a value to this JSONObject instance.
     *
     * @param name The name of the value.
     * @param value The value.
     */
    public void addValue(JSONString name, JSONValue value);

    /**
     * Adds a value to this JSONObject instance.
     *
     * @param name The name of the value.
     * @param value The value.
     */
    public void addValue(String name, JSONValue value);

}
