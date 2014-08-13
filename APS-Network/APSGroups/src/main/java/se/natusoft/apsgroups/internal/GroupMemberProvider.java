/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         1.0.0
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
package se.natusoft.apsgroups.internal;

import se.natusoft.apsgroups.GroupMember;
import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.net.MulticastTransport;
import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.*;
import se.natusoft.apsgroups.internal.protocol.message.Message;
import se.natusoft.apsgroups.internal.protocol.message.MessageListener;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacket;
import se.natusoft.apsgroups.internal.protocol.message.PacketType;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * This provides the API for a member.
 */
public class GroupMemberProvider implements MessageListener, GroupMember {
    //
    // Private Members
    //

    /** A local member instance. */
    private Member member = null;

    /** Our logger. */
    private APSGroupsLogger logger = null;

    /** our config. */
    private APSGroupsConfig config = null;

    /** Client listeners. */
    private List<MessageListener> messageListeners = new LinkedList<>();

    /** The transport to use. */
    private Transport transport = null;

    /** Receives messages for this member. */
    private MessageReceiver messageReceiver = null;

    //
    // Constructors
    //

    /**
     * Creates a new GroupMember.
     *
     * @param member The internal member object.
     * @param logger The logger to log to.
     */
    public GroupMemberProvider(Member member, APSGroupsLogger logger, APSGroupsConfig config) {
        this.member = member;
        this.logger = logger;
        this.config = config;
        this.transport = new MulticastTransport(this.logger, this.config);
    }

    //
    // Methods
    //

    /**
     * Makes the group member ready for business.
     *
     * @throws java.io.IOException
     */
    public void open() throws IOException {
        this.transport.open();
        Transport ackTransport = new MulticastTransport(this.logger, this.config);
        ackTransport.open();
        this.messageReceiver = new MessageReceiver(ackTransport, this.member, this.logger);
        this.messageReceiver.addMessageListener(this);
        DataReceiverThread.get().addMessagePacketListener(this.messageReceiver);
    }

    /**
     * Cleans up for the group member.
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        MessagePacket mp = new MessagePacket(this.member.getGroup(), this.member, UUID.randomUUID(), 0, PacketType.MEMBER_LEAVING);
        this.transport.send(mp.getPacketBytes());
        try {Thread.sleep(500);} catch (InterruptedException ie) {}

        this.transport.close();
        this.messageListeners.clear();
        DataReceiverThread.get().removeMessagePacketListener(this.messageReceiver);
        this.messageReceiver.getTransport().close();
        this.messageReceiver.removeMessageListener(this);
        this.messageReceiver = null;
    }

    /**
     * Adds a listener for incoming messages.
     * @param listener The listener to add.
     */
    @Override
    public synchronized void addMessageListener(MessageListener listener) {
        this.messageListeners.add(listener);
    }

    /**
     * Removes a listener for incoming messages.
     *
     * @param listener The listener to remove.
     */
    @Override
    public synchronized void removeMessageListener(MessageListener listener) {
        this.messageListeners.remove(listener);
    }

    /**
     * Creates a new Message to send. Use the sendMessage() method when ready to send it.
     */
    @Override
    public Message createNewMessage() {
        return this.member.getGroup().createNewMessage(this.member);
    }

    /**
     * Sends a previously created message to all current members of the group. If this returns without an exception
     * then all members have received the message.
     *
     * @param message The message to send.
     *
     * @throws java.io.IOException On failure to reach all members.
     */
    @Override
    public void sendMessage(Message message) throws IOException {
        if (this.member.getGroup().getNumberOfMembers() > 0) {
            MessageSender sender = new MessageSender(message, transport, this.config);
            DataReceiverThread.get().addMessagePacketListener(sender);
            try {
                sender.send();
            }
            finally {
                DataReceiverThread.get().removeMessagePacketListener(sender);
            }
        }
//        else {
//            this.logger.warn("I'm so lonely! I have no one to talk to!");
//        }
    }

    /**
     * @return The ID of the member.
     */
    @Override
    public UUID getMemberId() {
        return this.member.getId();
    }

    public Member getMember() {
        return this.member;
    }

    /**
     * Returns information about members.
     */
    @Override
    public List<String> getMemberInfo() {
        List<String> memberInfo = new LinkedList<>();
        for (Member membr : this.member.getGroup().getListOfMembers()) {
            memberInfo.add(membr.getId().toString()+ ", status:" + (membr.stillKicking(this.config.getMemberAnnounceInterval()) ?
                    "alive" : "dead"));
        }
        return memberInfo;
    }

    /**
     * @return The net time.
     */
    @Override
    public NetTime getNetTime() {
        return this.member.getGroup().getNetTime();
    }

    /**
     * Notification of received message.
     *
     * @param message The received message.
     */
    @Override
    public void messageReceived(Message message) {
        for (MessageListener listener : this.messageListeners) {
            listener.messageReceived(message);
        }
    }
}
