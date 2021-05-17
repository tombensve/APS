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
package se.natusoft.aps.api.messaging;

import se.natusoft.aps.types.APSHandler;
import se.natusoft.aps.types.APSResult;
import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.docutations.Todo;

import java.util.Map;

@Todo(description = "Consider removing this!")
/**
 * This is a sender that sends a message to zero or one subscribers. If there are more
 * than one subscriber to the destination then it is up to the implementation who gets
 * the message.
 *
 * For a Vertx eventbus based implementation it would do a round robin when there are more
 * than one subscriber for example. But this is entirely up to what is supported by the
 * message solution used by the implementation.
 */
public interface APSMessageSender {

    /**
     * Sends a message receiving a result of success or failure. On Success there
     * _can_ be a result value and on failure there is an Exception describing the failure
     * available.
     *
     * **Note** that depending on implementation the message might be delivered to more
     * than one receiver!
     *
     * @param destination The destination of the message. Note that this is just a
     * string! It is up to each implementation to handle/interpret this!
     *
     * @param message The message to send.
     * @param result The result of the send. If null an APSMessagingException will be
     * thrown on failure.
     */
    <T> void send( @NotNull String destination, @NotNull Map<String, Object> message,
               @Nullable APSHandler<APSResult<T>> result );
}
