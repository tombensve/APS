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
 *         2019-08-17: Created!
 *
 */
package se.natusoft.aps.api.messaging;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.docutations.Optional;
import se.natusoft.aps.types.APSHandler;
import se.natusoft.aps.types.APSResult;
import se.natusoft.aps.types.ID;

import java.util.Map;

public interface APSBus {

    /**
     * Sends a message.
     *
     * @param target The target to send to. Note that for send you can give multiple, comma separated
     *        targets to send same message to multiple places.
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler Receives the success or failure of the call.
     */
    void send( @NotNull String target, @NotNull Map<String, Object> message,
               @Optional @Nullable APSHandler<APSResult<?>> resultHandler );

    /**
     * Subscribes to messages to a target.
     *
     * @param id A unique ID to associate subscription with. Also used to unsubscribe.
     * @param target The target to subscribe to.
     * @param messageHandler The handler to call with messages sent to target.
     */
    void subscribe( @NotNull ID id, @NotNull String target, @Optional @Nullable APSHandler<APSResult<?>> resultHandler,
                    @NotNull APSHandler<Map<String, Object>> messageHandler );

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    void unsubscribe( @NotNull ID subscriberId );

    /**
     * Sends a message and expects to get a response message back.
     *
     * This is not forwarded to a APSBusRouter! This is locally implemented
     * and does the following:
     *
     * - Generates a unique reply address.
     * - Subscribes to address.
     *   - After reply message is received and forwarded to handler, the
     *     message subscription is unsubscribed.
     * - Updates message header.replyAddress with address
     * - Sends message.
     *
     * This should theoretically work for any APSBusRouter implementation. For
     * some it might not make sense however.
     *
     * @param target The target to send to.
     * @param message The message to send.
     * @param resultHandler optional handler to receive result of send.
     * @param responseMessage A message that is a response of the sent message.
     */
    void request( @NotNull String target, @NotNull Map<String, Object> message,
                  @Nullable @Optional APSHandler<APSResult<?>> resultHandler,
                  @NotNull APSHandler<Map<String, Object>> responseMessage );

    /**
     * Replies to a received message.
     *
     * @param replyTo The received message to reply to.
     * @param reply The reply.
     * @param resultHandler The result of sending reply.
     */
    void reply(@NotNull  Map<String, Object> replyTo, @NotNull Map<String, Object> reply,
               @Nullable APSHandler<APSResult<?>> resultHandler );
}
