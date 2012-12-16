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

import java.io.IOException;
import java.util.ArrayList;
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
public class JSONArray extends JSONValue{
    //
    // Private Members
    //

    /** The array values. */
    private List<JSONValue> values = new ArrayList<JSONValue>();

    //
    // Constructors
    //

    /**
     * Creates a new JSONArray for wrinting JSON output.
     */
    public JSONArray() {
        super();
    }

    /**
     * Creates a new JSONArray for reading JSON input and writing JSON output.
     *
     * @param errorHandler
     */
    public JSONArray(JSONErrorHandler errorHandler) {
        super(errorHandler);
    }

    //
    // Methods
    //

    /**
     * Checks if the specified character denotes a start of a JSON array.
     * 
     * @param c The character to test.
     */
    /*package*/ static boolean isArrayStart(char c) {
        return c == '[';
    }

    /**
     * Checks if the specified character denotes an end of a JSON array.
     *
     * @param c The character to test.
     */
    /*package*/ static boolean isArrayEnd(char c) {
        return c == ']';
    }

    /**
     * Adds a value to the array.
     *
     * @param value The value to add.
     */
    public void addValue(JSONValue value) {
        this.values.add(value);
    }

    /**
     * Returns the array values as a List.
     */
    public List<JSONValue> getAsList() {
        return this.values;
    }

    /**
     * Returns the array values as a list of a specific type.
     * 
     * @param type The class of the type to return values as a list of.
     * @param <T> One of the JSONValue subclasses.
     * @return A list of specified type if type is the same as in the list.
     */
    public <T extends JSONValue> List<T> getAsList(Class<T> type) {
        List<T> list = new LinkedList<T>();
        for (JSONValue value : this.values) {
            list.add((T)value);
        }
        
        return list;
    }
    
    /**
     * Loads this JSONArray model with data from the specified input stream.
     *
     * @param c A preread character from the input stream.
     * @param reader The JSONReader to read from.
     *
     * @throws IOException
     */
    @Override
    protected void readJSON(char c, JSONReader reader) throws IOException {
        c = reader.skipWhitespace(c);
        reader.assertChar(c, '[', "A JSON array must start with a '[' char!");
        c = reader.getChar();
        
        boolean done = false;
        while (!done) {
            c = reader.skipWhitespace(c);

            JSONValue value = null;
            if (JSONArray.isArrayEnd(c)) {
                // Do nothing, we have an empty array!
            }
            else {
                value = JSONValue.resolveAndParseJSONValue(c, reader, getErrorHandler());
            }

            if (value != null) {
                this.values.add(value);

                c = reader.getChar();
                c = reader.skipWhitespace(c);
                if (JSONArray.isArrayEnd(c)) {
                    done = true;
                }
                else {
                    reader.assertChar(c, ",", "An array value must be followed by a comma ',' or ended with a ']'! Found unexpected char: '" + c + "'!");
                    c = reader.getChar();
                }
            }
            else {
                // value is only null if we already have encountered and end of the array!
                done = true;
            }
        }
    }

    /**
     * Writes the JSONArray content in JSON format on the specified output stream.
     *
     * @param writer The writer to write to.
     * @param compact Write json in compact format.
     * @throws IOException
     */
    @Override
    protected void writeJSON(JSONWriter writer, boolean compact) throws IOException {
        if (!compact) {
            writer.writeln("");
            writer.write(getIndent());
        }
        writer.write("[");
        if (!compact) {
            writer.writeln("");
        }

        int current = 0;
        int max = this.values.size();
        for (JSONValue value : this.values) {
            value.setIndent(getIndent() + "    ");

            if (!compact) {
                writer.write(value.getIndent());
            }
            value.writeJSON(writer, compact);

            ++current;
            if (current < max) {
                writer.write(", ");
                if (!compact) {
                    writer.writeln("");
                }
            }
            else {
                if (!compact) {
                    writer.writeln("");
                }
            }
        }

        if (!compact) {
            writer.write(getIndent());
        }
        writer.write("]");
    }

}
