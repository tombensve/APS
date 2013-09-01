/* 
 * 
 * PROJECT
 *     Name
 *         APS RabbitMQ Message Service Provider
 *     
 *     Code Version
 *         0.9.3
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
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService;
import se.natusoft.osgi.aps.messaging.config.BunnyConnectionConfig;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;

import java.io.IOException;
import java.util.*;

/**
 * This provides an implementation of APSMessageService using RabbitMQ.
 */
@OSGiServiceProvider(properties = {
        @OSGiProperty(name = "underlying-provider", value = "RabbitMQ"),
        @OSGiProperty(name = "supports-complete-api", value = "true")
})
public class APSRabbitMQMessageServiceProvider implements APSMessageService {
    //
    // Private Members
    //

    /** Our logger. */
    @Managed(loggingFor = "aps-rabbitmq-message-service-provider")
    private APSLogger logger;

    /** Flag to see if the connection to the RabbitMQ service have been done. */
    private boolean connected = false;

    /** The RabbitMQ service connection. */
    private Connection connection = null;

    /** A RabbitMQ channel. */
    private Channel channel = null;

    /** The listener threads. */
    private Map<String, QueueProvider.ReceiverThread> listenerThreads = new HashMap<>();

    //
    // Constructors
    //

    /**
     * Creates a new APSRabbitMQMessageServiceProvider.
     */
    public APSRabbitMQMessageServiceProvider() {}

    //
    // Shutdown
    //

    @BundleStop
    public void shutdown() {
        for (QueueProvider.ReceiverThread receiverThread : this.listenerThreads.values()) {
            receiverThread.removeAllListeners();
            receiverThread.stopThread();
            try {receiverThread.join(8000);} catch (InterruptedException ie) {};
        }

        if (this.connection != null) {
            try {
                this.connection.close();
            }
            catch (IOException ioe) {
                this.logger.error("Failed to close RabbitMQ connection on shutdown!", ioe);
            }
        }

        if (this.channel != null) {
            try {
                this.channel.close();
            }
            catch (IOException ioe) {
                this.logger.error("Failed to close RabbitMQ channel on shutdown!", ioe);
            }
        }

        this.logger.info("Disconnected from RabbitMQ server at " +
                BunnyConnectionConfig.managed.get().rabbitMQHost.toString() + ":" +
                BunnyConnectionConfig.managed.get().rabbitMQPort.toString() + "!");

    }

    //
    // Methods
    //

    /**
     * Does delayed setup that we don't want to do during construction since the constructor is executed
     * by bundle activator on start.
     */
    private void connect() {
        if (!this.connected) {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(BunnyConnectionConfig.managed.get().rabbitMQHost.toString());
            connectionFactory.setPort(BunnyConnectionConfig.managed.get().rabbitMQPort.toInt());

            String userName = BunnyConnectionConfig.managed.get().rabbitMQUser.toString().trim();
            if (userName.length() > 0) {
                connectionFactory.setUsername(userName);
            }

            String password = BunnyConnectionConfig.managed.get().rabbitMQPassword.toString().trim();
            if (password.length() > 0) {
                connectionFactory.setPassword(password);
            }

            String virtualHost = BunnyConnectionConfig.managed.get().rabbitMQVirtualHost.toString().trim();
            if (virtualHost.length() > 0) {
                connectionFactory.setVirtualHost(virtualHost);
            }

            String timeout = BunnyConnectionConfig.managed.get().rabbitMQTimeout.toString().trim();
            if (timeout.length() > 0) {
                connectionFactory.setConnectionTimeout(Integer.valueOf(timeout));
            }

            try {
                this.connection = connectionFactory.newConnection();
                this.channel = connection.createChannel();
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
                    BunnyConnectionConfig.managed.get().rabbitMQHost.toString() + ":" +
                    BunnyConnectionConfig.managed.get().rabbitMQPort.toString() + "!");

            this.connected = true;
        }
    }

