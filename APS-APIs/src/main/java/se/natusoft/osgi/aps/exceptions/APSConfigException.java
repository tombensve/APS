package se.natusoft.osgi.aps.exceptions;

/**
 * Thrown on config problems.
 */
public class APSConfigException extends APSRuntimeException {

    /**
     * Creates a new _APSConfigException_ instance.
     *
     * @param message The exception message.
     */
    public APSConfigException(String message) {
        super(message);
    }

    /**
     * Creates a new _APSConfigException_ instance.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public APSConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
