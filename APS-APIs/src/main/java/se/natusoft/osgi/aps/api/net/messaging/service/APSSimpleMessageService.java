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
import se.natusoft.osgi.aps.api.net.util.TypedData;

import java.util.*;

/**
 * This defines a simple message service. Can be implemented by using a message bus like RabbitMQ, Active MQ, etc
 * or just a simple tcpip server or whatever.
 *
 * Since the actual members are outside of this service API, it doesn't really know who they are and doesn't
 * care, all members are defined by configuration.
 */
public interface APSSimpleMessageService {

    /**
     * Multiple providers of this service can be deployed at the same time. Using this property
     * when registering services for a provider allows clients to lookup a specific provider.
     */
    String APS_MESSAGE_SERVICE_PROVIDER = "aps-message-service-provider";

    /**
     * Listener for message.
     */
    interface MessageListener {

        /**
         * This is called when a message is received.
         *
         * @param topic The topic the message belongs to.
         * @param message The received message.
         */
        void messageReceived(String topic, TypedData message);
    }

    /**
     * Adds a listener for types.
     *
     * @param topic The topic to listen to.
     * @param listener The listener to add.
     */
    void addMessageListener(String topic, MessageListener listener);

    /**
     * Removes a messaging listener.
     *
     * @param topic The topic to stop listening to.
     * @param listener The listener to remove.
     */
    void removeMessageListener(String topic, MessageListener listener);

    /**
     * Sends a message.
     *
     * @param topic The topic of the message.
     * @param message The message to send.
     *
     * @throws APSMessagingException on failure.
     */
    void sendMessage(String topic, TypedData message) throws APSMessagingException;

    //
    // Inner Classes
    //

    /**
     * Provides an abstract implementation of the APSMessageService interface.
     */
    abstract class AbstractMessageServiceProvider implements APSSimpleMessageService {
        //
        // Private Members
        //

        /** Registered listeners. */
        private Map<String, List<MessageListener>> messageListeners = Collections.synchronizedMap(new HashMap<>());

        //
        // Methods
        //

        /**
         * Adds a listener for types.
         *
         * @param topic The topic to listen to.
         * @param listener The listener to add.
         */
        @Override
        public void addMessageListener(String topic, MessageListener listener) {
            List<MessageListener> listeners = lookupMessageListeners(topic);
            if (listeners == null) {
                listeners = new LinkedList<>();
                this.messageListeners.put(topic, listeners);
            }
            listeners.add(listener);
        }

        /**
         * Removes a messaging listener.
         *
         * @param topic The topic to stop listening to.
         * @param listener The listener to remove.
         */
        @Override
        public void removeMessageListener(String topic, MessageListener listener) {
            List<MessageListener> listeners = lookupMessageListeners(topic);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }

        /**
         * Sends a message to the registered listeners.
         *
         * @param message The message to send.
         */
        protected void sendToListeners(String topic, TypedData message) {
            List<MessageListener> listeners = lookupMessageListeners(topic);
            for (MessageListener messageListener : listeners) {
                messageListener.messageReceived(topic, message);
            }
        }

        /**
         * Returns the message listeners for a topic.
         *
         * @param topic The topic to get listeners for.
         */
        protected List<MessageListener> lookupMessageListeners(String topic) {
            return this.messageListeners.get(topic);
        }
    }

}
