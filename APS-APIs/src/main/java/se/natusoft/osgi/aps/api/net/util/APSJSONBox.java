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
 *     tommy ()
 *         Changes:
 *         2016-02-27: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.util;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService;

/**
 * Adds JSON storage support to APSBox. This makes use of the APS JSON APIs.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public interface APSJSONBox extends APSBox {

    /**
     * Sets the box contents as a JSON value.
     *
     * @param jsonValue The content to set.
     */
    void setJSONContent(JSONValue jsonValue);

    /**
     * Returns the box contents as a JSON value.
     */
    JSONValue getJSONContent();

    //
    // Inner Classes
    //

    /**
     * Creates APSJSONBox instances.
     */
    @SuppressWarnings("unused")
    interface APSJSONBoxFactory extends APSBoxFactory<APSJSONBox> {

        /**
         * Creates a new APSJSONBox with contents.
         *
         * @param jsonValue The contents of the new box.
         */
        APSJSONBox createBox(JSONValue jsonValue);
    }

    /**
     * A factory that creates a default implementation of APSJSONBox.
     */
    @SuppressWarnings("unused")
    class APSJSONBoxDefaultProviderFactory implements APSJSONBoxFactory {
        //
        // Private Members
        //

        private APSJSONService apsjsonService = null;

        private LogService logService = null;

        //
        // Constructors
        //

        @SuppressWarnings("WeakerAccess")
        public APSJSONBoxDefaultProviderFactory(APSJSONService apsjsonService) {
            this.apsjsonService = apsjsonService;
        }

        @SuppressWarnings("unused")
        public APSJSONBoxDefaultProviderFactory(APSJSONService apsjsonService, LogService logService) {
            this(apsjsonService);
            this.logService = logService;
        }

        //
        // Methods
        //

        /**
         * Creates a new APSJSONBox with contents.
         *
         * @param jsonValue The contents of the new box.
         */
        @Override
        public APSJSONBox createBox(JSONValue jsonValue) {
            APSJSONBox box = createBox();
            box.setJSONContent(jsonValue);
            return box;
        }

        /**
         * Returns a new APSBox without content.
         */
        @Override
        public APSJSONBox createBox() {
            APSJSONBoxDefaultProvider box = new APSJSONBoxDefaultProvider(this.apsjsonService);
            if (this.logService != null) {
                box.setLogService(this.logService);
            }

            return box;
        }

        /**
         * Returns a new APSBox with content.
         *
         * @param content The content of the box to create.
         */
        @Override
        public APSJSONBox createBox(byte[] content) {
            APSJSONBox box = createBox();
            box.setContent(content);
            return box;
        }
    }

    /**
     * Provides a default implementation of APSJSONBox.
     */
    @SuppressWarnings("WeakerAccess")
    class APSJSONBoxDefaultProvider extends APSBoxDefaultProvider implements APSJSONBox, JSONErrorHandler {
        //
        // Private Members
        //

        private APSJSONService apsjsonService = null;

        private LogService logService = new LogService() {
            private final String[] logTypes = {"ERROR", "WARNING", "INFO", "DEBUG"};

            private void println(int level, String message) {
                System.err.println("[" + logTypes[level - 1] + "]: " + message);
            }

            @Override
            public void log(int level, String message) {
                println(level, message);
            }

            @Override
            public void log(int level, String message, Throwable exception) {
                println(level, message);
                exception.printStackTrace(System.err);
            }

            @Override
            public void log(ServiceReference sr, int level, String message) {
            }

            @Override
            public void log(ServiceReference sr, int level, String message, Throwable exception) {
            }
        };

        //
        // Constructors
        //

        /**
         * Creates a new APSJSONBoxDefaultProvider instance.
         *
         * @param apsjsonService The APSJSONService to use.
         */
        APSJSONBoxDefaultProvider(APSJSONService apsjsonService) {
            this.apsjsonService = apsjsonService;
        }

        //
        // Methods
        //

        /**
         * Sets the LogService to use for logging failures.
         *
         * @param logService The LogService to set.
         */
        public final void setLogService(LogService logService) {
            this.logService = logService;
        }

        /**
         * Sets the box contents as a JSON value.
         *
         * @param jsonValue The content to set.
         */
        @Override
        public void setJSONContent(JSONValue jsonValue) {
            setContent(APSJSONService.Tools.toBytes(jsonValue, this.apsjsonService));
        }

        /**
         * Returns the box contents as a JSON value.
         */
        @Override
        public JSONValue getJSONContent() {
            return APSJSONService.Tools.fromBytes(getContent(), this.apsjsonService, this);
        }

        /**
         * Warns about something.
         *
         * @param message The warning message.
         */
        @Override
        public void warning(String message) {
            this.logService.log(LogService.LOG_WARNING, message);
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
            this.logService.log(LogService.LOG_ERROR, message, cause);
        }
    }
}
