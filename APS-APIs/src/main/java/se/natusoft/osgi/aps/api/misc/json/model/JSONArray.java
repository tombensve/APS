/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.3
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

import java.util.List;

/**
 * This class is based on the structure defined on http://www.json.org/.
 *
 * This represents the "array" diagram on the above mentioned web page:
 *
 *                   _______________________
 *                  /                       \
 *                  |                       |
 *     |_____ ([) __/_______ (value) _______\__ (]) _____|
 *     |              /                   \              |
 *                    |                   |
 *                    \_______ (,) _______/
 *
 * @author Tommy Svensson
 */
public interface JSONArray extends JSONValue {

    /**
     * Adds a value to the array.
     *
     * @param value The value to add.
     */
    public void addValue(JSONValue value);

    /**
     * Returns the array values as a List.
     */
    public List<JSONValue> getAsList();

    /**
     * Returns the array values as a list of a specific type.
     *
     * @param type The class of the type to return values as a list of.
     * @param <T> One of the JSONValue subclasses.
     * @return A list of specified type if type is the same as in the list.
     */
    public <T extends JSONValue> List<T> getAsList(Class<T> type);
}
