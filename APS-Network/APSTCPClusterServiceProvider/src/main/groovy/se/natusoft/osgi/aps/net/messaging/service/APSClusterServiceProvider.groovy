package se.natusoft.osgi.aps.net.messaging.service

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.service.APSClusterService
import se.natusoft.osgi.aps.api.net.messaging.types.APSCommonDateTime
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessageListener
import se.natusoft.osgi.aps.net.messaging.config.ClusterServiceConfig

/**
 * Provides and implementation of APSClusterService.
 */
class APSClusterServiceProvider implements APSClusterService {

    //
    // Private Members
    //

    private String clusterName

    //
    // Constructors
    //

    public APSClusterServiceProvider(String clusterName) {
        this.clusterName = clusterName
    }

    //
    // Methods
    //

    private ClusterServiceConfig.Cluster getConfig() {
        return (ClusterServiceConfig.Cluster)ClusterServiceConfig.managed.get().clusters.find { ClusterServiceConfig.Cluster cluster ->
            cluster.name.string == this.clusterName
        }
    }

    /**
     * Returns the name of the cluster.
     */
    @Override
    String getName() {
        return this.clusterName
    }

    /**
     * If the implementation has the notion of a master and this node is the master then
     * true is returned. In all other cases false is returned.
     */
    @Override
    boolean isMasterNode() {
        return false
    }

    /**
     * Returns the network common DateTime that is independent of local machine times.
     */
    @Override
    APSCommonDateTime getCommonDateTime() {
        return null
    }

    /**
     * Adds a listener for types.
     *
     * @param listener The listener to add.
     */
    @Override
    void addMessageListener(APSMessageListener listener) {

    }

    /**
     * Removes a messaging listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    void removeMessageListener(APSMessageListener listener) {

    }

    /**
     * Sends a message.
     *
     * @param message The message to send.
     *
     * @throws se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException on failure.
     */
    @Override
    void sendMessage(APSMessage message) throws APSMessagingException {

    }
}
