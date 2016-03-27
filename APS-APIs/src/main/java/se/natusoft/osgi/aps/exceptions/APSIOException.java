package se.natusoft.osgi.aps.exceptions;

/**
 * This is a general IO exception for APS.
 */
public class APSIOException extends APSRuntimeException {

    /**
     * Creates a new APSIOException instance.
     *
     * @param message The exception message.
     */
    public APSIOException(String message) {
        super(message);
    }

    /**
     * Creates a new APSIOException instance.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public APSIOException(String message, Throwable cause) {
        super(message, cause);
    }

}
