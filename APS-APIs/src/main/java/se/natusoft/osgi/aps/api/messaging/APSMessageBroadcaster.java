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
 *         2018-05-26: Created!
 *
 */
package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.docutations.NotNull;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;

/**
 * This is a broadcaster which means that it sends messages to multiple subscribers. The requirement
 * for this is that the message provider used to implement this supports having multiple subscribers
 * on the same destination/address, and that any message sent will reach all subscribers.
 *
 * @param <Message> The type of the message being published.
 */
public interface APSMessageBroadcaster<Message> {

    /**
     * Publishes a message.
     *
     * @param destination The destination of the message. Preferably this is something that the
     *                    service looks up to get a real destination, rather than an absolute
     *                    destination.
     * @param message The message to publish.
     *
     * @throws APSMessagingException on any failure. Note that this is a RuntimeException!
     */
    void send(@NotNull String destination, @NotNull Message message) throws APSMessagingException;

    /**
     * Publishes a message receiving a result of success or failure. On Success there
     * can be a result value and on failure there is an Exception describing the failure
     * available. This variant never throws an Exception.
     *
     * @param destination The destination of the message. Preferably this is something that the
     *                    service looks up to get a real destination, rather than an absolute
     *                    destination.
     * @param message The message to publish.
     * @param result Callback providing the success or failure of the call.
     */
    void send(@NotNull String destination, @NotNull Message message, @NotNull APSHandler<APSResult<Message>> result);
}
