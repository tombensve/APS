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

import se.natusoft.osgi.aps.api.misc.json.model.JSONArray;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is based on the structure defined on http://www.json.org/.
 * <p>
 * This represents the "array" diagram on the above mentioned web page:
 * <pre>
 *               _______________________
 *              /                       \
 *              |                       |
 * |_____ ([) __/_______ (value) _______\__ (]) _____|
 * |              /                   \              |
 *                |                   |
 *                \_______ (,) _______/
 * </pre>
 * @author Tommy Svensson
 */
public class JSONArrayModel  extends JSONModel<se.natusoft.osgi.aps.json.JSONArray> implements JSONArray {
    //
    // Constructors
    //

    /**
     * Creates a new JSONArrayModel.
     *
     * @param jsonArray The aggregated JSONArray from aps-json-lib.
     */
    public JSONArrayModel(se.natusoft.osgi.aps.json.JSONArray jsonArray) {
        super(jsonArray);
    }

    /**
     * Creates a new JSONArrayModel.
     */
    public JSONArrayModel() {
        setAggregated(new se.natusoft.osgi.aps.json.JSONArray());
    }
    
    //
    // Methods
    //


    /**
     * Adds a value to the array.
     *
     * @param value The value to add.
     */
    @Override
    public void addValue(JSONValue value) {
        getAggregated().addValue((se.natusoft.osgi.aps.json.JSONValue)((JSONModel)value).getAggregated());
    }

    /**
     * Returns the array values as a List.
     */
    @Override
    public List<JSONValue> getAsList() {
        List<JSONValue> values = new LinkedList<JSONValue>();

        for (se.natusoft.osgi.aps.json.JSONValue aggValue : getAggregated().getAsList()) {
            values.add(convertLibValue(aggValue));
        }

        return values;
    }

    /**
     * Returns the array values as a list of a specific type.
     *
     * @param type The class of the type to return values as a list of.
     * @param <T>  One of the JSONValue subclasses.
     *
     * @return A list of specified type if type is the same as in the list.
     */
    @Override
    public <T extends JSONValue> List<T> getAsList(Class<T> type) {
        return (List<T>)getAsList();
    }
}
