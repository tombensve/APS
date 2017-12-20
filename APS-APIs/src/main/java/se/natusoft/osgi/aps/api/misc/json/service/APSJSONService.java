/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
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
 *         2012-01-17: Created!
 *
 */
package se.natusoft.osgi.aps.api.misc.json.service;

import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValueFactory;
import se.natusoft.osgi.aps.exceptions.APSIOException;

import java.io.*;
import java.util.Map;

/**
 * This provides a service for reading and writing JSON and models representing the different JSON structures.
 */
public interface APSJSONService extends JSONValueFactory {

    /**
     * Reads JSON from an InputStream producing a _JSONValue_ subclass depending on what is on the stream.
     *
     * @param in The stream to read from.
     * @param errorHandler An optional error handler for parsing errors. This can be null in which case all parsing errors are ignored.
     *
     * @return A JSONObject.
     *
     * @throws APSIOException on IO failure.
     */
    JSONValue readJSON(InputStream in, JSONErrorHandler errorHandler) throws APSIOException;

    /**
     * Reads JSON from an InputStream producing a `Map<String, Object>`.
     *
     * @param in The stream to read from. *Must* be a JSON object! Does not support a sub JSON structure.
     * @param errorHandler An optional error handler for parsing errors. This can be null in which case all parsing errors are ignored.
     *
     * @return A Map of read JSON data.
     *
     * @throws APSIOException on IO failure.
     */
    Map<String, Object> readJSONObject(InputStream in, JSONErrorHandler errorHandler) throws APSIOException;

    /**
     * Writes a _JSONValue_ to an _OutputStream_ in compact format.
     *
     * @param out The stream to write to.
     * @param jsonValue The value to write.
     *
     * @throws APSIOException on IO failure.
     */
    void writeJSON(OutputStream out, JSONValue jsonValue) throws APSIOException;

    /**
     * Writes a _JSONValue_ to an _OutputStream_.
     *
     * @param out The stream to write to.
     * @param jsonValue The value to write.
     * @param compact If true then the output is compact and hard to read, if false then the output is easy to read and larger with indents.
     *
     * @throws APSIOException on IO failure.
     */
    void writeJSON(OutputStream out, JSONValue jsonValue, boolean compact) throws APSIOException;

    /**
     * Converts a JSONObject into a `Map<String, Object>`. This supports working with a JSON structure using a standard
     * java.util.Map. This works well in languages like Groovy.
     *
     * @param jsonObject The JSONObject to convert.
     *
     * @return A Map containing the same structure as the JSONObject.
     */
    Map<String, Object> toMap(JSONObject jsonObject);

    /**
     * Writes a JSON _Map_ to an _OutputStream_.
     *
     * @param out The output stream to write to.
     * @param jsonMap The Map to write.
     *
     * @throws APSIOException on IO failure.
     */
    void writeJSONObject(OutputStream out, Map<String, Object> jsonMap) throws APSIOException;

    /**
     * Converts a `Map<String, Object>` into a JSONObject. This supports working with a JSON structure using a standard
     * java.util.Map. This works well in languages like Groovy.
     *
     * @param jsonMap The map to convert. Expects a JSON compatible structure!
     *
     * @return The converted to JSONObject.
     */
    JSONObject toJSONObject(Map<String, Object> jsonMap);

    /**
     * Provides some static tools for this service that is independent of the service implementation.
     */
    class Tools {

        /**
         * Converts a JSONValue into bytes.
         *
         * @param jsonValue The JSONValue to convert.
         * @param jsonService The APSJSONService to use.
         *
         * @return A byte array.
         *
         * @throws APSIOException on any IO failure.
         */
        public static byte[] toBytes(JSONValue jsonValue, APSJSONService jsonService) throws APSIOException {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                jsonService.writeJSON(baos, jsonValue);
                baos.close();
                return baos.toByteArray();
            }
            catch(IOException ioe) {
                throw new APSIOException(ioe.getMessage(), ioe);
            }
        }

        /**
         * Converts a byte array into a JSONValue object. For this to work the byte array of course must contain valid JSON!
         *
         * @param bytes The bytes to convert.
         * @param jsonService The APSJSONService to use for converting.
         * @param errorHandler The error handler to use when reading. Can be null.
         *
         * @return A converted JSONValue.
         *
         * @throws APSIOException on any failure.
         */
        public static JSONValue fromBytes(byte[] bytes, APSJSONService jsonService, JSONErrorHandler errorHandler) throws APSIOException {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                JSONValue value = jsonService.readJSON(bais, errorHandler);
                bais.close();
                return value;
            }
            catch (IOException ioe) {
                throw new APSIOException(ioe.getMessage(), ioe);
            }
        }
    }
}
