package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.messages.APSSyncEvent;

/**
 * This defines a data synchronization service.
 */
public interface APSDataSyncService<SyncType> {

    /**
     * Synchronizes some data.
     *
     * @param syncEvent The sync event to send.
     *
     * @throws APSMessagingException on failure.
     */
    void syncData(APSSyncEvent<SyncType> syncEvent) throws APSMessagingException;

    /**
     * Makes all members resync everything.
     */
    void resync();

    /**
     * Makes all members resync the specified key.
     *
     * @param syncEvent Only the group and key are relevant here!
     */
    void resync(APSSyncEvent<SyncType> syncEvent);

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
