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
 *         2015-01-09: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage;
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessageListener;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * This defines a simple message service. Can be implemented by using a message bus like RabbitMQ, Active MQ, etc
 * or just a simple socket server.
 */
public interface APSMessageService {

    /**
     * Multiple providers of this service can be deployed at the same time. Using this property
     * when registering services for a provider allows clients to lookup a specific provider.
     */
    public static final String MESSAGING_PROVIDER = "aps-messaging-provider";

    /**
     * Each configured instance of this service should have this property with a unique
     * instance name so that client can lookup a specific instance of the service.
     */
    public static final String MESSAGING_INSTANCE_NAME = "aps-messaging-instance-name";

    /**
     * Returns the name of this instance.
     */
    String getName();

    /**
     * Every service implementation should have a UUID, which also gets passed in messages.
     */
    UUID getProviderUUID();

    /**
     * Returns general informative information about this instance. Implementations can return "" if they want.
     */
    String getMessagingProviderInfo();

    /**
     * Adds a listener for types.
     *
     * @param listener The listener to add.
     */
    void addMessageListener(APSMessageListener listener);

    /**
     * Removes a messaging listener.
     *
     * @param listener The listener to remove.
     */
    void removeMessageListener(APSMessageListener listener);

    /**
     * Sends a message.
     *
     * @param message The message to send.
     *
     * @throws se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException on failure.
     */
    void sendMessage(APSMessage message) throws APSMessagingException;

    //
    // Inner Classes
    //

    /**
     * Provides an abstract implementation of the APSMessageService interface.
     */
    public static abstract class AbstractMessageService implements APSMessageService {
        //
        // Private Members
        //

        /** Registered listeners. */
        private List<APSMessageListener> messageListeners = new LinkedList<>();

        //
        // Methods
        //

        /**
         * Adds a listener for types.
         *
         * @param listener The listener to add.
         */
        @Override
        public void addMessageListener(APSMessageListener listener) {
            this.messageListeners.add(listener);
        }

        /**
         * Removes a messaging listener.
         *
         * @param listener The listener to remove.
         */
        @Override
        public void removeMessageListener(APSMessageListener listener) {
            this.messageListeners.remove(listener);
        }

        /**
         * Sends a message to the registered listeners.
         *
         * @param message The message to send.
         */
        protected void sendToListeners(APSMessage message) {
            for (APSMessageListener messageListener : this.messageListeners) {
                messageListener.messageReceived(message.getBytes());
            }
        }
    }

}