package se.natusoft.osgi.aps.api.net.util;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

import java.io.IOException;

/**
 * Adds JSON storage support to APSBox. This makes use of the APS JSON APIs.
 */
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
    class APSJSONBoxDefaultProviderFactory implements APSJSONBoxFactory {
        //
        // Private Members
        //

        private APSJSONService apsjsonService = null;

        private LogService logService = null;

        //
        // Constructors
        //

        APSJSONBoxDefaultProviderFactory(APSJSONService apsjsonService) {
            this.apsjsonService = apsjsonService;
        }

        APSJSONBoxDefaultProviderFactory(APSJSONService apsjsonService, LogService logService) {
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
            public void log(ServiceReference sr, int level, String message) {}

            @Override
            public void log(ServiceReference sr, int level, String message, Throwable exception) {}
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
            try {
                setContent(APSJSONService.Tools.toBytes(jsonValue, this.apsjsonService));
            }
            catch (IOException ioe) {
                throw new APSRuntimeException("Failed to convert a JSONValue to bytes!", ioe);
            }
        }

        /**
         * Returns the box contents as a JSON value.
         */
        @Override
        public JSONValue getJSONContent() {
            try {
                return APSJSONService.Tools.fromBytes(getContent(), this.apsjsonService, this);
            }
            catch (IOException ioe) {
                throw new APSRuntimeException("Failed to read bytes as JSON!", ioe);
            }
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
