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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-28: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.groups.service;


import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * This is the API for _APSGroupsService_ members received when they join a group.
 * It is used to send and receive data messages to/from the group.
 */
@Deprecated
public interface GroupMember {
    /**
     * Adds a listener for incoming messages.
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
     * Sends a previously created messaging to all current members of the group. If this returns without an exception
     * then all members have received the messaging.
     *
     * @param message The messaging to send.
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
     * Returns the user properties for the members.
     */
    List<Properties> getMembersUserProperties();

    /**
     * @return The current time as net time.
     */
    NetTime getNow();

    /**
     * Creates from milliseconds in net time.
     *
     * @param netTimeMillis The net time milliseconds to create a NetTime for.
     */
    NetTime createFromNetTime(long netTimeMillis);

    /**
     * Creates from a Date in net time.
     *
     * @param netTimeDate The Date in net time to create a NetTime for.
     */
    NetTime createFromNetTime(Date netTimeDate);

    /**
     * Creates from milliseconds in local time.
     *
     * @param localTimeMillis The local time milliseconds to create a NetTime for.
     */
    NetTime createFromLocalTime(long localTimeMillis);

    /**
     * Creates from a Date in local time.
     *
     * @param localTimeDate The Date in local time to create a NetTime for.
     */
    NetTime createFromLocalTime(Date localTimeDate);
}
