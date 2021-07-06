/*
 *
 * PROJECT
 *     Name
 *         APS VertX Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         This service provides configured Vertx instances allowing multiple services to use the same Vertx instance.
 *
 *         This service also provides for multiple instances of VertX by associating an instance with a name. Everyone
 *         asking for the same name will get the same instance.
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
 *         2018-05-28: Created!
 *
 */
package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import se.natusoft.docutations.NotNull
import se.natusoft.aps.api.messaging.APSMessage

@CompileStatic
class APSMessageProvider implements APSMessage {

    Message vertxMsg
    Map<String, Object> message


    /**
     * @return the message content.
     */
    @Override
    Map<String, Object> content() {
        return this.message
    }

    /**
     * Replies to message.
     *
     * @param reply The message to reply with.
     */
    @Override
    void reply( @NotNull Map<String, Object> reply ) {
        this.vertxMsg.reply( new JsonObject( reply ) )
    }
}
