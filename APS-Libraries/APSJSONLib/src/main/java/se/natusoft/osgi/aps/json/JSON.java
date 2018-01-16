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

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.reactive.APSHandler;
import se.natusoft.osgi.aps.api.reactive.APSResult;
import se.natusoft.osgi.aps.api.reactive.APSValue;
import se.natusoft.osgi.aps.exceptions.APSIOException;
import se.natusoft.osgi.aps.json.tools.CollectingErrorHandler;

import java.io.*;

/**
 * This is the official API for reading and writing JSON values.
 */
public class JSON {

    /**
     * Reads any JSON object from the specified _InputStream_.
     *
     * @param jsonIn        The InputStream to read from.
     * @param resultHandler The handler to call with result.
     */
    public static void read(@NotNull InputStream jsonIn, @NotNull APSHandler<APSResult<JSONValue>> resultHandler) {
        // Fails will cause an APSIOException!
        CollectingErrorHandler errorHandler = new CollectingErrorHandler(true);

        try {
            JSONValueProvider.JSONReader reader =
                    new JSONValueProvider.JSONReader(new PushbackReader(new InputStreamReader(jsonIn, "UTF-8")), errorHandler);

            char c = reader.getChar();

            JSONValue value = JSONValueProvider.resolveAndParseJSONValue(c, reader, errorHandler);

            resultHandler.handle(new APSResult.Provider<>(new APSValue.Provider<>(value)));
        } catch (IOException | APSIOException ioe) {
            resultHandler.handle(new APSResult.Provider<>(ioe));
        }
    }

    /**
     * Reads any JSON object from the specified _InputStream_.
     *
     * @param jsonIn The InputStream to read from.
     * @param errorHandler An implementation of this interface should be supplied by the user to handle any errors during JSON parsing.
     *
     * @return A JSONValue subclass. Which depends on what was found on the stream.
     *
     * @throws APSIOException on any IO failures.
     */
    public static JSONValue read(@NotNull InputStream jsonIn, @NotNull JSONErrorHandler errorHandler) {

        try {
            JSONValueProvider.JSONReader reader =
                    new JSONValueProvider.JSONReader(new PushbackReader(new InputStreamReader(jsonIn, "UTF-8")), errorHandler);

            char c = reader.getChar();

            return JSONValueProvider.resolveAndParseJSONValue(c, reader, errorHandler);
        }
        catch (IOException ioe) {
            throw new APSIOException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Writes a _JSONValue_ to an _OutputStream_. This will write compact output by default.
     *
     * @param jsonOut The OutputStream to write to.
     * @param value   The value to write.
     * @param resultHandler handler for result. only success() or failure() is relevant.
     */
    @SuppressWarnings("Duplicates")
    public static void write(@NotNull OutputStream jsonOut, @NotNull JSONValue value, @Nullable APSHandler<APSResult<Void>> resultHandler) {
        try {
            write(jsonOut, value);
            if (resultHandler != null) resultHandler.handle(APSResult.success(null));
        }
        catch (APSIOException ioe) {
            if (resultHandler != null) resultHandler.handle(APSResult.failure(ioe));
        }
    }

    /**
     * Writes a _JSONValue_ to an _OutputStream_. This will write compact output by default.
     *
     * @param jsonOut The OutputStream to write to.
     * @param value   The value to write.
     * @throws APSIOException on failure.
     */
    public static void write(OutputStream jsonOut, JSONValue value) throws APSIOException {
        ((JSONValueProvider) value).writeJSON(jsonOut);
    }

    /**
     * Writes a _JSONValue_ to an _OutputStream_. This will write compact output by default.
     *
     * @param jsonOut The OutputStream to write to.
     * @param value   The value to write.
     * @param resultHandler handler for result. only success() or failure() is relevant.
     */
    public static void write(@NotNull OutputStream jsonOut, @NotNull JSONValue value, boolean compact,
                             @Nullable APSHandler<APSResult<Void>> resultHandler) {
        try {
            write(jsonOut, value, compact);
            if (resultHandler != null) resultHandler.handle(APSResult.success(null));
        }
        catch (APSIOException ioe) {
            if (resultHandler != null) resultHandler.handle(APSResult.failure(ioe));
        }
    }

    /**
     * Writes a _JSONValue_ to an _OutputStream_.
     *
     * @param jsonOut The OutputStream to write to.
     * @param value   The value to write.
     * @param compact If true the written JSON is made very compact and hard to read but produce less data.
     *                If false then the output will be larger but readable with indents. Use this when debugging.
     * @throws APSIOException on IO problems.
     */
    public static void write(OutputStream jsonOut, JSONValue value, boolean compact) throws APSIOException {
        ((JSONValueProvider) value).writeJSON(jsonOut, compact);
    }

}
