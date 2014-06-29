/* 
 * 
 * PROJECT
 *     Name
 *         APS RabbitMQ SimpleMessageService Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides an implementation of APSMessageService using RabbitMQ Java Client.
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
 *         2013-09-01: Created!
 *         
 */
package se.natusoft.osgi.aps.messaging;

import com.rabbitmq.client.*;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener;
import se.natusoft.osgi.aps.api.net.messaging.service.APSSimpleMessageService;
import se.natusoft.osgi.aps.messaging.config.RabbitMQConnectionConfig;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.*;

import java.io.IOException;
import java.util.*;

/**
 * This provides an implementation of APSSimpleMessageService using RabbitMQ.
 *
 * Please note that message groups are represented by RabbitMQ exchanges. When a group is joined
 * an exchange with that name and type "fanout" is created. The receiver uses an anonymous queue.
 * Each "group" has its own receiver and sender channel. Each group will also have its own receiver
 * thread. My first attempt was to reuse channels as much as possible, but that did not work
 * very well, but I'm rather new to RabbitMQ and have to admit I haven't yet fully understood
 * all its features. It was however rather easy to install and get upp and running.
 */
@OSGiServiceProvider(properties = {
        @OSGiProperty(name = "underlying-provider", value = "RabbitMQ"),
}, threadStart = true)
public class APSRabbitMQSimpleMessageServiceProvider implements APSSimpleMessageService {
    //
    // Private Members
    //

    /** Our logger. */
    @Managed(loggingFor = "aps-rabbitmq-message-service-provider")
    private APSLogger logger;

    /** A connection factory for connecting to RabbitMQ. */
    private ConnectionFactory connectionFactory = null;

    /** The RabbitMQ service connection. */
    private Connection connection = null;

    /** The listener threads. */
    private Map<String, MessageGroupProvider.ReceiverThread> listenerThreads = new HashMap<>();

    private APSConfigChangedListener configChangedListener;

    /** Created and cached queue providers. */
    private Map<String, MessageGroupProvider> messageGroupProviders = new HashMap<>();

    //
    // Constructors
    //

    /**
     * Creates a new APSRabbitMQSimpleMessageServiceProvider.
     */
    public APSRabbitMQSimpleMessageServiceProvider() {}

    @BundleStart
    public void startup() {
        // Please note that we can access config here due to the threadStart=true in the @OSGiServiceProvider
        // above. The instance of this class will be created in a separate thread from the OSGi server startup
        // thread that calls the activator.

        ensureConnection();

        this.configChangedListener = new APSConfigChangedListener() {
            @Override
            public void apsConfigChanged(APSConfigChangedEvent event) {
                synchronized (APSRabbitMQSimpleMessageServiceProvider.this) {
                    reconnect();
                }
            }
        };

        RabbitMQConnectionConfig.managed.get().addConfigChangedListener(this.configChangedListener);

    }

    //
    // Shutdown
    //

    @BundleStop
    public void shutdown() {
        if (this.configChangedListener != null) {
            RabbitMQConnectionConfig.managed.get().removeConfigChangedListener(this.configChangedListener);
        }

        for (MessageGroupProvider.ReceiverThread receiverThread : this.listenerThreads.values()) {
            receiverThread.removeAllListeners();
            receiverThread.stopThread();
            try {receiverThread.join(8000);} catch (InterruptedException ie) {}
        }

        ensureConnectionClosed();
    }

    //
    // Methods
    //

    /**
     * Ensures that there is a connection setup.
     */
    private synchronized void ensureConnection() {
        if (this.connectionFactory == null) {
            this.connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(RabbitMQConnectionConfig.managed.get().host.toString());
            connectionFactory.setPort(RabbitMQConnectionConfig.managed.get().port.toInt());

            String userName = RabbitMQConnectionConfig.managed.get().user.toString().trim();
            if (userName.length() > 0) {
                connectionFactory.setUsername(userName);
            }

            String password = RabbitMQConnectionConfig.managed.get().password.toString().trim();
            if (password.length() > 0) {
                connectionFactory.setPassword(password);
            }

            String virtualHost = RabbitMQConnectionConfig.managed.get().virtualHost.toString().trim();
            if (virtualHost.length() > 0) {
                connectionFactory.setVirtualHost(virtualHost);
            }

            String timeout = RabbitMQConnectionConfig.managed.get().timeout.toString().trim();
            if (timeout.length() > 0) {
                connectionFactory.setConnectionTimeout(Integer.valueOf(timeout));
            }
        }

        if (this.connection == null || !this.connection.isOpen()) {
            try {
                this.connection = connectionFactory.newConnection();
            }
            catch (IOException ioe) {
                if (this.connection != null) {
                    try {
                        this.connection.close();
                    }
                    catch (IOException ioe2) {
                        this.logger.error("Failed to close connection due to channel create failure!", ioe2);
                    }
                }
                throw new APSMessageException(ioe.getMessage(), ioe);
            }

            this.logger.info("Connected to RabbitMQ server at " +
                    RabbitMQConnectionConfig.managed.get().host.toString() + ":" +
                    RabbitMQConnectionConfig.managed.get().port.toString() + "!");
        }
    }

