/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         Provides network groups where named groups can be joined as members and then send and
 *         receive data messages to the group. This is based on multicast and provides a verified
 *         multicast delivery with acknowledgements of receive to the sender and resends if needed.
 *         The sender will get an exception if not all members receive all data. Member actuality
 *         is handled by members announcing themselves relatively often and will be removed when
 *         an announcement does not come in expected time. So if a member dies unexpectedly
 *         (network goes down, etc) its membership will resolve rather quickly. Members also
 *         tries to inform the group when they are doing a controlled exit. Most network aspects
 *         are configurable. Please note that this does not support streaming! That would require
 *         a far more complex protocol. It waits in all packets of a message before delivering
 *         the message.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.osgi.aps.groups.service;

import se.natusoft.apsgroups.internal.protocol.message.Message;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This wraps the internal Message implementing the service API, which the internal Message cannot do.
 */
public class MessageProvider implements se.natusoft.osgi.aps.api.net.groups.service.Message {
    //
    // Private Members
    //

    /** The real message we are wrapping. */
    private Message realMessage = null;

    //
    // Constructors
    //

    /**
     * Creates a new Message.
     *
     * @param message The real message to wrap.
     */
    public MessageProvider(Message message) {
        this.realMessage = message;
    }

    //
    // Methods
    //

    /**
     * @return The wrapped internal Message.
     */
    public Message getRealMessage() {
        return this.realMessage;
    }

    /**
     * Returns an OutputStream to write message on. Multiple calls to this will return the same OutputStream!
     */
    @Override
    public OutputStream getOutputStream() {
        return this.realMessage.getOutputStream();
    }

    /**
     * Returns an InputStream for reading the message. Multiple calls to this will return new InputStream:s starting
     * from the beginning!
     */
    @Override
    public InputStream getInputStream() {
        return this.realMessage.getInputStream();
    }

    /**
     * Returns the id of this message.
     */
    @Override
    public UUID getId() {
        return this.realMessage.getId();
    }

    /**
     * @return id of member as a string.
     */
    @Override
    public String getMemberId() {
        return this.realMessage.getMemberId();
    }

    /**
     * @return The name of the group this message belongs to.
     */
    @Override
    public String getGroupName() {
        return this.realMessage.getGroupName();
    }

}
