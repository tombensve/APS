/* 
 * 
 * PROJECT
 *     Name
 *         APS Discovery Service Provider
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         This is a simple discovery service to discover other services on the network.
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
 *         2012-12-30: Created!
 *         
 */
package se.natusoft.osgi.aps.discovery.service.protocol;

import se.natusoft.osgi.aps.api.net.groups.service.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Provides reading and writing of service descriptions.
 */
public class DiscoveryProtocol {
    // Please note that we keep the protocol to primitives and Strings to keep it more version safe.

    private static final int PROTOCOL_VERSION = 1;

    public static final int PUBLISH = 1;
    public static final int UNPUBLISH = 2;

    /**
     * Writes message data.
     *
     * @param message The message to write to.
     * @param protocol The protocol implementation to write.
     *
     * @throws IOException
     */
    public static void write(Message message, Protocol protocol) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(message.getOutputStream());
        dataStream.writeInt(PROTOCOL_VERSION);
        protocol.write(dataStream);
        dataStream.close();
    }

    /**
     * Reads message data.
     *
     * @param message The message to read from.
     *
     * @return A Protocol implementation representing what was read.
     *
     * @throws IOException
     */
    public static Protocol read(Message message) throws IOException {
        Protocol protocol = null;
        DataInputStream dataStream = new DataInputStream(message.getInputStream());
        int protocolVersion = dataStream.readInt();
        if (protocolVersion == PROTOCOL_VERSION) {
            int messageType = dataStream.readInt();
            switch (messageType) {
                case PUBLISH:
                    protocol = new Publish(message);
                    protocol.read(dataStream);
                    break;

                case UNPUBLISH:
                    protocol = new UnPublish(message);
                    protocol.read(dataStream);
                    break;

                default:
                    throw new IOException("Unknown message type: " + messageType);
            }
        }
        else {
            throw new IOException("Received unknown protocol version: " + protocolVersion);
        }

        dataStream.close();

        return protocol;
    }
}
