/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Library
 *     
 *     Code Version
 *         0.9.0
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
 * @author Tommy Svensson
 */
public class JSONBoolean extends JSONValue {
    //
    // private Members
    //

    /** The boolean value. */
    private boolean value;

    //
    // Constructors
    //

    /**
     * Creates a new JSONBoolean instance for writing JSON output.
     *
     * @param value The value for this boolean.
     */
    public JSONBoolean(boolean value) {
        super();
        this.value = value;
    }

    /**
     * Creates a new JSONBoolean instance for reading JSON input or writing JSON output.
     *
     * @param errorHandler
     */
    public JSONBoolean(JSONErrorHandler errorHandler) {
        super(errorHandler);
    }

    //
    // Methods
    //

    /**
     * Determines if the specified character denotes the start of a boolean value.
     *
     * @param c The character to test.
     */
    /*package*/ static boolean isBooleanStart(char c) {
        return c == 't' || c == 'f';
    }

    /**
     * Sets the value of this boolean.
     *
     * @param value The value to set.
     */
    public void setBooleanValue(boolean value) {
        this.value = value;
    }

    /**
     * Returns the value of this boolean.
     */
    public boolean getAsBoolean() {
        return this.value;
    }

    /**
     * Returns the value of this boolean as a String.
     */
    public String toString() {
        return "" + this.value;
    }

    /**
     * Loads the contents of this JSONBoolean model from the specified input stream.
     *
     * @param c A preread character from the input stream.
     * @param reader The JSONReader to read from.
     *
     * @throws IOException
     */
    @Override
    protected void readJSON(char c, JSONReader reader) throws IOException {
        if (c == 't') {
            c = reader.getChar();
            reader.assertChar(c, 'r' , "Expected a 'r' character (Second character of 'true')!");
            c = reader.getChar();
            reader.assertChar(c, 'u' , "Expected a 'u' character (Third character of 'true')!");
            c = reader.getChar();
            reader.assertChar(c, 'e' , "Expected a 'e' character (Fourth character of 'true')!");
            this.value = true;
        }
        else if (c == 'f') {
            c = reader.getChar();
            reader.assertChar(c, 'a' , "Expected a 'a' character (Second character of 'false')!");
            c = reader.getChar();
            reader.assertChar(c, 'l' , "Expected a 'l' character (Third character of 'false')!");
            c = reader.getChar();
            reader.assertChar(c, 's' , "Expected a 's' character (Fourth character of 'false')!");
            c = reader.getChar();
            reader.assertChar(c, 'e' , "Expected a 'e' character (Firth character of 'false')!");
            this.value = false;
        }
        else {
            fail("A boolean value must start with either a 't' or a 'f'!");
        }
    }

    /**
     * Writes the contents of this JSONBoolean to the specified output stream in JSON format.
     *
     * @param writer The JSONWriter to write to.
     * @param compact Write json in compact format.
     * 
     * @throws IOException
     */
    @Override
    protected void writeJSON(JSONWriter writer, boolean compact) throws IOException {
        writer.write(new Boolean(this.value).toString());
    }

    
}
