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
 *         2017-01-01: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;

import java.util.Properties;

/**
 * Implementations of this should register themselves with an APS.Messaging.Protocol.Name property with the
 * implemented protocol name. A protocol name of APS.Value.Messaging.Protocol.ROUTER means that the implementation
 * will use the APSMessageTopics service to map the topic to a protocol and track an implementation of
 * APSMessageService for that protocol and then forward to that.
 *
 * There should only be one implementation for each protocol deployed. If there are more than one for a
 * protocol it is undetermined which will be used.
 */
public interface APSMessageService {

    /**
     * Sends a message to one destination.
     *
     * @param topic The destination to send message.
     * @param message The message to send. What is allowed here depends on the provider.
     * @param properties Implementation specific properties. Can be null.
     */
    void publish(@NotNull String topic, @NotNull Object message, @Nullable Properties properties);

    /**
     * Sends a message to one destination.
     *
     * @param topic The destination to send message.
     * @param message The message to send. What is allowed here depends on the provider.
     */
    void publish(@NotNull String topic, @NotNull Object message);

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param topic The endpoint to listen to.
     * @param subscriber The subscriber to call with received messages.
     * @param properties Implementation specific properties. Can be null.
     */
    void subscribe(@NotNull String topic, @NotNull APSSubscriber subscriber, @Nullable Properties properties);

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param topic The endpoint to listen to.
     * @param subscriber The subscriber to call with received messages.
     */
    void subscribe(@NotNull String topic, @NotNull APSSubscriber subscriber);

    /**
     * Removes a listener for a source.
     *
     * @param topic The endpoint to remove listener for.
     * @param subscriber The subscriber to remove.
     */
    void unsubscribe(@NotNull String topic, @NotNull APSSubscriber subscriber);

    /**
     * An abstract base class to make implementation cleaner when no properties are supported.
     */
    abstract class AbstractAPSMessageService implements APSMessageService {

        /**
         * Sends a message to one destination.
         *
         * @param topic The destination to send message.
         * @param message The message to send. What is allowed here depends on the provider.
         */
        public void publish(@NotNull String topic, @NotNull Object message) {
            publish(topic, message, null);
        }

        /**
         * Adds a listener for messages arriving on a specific source.
         *
         * @param topic The endpoint to listen to.
         * @param subscriber The subscriber to call with received messages.
         */
        public void subscribe(@NotNull String topic, @NotNull APSSubscriber subscriber) {
            subscribe(topic, subscriber, null);
        }
    }
}