    /**
     * Closes the connection if not closed already.
     */
    private synchronized void ensureConnectionClosed() {
        for (String queueName : this.messageGroupProviders.keySet()) {
            MessageGroupProvider qp = this.messageGroupProviders.get(queueName);
            qp.leave();
        }

        if (this.connection != null) {
            try {
                this.connection.close();
                this.connection = null;
            }
            catch (IOException ioe) {
                this.logger.error("Failed to close RabbitMQ connection on shutdown!", ioe);
            }

            this.logger.info("Disconnected from RabbitMQ server at " +
                    RabbitMQConnectionConfig.managed.get().host.toString() + ":" +
                    RabbitMQConnectionConfig.managed.get().port.toString() + "!");
        }

        this.connectionFactory = null;
    }

    /**
     * Closes connection and then creates a new again.
     */
    private synchronized void reconnect() {
        ensureConnectionClosed();
        ensureConnection();
    }

    /**
     * @return The current connection.
     */
    private Connection getConnection() {
        ensureConnection();
        return this.connection;
    }

    /**
     * Joins a message group.
     *
     * @param name The name of the message group to join.
     *
     * @return A MessageGroup instance used to send and receive messages to/from the group.
     *
     * @throws APSSimpleMessageService.APSMessageException on any failure to join.
     */
    @Override
    public MessageGroup joinMessageGroup(String name) throws APSMessageException {
        MessageGroupProvider messageGroup = new MessageGroupProvider(name);
        this.messageGroupProviders.put(name, messageGroup);
        return messageGroup;
    }


    /**
     * Provides implementation of the Queue API.
     */
    private class MessageGroupProvider implements MessageGroup {
        //
        // Private Members
        //

        private String name;

        private Channel sendChannel = null;


        //
        // Constructors
        //

        /**
         * Creates a new QueueProvider.
         *
         * @param name The queue name represented by this instance.
         */
        public MessageGroupProvider(String name) {
            this.name = name;
        }

        //
        // Methods
        //

        /**
         * Returns the sender channel.
         * @throws Exception
         */
        private Channel getSendChannel() throws IOException {
            if (this.sendChannel == null || !this.sendChannel.isOpen()) {
                this.sendChannel = getConnection().createChannel();
                this.sendChannel.exchangeDeclare(this.name, "fanout");
            }

            return this.sendChannel;
        }

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
            return new Message.Provider();
        }

        /**
         * Creates a new message.
         *
         * @param content The content of the message.
         */
        @Override
        public Message createMessage(byte[] content) {
            Message.Provider message = new Message.Provider();
            message.setBytes(Arrays.copyOf(content, content.length));
            return message;
        }

        /**
         * Sends a message.
         *
         * @param message The message to send.
         * @throws APSSimpleMessageService.APSMessageException
         *
         */
        @Override
        public void sendMessage(Message message) throws APSMessageException {
            try {
                getSendChannel().basicPublish(this.name, "", null, message.getBytes());
                //logger.debug("Sent message of length " + message.getBytes().length);
            }
            catch (IOException ioe) {
                throw new APSMessageException(ioe.getMessage(), ioe);
            }
        }

        /**
         * Adds a listener to received messages.
         *
         * @param listener The listener to add.
         */
        @Override
        public void addMessageListener(Message.Listener listener) {
            ReceiverThread receiverThread = listenerThreads.get(this.name);
            if (receiverThread == null) {
                receiverThread = new ReceiverThread();
                receiverThread.start();
                listenerThreads.put(this.name, receiverThread);
                logger.info("Created new receiver for queue '" + this.name + "'!");
            }
            receiverThread.addMessageListener(listener);
        }

        /**
         * Removes a listener from receiving messages.
         *
         * @param listener The listener to remove.
         */
        @Override
        public void removeMessageListener(Message.Listener listener) {
            ReceiverThread receiverThread = listenerThreads.get(this.name);
            if (receiverThread != null) {
                receiverThread.removeMessageListener(listener);
                if (!receiverThread.haveListeners()) {
                    logger.warn("The receiver for queue '" + this.name + "' has no more listeners!");
                    // Not entirely sure this is a good idea ...
                    //listenerThreads.remove(this.name);
                    //receiverThread.stopThread();
                }
            }
            else {
                logger.error("Tried to remove MessageListener from ReceiverThread for group '" + this.name + "' " +
                        "This group however doesn't have a ReceiverThread! This means that no messages have " +
                        "been received for this group for an unknown amount of time!");
            }
        }

        /**
         * Closes this queue provider.
         *
         * @throws IOException
         */
        public void leave() {
            messageGroupProviders.remove(this.name);

            try {
                if (this.sendChannel != null) this.sendChannel.close();
            }
            catch (IOException ioe) {
                logger.error("Failed to close channels!", ioe);
            }
            ReceiverThread receiverThread = listenerThreads.remove(this.name);
            receiverThread.stopThread();
        }

