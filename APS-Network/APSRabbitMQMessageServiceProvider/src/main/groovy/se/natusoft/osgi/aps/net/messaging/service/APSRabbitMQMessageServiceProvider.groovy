/*
 *
 * PROJECT
 *     Name
 *         APS RabbitMQ Cluster Service Provider
 *
 *     Code Version
 *         1.0.0
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
 *     Licensed under the Apache License, Version 2.0 (the "License")
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
package se.natusoft.osgi.aps.net.messaging.service

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageListener
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.net.messaging.apis.ConnectionProvider
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQMessageServiceConfig
import se.natusoft.osgi.aps.net.messaging.rabbitmq.ReceiveThread
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * This provides an implementation of APSSimpleMessageService using RabbitMQ.
 *
 * Please note that message instances are represented by RabbitMQ exchanges. When a name is joined
 * an exchange with that name and type "fanout" is created. The receiver uses an anonymous queue.
 * Each "name" has its own receiver and sender instanceChannel. Each name will also have its own receiver
 * thread. My first attempt was to reuse channels as much as possible, but that did not work
 * very well, but I'm rather new to RabbitMQ and have to admit I haven't yet fully understood
 * all its features. It was however rather easy to install and get upp and running.
 */
@CompileStatic
@TypeChecked
public class APSRabbitMQMessageServiceProvider implements APSMessageService {

    //
    // Private Members
    //

    /** Basic RabbitMQ config for sending messages. */
    private AMQP.BasicProperties basicProperties

    /** Our cluster channel. */
    private Channel instanceChannel

    /** This receives messages from the cluster. */
    private ReceiveThread instanceReceiveThread

    //
    // Properties
    //

    /** The name of the instance. */
    String name

    /** The logger to log to. */
    APSLogger logger

    /**
     * Provides a RabbitMQ Connection. Rather than taking a Connection directly, this can
     * always provide a fresh connection.
     */
    ConnectionProvider connectionProvider

    /** A configuration for this specific cluster provider instance. */
    RabbitMQMessageServiceConfig.RMQInstance instanceConfig

    //
    // Constructors
    //

    /**
     * Creates a new APSRabbitMQClusterServiceProvider.
     */
    public APSRabbitMQMessageServiceProvider() {
        AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder()
        this.basicProperties = bob.contentType("application/octet-stream").build()
    }

    //
    // Methods
    //

    // ----- Side effects of using Groovy properties constructor ...

    private static validate(Object what, String message) {
        if (what == null) throw new IllegalArgumentException(message)
    }

    private void validate() {
        validate(this.connectionProvider, "Missing required 'connectionProvider'!")
        validate(this.instanceConfig, "Missing required 'instanceConfig'!")
    }

    // -----

    /**
     * Ensures an open instanceChannel and returns it.
     *
     * @throws IOException
     */
    private Channel ensureInstanceChannel() throws IOException {
        validate()

        if (this.instanceChannel == null || !this.instanceChannel.isOpen()) {
            this.instanceChannel = this.connectionProvider.connection.createChannel()
            this.instanceChannel.exchangeDeclare(this.instanceConfig.exchange.string, this.instanceConfig.exchangeType.string)
            this.instanceChannel.queueDeclare(this.instanceConfig.queue.string, true, false, false, null)
            String routingKey = this.instanceConfig.routingKey.string
            if (routingKey != null && routingKey.isEmpty()) {
                routingKey = null
            }
            this.instanceChannel.queueBind(this.instanceConfig.queue.string, this.instanceConfig.exchange.string, routingKey)
        }

        return this.instanceChannel
    }

    /**
     * Ensures a cluster receive thread and returns it.
     */
    private void startReceiveThread() {
        validate()

        if (this.instanceReceiveThread == null) {
            this.instanceReceiveThread = new ReceiveThread(
                    name: "cluster-receive-thread-" + getName(),
                    connectionProvider: this.connectionProvider,
                    instanceConfig: this.instanceConfig,
                    logger: this.logger
            )
            this.instanceReceiveThread.start()
        }
    }

    private void stopReceiveThread() {
        try {
            if (this.instanceReceiveThread != null) {
                this.instanceReceiveThread.stopThread()
                this.instanceReceiveThread.wait(20000)
            }
        }
        catch (InterruptedException ie) {
            throw new APSMessagingException("[${this.name}]:Failed to stop receive thread!", ie)
        }
    }

    public void start() {
        startReceiveThread()
    }

    public void stop() {
        stopReceiveThread()
    }

    /**
     * Sends a message.
     *
     * @param message The message to send.
     *
     * @throws APSMessagingException on failure.
     */
    @Override
    void sendMessage(byte[] message) throws APSMessagingException {
        String routingKey = this.instanceConfig.routingKey.string
        if (routingKey.isEmpty()) {
            routingKey = null
        }

        ensureInstanceChannel().basicPublish(this.instanceConfig.exchange.string, routingKey, this.basicProperties, message)
    }

    /**
     * Adds a listener for types.
     *
     * @param listener The listener to add.
     */
    @Override
    void addMessageListener(APSMessageListener listener) {
        this.instanceReceiveThread.addMessageListener(listener)
    }

    /**
     * Removes a messaging listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    void removeMessageListener(APSMessageListener listener) {
        this.instanceReceiveThread.removeMessageListener(listener)
    }
}

