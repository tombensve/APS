/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2015-01-09: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.types.APSCommonDateTime;
import se.natusoft.osgi.aps.api.net.messaging.types.APSSyncDataEvent;
import se.natusoft.osgi.aps.api.net.messaging.types.APSSyncEvent;

/**
 * This defines a data synchronization service.
 */
public interface APSSyncService {

    /**
     * A property key that should be registered with each service instance to indicate the
     * specific implementation of the service. This to allow multiple implementations to
     * be deployed and clients can ask for a specific if needed.
     */
    public static final String SYNC_PROVIDER = "aps-sync-provider";

    /**
     * There should be one service instance registered for each configured synchronization
     * group. Each instance should include this property with a unique name so that clients
     * can get the synchronizer for the correct group.
     */
    public static final String SYNC_INSTANCE_NAME = "aps-sync-instance-name";

    /**
     * Returns the network common DateTime that is independent of local machine times.
     */
    APSCommonDateTime getCommonDateTime();

    /**
     * Synchronizes data.
     *
     * @param syncEvent The sync event to send.
     * @throws APSMessagingException on failure.
     */
    void syncData(APSSyncDataEvent syncEvent) throws APSMessagingException;

    /**
     * Makes all members resync everything.
     */
    void resync();

    /**
     * Makes all members resync the specified key.
     *
     * @param key The key to resync.
     */
    void resync(String key);

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
         * Called to deliver a sync event. This can currently be one of:
         *
         * * APSSyncDataEvent
         * * APSReSyncEvent
         *
         * @param syncEvent The received sync event.
         */
        void syncDataReceived(APSSyncEvent syncEvent);
    }
}
