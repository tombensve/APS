/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.2
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
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValueFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
     * @throws IOException on IO failure.
     */
    public JSONValue readJSON(InputStream in, JSONErrorHandler errorHandler) throws IOException;

    /**
     * Writes a _JSONValue_ to an _OutputStream_ in compact format.
     *
     * @param out The stream to write to.
     * @param jsonValue The value to write.
     *
     * @throws IOException on IO failure.
     */
    public void writeJSON(OutputStream out, JSONValue jsonValue) throws IOException;

    /**
     * Writes a _JSONValue_ to an _OutputStream_.
     *
     * @param out The stream to write to.
     * @param jsonValue The value to write.
     * @param compact If true then the output is compact and hard to read, if false then the output is easy to read and larger with indents.
     *
     * @throws IOException on IO failure.
     */
    public void writeJSON(OutputStream out, JSONValue jsonValue, boolean compact) throws IOException;
}
