/* 
 * 
 * PROJECT
 *     Name
 *         APS Cluster Service Sync Service
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This is an implementation of APSSyncService that uses APSClusterService to do the synchronization with.
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
package se.natusoft.osgi.aps.sync.service;

import se.natusoft.osgi.aps.api.net.sharing.exception.APSSharingException;
import se.natusoft.osgi.aps.api.net.sharing.service.APSClusterService;
import se.natusoft.osgi.aps.api.net.sharing.service.APSSyncService;
import se.natusoft.osgi.aps.sync.message.SyncMessage;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;

import java.util.*;

/**
 * Provides an implementation of APSSyncService using APSClusterService to do the synchronization.
 */
@OSGiServiceProvider(serviceAPIs = {APSSyncService.class}, properties = {@OSGiProperty(name="baseImpl", value="APSClusterService")})
public class APSClusterServiceSyncServiceProvider<Message, State> implements APSSyncService<Message> {

    //
    // Private Members
    //

    @OSGiService()
    private APSClusterService<SyncMessage<Message>, State> clusterService;

    @Managed(loggingFor = "APSClusterServiceSyncServiceProvider")
    private APSLogger logger;

    //
    // Methods
    //

    /**
     * Joins a synchronization for a specific type.
     *
     * @param group The group to sync with.
     * @param syncListener A listener for synchronization messages.
     *
     * @return An APSSync instance from which synchronizations can be sent.
     *
     * @throws se.natusoft.osgi.aps.api.net.sharing.exception.APSSharingException
     */
    @Override
    public APSSync<Message> joinSyncGroup(String group, APSSyncListener<Message> syncListener) throws APSSharingException {
//        APSClusterService.APSCluster<SyncMessage<Message>> cluster = this.clusterService.joinCluster(group);
//
//        APSSyncProvider syncProvider = new APSSyncProvider(syncListener);
//        cluster.addMember(syncProvider);
//
//        return syncProvider;
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Provides an implementation of APSSync.
     */
    private class APSSyncProvider implements APSSync<Message>, APSClusterService.APSClusterMember<SyncMessage<Message>> {

        //
        // Private Members
        //

        /** The id of the cluster member. */
        private UUID id = UUID.randomUUID();

        /** The clusters this member belongs to. */
        private Map<String, APSClusterService.APSCluster<SyncMessage<Message>>> clusters =
                Collections.synchronizedMap(new HashMap<String, APSClusterService.APSCluster<SyncMessage<Message>>>());

        /** Our synchronization listener. */
        private APSSyncListener<Message> syncListener;

        /**
         * When newMemberNotificiations arrive we set this to true, and cancel it to false when a MEMBER_REFRESH type
         * message is received. This to avoid having multiple members send the same information to refresh a new
         * member.
         */
        private SynchronizedBoolean updateOthers = new SynchronizedBoolean(false);

        /** The key for UpdateOthersThread instances will be stored here and checked so that duplicates are not started. */
        private Map<String, String> activeUpdaters = Collections.synchronizedMap(new HashMap<String, String>());

        //
        // Constructors
        //

        /**
         * Creates a new APSSyncProvider instance.
         *
         * @param syncListener The listener for sync data.
         */
        public APSSyncProvider(APSSyncListener<Message> syncListener) {
            this.syncListener = syncListener;
        }

        //
        // Methods
        //

        /**
         * Sends a synchronization to other members.
         *
         * @param data The data to synchronize with others.
         * @throws se.natusoft.osgi.aps.api.net.sharing.exception.APSSharingException On any failure to synchronize.
         */
        @Override
        public void sync(Message data) throws APSSharingException {
            sync(new SyncMessage<>(data));
        }

        /**
         * Handles the internal sync message.
         *
         * @param syncMessage The sync message to send.
         */
        private void sync(SyncMessage<Message> syncMessage) {
            for (String clusterName : this.clusters.keySet()) {
                APSClusterService.APSCluster<SyncMessage<Message>> cluster = this.clusters.get(clusterName);
                cluster.share(syncMessage);
            }
        }

        // -- APSClusterMember methods -------

        /**
         * Returns the member id.
         */
        @Override
        public UUID getId() {
            return this.id;
        }

        /**
         * Receives the cluster instance added to.
         *
         * @param cluster The cluster this member were added to.
         */
        @Override
        public  void addCluster(APSClusterService.APSCluster<SyncMessage<Message>> cluster) {
            this.clusters.put(cluster.getName(), cluster);
        }

        /**
         * Called when this member is removed from a cluster.
         *
         * @param cluster The cluster removed from.
         */
        @Override
        public void removeCluster(APSClusterService.APSCluster<SyncMessage<Message>> cluster) {
            this.clusters.remove(cluster.getName());
        }

        /**
         * A member has shared a message.
         *
         * @param clusterName The name of the cluster that sent the message.
         * @param syncMessage The shared message.
         */
        @Override
        public void shared(String clusterName, SyncMessage<Message> syncMessage) {
            switch (syncMessage.getMessageType()) {
                case SYNC:
                    this.syncListener.synced(syncMessage.getSyncData());
                    break;

                case MEMBER_REFRESH:
                    this.updateOthers.setValue(false);
                    break;
            }
        }

        /**
         * Notifies member that there is a new member in the group. This in case
         * some information needs to be re-shared with the group for the benefit
         * of the new member.
         *
         * @param clusterName The name of the cluster the notification came from.
         * @param memberId The id of the new member.
         */
        @Override
        public void newMemberNotification(String clusterName, UUID memberId) {
            String key = clusterName + memberId.toString();
            if (!this.activeUpdaters.containsKey(key)) {
                this.activeUpdaters.put(key, key);
                new UpdateOthersThread(key).start();
            }
        }

        /**
         * Closes the sync session and makes this instance invalid.
         */
        @Override
        public void leaveSyncGroup() {
            for (String clusterName : this.clusters.keySet()) {
                APSClusterService.APSCluster<SyncMessage<Message>> cluster = this.clusters.get(clusterName);

                cluster.removeMember(this);
                cluster.leave();
            }
        }

        // ---------------------------------------------------------------------------------------------

        /**
         * This is a thread that will enable a flag for updating others, sleep for a random amount of time,
         * and if the flag is still enabled after waking up an updateOthers() callback is triggered on clients.
         * Before the callback however a specific sync message is sent out, and if that same message happens
         * to be received before waking up then the flag will have been disabled and nothing will be done.
         *
         * This way the minimum (in most cases only one) members should send out their data again to the others.
         *
         * This whole thing happens when there are new members available. This is so that at least one of the
         * old members should update the new member. Since messages cannot be sent to specific members all
         * members will receive the update. Members should however be able to handle receiving information
         * they already have.
         */
        public class UpdateOthersThread extends Thread {
            //
            // Private Members
            //

            private String key;

            //
            // Constructors
            //

            /**
             * Creates a new UpdateOthersThread instance.
             *
             * @param key The key to clear from activeUpdaters when done.
             */
            public UpdateOthersThread(String key) {
                this.key = key;
            }

            //
            // Methods
            //

            /**
             * Runs the thread.
             */
            public void run() {
                try {
                    APSSyncProvider.this.updateOthers.setValue(true);

                    // The member that sleeps the shortest will inform its client to update the others
                    // and also send a MEMBER_REFRESH message that will stop others from doing the same.
                    Random random = new Random(new Date().getTime());
                    Thread.sleep(1000 * random.nextInt(20));

                    if (APSSyncProvider.this.updateOthers.getValue()) {
                        APSSyncProvider.this.sync(new SyncMessage<Message>(SyncMessage.MessageType.MEMBER_REFRESH));
                        APSSyncProvider.this.syncListener.updateOthers();
                    }
                }
                catch (Exception e) {
                    APSClusterServiceSyncServiceProvider.this.logger.error("UpdateOthersThread failed!", e);
                }
                finally {
                    APSSyncProvider.this.activeUpdaters.remove(this.key);
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Handles a synchronized boolean value.
     */
    public static final class SynchronizedBoolean {
        //
        // Private Members
        //

        /** The boolean value handled. */
        private boolean value;

        //
        // Constructors
        //

        /**
         * Creates a new SynchronziedBoolean.
         *
         * @param value The initial value of the boolean.
         */
        public SynchronizedBoolean(boolean value) {
            this.value = value;
        }

        //
        // Methods
        //

        /**
         * Sets the boolean value.
         *
         * @param value The value to set.
         */
        public synchronized void setValue(boolean value) {
            this.value = value;
        }

        /**
         * Returns the current value.
         */
        public synchronized boolean getValue() {
            return this.value;
        }
    }
}
