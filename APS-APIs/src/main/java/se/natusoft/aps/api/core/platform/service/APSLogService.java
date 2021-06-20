package se.natusoft.aps.api.core.platform.service;

/**
 * A simple log service that in turn can log to whatever.
 */
public interface APSLogService {

    /**
     * Identify what is logging.
     *
     * Consider only calling this once and save instance, which may or may not be same as called!
     *
     * @param loggingFor An identifier of what is logging.
     *
     * @return An instance of APSLogService sitting on the identifier, most probably ...
     */
    APSLogService loggingFor(String loggingFor);

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
     * A warning log entry. Submit by calling log().
     *
     * @param message The message to log.
     */
    void warn(String message, Throwable cause);

    /**
     * A fail log entry. Submit by calling log().
     *
     * @param message The message to log.
     */
    void error(String message);

    /**
     * A fail log entry. Submit by calling log().
     *
     * @param message The fail message to log.
     * @param cause The exception that was the cause of this.
     */
    void error(String message, Throwable cause);

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
    void debug(String message, Throwable e);
}
