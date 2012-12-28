/* 
 * 
 * PROJECT
 *     Name
 *         APS APSNetworkGroups
 *     
 *     Code Version
 *         0.9.0
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
