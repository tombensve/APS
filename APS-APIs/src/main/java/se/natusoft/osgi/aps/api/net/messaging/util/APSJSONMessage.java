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
 *         2014-10-27: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.messaging.util;

import org.osgi.service.log.LogService;
import se.natusoft.osgi.aps.api.net.messaging.types.APSData;
import se.natusoft.osgi.aps.codedoc.Implements;
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService;
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage;

import java.io.*;

/**
 * Possible base messaging class for JSON based types. Makes use of the APSJSONService to read and write JSON.
 */
public class APSJSONMessage extends APSMessage.Default implements JSONErrorHandler {

    //
    // Private Members
    //

    /** The JSON service to user for creating and reading JSON. */
    private APSJSONService jsonService;

    /** A potential JSON read error. */
    private String jsonReadErrorMessage = null;

    /** A potential JSON read error cause. */
    private Throwable jsonReadErrorCause = null;

    /** An OSGi LogService instance. (hint: also implemented by APSToolsLib:APSLogger). This is optional. */
    private LogService logService= null;

    //
    // Constructors
    //

    /**
     * Creates a new APSJSONMessage.
     *
     * @param jsonService The APSJSONService to use for reading and writing JSON.
     */
    public APSJSONMessage(APSJSONService jsonService) {
        this.jsonService = jsonService;
    }

    /**
     * Creates a new APSJSONMessage.
     *
     * @param jsonService The APSJSONService to use for reading and writing JSON.
     * @param logService An OSGi LogService implementation to log to for JSON parsing errors.
     */
    public APSJSONMessage(APSJSONService jsonService, LogService logService) {
        this(jsonService);
        this.logService = logService;
    }

    //
    // Methods
    //

    /**
     * Provide JSON data as a String.
     *
     * @param jsonData The JSON data to provide in the message.
     */
    public void setJSONData(String jsonData) { super.setContent(new APSData.Default(jsonData.getBytes())); }

    /**
     * Provide JSON data as an APSJSONService JSONObject instance.
     *
     * @param jsonMessage The JSON data to provide in the message.
     *
     * @throws APSMessagingException on failure to serialize JSON to byte array.
     */
    public void setJSONData(JSONObject jsonMessage) throws APSMessagingException {
        try {
            APSData content = new APSData.Default();
            OutputStream stream = content.getContentOutputStream();
            getJsonService().writeJSON(stream, jsonMessage);
            stream.close();
            super.setContent(content);
        }
        catch (IOException ioe) {
            throw new APSMessagingException("", ioe);
        }
    }

    /**
     * Returns the JSON data in the message as a String.
     */
    public String getJSONDataAsString() { return new String(super.getContent().getContent()); }

    /**
     * Returns the JSON data in the message as a String.
     *
     * @throws APSMessagingException On failure to deserialize JSON from byte array.
     */
    public JSONObject getJSONDataAsObject() throws APSMessagingException {
        InputStream stream = super.getContent().getContentInputStream();
        JSONValue jsonValue = null;
        try {
            clearJSONErrors();
            jsonValue = getJsonService().readJSON(stream, this);
            assertNoJSONErrors();
        }
        catch (IOException ioe) {
            throw new APSMessagingException("Failed to read JSON from received messaging!", ioe);
        }
        finally {
            try {stream.close();} catch (IOException ok) {}
        }

        if (!JSONObject.class.isAssignableFrom(jsonValue.getClass())) {
            throw new APSMessagingException("Messages must be a JSON objects!");
        }

        return (JSONObject)jsonValue;
    }

    /**
     * Provides the JSON service.
     */
    protected APSJSONService getJsonService() {
        return this.jsonService;
    }

    /**
     * Clears any JSON error messaging.
     */
    private void clearJSONErrors() {
        this.jsonReadErrorMessage = null;
        this.jsonReadErrorCause = null;
    }

    /**
     * Asserts that there have been no JSON errors. If there have an IOException is thrown.
     *
     * @throws IOException
     */
    private void assertNoJSONErrors() throws IOException {
        if (this.jsonReadErrorMessage != null) {
            throw new IOException(this.jsonReadErrorMessage, this.jsonReadErrorCause);
        }
    }

    /**
     * Warns about something.
     *
     * @param message The warning messaging.
     */
    @Override
    @Implements(JSONErrorHandler.class)
    public void warning(String message) {
        if (this.logService != null) {
            this.logService.log(LogService.LOG_WARNING, message);
        }
    }

    /**
     * Indicate failure.
     *
     * @param message The failure messaging.
     * @param cause   The cause of the failure. Can be null!
     * @throws RuntimeException This method must throw a RuntimeException.
     */
    @Override
    @Implements(JSONErrorHandler.class)
    public void fail(String message, Throwable cause) {
        this.jsonReadErrorMessage = message;
        this.jsonReadErrorCause = cause;
        if (this.logService != null) {
            this.logService.log(LogService.LOG_ERROR, message, cause);
        }
    }
}
