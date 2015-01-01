package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.types.APSSyncEvent;

/**
 * This defines a data synchronization service.
 *
 * @param <SyncGroup> This is an object that indicates to the implementation what
 *                   group to synchronize with. What this is depends on the implementation.
 *                   For example an implementation that uses APSClusterService would specify
 *                   APSCluster here. Implementations should provide some named group configuration
 *                   and clients should also have configuration that names the sync group to
 *                   use, in which case getSyncGroupByName(name) can be called and then this
 *                   type can be treated as an Object, clients does not have to know what it
 *                   is.
 */
public interface APSSyncService<SyncGroup> {

    /**
     * Gets a Synchronizer for the specified group.
     *
     * @param group The group to synchronize with.
     */
    Synchronizer getSynchronizer(SyncGroup group);

    /**
     * This provides the API for doing actual synchronization.
     */
    interface Synchronizer {

        /**
         * Synchronizes data.
         *
         * @param syncEvent The sync event to send.
         * @throws APSMessagingException on failure.
         */
        void syncData(APSSyncEvent syncEvent) throws APSMessagingException;

        /**
         * Makes all members resync everything.
         */
        void resync();

        /**
         * Makes all members resync the specified key.
         *
         * @param syncEvent Only the group and key are relevant here!
         */
        void resync(APSSyncEvent syncEvent);

        /**
         * Adds a synchronization listener.
         *
         * @param listener The listener to add.
         */
        void addSyncListener(Listener listener);

        /**
         * Removes a synchronization listener.
         *
         * @param listener The listener to remove.
         */
        void removeSyncListener(Listener listener);

        /**
         * This should be implemented by synchronization data listeners.
         */
        interface Listener {

            /**
             * Called to deliver a sync value.
             *
             * @param syncEvent The received sync event.
             */
            void syncDataReceived(APSSyncEvent syncEvent);
        }
    }
}
