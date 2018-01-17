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
 *         2012-01-30: Created!
 *
 */
package se.natusoft.osgi.aps.json;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.api.reactive.APSHandler;
import se.natusoft.osgi.aps.api.reactive.APSResult;
import se.natusoft.osgi.aps.api.reactive.APSValue;
import se.natusoft.osgi.aps.exceptions.APSIOException;
import se.natusoft.osgi.aps.json.tools.CollectingErrorHandler;

import java.io.*;
import java.util.*;

/**
 * This is the official API for reading and writing JSON values.
 *
 * It also contains conversion methods between the JSON\* objects and Map\<String, Object\>, and
 * JSON\* objects and JSON structures in String. The latter for convenience for passing over network
 * in for example Vertx event bus.
 *
 * To Serialize/deserialize between Java use JSONToJava and JavaToJSON under tools.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class JSON {

    /**
     * Reads any JSON object from the specified _InputStream_.
     *
     * @param jsonIn        The InputStream to read from.
     * @param resultHandler The handler to call with result.
     */
    public static void read(@NotNull InputStream jsonIn, @NotNull APSHandler<APSResult<JSONValue>> resultHandler) {
        // Fails will cause an APSIOException!
        CollectingErrorHandler errorHandler = new CollectingErrorHandler(true);

        try {
            JSONValue.JSONReader reader =
                    new JSONValue.JSONReader(new PushbackReader(new InputStreamReader(jsonIn, "UTF-8")), errorHandler);

            char c = reader.getChar();

            JSONValue value = JSONValue.resolveAndParseJSONValue(c, reader, errorHandler);

            resultHandler.handle(new APSResult.Provider<>(new APSValue.Provider<>(value)));
        } catch (IOException | APSIOException ioe) {
            resultHandler.handle(new APSResult.Provider<>(ioe));
        }
    }

    /**
     * Reads any JSON object from the specified _InputStream_.
     *
     * @param jsonIn       The InputStream to read from.
     * @param errorHandler An implementation of this interface should be supplied by the user to handle any errors during JSON parsing.
     * @return A JSONValue subclass. Which depends on what was found on the stream.
     * @throws APSIOException on any IO failures.
     */
    public static @NotNull JSONValue read(@NotNull InputStream jsonIn, @NotNull JSONErrorHandler errorHandler) {

        try {
            JSONValue.JSONReader reader =
                    new JSONValue.JSONReader(new PushbackReader(new InputStreamReader(jsonIn, "UTF-8")), errorHandler);

            char c = reader.getChar();

            return JSONValue.resolveAndParseJSONValue(c, reader, errorHandler);
        } catch (IOException ioe) {
            throw new APSIOException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Writes a _JSONValue_ to an _OutputStream_. This will write compact output by default.
     *
     * @param jsonOut       The OutputStream to write to.
     * @param value         The value to write.
     * @param resultHandler handler for result. only success() or failure() is relevant.
     */
    @SuppressWarnings("Duplicates")
    public static void write(@NotNull OutputStream jsonOut, @NotNull JSONValue value, @Nullable APSHandler<APSResult<Void>> resultHandler) {
        try {
            write(jsonOut, value);
            if (resultHandler != null) resultHandler.handle(APSResult.success(null));
        } catch (APSIOException ioe) {
            if (resultHandler != null) resultHandler.handle(APSResult.failure(ioe));
        }
    }

    /**
     * Writes a _JSONValue_ to an _OutputStream_. This will write compact output by default.
     *
     * @param jsonOut The OutputStream to write to.
     * @param value   The value to write.
     * @throws APSIOException on failure.
     */
    public static void write(@NotNull OutputStream jsonOut, @NotNull JSONValue value) throws APSIOException {
        value.writeJSON(jsonOut);
    }

    /**
     * Writes a _JSONValue_ to an _OutputStream_. This will write compact output by default.
     *
     * @param jsonOut       The OutputStream to write to.
     * @param value         The value to write.
     * @param resultHandler handler for result. only success() or failure() is relevant.
     */
    public static void write(@NotNull OutputStream jsonOut, @NotNull JSONValue value, boolean compact,
                             @Nullable APSHandler<APSResult<Void>> resultHandler) {
        try {
            write(jsonOut, value, compact);
            if (resultHandler != null) resultHandler.handle(APSResult.success(null));
        } catch (APSIOException ioe) {
            if (resultHandler != null) resultHandler.handle(APSResult.failure(ioe));
        }
    }

    /**
     * Writes a _JSONValue_ to an _OutputStream_.
     *
     * @param jsonOut The OutputStream to write to.
     * @param value   The value to write.
     * @param compact If true the written JSON is made very compact and hard to read but produce less data.
     *                If false then the output will be larger but readable with indents. Use this when debugging.
     * @throws APSIOException on IO problems.
     */
    public static void write(@NotNull OutputStream jsonOut, @NotNull JSONValue value, boolean compact) throws APSIOException {
        value.writeJSON(jsonOut, compact);
    }

    //
    // Byte conversions
    //

    /**
     * Converts a JSONValue into bytes.
     *
     * @param jsonValue The JSONValue to convert.
     * @return A byte array.
     * @throws APSIOException on any IO failure.
     */
    public static byte[] jsonToBytes(@NotNull JSONValue jsonValue) throws APSIOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            write(baos, jsonValue);

            baos.close();

            return baos.toByteArray();
        } catch (IOException ioe) {

            throw new APSIOException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Converts a byte array into a JSONValue object. For this to work the byte array of course must contain valid JSON!
     *
     * @param bytes The bytes to conve  rt.
     */
    public static @NotNull JSONValue bytesToJson(byte[] bytes) {

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        try {

            CollectingErrorHandler errorHandler = new CollectingErrorHandler();
            return read(bais, errorHandler);

        } finally {

            try {
                bais.close();
            } catch (IOException ignore) {
            }
        }
    }

    //
    // String conversions
    //

    /**
     * Converts a JSONValue to a String of JSON.
     *
     * @param jsonValue The json value to convert.
     * @return A String of JSON.
     * @throws APSIOException on failure. Since the JSON is valid and we are writing to memory this is unlikely ...
     */
    public static @NotNull String jsonToString(@NotNull JSONValue jsonValue) throws APSIOException {
        byte[] bytes = jsonToBytes(jsonValue);
        String result;

        try {
            result = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException uee) {

            throw new APSIOException(uee.getMessage(), uee);
        } // "Should" not happen. UTF-8 is valid and the bytes are in UTF-8!

        return result;
    }

    /**
     * Converts a String with JSON into a JSONValue.
     *
     * @param jsonString The JSON String to convert.
     * @return Whatever JSON object the string contained, as a base JSONValue.
     * @throws APSIOException on failure, like bad JSON in string.
     */
    public static @NotNull JSONValue stringToJson(@NotNull String jsonString) throws APSIOException {

        JSONValue json;

        try {

            json = bytesToJson(jsonString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException uee) {

            throw new APSIOException(uee.getMessage(), uee);
        }

        return json;
    }

    //
    // JSON -> Map
    //

    /**
     * This takes an InputStream containing a JSON object and returns it as a Map.
     *
     * @param is The InputStream to read.
     * @throws APSIOException on failure.
     */
    @SuppressWarnings("WeakerAccess")
    public static @NotNull Map<String, Object> jsonObjectToMap(@NotNull InputStream is) throws APSIOException {
        return jsonObjectToMap(is, null);
    }

    /**
     * This takes an InputStream containing a JSON object and returns it as a Map.
     *
     * @param is The InputStream to read.
     * @throws APSIOException on failure.
     */
    @SuppressWarnings("WeakerAccess")
    public static @NotNull Map<String, Object> jsonObjectToMap(@NotNull InputStream is, @NotNull JSONErrorHandler errorHandler) throws APSIOException {
        JSONObject jsonObject = new JSONObject(errorHandler);
        jsonObject.readJSON(is);

        return toMap(jsonObject);
    }

    /**
     * This takes a JSONObject and returns a Map.
     *
     * @param jsonObject The JSONObject to convert to a Map.
     * @return The converted Map.
     */
    public static @NotNull Map<String, Object> jsonObjectToMap(@NotNull JSONObject jsonObject) {
        return toMap(jsonObject);
    }

    //
    // Map -> JSON
    //

    /**
     * Converts a `Map<String, Object>` to a JSONObject.
     *
     * @param map The Map to convert.
     * @return A converted JSONObject.
     */
    public static @NotNull JSONObject mapToJSONObject(@NotNull Map<String, Object> map) {
        return mapToObject(map);
    }

    /**
     * This takes a Map (as created by jsonObjectToMap(...)) and writes it as JSON to the specified OutputStream.
     *
     * @param map The Map to write as JSON.
     * @param os  The OutputStream to write to.
     */
    @SuppressWarnings("WeakerAccess")
    public static void writeMapAsJSON(@NotNull Map<String, Object> map, @NotNull OutputStream os) {
        JSONObject jsonObject = mapToObject(map);
        jsonObject.writeJSON(os, true);
    }

    //
    // String -> Map
    //

    /**
     * Converts from String to JSON to Map.
     *
     * @param json The JSON String to convert.
     * @return A Map representation of the JSON.
     */
    public static @NotNull Map<String, Object> stringToMap(@NotNull String json) {

        return toMap((JSONObject) stringToJson(json));
    }


    //
    // Map -> String
    //

    /**
     * Converts from Map to JSONObject to String.
     *
     * @param map The Map to convert.
     * @return A String containing JSON.
     */
    public static @NotNull String mapToString(@NotNull Map<String, Object> map) {

        return jsonToString(mapToJSONObject(map));
    }

    //
    // Internals
    //

    /**
     * Converts a Java value to an internal JSONValue.
     *
     * @param value The value to convert to JSONValue.
     */
    private static @NotNull JSONValue toJSON(@NotNull Object value) {
        if (value instanceof Map) {
            //noinspection unchecked
            return mapToObject((Map<String, Object>) value);
        } else if (value instanceof Collection) {
            return collectionToArray((Collection) value);
        } else if (value.getClass().isArray()) {
            LinkedList<Object> list = new LinkedList<>();
            Collections.addAll(list, ((Object[]) value));
            return collectionToArray(list);
        } else if (value instanceof String) {
            return new JSONString(value.toString());
        } else if (value instanceof Number) {
            return new JSONNumber((Number) value);
        } else if (value instanceof Boolean) {
            return new JSONBoolean((boolean) value);
        }

        return new JSONNull();
    }

    /**
     * Converts a Java Map to an internal JSONObject.
     *
     * @param map The Map to convert.
     */
    private static @NotNull  JSONObject mapToObject(@NotNull Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();

        map.forEach((key, value) -> jsonObject.setValue(key, toJSON(value)));

        return jsonObject;
    }

    /**
     * Converts a Collection to an internal JSONArray.
     *
     * @param collection The Collection to convert.
     */
    private static @NotNull JSONArray collectionToArray(@NotNull Collection collection) {
        JSONArray array = new JSONArray();

        //noinspection unchecked
        collection.forEach((entry) -> {
            JSONValue value = toJSON(entry);
            array.addValue(value);
        });

        return array;
    }

    /**
     * Support method to convert from JSON to Java types.
     *
     * @param value The JSON value to convert.
     */
    private static @Nullable Object toJava(@NotNull JSONValue value) {
        if (value instanceof JSONObject) {
            return toMap((JSONObject) value);
        } else if (value instanceof JSONArray) {
            return toArray((JSONArray) value);
        } else if (value instanceof JSONString) {
            return value.toString();
        } else if (value instanceof JSONNumber) {
            return ((JSONNumber) value).toNumber();
        } else if (value instanceof JSONBoolean) {
            return ((JSONBoolean) value).toBoolean();
        } else if (value instanceof JSONNull) { // Not entirely sure of this ...
            return null;
        }

        return null; // ... nor this!
    }

    /**
     * Converts a JSONObject to a Map.
     *
     * @param jsonObject The JSONObject to convert.
     */
    private static @NotNull Map<String, Object> toMap(@NotNull JSONObject jsonObject) {
        Map<String, Object> jsonMap = new HashMap<>();

        jsonObject.getValueNames().forEach((name) -> {
            JSONValue value = jsonObject.getValue(name);

            jsonMap.put(name.toString(), toJava(value));
        });

        return jsonMap;
    }

    /**
     * Converts a JSONArray to a Java array.
     *
     * @param array The JSONArray to convert.
     */
    private static @NotNull Object[] toArray(@NotNull JSONArray array) {
        ArrayList<Object> arrayList = new ArrayList<>();

        array.getAsList().forEach((value) -> arrayList.add(toJava(value)));

        return arrayList.toArray();
    }
}
