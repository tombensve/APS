package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.messaging.types.APSCluster;

import java.util.List;

/**
 * Provides a cluster of members that messages can be sent to and received from.
 *
 * Different named clusters should be defined through configuration. Users of the service
 * should configure the name of the cluster to use.
 */
public interface APSClusterService {

    /**
     * To be used as property in service registrations of implementing services to indicate what
     * underlying cluster/messaging solution is used. This to support multiple implementations deployed
     * concurrently, allowing clients to choose a specific implementation. In most cases a client
     * shouldn't care about the implementation. */
    public static final String CLUSTER_PROVIDER_PROPERTY = "cluster-provider";

    /**
     * Returns the named cluster or null if no cluster with specified name exists.
     *
     * @param name The name of the cluster to get.
     */
    APSCluster getClusterByName(String name);
}
