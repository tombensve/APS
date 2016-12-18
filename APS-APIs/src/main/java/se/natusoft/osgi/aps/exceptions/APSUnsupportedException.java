package se.natusoft.osgi.aps.exceptions;

/**
 * Can be thrown for anything that is unsupported.
 */
public class APSUnsupportedException extends APSRuntimeException {
    /**
     * Creates a new _APSUnsupportedException_ instance.
     *
     * @param message The exception message.
     */
    public APSUnsupportedException(String message) {
        super(message);
    }

    /**
     * Creates a new _APSUnsupportedException_ instance.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public APSUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

}
