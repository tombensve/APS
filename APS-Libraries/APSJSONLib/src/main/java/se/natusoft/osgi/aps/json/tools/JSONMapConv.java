package se.natusoft.osgi.aps.json.tools;

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
     *
     * @throws IOException
     */
    public static Map<String, Object> jsonObjectToMap(String json) throws IOException {
        try (ByteArrayInputStream byteIs = new ByteArrayInputStream(json.getBytes("UTF-8"))) {
            return jsonObjectToMap(byteIs);
        }
    }

    /**
     * This takes an InputStream containing a JSON object and returns it as a Map.
     *
     * @param is The InputStream to read.
     *
     * @throws IOException
     */
    public static Map<String, Object> jsonObjectToMap(InputStream is) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.readJSON(is);

        return toMap(jsonObject);
    }

    /**
     * Support method to convert from JSON to Java types.
     *
     * @param value The JSON value to convert.
     */
    private static Object toJava(JSONValue value) {
        if (value instanceof JSONObject) {
            return toMap((JSONObject)value);
        }
        else if (value instanceof JSONArray) {
            return toArray((JSONArray)value);
        }
        else if (value instanceof JSONString) {
            return value.toString();
        }
        else if (value instanceof JSONNumber) {
            return ((JSONNumber)value).toNumber();
        }
        else if (value instanceof JSONBoolean) {
            return ((JSONBoolean)value).getAsBoolean();
        }
        else if (value instanceof JSONNull) { // Not entirely sure of this ...
            return null;
        }

        return null; // ... nor this!
    }

    /**
     * Converts a JSONObject to a Map.
     *
     * @param jsonObject The JSONObject to convert.
     */
    private static Map<String, Object> toMap(JSONObject jsonObject) {
        Map<String, Object> jsonMap = new HashMap<>();

        jsonObject.getPropertyNames().forEach((name) -> {
            JSONValue value = jsonObject.getProperty(name);

            jsonMap.put(name.toString(), toJava(value));
        });

        return jsonMap;
    }

    /**
     * Converts a JSONArray to a Java array.
     *
     * @param array The JSONArray to convert.
     */
    private static Object[] toArray(JSONArray array) {
        ArrayList<Object> arrayList = new ArrayList<>();

        array.getAsList().forEach((value) -> {
            arrayList.add(toJava(value));
        });

        return arrayList.toArray();
    }

    //
    // Map -> JSON
    //

    /**
     * This takes a Map (as created by jsonObjectToMap(...)) and returns a JSON object.
     *
     * @param map The Map to convert to JSON.
     *
     * @throws IOException
     */
    public static String mapToJSONObject(Map<String, Object> map) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapToJSONObject(map, baos);
        baos.close();
        return new String(baos.toByteArray());
    }

    /**
     * This takes a Map (as created by jsonObjectToMap(...)) and writes it as JSON to the specified OutputStream.
     *
     * @param map The Map to write as JSON.
     * @param os The OutputStream to write to.
     *
     * @throws IOException
     */
    public static void mapToJSONObject(Map<String, Object> map, OutputStream os) throws IOException {
        JSONObject jsonObject = mapToObject(map);
        jsonObject.writeJSON(os, true);
    }

    /**
     * Converts a Java value to an internal JSONValue.
     *
     * @param value The value to convert to JSONValue.
     */
    private static JSONValue toJSON(Object value) {
        if (value instanceof Map) {
            //noinspection unchecked
            return mapToObject((Map<String, Object>)value);
        }
        else if (value instanceof Collection) {
            return collectionToArray((Collection)value);
        }
        else if (value.getClass().isArray()) {
            LinkedList<Object> list = new LinkedList<>();
            Collections.addAll(list, ((Object[]) value));
            return collectionToArray(list);
        }
        else if (value instanceof String) {
            return new JSONString(value.toString());
        }
        else if (value instanceof Number) {
            return new JSONNumber((Number)value);
        }
        else if (value instanceof Boolean) {
            return new JSONBoolean((boolean)value);
        }

        return new JSONNull();
    }

    /**
     * Converts a Java Map to an internal JSONObject.
     *
     * @param map The Map to convert.
     */
    private static JSONObject mapToObject(Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();

        map.entrySet().forEach((entry) -> {
            jsonObject.addProperty(entry.getKey(), toJSON(entry.getValue()));
        });

        return jsonObject;
    }

    /**
     * Converts a Collection to an internal JSONArray.
     *
     * @param collection The Collection to convert.
     */
    private static JSONArray collectionToArray(Collection collection) {
        JSONArray array = new JSONArray();

        //noinspection unchecked
        collection.forEach((entry) -> {
            JSONValue value = toJSON(entry);
            array.addValue(value);
        });

        return array;
    }
}
