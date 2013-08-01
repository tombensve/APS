package se.natusoft.osgi.aps.tools.exceptions;

import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

/**
 * This is thrown by APSActivator on failure.
 */
public class APSActivatorException extends APSRuntimeException {
    /**
     * Creates a new _APSRuntimeException_ instance.
     *
     * @param message The exception message.
     */
    public APSActivatorException(String message) {
        super(message);
    }

    /**
     * Creates a new _APSRuntimeException_ instance.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public APSActivatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
