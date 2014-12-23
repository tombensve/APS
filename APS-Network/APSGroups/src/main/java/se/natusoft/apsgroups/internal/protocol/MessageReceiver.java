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
package se.natusoft.apsgroups.internal.protocol;

import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.message.*;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.IOException;
import java.util.*;

/**
 * Handles receiving of complete messages.
 */
public class MessageReceiver implements MessagePacketListener {
    //
    // Private Members
    //

    /** Temporary storage of messages while the are being received. */
    private Map<UUID, Message> messages = new HashMap<>();

    /** The transport to use for sending acknowledgements. */
    private Transport transport = null;

    /** The member this receiver belongs to. */
    private Member member = null;

    /** The listeners on messages received by this receiver. */
    private List<MessageListener> listeners = new LinkedList<>();

    /** The logger to log to. */
    private APSGroupsLogger logger = null;

    //
    // Constructors
    //

    /**
     * Creates a new MessageReceiver.
     *
     * @param transport The transport to use.
     * @param member The member this receiver belongs to.
     * @param logger The logger to log to.
     */
    public MessageReceiver(Transport transport, Member member, APSGroupsLogger logger) {
        this.transport = transport;
        this.member = member;
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * This to avoid having to save the transport outside of this class just to be able to
     * close it again.
     */
    public Transport getTransport() {
        return this.transport;
    }

    /**
     * Adds a listener to this receiver.
     *
     * @param listener The listener to add.
     */
    public synchronized void addMessageListener(MessageListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a listener to this receiver.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removeMessageListener(MessageListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Sends an acknowledgement.
     *
     * @param messagePacket The packet to acknowledge.
     *
     * @throws IOException
     */
    private void sendAcknowledgement(MessagePacket messagePacket) throws IOException{
        MessagePacket ackPacket = new MessagePacket(
            messagePacket.getGroup(),
            this.member,
            messagePacket.getMessageId(),
            messagePacket.getPacketNumber(),
            PacketType.ACKNOWLEDGEMENT
        );
        ackPacket.getOutputStream().close();
        this.transport.send(ackPacket.getPacketBytes());
    }

    /**
     * Notifies all listeners of a fully received message.
     *
     * @param message The message to notify about.
     */
    private synchronized void notifyMessageReceived(Message message) {
        for (MessageListener listener : this.listeners) {
            listener.messageReceived(message);
        }
        this.messages.remove(message.getId());
    }

    /**
     * Notification of receive of a new MessagePacket.
     *
     * @param messagePacket The received MessagePacket.
     */
    @Override
    public void messagePacketReceived(MessagePacket messagePacket) {
        // We will get our own messages so ignore them.
        if (messagePacket.getMember().equals(this.member)) {
            return;
        }

        if (messagePacket.getGroup().getName().equals(this.member.getGroup().getName())) {
            try {
                switch (messagePacket.getType()) {

                    case MESSAGE_PART:
                        {
                            Message message = this.messages.get(messagePacket.getMessageId());
                            if (message == null) { // This is the first time we see this message.
                                message = new Message(messagePacket.getGroup(), messagePacket.getMember(), messagePacket.getMessageId());
                                this.messages.put(message.getId(), message);
                            }
                            // We only add the packet once. There might be re-sends of the packet!
                            if (!message.hasPacket(messagePacket)) {
                                message.addReceivedPacket(messagePacket);
                            }

                            // We do acknowledge a resend since it is probably a failed acknowledgement
                            // that is the reason for the resend.
                            sendAcknowledgement(messagePacket);

                            // Packets might not come in order!
                            if (message.hasReceivedAllPackets()) {
                                notifyMessageReceived(message);
                            }
                        }
                        break;

                    case MESSAGE_END:
                        {
                            Message message = this.messages.get(messagePacket.getMessageId());
                            // If the message is made up of only one packet it will be of type MESSAGE_END.
                            if (message == null) {
                                message = new Message(messagePacket.getGroup(), messagePacket.getMember(), messagePacket.getMessageId());
                                this.messages.put(message.getId(), message);
                            }
                            message.addReceivedPacket(messagePacket);
                            sendAcknowledgement(messagePacket);

                            // Packets might not come in order!
                            if (message.hasReceivedAllPackets()) {
                                notifyMessageReceived(message);
                            }
                        }
                        break;

                    case MEMBER_ANNOUNCEMENT:
                        {
                            if (!this.member.getGroup().hasMember(messagePacket.getMember())) {
                                this.member.getGroup().addMember(messagePacket.getMember());
                            }
                            messagePacket.getMember().updateLastHeardFrom();
                        }
                }
            }
            catch (IOException ioe) {
                try {Thread.sleep(1000);} catch (InterruptedException ie) {}
                try {
                    // This might be entirely pointless!
                    sendAcknowledgement(messagePacket);
                }
                catch (IOException ioe2) {
                    this.logger.error("Failed to send acknowledgement!", ioe);
                }
            }
        }
    }
}
