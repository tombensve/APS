package se.natusoft.osgi.aps.exceptions;

/**
 * This exception is thrown when an APS bundle has problems starting up.
 */
public class APSStartFailureException extends RuntimeException {

    /**
     * Creates a new APSStartFailureException.
     *
     * @param message The exception message.
     */
    public APSStartFailureException(String message) {
        super (message);
    }

    /**
     * Creates a new APSStartFailureException.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public APSStartFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
