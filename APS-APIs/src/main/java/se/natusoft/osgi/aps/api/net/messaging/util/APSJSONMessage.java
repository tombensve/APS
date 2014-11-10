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
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService;
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.messages.APSMessage;

import java.io.*;

/**
 * Possible base messaging class for JSON based messages. Makes use of the APSJSONService to read and write JSON.
 */
public class APSJSONMessage extends APSMessage.Provider implements JSONErrorHandler {

    //
    // Private Members
    //

    /** The JSON service to user for creating and reading JSON. */
    private APSJSONService jsonService;

    /** A potential JSON read error. */
    private String jsonReadErrorMessage = null;

    /** A potential JSON read error cause. */
    private Throwable jsonReadErrorCause = null;

    /** An OSGi LogService instance. (hint: implemented by APSToolsLib:APSLogger). This is optional. */
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

    public void setJSONData(String jsonData) {
        super.data(jsonData.getBytes());
    }

    public void setJSONData(JSONObject jsonMessage) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        getJsonService().writeJSON(stream, jsonMessage);
        stream.close();
        super.data(stream.toByteArray());
    }

    public String getJSONDataAsString() {
        return new String(super.getData());
    }

    public JSONObject getJSONDataAsObject() throws APSMessagingException {
        ByteArrayInputStream stream = new ByteArrayInputStream(super.getData());
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
    public void fail(String message, Throwable cause) throws RuntimeException {
        this.jsonReadErrorMessage = message;
        this.jsonReadErrorCause = cause;
        if (this.logService != null) {
            this.logService.log(LogService.LOG_ERROR, message, cause);
        }
    }
}
