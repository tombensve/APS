package se.natusoft.osgi.aps.api.misc.time;

import java.time.Instant;

/**
 * Provides an instant in time preferably from some external (and multi node common) time source.
 */
public interface APSTimeService {

    /**
     * Returns the time provided by the service.
     */
    Instant getTime();

    /**
     * Returns the last time the time was updated or null if there have been no successful updates.
     */
    Instant getLastTimeUpdate();

}
