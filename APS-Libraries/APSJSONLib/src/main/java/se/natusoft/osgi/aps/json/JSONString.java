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

/**
 * This class is based on the structure defined on http://www.json.org/.
 *
 * This represents the "string" diagram on the above mentioned web page:
 *
 *                ___________________________________________________________
 *               /    ____________________________________________________   \
 *               |   /                                                    \  |
 *     |___ (") _|___|___ (*1)                                        ____|__|_ (") ___|
 *     |           \                                                   /               |
 *                  |                                                  |
 *                  \__ (\) ___ (") (quotation mark) __________________|
 *                          |__ (\) (reverse solidus) _________________|
 *                          |__ (/) (solidus) _________________________|
 *                          |__ (b) (backspace) _______________________|
 *                          |__ (f) (formfeed) ________________________|
 *                          |__ (n) (newline) _________________________|
 *                          |__ (r) (carriage return) _________________|
 *                          |__ (t) (orizontal tab) ___________________|
 *                          \__ (u) (4 hexadecimal digits) ____________/
 *
 *     *1: Any UNICODE character except " or \ or control character
 *
 * @author Tommy Svensson
 */
public class JSONString extends JSONValue {
    //
    // Private Members
    //

    /** The value of this string. */
    private String value = "";

    //
    // Constructors
    //

    /**
     * Creates a new JSONString for writing JSON output.
     *
     * @param value The value of this JSONString.
     */
    public JSONString(String value) {
        super();
        this.value = value;
    }

    /**
     * Creates a new JSONString for reading JSON input and writing JSON output.
     *
     * @param errorHandler
     */
    public JSONString(JSONErrorHandler errorHandler) {
        super(errorHandler);
    }

    //
    // Methods
    //

    /**
     * Determines if the specified character denotes the start of an JSON string value.
     *
     * @param c The character to test.
     */
    /*package*/ static boolean isStringStart(char c) {
        return c == '"';
    }

    /**
     * Sets the string value. 
     * 
     * @param value The value to set.
     */
    /*pacakge*/ void setValue(String value) {
        this.value = value;
    }

    /**
     * Loads this JSONString model from the specified input stream.
     *
     * @param c A preread character from the input stream.
     * @param reader The JSONReader to read from.
     *
     * @throws IOException
     */
    @Override
    protected void readJSON(char c, JSONReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        c = reader.skipWhitespace(c);
        reader.assertChar(c, '"', "Expected a quote '\"' character starting a string, found '" + c + "'!");
        boolean stringEscape = true; // The reason for this is to make clear what the parameter represents!
        //noinspection ConstantConditions
        c = reader.readUntil("\"", sb, stringEscape);
        //assertChar(c, '"', "Expected a quote '\"' character ending a string, found '" + c + '!');
        this.value = sb.toString();
    }

    /**
     * Writes the contents of this JSONString to the specified output stream in JSON format.
     *
     * @param writer The JSONWriter to write to.
     * @param compact If true write compact.
     *
     * @throws IOException
     */
    @Override
    protected void writeJSON(JSONWriter writer, boolean compact) throws IOException {
        writer.write("\"");
        writer.write(this.value.replace("\"", "\\\""));
        writer.write("\"");
    }

    /**
     * Converts this to a String.
     */
    @Override
    public String toString() {
        return this.value;
    }

    // This is used as a Map key in JSONObject so we need to handle hashCode() and equals().

    /**
     * Returns the hash code of this instance.
     */
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    /**
     * Compares this object with another for equality.
     *
     * @param obj The object to compare to.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JSONString other = (JSONString) obj;
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        return true;
    }
}
