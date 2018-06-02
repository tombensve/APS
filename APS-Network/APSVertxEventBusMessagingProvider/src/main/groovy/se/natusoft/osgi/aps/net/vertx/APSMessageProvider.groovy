/* 
 * 
 * PROJECT
 *     Name
 *         APS Vertx Event Bus Messaging Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides an implementation of APSMessageService using Vert.x event bus.
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
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.api.messaging.APSMessage

@CompileStatic
@TypeChecked
class APSMessageProvider<M> implements APSMessage<M> {

    Message vertxMsg
    M message


    /**
     * @return the message content.
     */
    @Override
    M content() {
        return (M) this.message
    }

    /**
     * Replies to message.
     *
     * @param reply The message to reply with.
     */
    @Override
    void reply( @NotNull M reply ) {
        this.vertxMsg.reply( TypeConv.apsToVertx( reply ) )
    }
}
