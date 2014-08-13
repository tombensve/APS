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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-01-27: Created!
 *         
 */
package se.natusoft.osgi.aps.api.misc.json.model;

/**
 * This provides factory methods for creating _JSONValue_ objects.
 */
public interface JSONValueFactory {

    /**
     * Creates a new instance of a _JSONString_ implementation.
     *
     * @param value The string value.
     *
     * @return A new JSONString.
     */
    public JSONString createJSONString(String value);

    /**
     * Creates a new instance of a _JSONObject_ implementation.
     *
     * @return A new JSONObject.
     */
    public JSONObject createJSONObject();

    /**
     * Creates a new instance of a _JSONNumber_ implementation.
     *
     * @param number The numeric value of the number.
     *
     * @return A new JSONNumber.
     */
    public JSONNumber createJSONNumber(Number number);

    /**
     * Creates a new instance of a _JSONNull_ implementation.
     *
     * @return A new JSONNull.
     */
    public JSONNull createJSONNull();

    /**
     * Creates a new instance of a _JSONBoolean_ implementation.
     *
     * @param value The boolean value.
     *
     * @return A new JSONBoolean.
     */
    public JSONBoolean createJSONBoolean(Boolean value);

    /**
     * Creates a new instance of a _JSONArray_ implementation.
     *
     * @return A new JSONArray.
     */
    public JSONArray createJSONArray();
}
