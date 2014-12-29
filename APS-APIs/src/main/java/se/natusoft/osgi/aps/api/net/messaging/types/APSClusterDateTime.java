package se.natusoft.osgi.aps.api.net.messaging.types;

/**
 * This is here to handle the fact that no 2 machines on a network do have exactly the same time :-).
 */
public interface APSClusterDateTime {

    /**
     * Get "now" in cluster time.
     */
    public long getClusterDateTime();

    /**
     * Converts a cluster time to a local time.
     *
     * @param clusterDateTime The cluster time to convert.
     *
     * @return The equivalent local time.
     */
    public long toLocalDateTime(long clusterDateTime);

    /**
     * Converts local time to cluster time.
     *
     * @param localDateTime The local time to convert.
     *
     * @return The equivalent cluster time.
     */
    public long toClusterDateTime(long localDateTime);
}
