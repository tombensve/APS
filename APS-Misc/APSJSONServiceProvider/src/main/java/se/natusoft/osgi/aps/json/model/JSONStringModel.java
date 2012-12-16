/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides an implementation of aps-apis:se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService
 *         using aps-json-lib as JSON parser/creator.
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
 *         2012-01-22: Created!
 *         
 */
package se.natusoft.osgi.aps.json.model;

import se.natusoft.osgi.aps.api.model.json.JSONString;

/**
 * This class is based on the structure defined on http://www.json.org/.
 * <p>
 * This represents the "string" diagram on the above mentioned web page:
 * <pre>
 *            __________________________________________________________________________
 *           /    ___________________________________________________________________   \
 *           |   /                                                                   \  |
 * |___ (") _|___|___ (Any UNICODE character except " or \ or control character) ____|__|_ (") ___|
 * |           \                                                                  /               |
 *              |                                                                 |
 *              \__ (\) ___ (") (quotation mark) _________________________________|
 *                      |__ (\) (reverse solidus) ________________________________|
 *                      |__ (/) (solidus) ________________________________________|
 *                      |__ (b) (backspace) ______________________________________|
 *                      |__ (f) (formfeed) _______________________________________|
 *                      |__ (n) (newline) ________________________________________|
 *                      |__ (r) (carriage return) ________________________________|
 *                      |__ (t) (orizontal tab) __________________________________|
 *                      \__ (u) (4 hexadecimal digits) ___________________________/
 * </pre>
 *
 * @author Tommy Svensson
 */
public class JSONStringModel extends JSONModel<se.natusoft.osgi.aps.json.JSONString> implements JSONString {
    //
    // Constructors
    //

    /**
     * Creates a new JSONStringModel.
     *
     * @param jsonString The aggregated JSONString from aps-json-lib.
     */
    public JSONStringModel(se.natusoft.osgi.aps.json.JSONString jsonString) {
        super(jsonString);
    }

    /**
     * Creates a new JSONStringModel.
     *
     * @param value The value of the string.
     */
    public JSONStringModel(String value) {
        setAggregated(new se.natusoft.osgi.aps.json.JSONString(value));
    }

    //
    // Methods
    //

    /**
     * @return this JSONString as a Java String.
     */
    public String toString() {
        return getAggregated().toString();
    }

}
