/*
 *
 * PROJECT
 *     Name
 *         APS RabbitMQ SimpleMessageService Provider
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
package se.natusoft.osgi.aps.net.messaging.service;

import com.rabbitmq.client.Channel;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage;
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService;
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQMessageServiceConfig;
import se.natusoft.osgi.aps.net.messaging.rabbitmq.PeskyWabbitConnectionManager;
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
        @OSGiProperty(name = APSMessageService.MESSAGING_PROVIDER_PROPERTY, value = "RabbitMQ"),
}, threadStart = true)
public class APSRabbitMQMessageServiceProvider implements APSMessageService {
    //
    // Private Members
    //

    /** Our logger. */
    @Managed(loggingFor = "aps-rabbitmq-message-service-provider")
    private APSLogger logger;

    private APSConfigChangedListener configChangedListener;

    /** For connecting to RabbitMQ. */
    private PeskyWabbitConnectionManager rabbitMQManager;

    private Map<String/*Group*/, Channel> sendingChannels = new HashMap<>();


    //
    // Constructors
    //

    /**
     * Creates a new APSRabbitMQSimpleMessageServiceProvider.
     */
    public APSRabbitMQMessageServiceProvider() {}

    @BundleStart
    public void startup() {
        // Please note that we can access config here due to the threadStart=true in the @OSGiServiceProvider
        // above. The instance of this class will be created in a separate thread from the OSGi server startup
        // thread that calls the activator.

        try {
            this.logger.info(this.rabbitMQManager.ensureConnection());

            this.configChangedListener = new APSConfigChangedListener() {
                @Override
                public void apsConfigChanged(APSConfigChangedEvent event) {
                    synchronized (APSRabbitMQMessageServiceProvider.this) {
                        try {
                            APSRabbitMQMessageServiceProvider.this.rabbitMQManager.reconnect();
                        }
                        catch (IOException ioe) {
                            APSRabbitMQMessageServiceProvider.this.logger.error("Failed reconnecting to RabbitMQ!", ioe);
                        }
                    }
                }
            };

            RabbitMQMessageServiceConfig.managed.get().addConfigChangedListener(this.configChangedListener);
        }
        catch (IOException ioe) {
            this.logger.error("Failed to connect to RabbitMQ!", ioe);
        }
    }

    //
    // Shutdown
    //

    @BundleStop
    public void shutdown() {
        if (this.configChangedListener != null) {
            RabbitMQMessageServiceConfig.managed.get().removeConfigChangedListener(this.configChangedListener);
        }

        for (MessageGroupProvider.ReceiverThread receiverThread : this.listenerThreads.values()) {
            receiverThread.removeAllListeners();
            receiverThread.stopThread();
            try {receiverThread.join(8000);} catch (InterruptedException ie) {}
        }

        try {
            this.logger.info(this.rabbitMQManager.ensureConnectionClosed());
        }
        catch (IOException ioe) {
            this.logger.error("Failed to close RabbitMQ connection!", ioe);
        }
    }

    //
    // Methods
    //

    /**
     * The defined groups made available by this service. That is, the group names that can be passed
     * to sendMessage(...) and readMessage(...) without being guaranteed to throw an exception.
     */
    @Override
    public List<String> providedGroups() {
        List<String> groups = new LinkedList<>();
        for (APSConfigValue group : RabbitMQMessageServiceConfig.managed.get().groups) {
            groups.add(group.toString());
        }

        return groups;
    }

    /**
     * Sends a messaging.
     *
     * @param group The group to send to.
     * @param message The message to send.
     * @return true if the messaging was sent.
     * @throws se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException on failure.
     */
    @Override
    public boolean sendMessage(String group, APSMessage message) throws APSMessagingException {
        return false;
    }

    /**
     * Adds a listener for types.
     *
     * @param group The group to listen to.
     * @param listener The listener to add.
     */
    @Override
    public void addMessageListener(String group, Listener listener) {

    }

    /**
     * Removes a messaging listener.
     *
     * @param group The group listening to.
     * @param listener The listener to remove.
     */
    @Override
    public void removeMessageListener(String group, Listener listener) {

    }

    public void openGroups() throws IOException {
        for (RabbitMQMessageServiceConfig.GEQ group : RabbitMQMessageServiceConfig.managed.get().groups) {
            Channel channel =
                    this.rabbitMQManager.ensureOpenChannel(
                            null, // Will force creation of new!
                            group.exchange.toString(),
                            group.exchangeType.toString(),
                            group.queue.toString(),
                            group.routingKey.toString()
                    );
            this.sendingChannels.put(group.group.toString(), channel);
        }
    }

    public void closeGroups() throws IOException {
        IOException ioe = null;
        for (String groupName : this.sendingChannels.keySet()) {
            Channel channel = this.sendingChannels.get(groupName);
            try {
                channel.close();
            }
            catch (IOException ioe2) {
                Throwable cause = ioe;
                if (ioe == null) {
                    cause = ioe2;
                }
                ioe = new IOException("Failed to close channel for group '" + groupName + "'!", cause);
            }
        }
        if (ioe != null) throw ioe;
    }

}

