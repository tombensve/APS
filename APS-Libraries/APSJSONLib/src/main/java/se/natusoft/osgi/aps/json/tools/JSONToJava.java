/*
 *
 * PROJECT
 *     Name
 *         APS JSON Library
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides a JSON parser and creator. Please note that this bundle has no dependencies to any
 *         other APS bundle! It can be used as is without APS in any Java application and OSGi container.
 *         The reason for this is that I do use it elsewhere and don't want to keep 2 different copies of
 *         the code. OSGi wise this is a library. All packages are exported and no activator nor services
 *         are provided.
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
 *         2012-01-06: Created!
 *
 */
package se.natusoft.osgi.aps.json.tools;

import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.*;
import se.natusoft.osgi.aps.exceptions.APSIOException;
import se.natusoft.osgi.aps.json.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Creates a JavaBean instance and copies data from a JSON value to it.
 *
 * The following mappings are made in addition to the expected ones:
 *
 * * _JSONArray_ only maps to an array property.
 * * Date properties in bean are mapped from _JSONString_ "yyyy-MM-dd HH:mm:ss".
 * * Enum properties in bean are mapped from _JSONString_ which have to contain enum constant name.
 *
 */
public class JSONToJava {

    /**
     * Returns an instance of a java class populated with data from a json object value read from a stream.
     *
     * @param jsonStream The stream to read from.
     * @param javaClass The java class to instantiate and populate.
     *
     * @return A populated instance of javaClass.
     *
     * @throws APSIOException on IO failures.
     * @throws JSONConvertionException On JSON to Java failures.
     */
    public static <T> T convert(InputStream jsonStream, Class<T> javaClass) throws APSIOException, JSONConvertionException {
        JSONErrorHandler jsonErrHandler = new JSONErrorHandler() {
            @Override
            public void warning(String message) {
                // We have to ignore warnings here!
            }

            @Override
            public void fail(String message, Throwable cause) throws RuntimeException {
                throw new JSONConvertionException(message, cause);
            }
        };
        JSONObjectProvider obj = new JSONObjectProvider(jsonErrHandler);
        obj.readJSON(jsonStream);

        return convert(obj, javaClass);
    }

