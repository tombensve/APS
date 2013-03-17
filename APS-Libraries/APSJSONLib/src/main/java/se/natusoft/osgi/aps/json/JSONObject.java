/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Library
 *     
 *     Code Version
 *         0.9.1
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

import java.io.IOException;
import java.util.HashMap;
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
 * This is also the starting point.
 * <p/>
 * To write JSON, create a new JSONObject (new JSONObject()) and  call addProperty(name, value)
 * for children. Then do jsonObj.writeJSON(outputStream).
 * <p/>
 * To read JSON, create a new JSONObject (new JSONObject(jsonErrorHandler)) and then do jsonObj.readJSON(inputStream).
 * Then use getProperty(name) to extract children.
 *
 * @see JSONValue
 *
 * @author Tommy Svensson
 */
public class JSONObject extends JSONValue {
    //
    // Private Members
    //

    /** The object values. */
    private Map<JSONString, JSONValue> properties = new HashMap<JSONString, JSONValue>();

    //
    // Constructors
    //

    /**
     * Creates a JSONObject instance for writing JSON output. 
     */
    public JSONObject() {}

    /**
     * Creates a new JSONObject instance for reading JSON input or writing JSON output.
     *
     * @param errorHandler
     */
    public JSONObject(JSONErrorHandler errorHandler) {
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
    /*package*/ static boolean isObjectEnd(char c) {
        return c == '}';
    }


    /**
     * Returns the names of the available properties.
     */
    public Set<JSONString> getPropertyNames() {
        return this.properties.keySet();
    }

    /**
     * Returns the named property.
     *
     * @param name The name of the property to get.
     */
    public JSONValue getProperty(JSONString name) {
        return this.properties.get(name);
    }

    /**
     * Returns the named property.
     *
     * @param name The name of the property to get.
     */
    public JSONValue getProperty(String name) {
        return this.properties.get(new JSONString(name));
    }

    /**
     * Adds a property to this JSONObject instance.
     *
     * @param name The name of the property.
     * @param value The property value.
     */
    public void addProperty(JSONString name, JSONValue value) {
        this.properties.put(name, value);
    }

    /**
     * Adds a property to this JSONObject instance.
     *
     * @param name The name of the property.
     * @param value The property value.
     */
    public void addProperty(String name, JSONValue value) {
        this.properties.put(new JSONString(name), value);
    }

    /**
     * Loads this JSONObject model from the specified input stream.
     *
     * @param c A pre-read character from the input stream.
     * @param reader The JSONReader to read from.
     *
     * @throws IOException
     */
    @Override
    protected void readJSON(char c, JSONReader reader) throws IOException {
        c = reader.skipWhitespace(c);
        reader.assertChar(c, '{', "A JSON object must start with '{'!");
        c = reader.getChar();
        
        boolean done = false;
        while (!done) {
            c = reader.skipWhitespace(c);
            JSONString property = createString(getErrorHandler());
            property.readJSON(c, reader);
            
            c = reader.getChar();
            c = reader.skipWhitespace(c);
            reader.assertChar(c, ':', "Expected a ':' after the name '" + property + "'!");

            c = reader.getChar();
            c = reader.skipWhitespace(c);

            JSONValue value = JSONValue.resolveAndParseJSONValue(c, reader, getErrorHandler());

            this.properties.put(property, value);
            
            c = reader.getChar();
            c = reader.skipWhitespace(c);

            if (JSONObject.isObjectEnd(c)) {
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
     * @throws IOException
     */
    @Override
    protected void writeJSON(JSONWriter writer, boolean compact) throws IOException {
        if (!compact) {
            writer.writeln("");
            writer.write(getIndent());
        }
        writer.write("{");
        if (!compact) {
            writer.writeln("");
        }

        int current = 0;
        int max = this.properties.size();

        for (JSONString property : getPropertyNames()) {
            JSONValue value = this.properties.get(property);
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
