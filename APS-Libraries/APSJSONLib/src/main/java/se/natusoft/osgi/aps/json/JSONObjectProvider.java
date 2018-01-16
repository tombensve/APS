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
 *         2011-01-30: Created!
 *
 */
package se.natusoft.osgi.aps.json;

import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject;
import se.natusoft.osgi.aps.exceptions.APSIOException;
import se.natusoft.osgi.aps.json.tools.JSONMapConv;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is based on the structure defined on http://www.json.org/.
 *
 * It represents the "object" diagram on the above mentioned web page:
 *
 *                  ________________________________________
 *                 /                                        \
 *     |___ ({) __/_____ (string) ____ (:) ____ (value) _____\___ (}) ____|
 *     |           /                                        \             |
 *                 \__________________ (,) _________________/
 *
 *
 * This is also the starting point.
 *
 * To write JSON, create a new _JSONObject_ (`new JSONObject()`) and  call `addProperty(name, value)`
 * for children. Then do jsonObj.writeJSON(outputStream)`.
 *
 * To read JSON, create a new _JSONObject_ (`new JSONObject(jsonErrorHandler)`) and then do `jsonObj.readJSON(inputStream)`.
 * Then use `getProperty(name)` to extract children.
 *
 * @see JSONValueProvider
 *
 * @author Tommy Svensson
 */
@SuppressWarnings("unused")
public class JSONObjectProvider extends JSONValueProvider implements JSONObject {
    //
    // Private Members
    //

    /** The object values. */
    private Map<JSONStringProvider, JSONValueProvider> values = new HashMap<>();

    //
    // Constructors
    //

    /**
     * Creates a JSONObject instance for writing JSON output.
     */
    public JSONObjectProvider() {}

    /**
     * Creates a new JSONObject instance for reading JSON input or writing JSON output.
     *
     * @param errorHandler The error handler to use.
     */
    public JSONObjectProvider(JSONErrorHandler errorHandler) {
        super(errorHandler);
    }

    //
    // Methods
    //

    /**
     * Determines if the specified character denotes the start of an JSON object value.
     *
     * @param c The character to test.
     */
    /*package*/ static boolean isObjectStart(char c) {
        return c == '{';
    }

    /**
     * Determines if the specified character denotes the end of an JSON object value.
     *
     * @param c The character to test.
     */
    /*package*/ @SuppressWarnings("WeakerAccess")
    static boolean isObjectEnd(char c) {
        return c == '}';
    }


    /**
     * Returns the names of the available properties.
     */
    @Override
    public Set<se.natusoft.osgi.aps.api.misc.json.model.JSONString> getValueNames() {
        //noinspection unchecked
        return (Set)this.values.keySet();
    }

    /**
     * Returns the named property.
     *
     * @param name The name of the property to get.
     */
    @Override
    public JSONValueProvider getValue(se.natusoft.osgi.aps.api.misc.json.model.JSONString name) {
        //noinspection SuspiciousMethodCalls
        return this.values.get(name);
    }

    /**
     * Returns the named property.
     *
     * @param name The name of the property to get.
     */
    @Override
    public JSONValueProvider getValue(String name) {
        return this.values.get(new JSONStringProvider(name));
    }

    /**
     * Adds a value to this JSONObject instance.
     *
     * @param name  The name of the value.
     * @param value The value.
     */
    @Override
    public void setValue(se.natusoft.osgi.aps.api.misc.json.model.JSONString name, se.natusoft.osgi.aps.api.misc.json.model.JSONValue value) {
        this.values.put((JSONStringProvider)name, (JSONValueProvider)value);
    }

    /**
     * Adds a string value.
     *
     * @param name  The name of the value.
     * @param value The value.
     */
    @Override
    public void setValue(String name, String value) {
        this.values.put(new JSONStringProvider(name), new JSONStringProvider(value));
    }

    /**
     * Adds a numeric value.
     *
     * @param name  The name of the value.
     * @param value The value.
     */
    @Override
    public void setValue(String name, Number value) {
        this.values.put(new JSONStringProvider(name), new JSONNumberProvider(value));
    }

    /**
     * Adds a boolean vlaue.
     *
     * @param name  The name of the value.
     * @param value The value.
     */
    @Override
    public void setValue(String name, boolean value) {
        this.values.put(new JSONStringProvider(name), new JSONBooleanProvider(value));
    }

    /**
     * populates this JSONObject from the specified Map.
     *
     * @param map The Map to import.
     */
    @Override
    public void fromMap(Map<String, Object> map) {
        JSONObjectProvider obj = (JSONObjectProvider)JSONMapConv.mapToJSONObject(map);
        this.values = obj.values;
    }

    /**
     * Returns the JSONObject as a Map.
     */
    @Override
    public Map<String, Object> toMap() {
        return JSONMapConv.jsonObjectToMap(this);
    }

    /**
     * Adds a property to this JSONObject instance.
     *
     * @param name The name of the property.
     * @param value The property value.
     */
    public void setValue(se.natusoft.osgi.aps.api.misc.json.model.JSONString name, JSONValueProvider value) {
        this.values.put((JSONStringProvider)name, value);
    }

    /**
     * Adds a property to this JSONObject instance.
     *
     * @param name The name of the property.
     * @param value The property value.
     */
    public void setValue(String name, se.natusoft.osgi.aps.api.misc.json.model.JSONValue value) {
        this.values.put(new JSONStringProvider(name), (JSONValueProvider)value);
    }

    /**
     * Loads this JSONObject model from the specified input stream.
     *
     * @param c A pre-read character from the input stream.
     * @param reader The JSONReader to read from.
     *
     * @throws APSIOException on IO problems.
     */
    @Override
    protected void readJSON(char c, JSONReader reader) throws APSIOException {
        c = reader.skipWhitespace(c);
        reader.assertChar(c, '{', "A JSON object must start with '{'!");
        c = reader.getChar();

        boolean done = false;
        while (!done) {
            c = reader.skipWhitespace(c);
            // Allow for an empty object!
            if (!JSONObjectProvider.isObjectEnd(c)) {
                JSONStringProvider property = createString(getErrorHandler());
                property.readJSON(c, reader);

                c = reader.getChar();
                c = reader.skipWhitespace(c);
                reader.assertChar(c, ':', "Expected a ':' after the name '" + property + "'!");

                c = reader.getChar();
                c = reader.skipWhitespace(c);

                JSONValueProvider value = JSONValueProvider.resolveAndParseJSONValue(c, reader, getErrorHandler());

                this.values.put(property, value);

                c = reader.getChar();
                c = reader.skipWhitespace(c);
            }

            if (JSONObjectProvider.isObjectEnd(c)) {
                done = true;
            }
            else {
                reader.assertChar(c, ",", "There must be a comma ',' after each name, value pair, or an '}' to end the object!");
                c = reader.getChar();
            }
        }
    }

    /**
     * Writes the contents of this JSONObject model to the specified output stream in JSON format.
     *
     * @param writer The JSONWriter to write to.
     * @param compact Write json in compact format.
     *
     * @throws APSIOException on IO problems.
     */
    @Override
    protected void writeJSON(JSONWriter writer, boolean compact) throws APSIOException {
        if (!compact) {
            writer.writeln("");
            writer.write(getIndent());
        }
        writer.write("{");
        if (!compact) {
            writer.writeln("");
        }

        int current = 0;
        int max = this.values.size();

        //noinspection unchecked
        Set<JSONStringProvider> propNames = (Set) getValueNames();
        for (JSONStringProvider property : propNames) {
            JSONValueProvider value = this.values.get(property);
            value.setIndent(getIndent() + "    ");

            if (!compact) {
                writer.write(value.getIndent());
            }
            property.writeJSON(writer, compact);

            writer.write(": ");
            value.writeJSON(writer, compact);

            ++current;
            if (current < max) {
                writer.write(", ");
                if (!compact) {
                    writer.writeln("");
                }
            }
        }

        if (!compact) {
            writer.writeln("");
            writer.write(getIndent());
        }
        writer.write("}");
        if (!compact) {
            writer.writeln("");
        }
    }

}