    /**
     * Returns an instance of a java class populated with data from a json object value read from a String containing JSON.
     *
     * @param json The String to read from.
     * @param javaClass The java class to instantiate and populate.
     *
     * @return A populated instance of javaClass.
     *
     * @throws APSIOException on IO failures.
     * @throws JSONConvertionException On JSON to Java failures.
     */
    public static <T> T convert(String json, Class<T> javaClass) throws APSIOException, JSONConvertionException {
        ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes());
        try {
            return convert(bais, javaClass);
        }
        finally {
            //noinspection EmptyCatchBlock
            try {bais.close();} catch (IOException ioe) {}
        }
    }

    /**
     * Returns an instance of java class populated with data from json.
     *
     *
     * @param json The json to convert to java.
     * @param javaClass The class of the java object to convert to.
     *
     * @return A converted Java object.
     *
     * @throws JSONConvertionException On failure to convert.
     */
    public static <T> T convert(JSONValue json, Class<T> javaClass) throws JSONConvertionException {
        T converted;

        if (json instanceof JSONObject) {
            converted = createAndLoadModel((JSONObject)json, javaClass);
        }
        else {
            converted = convertJSONValue(json, javaClass);
        }

        return converted;
    }

    /**
     * Loads data into a java object wrapped in a _ModelInstance_ from a _JSONObject_.
     *
     * @param bean The bean to load.
     * @param jsonObject The JSON object to load from.
     *
     * @throws JSONConvertionException on failure to load.
     */
    private static void load(BeanInstance bean, JSONObject jsonObject) throws JSONConvertionException {
        // Load bean properties.
        for (JSONString prop : jsonObject.getValueNames()) {
            JSONValue value = jsonObject.getValue(prop);

            @SuppressWarnings("unchecked") Object convertedValue = convertJSONValue(value, bean.getPropertyType(prop.toString()));
            bean.setProperty(prop.toString(), convertedValue);
        }
    }

    /**
     * Creates and loads a model from JSON data.
     *
     * @param value The JSON value to load from.
     * @param beanType The type of the java model to return.
     *
     * @return A loaded instance of the java model.
     *
     * @throws JSONConvertionException on any failure.
     */
    @SuppressWarnings("unchecked")
    private static <T> T createAndLoadModel(JSONObject value, Class<T> beanType) throws JSONConvertionException {
        try {
            BeanInstance bean = new BeanInstance(beanType.newInstance());
            load(bean, value);

            return (T)bean.getModelInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new JSONConvertionException(e.getMessage(), e);
        }
    }

    /**
     * Converts a JSON value to a Java value.
     *
     * @param value The JSON value to convert.
     * @param modelType The type of the java value.
     *
     * @return A converted Java instance.
     *
     * @throws JSONConvertionException on any failure.
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertJSONValue(JSONValue value, Class<T> modelType) throws JSONConvertionException {
        T resVal = null;

        if (value instanceof JSONString) {
            resVal = (T)value.toString();
        }
        else if (value instanceof JSONBoolean) {
            resVal = (T)((JSONBooleanProvider)value).toBoolean();
        }
        else if (value instanceof JSONNumber) {
            resVal = (T)((JSONNumberProvider)value).to(modelType);
        }
        else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray)value;
            List arrayEntries = new ArrayList();
            for (JSONValue entry : array.getAsList()) {
                if (entry instanceof JSONObject) {
                    arrayEntries.add(createAndLoadModel((JSONObject) entry, modelType));
                }
                else {
                    arrayEntries.add(convertJSONValue(entry, modelType));
                }
            }
            resVal = (T)arrayEntries;
        }
        else if (value instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject)value;

            if (Properties.class.isAssignableFrom(modelType)) {
                Properties props = new Properties();
                for (JSONString name : jsonObject.getValueNames()) {
                    JSONValue jsonValue = jsonObject.getValue(name);
                    props.setProperty(name.toString(), jsonValue.toString());
                }
                resVal = (T)props;
            }
            else if (Map.class.isAssignableFrom(modelType)) {
                resVal = (T)jsonObjectToMap(jsonObject);
            }
            else {
                resVal = createAndLoadModel((JSONObject) value, modelType);
            }
        }
        else //noinspection StatementWithEmptyBody
            if (value instanceof JSONNull) {
            // Do nothing and return null.
        }

        return resVal;
    }

    private static Map jsonObjectToMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        for (JSONString name : jsonObject.getValueNames()) {
            JSONValue jsonValue = jsonObject.getValue(name);

            if (jsonValue instanceof JSONString) {
                map.put(name.toString(), jsonValue.toString());
            }
            else if (jsonValue instanceof JSONBoolean) {
                map.put(name.toString(), ((JSONBooleanProvider)jsonValue).toBoolean());
            }
            else if (jsonValue instanceof JSONNumber) {
                map.put(name.toString(), ((JSONNumberProvider)jsonValue).toNumber());
            }
            else if (jsonValue instanceof JSONObject) {
                // This is the best we can do since we don't have a type for this object!
                map.put(name.toString(), jsonObjectToMap((JSONObject) jsonValue));
            }
            else if (jsonValue instanceof JSONArray) {
                map.put(name.toString(), jsonArrayToList((JSONArray)jsonValue));
            }
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    private static List jsonArrayToList(JSONArray jsonArray) {
        List list = new ArrayList();
        for (JSONValue jsonValue : jsonArray.getAsList()) {

            if (jsonValue instanceof JSONString) {
                list.add(jsonValue.toString());
            }
            else if (jsonValue instanceof JSONBoolean) {
                list.add(((JSONBooleanProvider)jsonValue).toBoolean());
            }
            else if (jsonValue instanceof JSONNumber) {
                list.add(((JSONNumberProvider)jsonValue).toNumber());
            }
            else if (jsonValue instanceof JSONObject) {
                // This is the best we can do since we don't have a type for the object!
                list.add(jsonObjectToMap((JSONObject)jsonValue));
            }
            else if (jsonValue instanceof JSONArray) {
                list.add(jsonArrayToList((JSONArrayProvider)jsonValue));
            }
        }

        return list;
    }
}
