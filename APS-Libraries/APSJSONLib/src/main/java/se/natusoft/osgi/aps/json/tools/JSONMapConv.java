package se.natusoft.osgi.aps.json.tools;

import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.exceptions.APSIOException;
import se.natusoft.osgi.aps.json.*;

import java.io.*;
import java.util.*;

/**
 * This converts between a Java Map and JSON. Do note that this of course uses this library to read and write JSON,
 * but this specific public API only deals with Java and JSON as String or on/in a stream.
 * <p/>
 * This class becomes more useful when used from Groovy since the latter provides much nicer usage of
 * data in Maps. Yes, I know about JSONSlurper and JSONBuilder in Groovy. Those however does not work
 * with @CompileStatic. Maps does.
 */
public class JSONMapConv {

    //
    // JSON -> Map
    //

    /**
     * This takes a String containing a JSON object and returns it as a Map.
     *
     * @param json The JSON content to convert to a Map.
     * @throws APSIOException on failure.
     */
    public static Map<String, Object> jsonObjectToMap(String json) throws APSIOException {
        try (ByteArrayInputStream byteIs = new ByteArrayInputStream(json.getBytes("UTF-8"))) {
            return jsonObjectToMap(byteIs);
        } catch (IOException ioe) {
            throw new APSIOException(ioe.getMessage(), ioe);
        }
    }

    /**
     * This takes an InputStream containing a JSON object and returns it as a Map.
     *
     * @param is The InputStream to read.
     * @throws APSIOException on failure.
     */
    @SuppressWarnings("WeakerAccess")
    public static Map<String, Object> jsonObjectToMap(InputStream is) throws APSIOException {
        return jsonObjectToMap(is, null);
    }

    /**
     * This takes an InputStream containing a JSON object and returns it as a Map.
     *
     * @param is The InputStream to read.
     * @throws APSIOException on failure.
     */
    @SuppressWarnings("WeakerAccess")
    public static Map<String, Object> jsonObjectToMap(InputStream is, JSONErrorHandler errorHandler) throws APSIOException {
        JSONObjectProvider jsonObject = new JSONObjectProvider(errorHandler);
        jsonObject.readJSON(is);

        return toMap(jsonObject);
    }

    /**
     * This takes a JSONObject and returns a Map.
     *
     * @param jsonObject The JSONObject to convert to a Map.
     * @return The converted Map.
     */
    public static Map<String, Object> jsonObjectToMap(se.natusoft.osgi.aps.api.misc.json.model.JSONObject jsonObject) {
        return toMap(jsonObject);
    }

    /**
     * Support method to convert from JSON to Java types.
     *
     * @param value The JSON value to convert.
     */
    private static Object toJava(se.natusoft.osgi.aps.api.misc.json.model.JSONValue value) {
        if (value instanceof JSONObjectProvider) {
            return toMap((se.natusoft.osgi.aps.api.misc.json.model.JSONObject) value);
        } else if (value instanceof JSONArrayProvider) {
            return toArray((se.natusoft.osgi.aps.api.misc.json.model.JSONArray) value);
        } else if (value instanceof JSONStringProvider) {
            return value.toString();
        } else if (value instanceof JSONNumberProvider) {
            return ((se.natusoft.osgi.aps.api.misc.json.model.JSONNumber) value).toNumber();
        } else if (value instanceof JSONBooleanProvider) {
            return ((se.natusoft.osgi.aps.api.misc.json.model.JSONBoolean) value).toBoolean();
        } else if (value instanceof JSONNullProvider) { // Not entirely sure of this ...
            return null;
        }

        return null; // ... nor this!
    }

    /**
     * Converts a JSONObject to a Map.
     *
     * @param jsonObject The JSONObject to convert.
     */
    private static Map<String, Object> toMap(se.natusoft.osgi.aps.api.misc.json.model.JSONObject jsonObject) {
        Map<String, Object> jsonMap = new HashMap<>();

        jsonObject.getValueNames().forEach((name) -> {
            se.natusoft.osgi.aps.api.misc.json.model.JSONValue value = jsonObject.getValue(name);

            jsonMap.put(name.toString(), toJava(value));
        });

        return jsonMap;
    }

    /**
     * Converts a JSONArray to a Java array.
     *
     * @param array The JSONArray to convert.
     */
    private static Object[] toArray(se.natusoft.osgi.aps.api.misc.json.model.JSONArray array) {
        ArrayList<Object> arrayList = new ArrayList<>();

        array.getAsList().forEach((value) -> arrayList.add(toJava(value)));

        return arrayList.toArray();
    }

    //
    // Map -> JSON
    //

    /**
     * This takes a Map (as created by jsonObjectToMap(...)) and returns a JSON String.
     *
     * @param map The Map to convert to JSON.
     * @throws APSIOException on I/O failures.
     */
    public static String mapToJSONObjectString(Map<String, Object> map) throws APSIOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mapToJSONObject(map, baos);
            baos.close();
            return new String(baos.toByteArray());
        } catch (IOException ioe) {
            throw new APSIOException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Converts a `Map<String, Object>` to a JSONObject.
     *
     * @param map The Map to convert.
     * @return A converted JSONObject.
     */
    public static se.natusoft.osgi.aps.api.misc.json.model.JSONObject mapToJSONObject(Map<String, Object> map) {
        return mapToObject(map);
    }

    /**
     * This takes a Map (as created by jsonObjectToMap(...)) and writes it as JSON to the specified OutputStream.
     *
     * @param map The Map to write as JSON.
     * @param os  The OutputStream to write to.
     * @throws APSIOException on I/O failures.
     */
    @SuppressWarnings("WeakerAccess")
    public static void mapToJSONObject(Map<String, Object> map, OutputStream os) throws APSIOException {
        JSONObjectProvider jsonObject = mapToObject(map);
        jsonObject.writeJSON(os, true);
    }

    // Internals

    /**
     * Converts a Java value to an internal JSONValue.
     *
     * @param value The value to convert to JSONValue.
     */
    private static JSONValueProvider toJSON(Object value) {
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
            return new JSONStringProvider(value.toString());
        } else if (value instanceof Number) {
            return new JSONNumberProvider((Number) value);
        } else if (value instanceof Boolean) {
            return new JSONBooleanProvider((boolean) value);
        }

        return new JSONNullProvider();
    }

    /**
     * Converts a Java Map to an internal JSONObject.
     *
     * @param map The Map to convert.
     */
    private static JSONObjectProvider mapToObject(Map<String, Object> map) {
        JSONObjectProvider jsonObject = new JSONObjectProvider();

        map.forEach((key, value) -> jsonObject.addValue(key, toJSON(value)));

        return jsonObject;
    }

    /**
     * Converts a Collection to an internal JSONArray.
     *
     * @param collection The Collection to convert.
     */
    private static JSONArrayProvider collectionToArray(Collection collection) {
        JSONArrayProvider array = new JSONArrayProvider();

        //noinspection unchecked
        collection.forEach((entry) -> {
            JSONValueProvider value = toJSON(entry);
            array.addValue(value);
        });

        return array;
    }
}
