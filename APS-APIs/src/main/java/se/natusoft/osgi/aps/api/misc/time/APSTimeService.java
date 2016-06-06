package se.natusoft.osgi.aps.api.misc.time;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;

import java.time.Instant;

/**
 * Intended for providing a common time across nodes. This is most simply implemented by using a common ntp server
 * as source. Note that the time returned by this service might not be the same as the local time!
 */
public interface APSTimeService {

    /**
     * Returns the time provided by the service.
     */
    @NotNull Instant getTime();

    /**
     * Returns the last time the time was updated or null if there have been no successful updates.
     */
    @Nullable Instant getLastTimeUpdate();
}
