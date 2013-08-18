package se.natusoft.osgi.aps.core.config.api;

/**
 * This is a service that provides management functions for config synchronization.
 */
public interface APSConfigSyncMgmtService {

    /**
     * Returns the time of the last sync message received.
     */
    public String getLastMessageTimestamp();

    /**
     * Trigger this service to request updates from other nodes.
     *
     * A response or potential error message is returned.
     */
    public String requestUpdate();
}
