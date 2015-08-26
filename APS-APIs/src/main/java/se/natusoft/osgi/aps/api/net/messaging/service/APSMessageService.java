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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This defines a simple message service. Can be implemented by using a message bus like RabbitMQ, Active MQ, etc
 * or just a simple tcpip server or whatever.
 *
 * Since the actual members are outside of this service API, it doesn't really know who they are and doesn't
 * care, all members are defined by configuration.
 */
public interface APSMessageService {

    /**
     * Multiple providers of this service can be deployed at the same time. Using this property
     * when registering services for a provider allows clients to lookup a specific provider.
     */
    String APS_MESSAGE_SERVICE_PROVIDER = "aps-message-service-provider";

    /**
     * Each configured instance of this service should have this property with a unique
     * instance name so that client can lookup a specific instance of the service.
     */
    String APS_MESSAGE_SERVICE_INSTANCE_NAME = "aps-message-service-instance-name";

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
     * @throws APSMessagingException on failure.
     */
    void sendMessage(byte[] message) throws APSMessagingException;

    //
    // Inner Classes
    //

    /**
     * Provides an abstract implementation of the APSMessageService interface.
     */
    public static abstract class AbstractMessageServiceProvider implements APSMessageService {
        //
        // Private Members
        //

        /** Registered listeners. */
        private List<APSMessageListener> messageListeners = Collections.synchronizedList(new LinkedList<APSMessageListener>());

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
        protected void sendToListeners(byte[] message) {
            for (APSMessageListener messageListener : this.messageListeners) {
                messageListener.messageReceived(message);
            }
        }

        /**
         * Returns the message listeners.
         */
        protected List<APSMessageListener> getMessageListeners() {
            return this.messageListeners;
        }
    }

}
