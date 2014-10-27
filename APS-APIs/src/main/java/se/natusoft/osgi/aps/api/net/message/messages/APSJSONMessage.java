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
package se.natusoft.osgi.aps.api.net.message.messages;

import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Possible base message class for JSON based messages. Makes use of the APSJSONService to read and write JSON.
 */
public class APSJSONMessage extends APSMessage implements JSONErrorHandler {

    //
    // Private Members
    //

    /** The JSON service to user for creating and reading JSON. */
    private APSJSONService jsonService;

    /** The time the message was received. */
    private long receivedAt;

    /** The message. */
    private JSONObject message;

    /** A potential JSON read error. */
    private String jsonReadErrorMessage = null;

    /** A potential JSON read error cause. */
    private Throwable jsonReadErrorCause = null;

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

    //
    // Methods
    //

    /**
     * This should be overridden by specific messages and super called as first thing done.
     *
     * @param dataStream The stream to read from.
     *
     * @throws java.io.IOException On failure to read.
     */
    protected
    @Override
    void readData(DataInputStream dataStream) throws IOException {
        clearJSONErrors();
        this.message = (JSONObject)getJsonService().readJSON(dataStream, this);
        assertNoJSONErrors();
        this.receivedAt = new Date().getTime();
    }

    /**
     * This should be overridden by specific messages and super called as first thing done.
     *
     * @param dataStream The stream to write to.
     *
     * @throws IOException On failure to write.
     */
    @Override
    protected void writeData(DataOutputStream dataStream) throws  IOException {
        getJsonService().writeJSON(dataStream, this.message, true);
    }

    /**
     * Provides the JSON service.
     */
    protected APSJSONService getJsonService() {
        return this.jsonService;
    }

    /**
     * Returns the JSON message object. If not read nor updated by subclasses it will be empty.
     */
    public JSONObject getMessage() {
        if (this.message == null) {
            this.message = getJsonService().createJSONObject();
        }
        return this.message;
    }

    /**
     * Returns the timestamp when the message was received.
     */
    public long getReceivedAt() {
        return this.receivedAt;
    }

    /**
     * Clears any JSON error message.
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
     * @param message The warning message.
     */
    @Override
    public void warning(String message) {
        System.err.println(message);
    }

    /**
     * Indicate failure.
     *
     * @param message The failure message.
     * @param cause   The cause of the failure. Can be null!
     * @throws RuntimeException This method must throw a RuntimeException.
     */
    @Override
    public void fail(String message, Throwable cause) throws RuntimeException {
        this.jsonReadErrorMessage = message;
        this.jsonReadErrorCause = cause;
        System.err.println(message);
        cause.printStackTrace(System.err);
    }
}
