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
package se.natusoft.apsgroups.internal.net;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Defines network transport API.
 */
public interface Transport {

    /**
     * Sends the byte of data to the destination(s).
     *
     * @param data The data to send.
     *
     * @throws java.io.IOException on failure to send.
     */
    public void send(byte[] data) throws IOException;

    /**
     * Receives data.
     *
     * @throws IOException
     */
    public Packet receive() throws IOException;


    /**
     * Sets up the multicast tcpip.
     *
     * @throws java.net.UnknownHostException
     * @throws java.net.SocketException
     * @throws java.io.IOException
     */
    public void open() throws IOException;

    /**
     * Closes this transport.
     *
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * Represents needed packet data in a nicer form than DatagramPacket.
     */
    interface Packet {

        /**
         * @return The packet bytes. This will return only the received amount of bytes.
         */
        byte[] getBytes();

        /**
         * @return The address the packet came from.
         */
        InetAddress getAddress();
    }
}
