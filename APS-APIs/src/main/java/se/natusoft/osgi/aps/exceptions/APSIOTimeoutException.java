package se.natusoft.osgi.aps.exceptions;

/**
 * This indicates an IO timeout.
 */
public class APSIOTimeoutException extends APSIOException {

    /**
     * Creates a new APSIOTimeoutException.
     */
    public APSIOTimeoutException() {
        super("TIMEOUT");
    }

    /**
     * Creates a new APSIOTimeoutException instance.
     *
     * @param message The exception message.
     */
    public APSIOTimeoutException(String message) {
        super(message);
    }

    /**
     * Creates a new APSIOTimeoutException instance.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public APSIOTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
