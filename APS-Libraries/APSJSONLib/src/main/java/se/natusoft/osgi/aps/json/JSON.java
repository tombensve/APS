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

import java.io.*;

/**
 * This is the official API for reading and writing JSON values.
 */
public class JSON {

    /**
     * Reads any JSON object from the specified InputStream.
     *
     * @param jsonIn The InputStream to read from.
     * @param errorHandler An implementation of this interface should be supplied by the user to handle any errors during JSON parsing.
     *
     * @return A JSONValue subclass. Which depends on what was found on the stream.
     *
     * @throws IOException on any IO failures.
     */
    public static JSONValue read(InputStream jsonIn, JSONErrorHandler errorHandler) throws IOException {
        // Even though JSON is supposedly UTF-8 as far as I can determine from the web I decided
        // to use a Reader and read chars to support other.
        JSONValue.JSONReader reader = new JSONValue.JSONReader(new PushbackReader(new InputStreamReader(jsonIn)), errorHandler);

        char c = reader.getChar();
        JSONValue value = JSONValue.resolveAndParseJSONValue(c, reader, errorHandler);

        return value;
    }

    /**
     * Writes a JSONValue to an OutputStream. This will write compact output by default.
     *
     * @param jsonOut The OutputStream to write to.
     * @param value The value to write.
     *
     * @throws IOException on failure.
     */
    public static void write(OutputStream jsonOut, JSONValue value) throws IOException {
        value.writeJSON(jsonOut);
    }

    /**
     * Writes a JSONValue to an OutputStream.
     *
     * @param jsonOut The OutputStream to write to.
     * @param value The value to write.
     * @param compact If true the written JSON is made very compact and hard to read but produce less data.
     *                If false then the output will be larger but readable with indents. Use this when debugging.
     * @throws IOException
     */
    public static void write(OutputStream jsonOut, JSONValue value, boolean compact) throws IOException {
        value.writeJSON(jsonOut, compact);
    }

}
