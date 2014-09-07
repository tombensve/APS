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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2014-08-25: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.sharing.service;

import se.natusoft.osgi.aps.api.net.sharing.exception.APSSharingException;

/**
 * This provides several clusters identified by a cluster name. All members
 * in a cluster will be able to receive and send messages to the other members in the
 * cluster.
 *
 * The intention for this service is to be used by other services like APSPublishSubscribeService and
 * APSSyncService. This have a simple API and can be implemented using different solutions, like
 * JGroups, RabbitMQ, etc. Any messaging protocol will do.
 *
 * The state was inspired by JGroups.
 */
@SuppressWarnings("UnusedDeclaration")
public interface APSClusterService<Message> {

    /**
     * Joins the named cluster.
     *
     * @param name The cluster to join.
     * @param member The member to receive cluster messages.
     */
    APSCluster<Message> joinCluster(String name, APSClusterMember member) throws APSSharingException;

    /**
     * This is a specific cluster.
     */
    @SuppressWarnings("UnusedDeclaration")
    public interface APSCluster<Message> {

        /**
         * Returns the name of the cluster.
         */
        String getName();

        /**
         * Shares a message with the cluster.
         *
         * @param message The message to share.
         *
         * @throws APSSharingException on failure.
         */
        void share(Message message) throws APSSharingException;

        /**
         * Requests that current state be sent.
         *
         * @throws APSSharingException
         */
        void requestState() throws APSSharingException;

        /**
         * Leaves the cluster.
         */
        void leave() throws APSSharingException;
    }


    /**
     * This has to be implements by cluster members.
     */
    @SuppressWarnings("UnusedDeclaration")
    public interface APSClusterMember<Message, State> {

        /**
         * A member has shared a message.
         *
         * @param message The shared message.
         */
        void shared(Message message);

        /**
         * The current state have been shared.
         *
         * @param state The shared state.
         */
        void sharedState(State state);

    }

    /**
     * A cluster member implementing this will receive the instance of the cluster it is a member.
     *
     * This interface is entirely optional for members. This is here as a convenience if the cluster is joined
     * by some other class than the member.
     */
    @SuppressWarnings("UnusedDeclaration")
    public interface APSClusterReceivingMember<Message, State> extends APSClusterMember<Message, State> {

        /**
         * Receives the APSCluster instance.
         *
         * @param cluster The cluster the member is part of.
         */
        void setAPSCluster(APSCluster<Message> cluster);
    }
}
