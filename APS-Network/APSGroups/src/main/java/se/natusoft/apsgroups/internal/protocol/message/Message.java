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
 *         are configurable.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files and other things.
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
package se.natusoft.apsgroups.internal.protocol.message;

import se.natusoft.apsgroups.internal.protocol.Group;
import se.natusoft.apsgroups.internal.protocol.Member;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * This represents a complete message of data of any length. It is hacked up into smaller parts internally
 * for easier transmission over non streamed protocols.
 */
public class Message {
    //
    // Private Members
    //

    /** The id of this message. */
    private UUID id = null;

    /** The group this message belongs to. */
    private Group group = null;

    /** The member creating the message. */
    private Member member = null;

    private OutputStream outputStream = null;

    /** Holds The data of the message. */
    private Map<Integer, MessagePacket> packets = new HashMap<>();

    /** The packet number of the end packet. This is used to determine if we have received all packets of the message. */
    private int endPacketNumber = -1;

    /** The number of received packets. This is used to determine if we have received all packets of the message. */
    private int receivedPackets = 0;

    //
    // Constructors
    //

    /**
     * Creates a new Message (for sending).
     *
     * @param group The group the message belongs to.
     * @param member The member creating the message.
     */
    public Message(Group group, Member member) {
        this(group, member, UUID.randomUUID());
    }

    /**
     * Creates a new Message (for receiving).
     *
     * @param group The group the message belongs to.
     * @param member The member creating the message.
     * @param id The id of the message.
     */
    public Message(Group group, Member member, UUID id) {
        this.group = group;
        this.member = member;
        this.id = id;
        this.outputStream = new DMOutputStream();
    }

    //
    // Methods
    //

    /**
     * Returns an OutputStream to write message on. Multiple calls to this will return the same OutputStream!
     */
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    /**
     * Returns an InputStream for reading the message. Multiple calls to this will return new InputStream:s starting
     * from the beginning!
     */
    public InputStream getInputStream() {
        return new DMInputStream();
    }

    /**
     * Returns the id of this message.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * @return id of member as a string.
     */
    public String getMemberId() {
        return this.member.getId().toString();
    }

    /**
     * @return The name of the group this message belongs to.
     */
    public String getGroupName() {
        return this.group.getName();
    }

    /**
     * @return The message member.
     */
    public Member getMember() {
        return this.member;
    }

    /**
     * Adds a received packet to the message.
     *
     * @param packet The packet to add.
     */
    public void addReceivedPacket(MessagePacket packet) {
        this.packets.put(packet.getPacketNumber(), packet);
        ++this.receivedPackets;
        if (packet.getType() == PacketType.MESSAGE_END) {
            this.endPacketNumber = packet.getPacketNumber();
        }
    }

    /**
     * Returns a packet by its packet number.
     *
     * @param packetNumber The packet number to get packet for.
     */
    public synchronized MessagePacket getPacketByNumber(Integer packetNumber) {
        return this.packets.get(packetNumber);
    }

    /**
     * Returns all packets.
     * <P/>
     * If this is a received message then the packet list will be incomplete until hasReceivedAllPackets() return true.
     */
    public List<MessagePacket> getAllPackets() {
        List<MessagePacket> allReceivedPackets = new LinkedList<>();
        for (int i = 0; i < this.packets.size(); i++) {
            MessagePacket mp = this.packets.get(i);
            if (mp != null) {
                allReceivedPackets.add(mp);
            }
        }

        return allReceivedPackets;
    }

    /**
     * Returns the group of the message.
     */
    public Group getGroup() {
        return this.group;
    }

    /**
     * @return true if all packets have been acknowledged.
     */
    public synchronized boolean isAllPacketsAcknowledged() {
        boolean acknowledged = true;

        for (Integer key : this.packets.keySet()) {
            MessagePacket packet = this.packets.get(key);

            if (!packet.hasAllAcknowledgements()) {
                acknowledged = false;
                break;
            }
        }

        return acknowledged;
    }

