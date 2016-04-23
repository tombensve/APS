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


import se.natusoft.osgi.aps.api.misc.json.model.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is based on the structure defined on http://www.json.org/.
 * <p>
 * It represents the "object" diagram on the above mentioned web page:
 * <pre>
 *              ________________________________________
 *             /                                        \
 * |___ ({) __/_____ (string) ____ (:) ____ (value) _____\___ (}) ____|
 * |           /                                        \             |
 *             \__________________ (,) _________________/
 *
 * </pre>
 *
 * @see JSONValue
 *
 * @author Tommy Svensson
 */
public class JSONObjectModel extends JSONModel<se.natusoft.osgi.aps.json.JSONObject> implements JSONObject {
    //
    // Constructors
    //

    /**
     * Creates a new JSONObjectModel.
     *
     * @param jsonObject An external JSONObject library model to wrap.
     */
    public JSONObjectModel(se.natusoft.osgi.aps.json.JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Creates a new JSONObjectModel creating a new library JSONObject to wrap internally.
     */
    public JSONObjectModel() {
        super(new se.natusoft.osgi.aps.json.JSONObject());
    }

    //
    // Methods
    //


    /**
     * Returns the names of the available values.
     */
    @Override
    public Set<JSONString> getValueNames() {
        Set<se.natusoft.osgi.aps.json.JSONString> propNamesAgg = getAggregated().getPropertyNames();
        HashSet propNames = new HashSet();
        for (se.natusoft.osgi.aps.json.JSONString prop : propNamesAgg) {
            propNames.add(new JSONStringModel(prop));
        }
        return propNames;
    }

    /**
     * Returns the named value.
     *
     * @param name The name of the value to get.
     */
    @Override
    public JSONValue getValue(JSONString name) {
        return convertLibValue(getAggregated().getProperty(((JSONStringModel)name).getAggregated()));
    }

    /**
     * Returns the named value.
     *
     * @param name The name of the value to get.
     */
    @Override
    public JSONValue getValue(String name) {
        return convertLibValue(getAggregated().getProperty(name));
    }

    /**
     * Adds a value to this JSONObject instance.
     *
     * @param name  The name of the value.
     * @param value The value.
     */
    @Override
    public void addValue(JSONString name, JSONValue value) {
        getAggregated().addProperty(((JSONStringModel)name).getAggregated(),
                (se.natusoft.osgi.aps.json.JSONValue)((JSONModel)value).getAggregated());
    }

    /**
     * Adds a value to this JSONObject instance.
     *
     * @param name  The name of the value.
     * @param value The value.
     */
    @Override
    public void addValue(String name, JSONValue value) {
        getAggregated().addProperty(name, (se.natusoft.osgi.aps.json.JSONValue)((JSONModel)value).getAggregated());
    }

    /**
     * Adds a string value.
     *
     * @param name The name of the value.
     * @param value The value.
     */
    @Override
    public void addValue(String name, String value) {
        getAggregated().addProperty(name, new se.natusoft.osgi.aps.json.JSONString(value));
    }

    /**
     * Adds a numeric value.
     *
     * @param name The name of the value.
     * @param value The value.
     */
    @Override
    public void addValue(String name, Number value) {
        getAggregated().addProperty(name, new se.natusoft.osgi.aps.json.JSONNumber(value));
    }

    /**
     * Adds a boolean value.
     *
     * @param name The name of the value.
     * @param value The value.
     */
    @Override
    public void addValue(String name, boolean value) {
        getAggregated().addProperty(name, new se.natusoft.osgi.aps.json.JSONBoolean(value));
    }

    /**
     * populates this JSONObject from the specified Map.
     *
     * @param map The Map to import.
     */
    @Override
    public void fromMap(Map<String, Serializable> map) {
        for (Map.Entry<String, Serializable> entry : map.entrySet()) {
            Class entryClass = entry.getValue().getClass();

            if (String.class.isAssignableFrom(entryClass)) {
                addValue(entry.getKey(), (String)entry.getValue());
            }
            else if (Number.class.isAssignableFrom(entryClass)) {
                addValue(entry.getKey(), (Number)entry.getValue());
            }
            else if (Boolean.class.isAssignableFrom(entryClass)) {
                addValue(entry.getKey(), (Boolean)entry.getValue());
            }
            else if (Map.class.isAssignableFrom(entryClass)) {
                JSONObject jsonObject = new JSONObjectModel();
                //noinspection unchecked
                jsonObject.fromMap((Map)entry.getValue());
                addValue(entry.getKey(), jsonObject);
            }
        }
    }

    /**
     * Returns the JSONObject as a Map.
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        for (JSONString key : getValueNames()) {
            JSONValue value = getValue(key);

            if (JSONString.class.isAssignableFrom(value.getClass())) {
                map.put(key.toString(), value.toString());
            }
            else if (JSONNumber.class.isAssignableFrom(value.getClass())) {
                map.put(key.toString(), ((JSONNumber)value).toNumber());
            }
            else if (JSONBoolean.class.isAssignableFrom(value.getClass())) {
                map.put(key.toString(), ((JSONBoolean)value).toBoolean());
            }
            else if (JSONObject.class.isAssignableFrom(value.getClass())) {
                map.put(key.toString(), ((JSONObject)value).toMap());
            }
        }

        return map;
    }

}

