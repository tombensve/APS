/* 
 * 
 * PROJECT
 *     Name
 *         APSGroups
 *     
 *     Code Version
 *         0.9.0
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
