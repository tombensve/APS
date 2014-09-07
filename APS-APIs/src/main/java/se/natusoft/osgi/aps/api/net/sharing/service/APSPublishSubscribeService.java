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
 *         2014-08-13: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.sharing.service;

import se.natusoft.osgi.aps.api.net.sharing.exception.APSSharingException;

/**
 * This is a simple publish/subscribe service.
 */
public interface APSPublishSubscribeService {

    /**
     * Joins the specified topic.
     *
     * @param topic The topic to join.
     *
     * @throws APSSharingException on failure.
     */
    <Data> APSTopic<Data> joinTopic(Class<Data> topic) throws APSSharingException;

    /**
     * This provides the API for communicating in the group.
     */
    public interface APSTopic<Data> {
        /**
         * Publishes data.
         *
         * @param data The data to publish.
         *
         * @throws APSSharingException on failure.
         */
        void publishData(Data data) throws APSSharingException;

        /**
         * Leaves the topic.
         */
        void leave();

        /**
         * Rejoins the topic again after a leave() call.
         *
         * @throws APSSharingException on failure.
         */
        void rejoin() throws APSSharingException;

        /**
         * Adds a listener for receiving data.
         *
         * @param dataListener The listener to add.
         */
        void addDataListener(DataListener<Data> dataListener);

        /**
         * Removes a listener from receiving data.
         *
         * @ param dataListener The listener to remove.
         */
        void removeDataListener(DataListener<Data> dataListener);
    }

    /**
     * A listener receiving published data.
     */
    public interface DataListener<Data> {

        /**
         * Receives data.
         *
         * @param data The data.
         */
        void dataReceived(Data data);
    }
}