    /**
     * Returns true if this message has the specified packet.
     *
     * @param packet The packet to check for.
     */
    public boolean hasPacket(MessagePacket packet) {
        return this.packets.get(packet.getPacketNumber()) != null;
    }

    /**
     * @return true if all packets of the message have been received.
     */
    public boolean hasReceivedAllPackets() {
        return this.endPacketNumber >= 0 && this.receivedPackets == (this.endPacketNumber + 1);
    }

    //
    // Inner Classes
    //

    /**
     * This OutputStream writes the data into multiple MessagePackets internally.
     */
    private class DMOutputStream extends OutputStream {
        //
        // Private Members
        //

        private MessagePacket packet = null;
        private OutputStream packetStream = null;
        private int packetNumber = 0;
        private int count = 0;

        //
        // Constructors
        //

        DMOutputStream() {
            this.packet = new MessagePacket(Message.this.group, Message.this.member, Message.this.id, this.packetNumber,
                    PacketType.MESSAGE_PART);
            packetStream = this.packet.getOutputStream();
        }

        //
        // Methods
        //

        /**
         * Writes the specified byte to this output stream. The general
         * contract for <code>write</code> is that one byte is written
         * to the output stream. The byte to be written is the eight
         * low-order bits of the argument <code>b</code>. The 24
         * high-order bits of <code>b</code> are ignored.
         * <p/>
         * Subclasses of <code>OutputStream</code> must provide an
         * implementation for this method.
         *
         * @param b the <code>byte</code>.
         * @throws java.io.IOException if an I/O error occurs. In particular,
         *                             an <code>IOException</code> may be thrown if the
         *                             output stream has been closed.
         */
        @Override
        public void write(int b) throws IOException {
            if (this.count > 1000) {
                this.packetStream.close();
                Message.this.packets.put(this.packetNumber, this.packet);
                ++this.packetNumber;
                this.count = 0;
                this.packet = new MessagePacket(Message.this.group, Message.this.member, Message.this.id, this.packetNumber,
                        PacketType.MESSAGE_PART);
                this.packetStream = this.packet.getOutputStream();
            }

            ++this.count;

            this.packetStream.write(b);
        }

        /**
         * Closes the stream.
         *
         * @throws IOException
         */
        @Override
        public void close() throws IOException {
            try {
                super.close();
            }
            finally {
                this.packetStream.close();
                this.packet.setType(PacketType.MESSAGE_END);
                Message.this.packets.put(this.packetNumber, this.packet);
            }
        }
    }

    /**
     * This InputStream reads from one or multiple MessagePackets internally.
     */
    private class DMInputStream extends InputStream {
        //
        // Private Members
        //

        private int packetNumber = 0;
        private InputStream packetStream = null;

        //
        // Constructors
        //

        DMInputStream() {
            MessagePacket messagePacket = Message.this.packets.get(this.packetNumber);
            this.packetStream = messagePacket.getInputStream();
        }

        //
        // Methods
        //

        /**
         * Reads the next byte of data from the input stream. The value byte is
         * returned as an <code>int</code> in the range <code>0</code> to
         * <code>255</code>. If no byte is available because the end of the stream
         * has been reached, the value <code>-1</code> is returned. This method
         * blocks until input data is available, the end of the stream is detected,
         * or an exception is thrown.
         * <p/>
         * <p> A subclass must provide an implementation of this method.
         *
         * @return the next byte of data, or <code>-1</code> if the end of the
         *         stream is reached.
         * @throws java.io.IOException if an I/O error occurs.
         */
        @Override
        public int read() throws IOException {
            int b = this.packetStream.read();
            if (b == -1) {
                ++this.packetNumber;
                MessagePacket messagePacket = Message.this.packets.get(this.packetNumber);
                if (messagePacket != null) {
                    this.packetStream = messagePacket.getInputStream();
                    b = this.packetStream.read();
                }
            }

            return b;
        }
    }
}
