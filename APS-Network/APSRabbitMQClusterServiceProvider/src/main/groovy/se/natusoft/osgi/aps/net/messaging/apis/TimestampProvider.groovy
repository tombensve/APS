package se.natusoft.osgi.aps.net.messaging.apis

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Provides current time as a long.
 */
@CompileStatic
@TypeChecked
interface TimestampProvider {

    /**
     * Returns the current time.
     */
    long getDateTime();
}
