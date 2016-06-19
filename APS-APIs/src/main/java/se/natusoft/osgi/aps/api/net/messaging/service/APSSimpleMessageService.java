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

import java.util.*;

/**
 * This defines a simple message service. Can be implemented by using a message bus like RabbitMQ, Active MQ, etc
 * or just a simple tcpip server or whatever.
 *
 * Since the actual members are outside of this service API, it doesn't really know who they are and doesn't
 * care, all members are defined by configuration.
 *
 * The term 'target' is used in this API. This can be anything relevant for routing a message to its
 * destination. It depends a lot on the underlaying implementation. JMS for example have both a Queues
 * that can be sent to using queue names, but also topics. If JMS is used then the target can be either a
 * queue or a topic. The target just represents some destination of the message.
 */
public interface APSSimpleMessageService {

    /**
     * Multiple providers of this service can be deployed at the same time. Using this property
     * when registering services for a provider allows clients to lookup a specific provider.
     */
    @SuppressWarnings("unused")
    String APS_MESSAGE_SERVICE_PROVIDER = "aps-message-service-provider";

    /**
     * Listener for message.
     */
    interface MessageListener {

        /**
         * This is called when a message is received.
         *
         * @param target The target the message was sent to.
         * @param message The received message.
         */
        void messageReceived(String target, APSMessage message);
    }

    /**
     * Adds a listener for types.
     *
     * @param target The target to listen to.
     * @param listener The listener to add.
     */
    void addMessageListener(String target, MessageListener listener);

    /**
     * Removes a messaging listener.
     *
     * @param target The target to stop listening to.
     * @param listener The listener to remove.
     */
    void removeMessageListener(String target, MessageListener listener);

    /**
     * Creates a new APSMessage.
     */
    APSMessage createMessage();

    /**
     * Sends a message.
     *
     * @param target The target of the message.
     * @param message The message to send.
     *
     * @throws APSMessagingException on failure.
     */
    void sendMessage(String target, APSMessage message) throws APSMessagingException;

    //
    // Inner Classes
    //

    /**
     * Provides an abstract implementation of the APSMessageService interface.
     */
    @SuppressWarnings("unused")
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
         * @param target The topic to listen to.
         * @param listener The listener to add.
         */
        @Override
        public void addMessageListener(String target, MessageListener listener) {
            List<MessageListener> listeners = lookupMessageListeners(target);
            if (listeners == null) {
                listeners = new LinkedList<>();
                this.messageListeners.put(target, listeners);
            }
            listeners.add(listener);
        }

        /**
         * Removes a messaging listener.
         *
         * @param target The topic to stop listening to.
         * @param listener The listener to remove.
         */
        @Override
        public void removeMessageListener(String target, MessageListener listener) {
            List<MessageListener> listeners = lookupMessageListeners(target);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }

        /**
         * Sends a message to the registered listeners.
         *
         * @param target The target of the message whose listeners to forward to.
         * @param message The message to send.
         */
        @SuppressWarnings("unused")
        protected void sendToListeners(String target, APSMessage message) {
            List<MessageListener> listeners = lookupMessageListeners(target);
            for (MessageListener messageListener : listeners) {
                messageListener.messageReceived(target, message);
            }
        }

        /**
         * Returns the message listeners for a target.
         *
         * @param target The target to get listeners for.
         */
        @SuppressWarnings("WeakerAccess")
        protected List<MessageListener> lookupMessageListeners(String target) {
            return this.messageListeners.get(target);
        }
    }

}
