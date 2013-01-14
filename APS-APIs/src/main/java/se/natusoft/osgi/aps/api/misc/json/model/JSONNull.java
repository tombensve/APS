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
 *         2012-01-17: Created!
 *         
 */
package se.natusoft.osgi.aps.api.misc.json.model;

/**
 * This class is based on the structure defined on http://www.json.org/.
 * <p/>
 * This represents a JSON null value.
 * <pre>
 *                                                   Subclasses
 *                                                   ----------
 * |________________ (STRING) ________________|      JSONString
 * |  |_____________ (NUMBER) _____________|  |      JSONNumber
 *    |_____________ (OBJECT) _____________|         JSONObject
 *    |_____________ (ARRAY)  _____________|         JSONArray
 *    |_____________ (true)   _____________|     \__ JSONBoolean
 *    |_____________ (false)  _____________|     /
 *    \_____________ (null)   _____________/         <b>JSONNull</b>
 *
 * </pre>
 *
 * @author Tommy Svensson
 */
public interface JSONNull extends JSONValue {

    /**
     * @return this JSONNull as a String value.
     */
    public String toString();
}
