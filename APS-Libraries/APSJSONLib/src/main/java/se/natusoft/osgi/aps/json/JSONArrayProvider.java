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
import se.natusoft.osgi.aps.api.misc.json.model.JSONArray;
import se.natusoft.osgi.aps.exceptions.APSIOException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is based on the structure defined on http://www.json.org/.
 *
 * This represents the "array" diagram on the above mentioned web page:
 *
 *                   _______________________
 *                  /                       \
 *                  |                       |
 *     |_____ ([) __/_______ (value) _______\__ (]) _____|
 *     |              /                   \              |
 *                    |                   |
 *                    \_______ (,) _______/
 *
 * @author Tommy Svensson
 */
@SuppressWarnings("WeakerAccess")
public class JSONArrayProvider extends JSONValueProvider implements JSONArray {
    //
    // Private Members
    //

    /** The array values. */
    private List<JSONValueProvider> values = new ArrayList<>();

    //
    // Constructors
    //

    /**
     * Creates a new JSONArray for wrinting JSON output.
     */
    public JSONArrayProvider() {
        super();
    }

    /**
     * Creates a new JSONArray for reading JSON input and writing JSON output.
     *
     * @param errorHandler The error handler to use.
     */
    public JSONArrayProvider(JSONErrorHandler errorHandler) {
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
    /*package*/ @SuppressWarnings("WeakerAccess")
    static boolean isArrayEnd(char c) {
        return c == ']';
    }

    /**
     * Adds a value to the array.
     *
     * @param value The value to add.
     */
    public void addValue(JSONValueProvider value) {
        this.values.add(value);
    }

    /**
     * Adds a value to the array.
     *
     * @param value The value to add.
     */
    @Override
    public void addValue(se.natusoft.osgi.aps.api.misc.json.model.JSONValue value) {

    }

    /**
     * Returns the array values as a List.
     */
    public List<se.natusoft.osgi.aps.api.misc.json.model.JSONValue> getAsList() {
        // Yes, this is ridiculous!
        // I can add a complete List of implementations of JSONValue into a list of the implemented interface,
        // but there is no way in hell that I can cast one to the other even though the end result would be
        // identical!! "List<? extends JSONValue> values" failed also. Then it was suddenly impossible to add
        // new implementations to the list. No matter what you do, there is some place where you have to
        // write crazy, ridiculous code like this!
        List<se.natusoft.osgi.aps.api.misc.json.model.JSONValue> list = new LinkedList<>();
        list.addAll(this.values);
        return list;
    }

    /**
     * Returns the array values as a list of a specific type.
     *
     * @param type The class of the type to return values as a list of.
     * @param <T> One of the JSONValue subclasses.
     * @return A list of specified type if type is the same as in the list.
     */
    public <T extends se.natusoft.osgi.aps.api.misc.json.model.JSONValue> List<T> getAsList(Class<T> type) {
        List<T> list = new LinkedList<>();
        for (se.natusoft.osgi.aps.api.misc.json.model.JSONValue value : this.values) {
            //noinspection unchecked
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
     * @throws APSIOException on I/O failures.
     */
    @Override
    protected void readJSON(char c, JSONReader reader) throws APSIOException {
        c = reader.skipWhitespace(c);
        reader.assertChar(c, '[', "A JSON array must start with a '[' char!");
        c = reader.getChar();

        boolean done = false;
        while (!done) {
            c = reader.skipWhitespace(c);

            JSONValueProvider value = null;
            if (!JSONArrayProvider.isArrayEnd(c)) {
                value = JSONValueProvider.resolveAndParseJSONValue(c, reader, getErrorHandler());
            }

            if (value != null) {
                this.values.add(value);

                c = reader.getChar();
                c = reader.skipWhitespace(c);
                if (JSONArrayProvider.isArrayEnd(c)) {
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
     * @throws APSIOException on IOFailure.
     */
    @Override
    protected void writeJSON(JSONWriter writer, boolean compact) throws APSIOException {
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
        for (JSONValueProvider value : this.values) {

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
