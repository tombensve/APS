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
package se.natusoft.apsgroups.internal.net;

import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Sends and receives data over multicast.
 */
public class MulticastTransport implements Transport {
    //
    // Private Members
    //

    /** The multicast socket to listen to. */
    private MulticastSocket msocket = null;

    /** The multicast group to join. */
    private InetAddress group = null;

    /** The port we communicate on. */
    private int port = 0;

    /** The logger to log to. */
    private APSGroupsLogger logger;

    /** Our config. */
    private APSGroupsConfig config = null;

    //
    // Constructors
    //

    /**
     * Creates a new MulticastTransport instance.
     *
     * @param logger The logger to log to.
     * @param config The config to use.
     */
    public MulticastTransport(APSGroupsLogger logger, APSGroupsConfig config) {
        this.logger = logger;
        this.config = config;
    }

    //
    // Methods
    //

    /**
     * Sets up the multicast socket.
     *
     * @throws java.net.UnknownHostException
     * @throws java.net.SocketException
     * @throws java.io.IOException
     */
    public void open() throws IOException {
        String multicastAddress = this.config.getMulticastAddress();
        this.port = this.config.getMulticastPort();
        this.group = InetAddress.getByName(multicastAddress);
        this.msocket = new MulticastSocket(this.port);
        this.msocket.setLoopbackMode(false);
        this.msocket.setSoTimeout(5000);
        this.msocket.setReuseAddress(true);
        this.msocket.setBroadcast(false);
        this.msocket.setSoTimeout(1000);
        this.msocket.joinGroup(this.group);
        this.logger.info("Listening for multicast messages on " + multicastAddress + ", targetPort " + this.port);
    }

    /**
     * Closes this transport.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        this.msocket.leaveGroup(this.group);
        this.msocket.close();
    }

    /**
     * Sends a packet with error handling.
     *
     * @param packet
     *
     * @throws IOException
     */
    private void sendPacket(DatagramPacket packet) throws IOException {
        try {
            this.msocket.send(packet);
        }
        catch (SocketException se) {
            Exception fail = null;
            try {
                this.msocket.send(packet);
            }
            catch (UnknownHostException uhe) {
                fail = uhe;
            }
            catch (IOException ioe) {
                fail = ioe;
            }

            if (fail != null) {
                this.logger.error("Multicast socket failed, and failed to recreate socket! MESSAGES CANNOT BE SENT! Reason: " + fail.getMessage());
            }
        }
    }

    /**
     * Sends the byte of data to the destination(s).
     *
     * @param data The data to send.
     *
     * @throws IOException on failure to send.
     */
    public synchronized void send(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, this.group, this.port);
        sendPacket(packet);
    }

    /**
     * Receives data.
     *
     * @throws IOException
     */
    public synchronized Packet receive() throws IOException {
        byte[] buffer = new byte[1500];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        this.msocket.receive(packet);
        byte[] packetbuf = packet.getData();
        return new MCTPPacket(Arrays.copyOf(packetbuf, packet.getLength()), packet.getAddress());
    }

    //
    // Inner Classes
    //

    /**
     * Provides an implementation of Transport.Packet.
     */
    private static class MCTPPacket implements Packet {
        //
        // Private Members
        //

        /** The bytes of this packet. */
        private byte[] bytes = null;

        /** The address the packet came from. */
        private InetAddress address = null;

        //
        // Constructors
        //

        /**
         * Creates a new MCTPPacket.
         *
         * @param bytes The packet bytes.
         * @param address The packet sender address.
         */
        public MCTPPacket(byte[] bytes, InetAddress address) {
            this.bytes = bytes;
            this.address = address;
        }

        //
        // Methods
        //

        /**
         * @return The packet bytes. This will return only the received amount of bytes.
         */
        @Override
        public byte[] getBytes() {
            return this.bytes;
        }

        /**
         * @return The address the packet came from.
         */
        @Override
        public InetAddress getAddress() {
            return this.address;
        }
    }
}
