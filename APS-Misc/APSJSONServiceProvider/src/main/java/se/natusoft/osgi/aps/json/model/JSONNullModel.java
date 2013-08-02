/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Service Provider
 *     
 *     Code Version
 *         0.9.2
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

import se.natusoft.osgi.aps.api.misc.json.model.JSONNull;

/**
 * This class is based on the structure defined on http://www.json.org/.
 *
 * @author Tommy Svensson
 */
public class JSONNullModel extends JSONModel<se.natusoft.osgi.aps.json.JSONNull> implements JSONNull {
    //
    // Constructors
    //

    /**
     * Creates a new JSONNullModel.
     *
     * @param jsonNull The aggregated JSONNull from aps-json-lib.
     */
    public JSONNullModel(se.natusoft.osgi.aps.json.JSONNull jsonNull) {
        super(jsonNull);
    }

    /**
     * Creates a new JSONNullModel.
     */
    public JSONNullModel() {
        setAggregated(new se.natusoft.osgi.aps.json.JSONNull());
    }

    //
    // Methods
    //

    /**
     * @return The JSONNull as a String value.
     */
    public String toString() {
        return getAggregated().toString();
    }

}