        /**
         * Thread that receives queue messages.
         */
        private class ReceiverThread extends Thread {
            //
            // Private Members
            //

            private boolean running = true;

            private Channel recvChannel = null;
            private String recvQueueName = null;

            List<MessageGroup.Message.Listener> listeners =
                    Collections.synchronizedList(new LinkedList<MessageGroup.Message.Listener>());

            //
            // Constructors
            //

            /**
             * Creates a new ReceiverThread.
             */
            public ReceiverThread() {}

            //
            // Methods
            //

            /**
             * Stops this thread.
             */
            public synchronized void stopThread() {
                this.running = false;
            }

            /**
             * Returns true for as long as the thread has not been stopped.
             */
            private synchronized boolean keepRunning() {
                return this.running;
            }

            /**
             * Adds a message listener to this receiver thread.
             *
             * @param listener The listener to add.
             */
            public void addMessageListener(Message.Listener listener) {
                this.listeners.add(listener);
            }

            /**
             * Removes a message listener from this receiver thread.
             *
             * @param listener The listener to remove.
             */
            public void removeMessageListener(Message.Listener listener) {
                this.listeners.remove(listener);
            }

            /**
             * Returns true if there are listeners available.
             */
            public boolean haveListeners() {
                return !this.listeners.isEmpty();
            }

            /**
             * Removes all listeners.
             */
            public void removeAllListeners() {
                this.listeners.clear();
            }

            /**
             * Returns the receive channel.
             *
             * @throws Exception
             */
            private Channel getRecvChannel() throws IOException {
                if (this.recvChannel == null || !this.recvChannel.isOpen()) {
                    this.recvChannel = getConnection().createChannel();
                    this.recvChannel.exchangeDeclare(name, "fanout");
                    this.recvQueueName = this.recvChannel.queueDeclare().getQueue();
                    this.recvChannel.queueBind(this.recvQueueName, name, "");
                }
                return this.recvChannel;
            }

            /**
             * Thread entry and exit point.
             */
            public void run() {

                int failureCount = 0;

                try {

                    QueueingConsumer consumer = new QueueingConsumer(getRecvChannel());
                    getRecvChannel().basicConsume(this.recvQueueName, true, consumer);

                    while (keepRunning()) {
                        try {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery(5000);
                            if (delivery != null) {
                                byte[] body = delivery.getBody();
                                //logger.debug("======== Received message of length " + body.length + " ==========");
                                //logger.debug("  Current no listeners: " + this.listeners.size());
                                Message message = createMessage(body);

                                new ListenerCallThread(name, message, this.listeners).start();
                            }
                            else {
                                //logger.debug("====== TIMEOUT ======");
                            }

                        }
                        catch (ShutdownSignalException | ConsumerCancelledException sse) {
                            throw sse;
                        }
                        // We dont want this thread to die on Exception!
                        catch (Exception e) {
                            logger.error("ReceiverThread got an Exception!", e);
                            if (failureCount < 3) {
                                ++failureCount;

                                consumer = new QueueingConsumer(getRecvChannel());
                                getRecvChannel().basicConsume(this.recvQueueName, true, consumer);
                            }
                            else {
                                logger.error("Sleeping for 5 seconds hoping for better times! If this keeps recurring there is " +
                                        "a serious problem!");
                                Thread.sleep(5000);
                                failureCount = 0;
                            }
                        }
                    }

                    this.recvChannel.close();
                    this.recvChannel = null;
                }
                catch (ShutdownSignalException sse) {
                    logger.error("We are being shutdown!", sse);
                }
                catch (ConsumerCancelledException cce) {
                    logger.error("The consumer has been cancelled!", cce);
                }
                catch (IOException ioe) {
                    logger.error("ReceiverThread: Failed to create consumer! This thread will die and not " +
                            "receive anything!", ioe);
                }
                catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            /**
             * Lets protect ourself from called user code!
             */
            private class ListenerCallThread extends Thread {
                String name;
                Message message;
                LinkedList<Message.Listener> listenersCopy = new LinkedList<>();

                public ListenerCallThread(String name, Message message, List<Message.Listener> listeners) {
                    super("APSRabbitMQSimpleMessageServiceProvider-ListenerCallThread");
                    this.name = name;
                    this.message = message;
                    this.listenersCopy.addAll(listeners);
                }

                @Override
                public void run() {
                    // TODO: This is still not optimal since if a 'listener.receiveMessage(...)' call
                    // decides to not return consecutive listeners will not be called!
                    for (Message.Listener listener: this.listenersCopy) {
                        try {
                            listener.receiveMessage(this.name, message);
                        }
                        catch (Exception e) {
                            logger.error("Calling listener '" + listener + "' failed!", e);
                        }
                    }
                }
            }
        }
    }
}
