package se.natusoft.osgi.aps.tools.util;

import se.natusoft.osgi.aps.tools.APSLogger;

/**
 * This implements Runnable and wraps the call with a try+catch.
 *
 * This class is abstract and must be extended to implement doRun().
 */
public abstract class LoggingRunnable implements Runnable {
    //
    // Private Members
    //

    private APSLogger logger;

    //
    // Constructor
    //

    /**
     * Creates a new LoggingRunnable.
     *
     * @param logger The logger to log to.
     */
    public LoggingRunnable(APSLogger logger) {
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Subclasses must provide this. It will be called on run().
     *
     * @throws Exception Any such will be cauth and logged.
     */
    public abstract void doRun() throws Exception;

    /**
     * Calls doRun() wrapped with a try+catch.
     */
    public void run() {
        try {
            doRun();
        }
        catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}