    /**
     * Checks if the named queue exists.
     *
     * @param name The name of the queue to check.
     * @return true if queue exists, false otherwise.
     */
    @Override
    public boolean queueExists(String name) {
        connect();
        try {
            this.channel.queueDeclarePassive(name);
            return true;
        }
        catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Defines a queue that lives as long as the queue providing service lives.
     * <p/>
     * If the queue already exist nothing happens. If the queue is of another type an
     * _APSMessageException_ could possibly be throws depending on implementation and
     * underlying service used.
     *
     * @param name The name of the queue to define.
     * @throws se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService.APSMessageException
     *          (possibly) on trying to redefine type.
     */
    @Override
    public Queue defineQueue(String name) throws APSMessageException {
        connect();
        try {
            this.channel.queueDeclare(name, false, false, false, null);
        }
        catch (IOException ioe) {
            throw new APSMessageException(ioe.getMessage(), ioe);
        }

        return new QueueProvider(name);
    }

    /**
     * Defines a queue that lives for a long time.
     * <p/>
     * If the queue already exist nothing happens. If the queue is of another type an
     * _APSMessageException_ could possibly be thrown depending on implementation and
     * underlying service used.
     *
     * @param name The name of the queue to define.
     * @throws se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService.APSMessageException
     *                                       (possibly) on trying to redefine type.
     * @throws UnsupportedOperationException If this type of queue is not supported by the implementation.
     */
    @Override
    public Queue defineDurableQueue(String name) throws APSMessageException, UnsupportedOperationException {
        connect();
        try {
            this.channel.queueDeclare(name, true, false, false, null);
        }
        catch (IOException ioe) {
            throw new APSMessageException(ioe.getMessage(), ioe);
        }

        return new QueueProvider(name);
    }

    /**
     * Defines a queue that is temporary and gets deleted when no longer used.
     * <p/>
     * If the queue already exist nothing happens. If the queue is of another type an
     * _APSMessageException_ could possibly be thrown depending on implementation and
     * underlying service used.
     *
     * @param name The name of the queue to define.
     * @throws se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService.APSMessageException
     *                                       (possibly) on trying to redefine type.
     * @throws UnsupportedOperationException If this type of queue is not supported by the implementation.
     */
    @Override
    public Queue defineTemporaryQueue(String name) throws APSMessageException, UnsupportedOperationException {
        connect();
        try {
            this.channel.queueDeclare(name, false, false, true, null);
        }
        catch (IOException ioe) {
            throw new APSMessageException(ioe.getMessage(), ioe);
        }

        return new QueueProvider(name);
    }

    /**
     * Returns the named queue or null if no such queue exists.
     *
     * @param name The name of the queue to get.
     */
    @Override
    public Queue getQueue(String name) {
        return queueExists(name) ? new QueueProvider(name) : null;
    }

    /**
     * Provides implementation of the Queue API.
     */
    private class QueueProvider implements Queue {
        //
        // Private Members
        //

        private String name;

        //
        // Constructors
        //

        /**
         * Creates a new QueueProvider.
         *
         * @param name The queue name represented by this instance.
         */
        public QueueProvider(String name) {
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
         * @throws se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService.APSMessageException
         *
         */
        @Override
        public void sendMessage(Message message) throws APSMessageException {
            try {
                channel.basicPublish("", this.name, MessageProperties.PERSISTENT_BASIC, message.getBytes());
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
                logger.info("Created new receiver for queue '" + this.name + "'!");
                listenerThreads.put(this.name, receiverThread);
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
                    logger.info("The receiver for queue '" + this.name + "' has no more listeners so it is being " +
                        "shutdown until new listeners becomes available again.");
                    listenerThreads.remove(receiverThread);
                    receiverThread.stopThread();
                    //try {receiverThread.join(8000);} catch (InterruptedException ie) {}
                }
            }
        }

        /**
         * Deletes this queue.
         *
         * @throws se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService.APSMessageException
         *                                       on failure.
         * @throws UnsupportedOperationException if this is not allowed by the implementation.
         */
        @Override
        public void delete() throws APSMessageException, UnsupportedOperationException {
            try {
                channel.queueDelete(this.name);
            }
            catch (IOException ioe) {
                throw new APSMessageException(ioe.getMessage(), ioe);
            }
        }

        /**
         * Clears all the messages from the queue.
         *
         * @throws se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService.APSMessageException
         *                                       on failure.
         * @throws UnsupportedOperationException If this is not allowed by the implementation.
         */
        @Override
        public void clear() throws APSMessageException, UnsupportedOperationException {
            try {
                channel.queuePurge(this.name);
            }
            catch (IOException ioe) {
                throw new APSMessageException(ioe.getMessage(), ioe);
            }
        }

        /**
         * Thread that receives queue messages.
         */
        private class ReceiverThread extends Thread {
            //
            // Private Members
            //

            private boolean running = true;

            List<Queue.Message.Listener> listeners =
                    Collections.synchronizedList(new LinkedList<Queue.Message.Listener>());

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
             * Thread entry and exit point.
             */
            public void run() {
                try {
                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(name, false, consumer);
                    while (keepRunning()) {
                        try {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery(5000);
                            Message message = createMessage(delivery.getBody());
                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                            for (Message.Listener listener : this.listeners) {
                                listener.receiveMessage(name, message);
                            }

                        }
                        catch (InterruptedException ie) {
                            // This is OK, we get here on timeout. In this case we want to check if we
                            // should keep running and if so wait some more for a delivery.
                        }
                    }
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
        }
    }
}
