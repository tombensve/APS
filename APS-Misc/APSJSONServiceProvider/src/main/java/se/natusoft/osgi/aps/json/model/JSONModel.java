/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Service Provider
 *     
 *     Code Version
 *         0.9.0
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


import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;

/**
 * Converts between service models and aps-json-lib models.
 */
public class JSONModel<Aggregated> {

    //
    // Private Members
    //

    /** Our aggregated object. */
    private Aggregated aggregated = null;

    //
    // Constructors
    //

    /**
     * Creates a new JSONModel instance.
     *
     * @param aggregated The aggregated aps-json-lib model.
     */
    protected JSONModel(Aggregated aggregated) {
        this.aggregated = aggregated;
    }

    /**
     * Creates a new JSONModel instance.
     */
    protected JSONModel() {}

    //
    // Methods
    //

    /**
     * @return The aps-json-lib aggregated instance.
     */
    public Aggregated getAggregated() {
        return this.aggregated;
    }

    /**
     * Sets the aps-json-lib aggregated instance.
     *
     * @param aggregated The instance to set.
     */
    protected void setAggregated(Aggregated aggregated) {
        this.aggregated = aggregated;
    }

    /**
     * Converts from aps-json-lib model value to a wrapped JSON*Model value.
     *
     * @param value The aps-json-lib value to wrap into an JSON*Model value.
     *
     * @return The appropriate JSON*Model object.
     */
    public static JSONValue convertLibValue(se.natusoft.osgi.aps.json.JSONValue value) {
        if (value instanceof se.natusoft.osgi.aps.json.JSONString) {
            return new JSONStringModel((se.natusoft.osgi.aps.json.JSONString)value);
        }
        else if (value instanceof se.natusoft.osgi.aps.json.JSONNumber) {
            return new JSONNumberModel((se.natusoft.osgi.aps.json.JSONNumber)value);
        }
        else if (value instanceof se.natusoft.osgi.aps.json.JSONBoolean) {
            return new JSONBooleanModel((se.natusoft.osgi.aps.json.JSONBoolean)value);
        }
        else if (value instanceof se.natusoft.osgi.aps.json.JSONObject) {
            return new JSONObjectModel((se.natusoft.osgi.aps.json.JSONObject)value);
        }
        else if (value instanceof se.natusoft.osgi.aps.json.JSONArray) {
            return new JSONArrayModel((se.natusoft.osgi.aps.json.JSONArray)value);
        }
        else if (value instanceof se.natusoft.osgi.aps.json.JSONNull) {
            return new JSONNullModel((se.natusoft.osgi.aps.json.JSONNull)value);
        }
        return null;
    }
}
