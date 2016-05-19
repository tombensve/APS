package se.natusoft.osgi.aps.api.net.time.service;

import java.time.Instant;

/**
 * Intended for providing a common time across nodes. This is most simply implemented by using a common ntp server
 * as source. Note that the time returned by this service might not be the same as the local time!
 */
public interface APSTimeService {

    /**
     * Returns the time provided by the service.
     */
    Instant getTime();
}
