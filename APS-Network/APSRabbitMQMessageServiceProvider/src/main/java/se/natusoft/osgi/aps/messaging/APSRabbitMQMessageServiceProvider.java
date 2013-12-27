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
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener;
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService;
import se.natusoft.osgi.aps.messaging.config.RabbitMQConnectionConfig;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.*;

import java.io.IOException;
import java.util.*;

/**
 * This provides an implementation of APSMessageService using RabbitMQ.
 *
 * **WARNING:** For some reason when many consecutive messages are send in a burst
 *              the data seem to get garbled at the receiving end. Sending sporadic
 *              messages seems to work fine. I still fail to see why.
 */
@OSGiServiceProvider(properties = {
        @OSGiProperty(name = "underlying-provider", value = "RabbitMQ"),
        @OSGiProperty(name = "supports-complete-api", value = "true")
}, threadStart = true)
public class APSRabbitMQMessageServiceProvider implements APSMessageService {
    //
    // Private Members
    //

    /** Our logger. */
    @Managed(loggingFor = "aps-rabbitmq-message-service-provider")
    private APSLogger logger;

    /** Flag to see if the connection to the RabbitMQ service have been done. */
    private boolean connected = false;

    /** A connection factory for connecting to RabbitMQ. */
    private ConnectionFactory connectionFactory = null;

    /** The RabbitMQ service connection. */
    private Connection connection = null;

    /** The channel to communicate on. Always use getGeneralChannel() to get this! */
    private Channel generalChannel = null;

    /** The listener threads. */
    private Map<String, QueueProvider.ReceiverThread> listenerThreads = new HashMap<>();

    private APSConfigChangedListener configChangedListener;

    private Runnable queueUpdater;

    /** Created and cached queue providers. */
    private Map<String, QueueProvider> queueProviders = new HashMap<>();

    //
    // Constructors
    //

    /**
     * Creates a new APSRabbitMQMessageServiceProvider.
     */
    public APSRabbitMQMessageServiceProvider() {}

