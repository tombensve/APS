package se.natusoft.osgi.aps.exceptions;

/**
 * This exception is thrown when an APS bundle has problems starting up.
 */
public class APSStartException extends RuntimeException {

    /**
     * Creates a new APSStartException.
     *
     * @param message The exception message.
     */
    public APSStartException( String message) {
        super (message);
    }

    /**
     * Creates a new APSStartException.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public APSStartException( String message, Throwable cause) {
        super(message, cause);
    }
}
