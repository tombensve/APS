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
import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.exceptions.APSException;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;

/**
 * This is a sender that sends a message to zero or one subscribers. If there are more than one subscriber
 * to the destination then it is up to the implementation who gets the message.
 *
 * For a Vertx eventbus based implementation it would do a round robin when there are more than one
 * subscriber for example. But this is entirely up to what is supported by the message solution used
 * by the implementation.
 *
 * @param <Message> The type of the message being sent.
 */
public interface APSMessageSender<Message> {

    /**
     * Sends a message receiving a result of success or failure. On Success there
     * _can_ be a result value and on failure there is an Exception describing the failure
     * available. This variant never throws an Exception.
     *
     * @param destination The destination of the message. Preferably this is something that the
     *                    service looks up to get a real destination, rather than an absolute
     *                    destination. Also note that if the implementation supports sending to
     *                    one or many receivers, this information should be part of the destination
     *                    value. For example: "mylistener" or "all:mylisteners". It is up to the
     *                    implementation to define this. And if a configured name is looked up then
     *                    that should also resolve how to send message.
     * @param message The message to send.
     * @param result  The result of the send. If null an APSMessagingException will be thrown on failure.
     */
    void send(@NotNull String destination, @NotNull Message message, @Nullable APSHandler<APSResult> result);

    /**
     * This must be called before send(...). send will use the last supplied reply subscriber.
     *
     * Note that this uses a fluent API that returns this. This allows for just adding
     * ".send(...)" after the call to this.
     *
     * @param handler the handler of the reply.
     */
    @SuppressWarnings("unused")
    default APSMessageSender<Message> replyTo(APSHandler<APSMessage<Message>> handler) {
        throw new APSException( "replyTo(...) is not supported by this implementation!" );
    }

}
