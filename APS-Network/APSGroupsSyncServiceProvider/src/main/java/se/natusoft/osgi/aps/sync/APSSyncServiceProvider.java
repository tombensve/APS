/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups Sync Service Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         This provides APSSyncService based on the APSGroupService.
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
 *         2013-08-30: Created!
 *         
 */
package se.natusoft.osgi.aps.sync;

import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.api.net.groups.service.GroupMember;
import se.natusoft.osgi.aps.api.net.groups.service.MessageListener;
import se.natusoft.osgi.aps.api.net.sync.service.APSSyncService;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Provides an implementation of APSMessageService that uses APSGroups to send and receive messages.
 */
@OSGiServiceProvider(properties = {@OSGiProperty(name = "message-provider", value = "APSGroups")})
public class APSSyncServiceProvider implements APSSyncService {

    //
    // Private Members
    //

    @OSGiService(timeout = "30 seconds")
    private APSGroupsService apsGroups;

    @Managed(loggingFor = "APSGroupsMessageServiceProvider")
    private APSLogger logger;

    private Map<String, SyncGroup> groups = new HashMap<>();

    //
    // Methods
    //

    /**
     * Joins a queue.

     * @param name The name of the queue to join.
     * @return The joined to queue.
     */
    @Override
    public SyncGroup joinSyncGroup(String name) {
        SyncGroup group = this.groups.get(name);
        if (group == null) {
            group = new SyncGroupProvider(name);
            this.groups.put(name, group);
        }

        return group;
    }

    @BundleStop
    public void shutdown() {
        for (SyncGroup entry : this.groups.values()) {
            try {
                entry.leaveSyncGroup();
            }
            catch (Exception e) {
                this.logger.error("Queue clearing failure on shutdown!", e);
            }
        }
    }

    private static enum MessageType {
        SYNC_DATA,
        RESYNC_REQUEST
    }

    public class SyncGroupProvider implements SyncGroup {
        //
        // Private Members
        //

        private String name;

        private GroupMember groupMember;

        private List<MsgListener> listeners = new LinkedList<>();

        private List<ReSyncListener> reSyncListeners = new LinkedList<>();

        private SendResyncThread sendResyncThread;

        //
        // Constructors
        //

        /**
         * Creates a new SyncGroupProvider.
         *
         * @param name The name of the queue.
         */
        public SyncGroupProvider(String name) {
            this.name = name;
        }

        //
        // Methods
        //

        /**
         * Returns the name of the queue.
         */
        @Override
        public String getName() {
            return this.name;
        }

        /**
         * Creates a new message.
         */
        @Override
        public Message createMessage() {
            return new Message.Provider(this);
        }

        /**
         * Creates a new message.
         *
         * @param content The content of the message.
         */
        @Override
        public Message createMessage(byte[] content) {
            Message message = new Message.Provider(this);
            message.setBytes(content);
            return message;
        }

        /**
         * Sends a message.
         *
         * @param message The message to send.
         * @throws se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService.APSMessageException
         *
         */
        @Override
        public synchronized void sendMessage(Message message) throws APSSyncException {
            try {
                if (this.groupMember == null) {
                    this.groupMember = APSSyncServiceProvider.this.apsGroups.joinGroup(this.name);
                }

                se.natusoft.osgi.aps.api.net.groups.service.Message msg = this.groupMember.createNewMessage();
                ObjectOutputStream dataStream = new ObjectOutputStream(msg.getOutputStream());
                dataStream.writeObject(MessageType.SYNC_DATA);
                dataStream.writeInt(message.getBytes().length);
                dataStream.write(message.getBytes());
                dataStream.flush();
                dataStream.close();
                this.groupMember.sendMessage(msg);
            }
            catch (IOException ioe) {
                throw new APSSyncException("APSGroupsSyncServiceProvider: Failed to send message!", ioe);
            }
        }

        /**
         * Adds a listener to received messages.
         *
         * @param listener The listener to add.
         */
        @Override
        public void addMessageListener(Message.Listener listener) {
            try {
                if (this.groupMember == null) {
                    this.groupMember = APSSyncServiceProvider.this.apsGroups.joinGroup(this.name);
                }

                MsgListener msgListener = new MsgListener(listener);
                this.listeners.add(msgListener);
                this.groupMember.addMessageListener(msgListener);
            }
            catch (IOException ioe) {
                throw new APSSyncException("APSGroupsSyncServiceProvider: Failed to join group!", ioe);
            }
        }

        private class MsgListener implements MessageListener {
            private Message.Listener listener;

            public MsgListener(Message.Listener listener) {
                this.listener = listener;
            }

            public Message.Listener getListener() {
                return this.listener;
            }

