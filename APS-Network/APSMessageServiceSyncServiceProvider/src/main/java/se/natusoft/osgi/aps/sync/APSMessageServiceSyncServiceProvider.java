package se.natusoft.osgi.aps.sync;

import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService;
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService.Queue;
import se.natusoft.osgi.aps.api.net.sync.service.APSSyncService;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * This provides synchronization over APSMessageService.
 */
@OSGiServiceProvider(
        properties = {
                @OSGiProperty(name = "message-provider", value = "APSMessageService")
        }
)
public class APSMessageServiceSyncServiceProvider implements APSSyncService {
    //
    // Constants
    //

    private static final int MSG_PROTOCOL_VERSION = 1;

    //
    // Private Members
    //

    @OSGiService
    private APSMessageService messageService;

    @Managed(loggingFor = "aps-message-service-sync-service-provider")
    private APSLogger logger;

    private Map<String, SyncGroupProvider> syncGroupProviders = new HashMap<>();

    //
    // Constructors
    //


    //
    // Methods
    //

    /**
     * Joins a synchronization group.
     *
     * @param name The name of the group to join.
     * @return joined group.
     */
    @Override
    public SyncGroup joinSyncGroup(String name) {
        SyncGroupProvider syncGroupProvider = this.syncGroupProviders.get(name);
        if (syncGroupProvider == null) {
            syncGroupProvider = new SyncGroupProvider(name);
            this.syncGroupProviders.put(name, syncGroupProvider);
        }

        return syncGroupProvider;
    }

    //
    // Inner Classes
    //

    public class SyncGroupProvider implements SyncGroup, APSMessageService.Queue.Message.Listener {
        //
        // Private Members
        //

        private String name;

        private List<Message.Listener> listeners =
                Collections.synchronizedList(new LinkedList<Message.Listener>());

        private List<ReSyncListener> reSyncListeners =
                Collections.synchronizedList(new LinkedList<ReSyncListener>());

        private SendResyncThread sendResyncThread = null;

        private UUID uuid = UUID.randomUUID();

        //
        // Constructors
        //

        public SyncGroupProvider(String name) {
            this.name = name;
            Queue queue = messageService.getQueue(this.name);
            if (queue == null) {
                throw new APSSyncException("APSMessageService queue with name '" + this.name + "' does not exist!");
            }

            messageService.getQueue(this.name).addMessageListener(this);
        }

        //
        // Methods
        //

        /**
         * Returns the name of the group.
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
         * @throws se.natusoft.osgi.aps.api.net.sync.service.APSSyncService.APSSyncException
         *
         */
        @Override
        public void sendMessage(Message message) throws APSSyncException {
            try {
                Queue.Message msg = messageService.getQueue(this.name).createMessage();
                ObjectOutputStream dataStream = new ObjectOutputStream(msg.getOutputStream());
                dataStream.writeInt(MSG_PROTOCOL_VERSION);
                dataStream.writeObject(this.uuid);
                dataStream.writeObject(MessageType.MESSAGE);
                dataStream.writeInt(message.getBytes().length);
                dataStream.write(message.getBytes());
                dataStream.close();

                messageService.getQueue(this.name).sendMessage(msg);
            }
            catch (APSMessageService.APSMessageException | IOException me) {
                throw new APSSyncException(me.getMessage(), me);
            } catch (NullPointerException npe) {
                throw new APSSyncException("Queue '" + this.name + "' is undefined!");
            }
        }

        /**
         * Adds a listener to received messages.
         *
         * @param listener The listener to add.
         */
        @Override
        public void addMessageListener(Message.Listener listener) {
            this.listeners.add(listener);
        }

        /**
         * Removes a listener from receiving messages.
         *
         * @param listener The listener to remove.
         */
        @Override
        public void removeMessageListener(Message.Listener listener) {
            this.listeners.remove(listener);
        }

        /**
         * Called when a message is received.
         *
         * @param queueName The name of the queue that delivered the message.
         * @param message   The received message.
         */
        @Override
        public void receiveMessage(String queueName, APSMessageService.Queue.Message message) {
            ObjectInputStream dataStream = null;
            try {
                dataStream = new ObjectInputStream(message.getInputStream());
                int version = dataStream.readInt();
                if (version == MSG_PROTOCOL_VERSION) {
                    UUID recvUUID = (UUID)dataStream.readObject();
                    if (!recvUUID.equals(this.uuid)) {
                        MessageType messageType = (MessageType)dataStream.readObject();
                        switch (messageType) {
                            case MESSAGE:
                                int msgSize = dataStream.readInt();
                                byte[] msgBytes = new byte[msgSize];
                                dataStream.read(msgBytes);
                                Message msg = new Message.Provider(this);
                                msg.setBytes(msgBytes);
                                for (Message.Listener listener : this.listeners) {
                                    listener.receiveMessage(msg);
                                }
                                break;

                            case RESYNC_REQUEST:
                                for (ReSyncListener reSyncListener : this.reSyncListeners) {
                                    reSyncListener.reSyncAll(this);
                                }
                                break;
                        }
                    }
                }
                else {
                    logger.error("Received message with unknown version: " + version);
                }
            }
            catch (IOException | ClassNotFoundException e) {
                throw new APSSyncException("Failed to read received message!", e);
            }
            finally {
                if (dataStream != null) {
                    try {dataStream.close();}
                    catch (IOException ioe) {logger.error("Failed to close received message stream!", ioe);}
                }
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
                Queue.Message msg = messageService.getQueue(this.name).createMessage();
                ObjectOutputStream dataStream = new ObjectOutputStream(msg.getOutputStream());
                dataStream.writeInt(MSG_PROTOCOL_VERSION);
                dataStream.writeObject(this.uuid);
                dataStream.writeObject(MessageType.RESYNC_REQUEST);
                dataStream.close();
                messageService.getQueue(this.name).sendMessage(msg);
            }
            catch (IOException ioe) {
                throw new APSSyncException("APSMessageServiceSyncServiceProvider: Failed to send resync message!", ioe);
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
                        logger.error("Unexpectedly interrupted from sleep!", ie);
                    }

                    if (isRun()) {
                        _reSyncAll();
                    }
                }
                catch (Exception e) {
                    logger.error("Failed to send RESYNC message!", e);
                }
            }
        }

        /**
         * Leaves the synchronization group.
         */
        @Override
        public void leaveSyncGroup() {
            messageService.getQueue(this.name).removeMessageListener(this);
        }
    }

    /**
     * Defines the types of messages that can be send/received.
     */
    private enum MessageType {
        MESSAGE,
        RESYNC_REQUEST;
    }
}
