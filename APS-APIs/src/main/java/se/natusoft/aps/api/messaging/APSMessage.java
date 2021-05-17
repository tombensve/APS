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

import se.natusoft.aps.types.APSValue;

import java.util.Map;

/**
 * This is a message container that wrap actual received messages. This to support reply-ability
 * per message. This in turn is due to that is how the Vert.x eventbus works. This supports that
 * and other. To support miscellaneous messaging apis the received messages must be wrapped.
 */
@SuppressWarnings("unused")
public interface APSMessage extends APSValue<Map<String, Object>> {

    /**
     * Provides a simple default implementation. This can be extended.
     */
    class Provider extends APSValue.Provider<Map<String, Object>> implements APSMessage {

        /**
         * Creates a new Provider instance.
         *
         * @param content The message content.
         */
        public Provider(Map<String, Object> content) {
            super(content);
        }

    }
}
