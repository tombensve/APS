package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.util.APSBox;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * This service defines a synchronized cluster.
 */
public interface APSClusterService {

    /**
     * Multiple providers of this service can be deployed at the same time. Using this property
     * when registering services for a provider allows clients to lookup a specific provider.
     */
    String APS_CLUSTER_SERVICE_PROVIDER = "aps-cluster-service-provider";

    /** Multiple APSClusterService instances can be registered. This specifies which instance you want. */
    String APS_CLUSTER_NAME = "aps-cluster-name";

    /**
     * This should be implemented by any code wanting to receive cluster updates.
     */
    interface UpdateListener {
        /**
         * Receives an updated value.
         *
         * @param key The key of the updated value.
         * @param value The actual value.
         */
        void clusterUpdated(String key, APSBox value);
    }

    /**
     * Updates a keyed value to the cluster.
     *
     * @param key This uniquely specifies what value this is. How it is used is upp tp the actual cluster using it.
     * @param value The modified value to update.
     */
    void update(String key, APSBox value);

    /**
     * Adds an update listener.
     *
     * @param updateListener The update listener to add.
     */
    void addUpdateListener(UpdateListener updateListener);

    /**
     * Removes an update listener.
     *
     * @param updateListener The listener to remove.
     */
    void removeUpdateListener(UpdateListener updateListener);

    /**
     * Gets named cluster-wide object. If it does not exist it will be created.
     *
     * @param name The name of the cluster object to get.
     */
    APSBox getNamedObject(String name);

    /**
     * Gets a cluster-wide named list. If it does not exist it will be created.
     *
     * @param name The name of the list to get.
     */
    List<APSBox> getNamedList(String name);

    /**
     * Gets a cluster-wide named Map. If it does not exist, it will be created.
     *
     * Do note that this is mostly a convenience. Implementations can (and most will) use the name as
     * a prefix to the map key and then call getNamedObject(expandedKey).
     *
     * @param name The name of the map to get.
     */
    Map<String, APSBox> getNamedMap(String name);

    /**
     * Locks a specific entry in the cluster or all entries starting with the specified 'nameToLock'.
     * When a lock is acquired the future is executed and when done the lock is released.
     *
     * Any other client who tries to update the locked name(s) during the lock period without acquiring their own
     * lock will get an exception.
     *
     * @param nameToLock The name of the value(s) to lock.
     * @param future A Future to execute when the lock is acquired.
     */
    void withLock(String nameToLock, Future future);

    //
    // Inner Classes
    //

    /**
     * Support base for implementations.
     */
    abstract class AbstractAPSClusterService implements APSClusterService {

        //
        // Private Members
        //

        /** The listeners. */
        List<UpdateListener> listeners = Collections.synchronizedList(new LinkedList<UpdateListener>());

        //
        // Methods
        //

        /**
         * Adds an update listener.
         *
         * @param updateListener The update listener to add.
         */
        @Override
        public void addUpdateListener(UpdateListener updateListener) {
            this.listeners.add(updateListener);
        }

        /**
         * Removes an update listener.
         *
         * @param updateListener The listener to remove.
         */
        @Override
        public void removeUpdateListener(UpdateListener updateListener) {
            this.listeners.remove(updateListener);
        }

        /**
         * Updates all listeners.
         *
         * @param key The key of the update.
         * @param value The value of the update.
         */
        protected void updateListeners(String key, APSBox value) {
            for (UpdateListener listener : this.listeners) {
                listener.clusterUpdated(key, value);
            }
        }

        /**
         * Returns the listeners.
         */
        protected List<UpdateListener> getListeners() {
            return this.listeners;
        }
    }
}
