package se.natusoft.apsgroups.internal;

import se.natusoft.apsgroups.logging.APSGroupsLogger;
import se.natusoft.apsgroups.logging.APSGroupsSystemOutLogger;

/**
 * Provides logging.
 */
public class StaticLogger {

    private static APSGroupsLogger logger = null;

    public static APSGroupsLogger getLogger() {
        if (logger == null) {
            logger = new APSGroupsSystemOutLogger();
        }
        return logger;
    }

    public static void setLogger(APSGroupsLogger logger) {
        StaticLogger.logger = logger;
    }
}
