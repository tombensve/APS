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
import se.natusoft.osgi.aps.types.APSValue;

/**
 * This is a message container that wrap actual received messages. This to support reply-ability
 * per message. This in turn is due to that is how the Vert.x eventbus works. This supports that
 * and other. To support miscellaneous messaging apis the received messages must be wrapped.
 *
 * @param <Message> The type of the content.
 */
@SuppressWarnings("unused")
public interface APSMessage<Message> extends APSValue<Message> {

    /**
     * Replies to message.
     *
     * @param reply The message to reply with.
     */
    default void reply(@NotNull Message reply) {
        throw new APSMessagingException( "This message cannot be replied to!" );
    }

    /**
     * Provides a simple default implementation. This should probably be extended.
     *
     * @param <Message> The type of the content.
     */
    class Provider<Message> extends APSValue.Provider<Message> implements APSMessage<Message> {

        /**
         * Creates a new Provider instance.
         *
         * @param content The message content.
         */
        public Provider(Message content) {
            super(content);
        }

    }
}
