package se.natusoft.aps.api.core.platform.service;

/**
 * A simple log service that in turn can log to whatever.
 */
public interface APSLogService {

    /**
     * An informational log entry. Submit by calling log().
     *
     * @param message The message to log.
     */
    void info(String message);

    /**
     * A warning log entry. Submit by calling log().
     *
     * @param message The message to log.
     */
    void warn(String message);

    /**
     * A fail log entry. Submit by calling log().
     *
     * @param message The message to log.
     */
    void fail(String message);

    /**
     * A fail log entry. Submit by calling log().
     *
     * @param message The fail message to log.
     * @param cause The exception that was the cause of this.
     *
    void fail(String message, Exception cause);

    /**
     * A debug log entry. Submit by calling log().
     *
     * @param message The message to log.
     */
    void debug(String message);

    /**
     * A debug log entry. Submit by calling log().
     *
     * @param message The message to log.
     * @param e A possible exception to log.
     */
    void debug(String message, Exception e);
}
