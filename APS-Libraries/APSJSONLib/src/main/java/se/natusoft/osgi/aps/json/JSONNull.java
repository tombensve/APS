/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Library
 *     
 *     Code Version
 *         0.11.0
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
 * This class is based on the structure defined on <http://www.json.org/>.
 *
 * @author Tommy Svensson
 */
public class JSONNull extends JSONValue {
    //
    // Constructors
    //

    /**
     * Creates a new JSONNull instance for writing JSON output.
     */
    public JSONNull() {
        super();
    }

    /**
     * Creates a new JSONNull instance for reading JSON input or writing JSON output.
     *
     * @param errorHandler
     */
    public JSONNull(JSONErrorHandler errorHandler) {
        super(errorHandler);
    }

    //
    // Methods
    //

    /**
     * Determines if the specified character denotes the start of a null value.
     *
     * @param c The character to check.
     */
    /*package*/ static boolean isNullStart(char c) {
        return c == 'n';
    }

    /**
     * @return as String.
     */
    public String toString() {
        return "null";
    }
    
    /**
     * Reads the content of the JSONNull model from the specified input stream.
     *
     * @param c A preread character from the input stream.
     * @param reader The JSONReader to read from.
     *
     * @throws IOException
     */
    @Override
    protected void readJSON(char c, JSONReader reader) throws IOException {
        reader.assertChar(c, "n", "Expected an 'n' (first char in \"null\"!");
        c = reader.getChar();
        reader.assertChar(c, "u", "Expected an 'u' (second char in \"null\"!");
        c = reader.getChar();
        reader.assertChar(c, "l", "Expected an 'l' (third char in \"null\"!");
        c = reader.getChar();
        reader.assertChar(c, "l", "Expected an 'l' (fourth char in \"null\"!");
    }

    /**
     * Writes the content of this JSONNull model to the specified output stream in JSON format.
     *
     * @param writer The JSONWriter to write to.
     * @param compact Write json in compact format.
     * 
     * @throws IOException
     */
    @Override
    protected void writeJSON(JSONWriter writer, boolean compact) throws IOException {
        writer.write("null");
    }

}
