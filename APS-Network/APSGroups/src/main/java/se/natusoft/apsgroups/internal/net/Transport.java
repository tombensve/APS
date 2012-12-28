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
     * Sets up the multicast socket.
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
