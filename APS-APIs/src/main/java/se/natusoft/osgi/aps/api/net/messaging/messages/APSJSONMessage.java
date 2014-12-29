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
package se.natusoft.osgi.aps.api.net.messaging.messages;

import org.osgi.service.log.LogService;
import se.natusoft.osgi.aps.api.net.messaging.types.APSCluster;
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage;
import se.natusoft.osgi.aps.codedoc.Implements;
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService;
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;

import java.io.*;

/**
 * Possible base messaging class for JSON based types. Makes use of the APSJSONService to read and write JSON.
 *
 * This requires each message to be a JSON object. The object must also contain a string member called "messageType".
 */
public class APSJSONMessage implements APSMessage, JSONErrorHandler {

    //
    // Constants
    //

    private static final String MESSAGE_TYPE = "messageType";

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

    /** The JSON message. */
    private JSONObject json;

    //
    // Constructors
    //

    /**
     * Creates a new APSJSONMessage. (This to support Groovy properties constructor).
     */
    public APSJSONMessage() {}

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

    private void validate() {
        if (this.jsonService == null) {
            throw new APSMessagingException("No APSJSONService instance have been provided to the message! This is required!");
        }
    }

    /**
     * Sets the APSJSONService instance to use. (This to support Groovy properties constructor).
     *
     * @param jsonService The APSJSONService instance to set.
     */
    public void setJsonService(APSJSONService jsonService) {
        this.jsonService = jsonService;
    }

    /**
     * Sets a LogService to log to. (This to support Groovy properties constructor).
     *
     * @param logService The LogService to set.
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Provide JSON data as an APSJSONService JSONObject instance.
     *
     * @param jsonMessage The JSON data to provide in the message.
     */
    public void setJSON(JSONObject jsonMessage)  {
        this.json = jsonMessage;
    }

    /**
     * Returns the internal APSJSONService JSONObject.
     */
    public JSONObject getJSON() {
        validate();
        if (this.json == null) {
            this.json = this.jsonService.createJSONObject();
            this.json.addValue(MESSAGE_TYPE, this.jsonService.createJSONString("UNTYPED"));
        }
        return this.json;
    }

    /**
     * Provide JSON data as a String.
     *
     * @param jsonData The JSON data to provide in the message.
     *
     * @throws APSMessagingException on failure to parse JSON.
     */
    public void setJSONFromString(String jsonData) throws APSMessagingException {
        validate();
        try {
            ByteArrayInputStream jstream = new ByteArrayInputStream(jsonData.getBytes("UTF-8"));
            clearJSONErrors();
            JSONValue value = this.jsonService.readJSON(jstream, this);
            assertNoJSONErrors();
            if (!JSONObject.class.isAssignableFrom(value.getClass())) {
                throw new IOException("Expected JSON object! Got: " + value.getClass().getSimpleName());
            }
        }
        catch (IOException ioe){
            throw new APSMessagingException("Bad JSON string! - " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Returns the JSON data in the message as a String.
     */
    public String getJSONAsString() {
        validate();
        String jsonStr = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            this.jsonService.writeJSON(baos, this.json, true);
            jsonStr = new String(baos.toByteArray(), "UTF-8");
        }
        catch (IOException ioe) {
            throw new APSMessagingException("This should not happen since we are writing to a byte array!", ioe);
        }

        return jsonStr;
    }


    /**
     * Provides the JSON service.
     */
    protected APSJSONService getJsonService() {
        validate();
        return this.jsonService;
    }

    /**
     * Returns the type of the message.
     */
    @Override
    @Implements(APSMessage.class)
    public String getType() {
        return getJSON().getValue(MESSAGE_TYPE).toString();
    }

    /**
     * Returns the complete message as a byte array.
     */
    @Override
    @Implements(APSMessage.class)
    public byte[] getBytes() {
        try {
            return getJSONAsString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // I strongly suspect that this will never happen :-)
            throw new APSMessagingException("UTF-8 is unknown to this system!", e);
        }
    }

    /**
     * Sets the message bytes.
     *
     * @param bytes The bytes to set.
     */
    @Override
    @Implements(APSMessage.class)
    public void setBytes(byte[] bytes) {
        try {
            setJSONFromString(new String(bytes, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // I strongly suspect that this will never happen :-)
            throw new APSMessagingException("UTF-8 is unknown to this system!", e);
        }
    }

    //
    // JSON Parsing support methods
    //

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

    //
    // Inner Classes
    //

    /**
     * A slightly more useful default MessageResolver can be provided for APSJSONMessage since it
     * is not totally dependent on subclasses if you work with the JSONObject object.
     *
     * Subclasses using standard Java Bean APIs can of course be used with JSON messages also, but
     * then you need to provide your own implementation of the MessageResolver interface.
     */
    public static class DefaultMessageResolver implements APSCluster.MessageResolver {

        private APSJSONService jsonService;
        private LogService logService;

        public DefaultMessageResolver(APSJSONService jsonService, LogService logService) {
            this.jsonService = jsonService;
            this.logService = logService;
        }

        /**
         * Returns an APSMessage implementation based on the message data.
         *
         * @param messageData The message data.
         */
        @Override
        public APSMessage resolveMessage(byte[] messageData) {
            APSMessage message = new APSJSONMessage(this.jsonService, this.logService);
            message.setBytes(messageData);
            return message;
        }
    }
}
