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
package se.natusoft.apsgroups;

import se.natusoft.apsgroups.internal.protocol.NetTime;
import se.natusoft.apsgroups.internal.protocol.message.Message;
import se.natusoft.apsgroups.internal.protocol.message.MessageListener;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * The API for an APSGroups member.
 */
public interface GroupMember {
    /**
     * Adds a listener for incoming messages.
     *
     * @param listener The listener to add.
     */
    void addMessageListener(MessageListener listener);

    /**
     * Removes a listener for incoming messages.
     *
     * @param listener The listener to remove.
     */
    void removeMessageListener(MessageListener listener);

    /**
     * Creates a new Message to send. Use the sendMessage() method when ready to send it.
     */
    Message createNewMessage();

    /**
     * Sends a previously created message to all current members of the group. If this returns without an exception
     * then all members have received the message.
     *
     * @param message The message to send.
     *
     * @throws java.io.IOException On failure to reach all members.
     */
    void sendMessage(Message message) throws IOException;

    /**
     * @return The ID of the member.
     */
    UUID getMemberId();

    /**
     * Returns information about members.
     */
    List<String> getMemberInfo();

    /**
     * @return The net time.
     */
    NetTime getNetTime();
}