            /**
             * Notification of received message.
             *
             * @param message The received message.
             */
            @Override
            public void messageReceived(se.natusoft.osgi.aps.api.net.groups.service.Message message) {
                try {
                    ObjectInputStream dataStream = new ObjectInputStream(message.getInputStream());
                    MessageType msgType = (MessageType)dataStream.readObject();
                    if (msgType == MessageType.SYNC_DATA) {
                        int size = dataStream.readInt();
                        byte[] content = new byte[size];
                        //noinspection ResultOfMethodCallIgnored
                        dataStream.read(content);
                        dataStream.close();
                        Message msg = new Message.Provider(SyncGroupProvider.this);
                        msg.setBytes(content);
                        this.listener.receiveMessage(msg);
                    }
                    else if (msgType == MessageType.RESYNC_REQUEST) {
                        if (SyncGroupProvider.this.sendResyncThread != null) {
                            SyncGroupProvider.this.sendResyncThread.cancel();
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    for (ReSyncListener reSyncListener : SyncGroupProvider.this.reSyncListeners) {
                                        reSyncListener.reSyncAll(SyncGroupProvider.this);
                                    }
                                }
                                catch (Exception e) {
                                    APSSyncServiceProvider.this.logger.error("Failed to resync!", e);
                                }
                            }
                        }).start();
                    }
                }
                catch (IOException | ClassNotFoundException e) {
                    throw new APSSyncException("APSGroupsSyncServiceProvider:Failed to extract from received message!", e);
                }
            }
        }

        /**
         * Removes a listener from receiving messages.
         *
         * @param listener The listener to remove.
         */
        @Override
        public void removeMessageListener(Message.Listener listener) {
            MsgListener msgListener = null;
            for (MsgListener mListener : this.listeners) {
                if (mListener.getListener().equals(listener)) {
                    msgListener = mListener;
                    break;
                }
            }

            if (msgListener != null) {
                this.groupMember.removeMessageListener(msgListener);
                this.listeners.remove(msgListener);
            }
        }

        /**
         * Adds a resynchronization listener.
         *
         * @param reSyncListener The listener to add.
         */
        @Override
        public void addReSyncListener(ReSyncListener reSyncListener) {
            this.reSyncListeners.add(reSyncListener);
        }

        /**
         * Removes a Resynchronization listener.
         *
         * @param reSyncListener The listener to remove.
         */
        @Override
        public void removeReSyncListener(ReSyncListener reSyncListener) {
            this.reSyncListeners.remove(reSyncListener);
        }

        /**
         * Triggers a re-synchronization between all data and all members.
         */
        @Override
        public void reSyncAll() {
            this.sendResyncThread = new SendResyncThread();
            this.sendResyncThread.start();
        }

        /**
         * Triggers a re-synchronization between all data and all members.
         */
        private void _reSyncAll() {
            try {
                if (this.groupMember == null) {
                    this.groupMember = APSSyncServiceProvider.this.apsGroups.joinGroup(this.name);
                }

                se.natusoft.osgi.aps.api.net.groups.service.Message msg = this.groupMember.createNewMessage();
                ObjectOutputStream dataStream = new ObjectOutputStream(msg.getOutputStream());
                dataStream.writeObject(MessageType.RESYNC_REQUEST);
                dataStream.flush();
                dataStream.close();
                this.groupMember.sendMessage(msg);
            }
            catch (IOException ioe) {
                throw new APSSyncException("APSGroupsSyncServiceProvider: Failed to send resync message!", ioe);
            }
        }

        /**
         * This sends the FEED_ME message in a separate thread with some delay.
         */
        private class SendResyncThread extends Thread {
            private boolean run = true;

            public synchronized void cancel() {
                this.run = false;
            }

            private synchronized boolean isRun() {
                return this.run;
            }

            public void run() {
                try {
                    Random random = new Random(new Date().getTime());
                    try {
                        // Wait between 5 to 15 seconds. This is an attempt to avoid several nodes sending a "feed me"
                        // simultaneously. When a "feed me" message is received cancel is done on this thread.
                        Thread.sleep(random.nextInt(10000) + 5000);
                    }
                    catch (InterruptedException ie) {
                        APSSyncServiceProvider.this.logger.error("Unexpectedly interrupted from sleep!", ie);
                    }

                    if (isRun()) {
                        _reSyncAll();
                    }
                }
                catch (Exception e) {
                    APSSyncServiceProvider.this.logger.error("Failed to send FEED_ME message!", e);
                }
            }
        }


        /**
         * Leaves the queue.
         */
        @Override
        public void leaveSyncGroup() {
            for (MsgListener mListener : this.listeners) {
                this.groupMember.removeMessageListener(mListener);
            }
            this.listeners = new LinkedList<>();
            APSSyncServiceProvider.this.groups.remove(this.name);
            try {
                APSSyncServiceProvider.this.apsGroups.leaveGroup(this.groupMember);
            }
            catch (IOException ioe) {
                throw new APSSyncException("APSGroupsSyncServiceProvider:Failure leaving group!", ioe);
            }

        }
    }
}
