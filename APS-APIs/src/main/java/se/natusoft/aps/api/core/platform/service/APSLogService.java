package se.natusoft.aps.api.core.platform.service;

/**
 * A simple log service that in turn can log to whatever.
 */
public interface APSLogService {

    /**
     * An informational log entry. Submit by calling log().
     *
     * @param message The message to log.
     *
     * @return APSLogService
     */
    APSLogService info(String message);

    /**
     * A warning log entry. Submit by calling log().
     *
     * @param message The message to log.
     *
     * @return APSLogService.
     */
    APSLogService warn(String message);

    /**
     * A fail log entry. Submit by calling log().
     *
     * @param message The message to log.
     *
     * @return APSLogService.
     */
    APSLogService fail(String message);

    /**
     * A debug log entry. Submit by calling log().
     *
     * @param message The message to log.
     *
     * @return APSLogService.
     */
    APSLogService debug(String message);

    /**
     * Adds and exception to the log entry.
     *
     * @param exception The exception to add.
     *
     * @return APDSLogService.
     */
    APSLogService exception(Exception exception);

    /**
     * Submits current log data to log.
     */
    void log();
}
