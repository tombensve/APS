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
 *         2013-08-31: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.sharing.service;

import se.natusoft.osgi.aps.api.net.sharing.exception.APSSharingException;

/**
 * Defines a data synchronization service.
 *
 * @param <SyncType> The generic synchronization type.
 */
public interface APSSyncService<SyncType> {

    /**
     * Joins a synchronization for a specific group.
     *
     * @param group The group to sync with.
     * @param syncListener A listener for synchronization messages.
     *
     * @return An APSSync instance from which synchronizations can be sent.
     *
     * @throws APSSharingException
     */
    APSSync<SyncType> joinSyncGroup(String group, APSSyncListener<SyncType> syncListener) throws APSSharingException;

    /**
     * A member instance used to send synchronizations to other members.
     *
     * @param <SyncType> The generics type of the synchronization type.
     */
    public interface APSSync<SyncType> {

        /**
         * Sends a synchronization to other members.
         *
         * @param data The data to synchronize with others.
         *
         * @throws APSSharingException On any failure to synchronize.
         */
        void sync(SyncType data) throws APSSharingException;

        /**
         * Closes the sync session and makes this instance invalid.
         */
        void leaveSyncGroup();

    }

    /**
     * Listener for synchronization data.
     *
     * @param <SyncType> The generic synchronization type.
     */
    public interface APSSyncListener<SyncType> {

        /**
         * Called for each new synchronization data available.
         *
         * @param syncData The received synchronization data.
         */
        void synced(SyncType syncData);

        /**
         * If this gets called then this synchronization member should resync all of its information.
         */
        void updateOthers();
    }
}
