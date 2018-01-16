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

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValueFactory;
import se.natusoft.osgi.aps.api.reactive.APSHandler;
import se.natusoft.osgi.aps.api.reactive.APSResult;
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
     * @param in            The stream to read from.
     * @param resultHandler Receives a APSResult containing the read JSON or an Exception on failure.
     */
    void readJSON(@NotNull InputStream in, @NotNull APSHandler<APSResult<JSONValue>> resultHandler);

    /**
     * Reads JSON from an InputStream producing a `Map<String, Object>`.
     *
     * @param in            The stream to read from. *Must* be a JSON object! Does not support a sub JSON structure.
     * @param resultHandler This will receive an APSResult containing a Map&lt;String, Object&gt; or an Exception on failure.
     */
    void readJSONObject(@NotNull InputStream in, @NotNull APSHandler<APSResult<Map<String, Object>>> resultHandler);

    /**
     * Writes a _JSONValue_ to an _OutputStream_ in compact format.
     *
     * @param out           The stream to write to.
     * @param jsonValue     The value to write.
     * @param resultHandler Will be called with result if provided. Only success() or failure() are valid. result() will always be null.
     */
    void writeJSON(@NotNull OutputStream out, @NotNull JSONValue jsonValue, @Nullable APSHandler<APSResult<Void>> resultHandler);

    /**
     * Writes a _JSONValue_ to an _OutputStream_.
     *
     * @param out           The stream to write to.
     * @param jsonValue     The value to write.
     * @param compact       If true then the output is compact and hard to read, if false then the output is easy to read and larger with indents.
     * @param resultHandler Will be called with the result if provided. Only success() or failure() are valid. result() will always be null.
     */
    void writeJSON(@NotNull OutputStream out, @NotNull JSONValue jsonValue, boolean compact,
                   @Nullable APSHandler<APSResult<Void>> resultHandler);

    /**
     * Writes a JSON _Map_ to an _OutputStream_.
     *
     * @param out           The output stream to write to.
     * @param jsonMap       The Map to write.
     * @param resultHandler Will be called with result if provided. Only success() or failure() are valid. result() will always be null.
     */
    void writeJSONObject(@NotNull OutputStream out, @NotNull Map<String, Object> jsonMap,
                         @Nullable APSHandler<APSResult<Void>> resultHandler);

    /**
     * Converts a JSONObject into a `Map<String, Object>`. This supports working with a JSON structure using a standard
     * java.util.Map. This works well in languages like Groovy.
     *
     * @param jsonObject The JSONObject to convert.
     * @return A Map containing the same structure as the JSONObject.
     */
    @NotNull Map<String, Object> toMap(@NotNull JSONObject jsonObject);

    /**
     * Converts a `Map<String, Object>` into a JSONObject. This supports working with a JSON structure using a standard
     * java.util.Map. This works well in languages like Groovy.
     *
     * @param jsonMap The map to convert. Expects a JSON compatible structure!
     * @return The converted to JSONObject.
     */
    @NotNull JSONObject toJSONObject(@NotNull Map<String, Object> jsonMap);

    /**
     * Provides some static tools for this service that is independent of the service implementation.
     */
    class Tools {

        /**
         * Converts a JSONValue into bytes.
         *
         * @param jsonValue   The JSONValue to convert.
         * @param jsonService The APSJSONService to use.
         * @return A byte array.
         * @throws APSIOException on any IO failure.
         */
        public static byte[] toBytes(@NotNull JSONValue jsonValue, @NotNull APSJSONService jsonService) throws APSIOException {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                jsonService.writeJSON(baos, jsonValue, null);
                baos.close();
                return baos.toByteArray();
            } catch (IOException ioe) {
                throw new APSIOException(ioe.getMessage(), ioe);
            }
        }

        /**
         * Converts a byte array into a JSONValue object. For this to work the byte array of course must contain valid JSON!
         *
         * @param bytes         The bytes to convert.
         * @param jsonService   The APSJSONService to use for converting.
         * @param resultHandler Called with result.
         */
        public static void fromBytes(byte[] bytes, @NotNull APSJSONService jsonService,
                                     @NotNull APSHandler<APSResult<JSONValue>> resultHandler) {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            try {
                jsonService.readJSON(bais, resultHandler);
            } finally {
                try {
                    bais.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