    @BundleStart
    public void startup() {
        // Please note that we can access config here due to the threadStart=true in the @OSGiServiceProvider
        // above. The instance of this class will be created in a separate thread from the OSGi server startup
        // thread that calls the activator.

        ensureConnection();

        this.queueUpdater = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < RabbitMQConnectionConfig.managed.get().queues.size(); i++) {
                    RabbitMQConnectionConfig.RabbitMQQueueConfig queue =
                            RabbitMQConnectionConfig.managed.get().queues.get(i);
                    boolean durable = queue.durable.toBoolean();
                    try {
                        getGeneralChannel().queueDeclare(queue.name.toString(), durable, false, false, null);
                        logger.info("Declaring RabbitMQ queue: " + queue.name.toString());
                    }
                    catch (IOException ioe) {
                        throw new APSMessageException("Failed to declare queue! [" + queue.name.toString() + "]", ioe);
                    }
                }
            }
        };

        this.queueUpdater.run();

        this.configChangedListener = new APSConfigChangedListener() {
            @Override
            public void apsConfigChanged(APSConfigChangedEvent event) {
                synchronized (APSRabbitMQMessageServiceProvider.this) {
                    ensureConnectionClosed();
                    ensureConnection();
                }
                queueUpdater.run();
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

        for (QueueProvider.ReceiverThread receiverThread : this.listenerThreads.values()) {
            receiverThread.removeAllListeners();
            receiverThread.stopThread();
            try {receiverThread.join(8000);} catch (InterruptedException ie) {};
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
        if (this.generalChannel != null && this.generalChannel.isOpen()) {
            try {
                this.generalChannel.close();
            }
            catch (IOException ioe) {
                this.logger.error("Failed to close RabbitMQ channel!", ioe);
            }
        }

        for (String queueName : this.queueProviders.keySet()) {
            QueueProvider qp = this.queueProviders.get(queueName);
            try {
                qp.close();
            }
            catch (IOException ioe) {
                this.logger.error("Failed to close QueueProvider for '" + queueName + "'!" ,ioe);
            }
        }

        if (this.connection != null) {
            try {
                this.connection.close();
            }
            catch (IOException ioe) {
                this.logger.error("Failed to close RabbitMQ connection on shutdown!", ioe);
            }

            this.logger.info("Disconnected from RabbitMQ server at " +
                    RabbitMQConnectionConfig.managed.get().host.toString() + ":" +
                    RabbitMQConnectionConfig.managed.get().port.toString() + "!");
        }

    }

    /**
     * Returns the current channel or creates a new if it does not exist or the existing one has been closed.
     *
     * @return A Channel.
     */
    private Channel getGeneralChannel() {
        ensureConnection();
        if (this.generalChannel == null || !this.generalChannel.isOpen()) {
            try {
                this.generalChannel = this.connection.createChannel();
            }
            catch (IOException ioe) {
                throw new APSMessageException("Failed to create channel!", ioe);
            }
        }

        return this.generalChannel;
    }

    /**
     * @return The current connection.
     */
    private Connection getConnection() {
        ensureConnection();
        return this.connection;
    }

    /**
     * Checks if the named queue exists.
     *
     * @param name The name of the queue to check.
     * @return true if queue exists, false otherwise.
     */
    @Override
    public boolean queueExists(String name) {
        try {
            getGeneralChannel().queueDeclarePassive(name);
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
        try {
            getGeneralChannel().queueDeclare(name, false, false, false, null);
            return new QueueProvider(name);
        }
        catch (IOException ioe) {
            throw new APSMessageException(ioe.getMessage(), ioe);
        }
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
        try {
            getGeneralChannel().queueDeclare(name, true, false, false, null);
            return new QueueProvider(name);
        }
        catch (IOException ioe) {
            throw new APSMessageException(ioe.getMessage(), ioe);
        }
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
        try {
            getGeneralChannel().queueDeclare(name, false, false, true, null);
            return new QueueProvider(name);
        }
        catch (IOException ioe) {
            throw new APSMessageException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Returns the named queue or null if no such queue exists.
     *
     * @param name The name of the queue to get.
     */
    @Override
    public Queue getQueue(String name) {
        QueueProvider queue = this.queueProviders.get(name);
        if (queue == null && queueExists(name)) {
            try {
                queue = new QueueProvider(name);
                this.queueProviders.put(name, queue);
            }
            catch (IOException ioe) {
                throw new APSMessageException(ioe.getMessage(), ioe);
            }
        }

        return queue;
    }

    /**
     * Provides implementation of the Queue API.
     */
    private class QueueProvider implements Queue {
        //
        // Private Members
        //

        private String name;

        private Channel sendChannel = null;

        private Channel recvChannel = null;
        private String recvQueueName = null;

        //
        // Constructors
        //

        /**
         * Creates a new QueueProvider.
         *
         * @param name The queue name represented by this instance.
         */
        public QueueProvider(String name) throws IOException {
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
         * Returns the receive channel.
         * @throws Exception
         */
        private Channel getRecvChannel() throws IOException {
            if (this.recvChannel == null || !this.recvChannel.isOpen()) {
                this.recvChannel = getConnection().createChannel();
                this.recvChannel.exchangeDeclare(this.name, "fanout");
                this.recvQueueName = this.recvChannel.queueDeclare().getQueue();
                this.recvChannel.queueBind(this.recvQueueName, this.name, "");
            }
            return this.recvChannel;
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
         * @throws se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService.APSMessageException
         *
         */
        @Override
        public void sendMessage(Message message) throws APSMessageException {
            try {
                getSendChannel().basicPublish(this.name, "", null, message.getBytes());
                logger.debug("Sent message of length " + message.getBytes().length);
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
                    logger.warn("The receiver for queue '" + this.name + "' has no more listeners!");
                    // Not entirely sure this is a good idea ...
                    //listenerThreads.remove(this.name);
                    //receiverThread.stopThread();
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
                getGeneralChannel().queueDelete(this.name);
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
                getGeneralChannel().queuePurge(this.name);
            }
            catch (IOException ioe) {
                throw new APSMessageException(ioe.getMessage(), ioe);
            }
        }

        /**
         * Closes this queue provider.
         *
         * @throws IOException
         */
        public void close() throws IOException {
            try {
                if (this.sendChannel != null) this.sendChannel.close();
            }
            finally {
                if (this.recvChannel != null) this.recvChannel.close();
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
                    QueueingConsumer consumer = new QueueingConsumer(getRecvChannel());
                    getRecvChannel().basicConsume(QueueProvider.this.recvQueueName, true, consumer);
                    while (keepRunning()) {
                        try {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery(5000);
                            if (delivery != null) {
                                byte[] body = delivery.getBody();
                                //logger.debug("======== Received message of length " + body.length + " ==========");
                                //logger.debug("  Current no listeners: " + this.listeners.size());
                                Message message = createMessage(body);
                                getGeneralChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                                for (Message.Listener listener : this.listeners) {
                                    listener.receiveMessage(name, message);
                                }
                            }
                            else {
                                //logger.debug("====== TIMEOUT ======");
                            }

                        }
                        catch (ShutdownSignalException sse) {
                            throw sse;
                        }
                        catch (ConsumerCancelledException cce) {
                            throw cce;
                        }
                        // We dont want this thread to die on Exception!
                        catch (Exception e) {
                            logger.error("ReceiverThread got an Exception!", e);
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
