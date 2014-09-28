package se.natusoft.osgi.aps.api.net.sharing.service;

/**
 * Provides information about the cluster.
 */
public interface APSClusterInfoService {

    /**
     * Returns information about the current clusters.
     */
    String getClusterInfo();
}
