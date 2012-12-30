/* 
 * 
 * PROJECT
 *     Name
 *         APS Discovery Service Provider
 *     
 *     Code Version
 *         0.9.0
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

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;
import se.natusoft.osgi.aps.api.net.groups.service.Message;
import se.natusoft.osgi.aps.discovery.service.provider.ServiceDescriptionProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Provides reading and writing of service descriptions.
 */
public class DiscoveryProtocol {
    // Please note that we keep the protocol to primitives and Strings to keep it more version safe.

    private static final int PROTOCOL_VERSION = 1;

    /**
     * Writes a discovery message for sending.
     *
     * @param message The message to write to.
     * @param discoveryAction The information to write.
     *
     * @throws IOException This can theoretically only happen if we run out of memory since we are writing to a
     *                     byte array in this case.
     */
    public static void writeDiscoveryAction(Message message, DiscoveryAction discoveryAction) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(message.getOutputStream());
        dataStream.writeInt(PROTOCOL_VERSION);
        dataStream.writeInt(discoveryAction.getAction());
        dataStream.writeUTF(discoveryAction.serviceDescription.getDescription());
        dataStream.writeUTF(discoveryAction.serviceDescription.getServiceId());
        dataStream.writeUTF(discoveryAction.serviceDescription.getVersion());
        dataStream.writeUTF(discoveryAction.serviceDescription.getServiceHost());
        dataStream.writeInt(discoveryAction.serviceDescription.getServicePort());
        dataStream.writeUTF(discoveryAction.serviceDescription.getServiceURL());
        dataStream.close();
    }

    /**
     * Reads a DiscoveryAction from a received message.
     *
     * @param message The message to read from.
     *
     * @return The read instance of DiscoveryAction.
     */
    public static DiscoveryAction readDiscoveryAction(Message message) {
        ServiceDescriptionProvider serviceDescription = new ServiceDescriptionProvider();
        int action = -1;

        DataInputStream dataStream = new DataInputStream(message.getInputStream());
        try {
            int protocolVersion = dataStream.readInt();
            if (protocolVersion == PROTOCOL_VERSION) {
                action = dataStream.readInt();
                serviceDescription.setDescription(dataStream.readUTF());
                serviceDescription.setServiceId(dataStream.readUTF());
                serviceDescription.setVersion(dataStream.readUTF());
                serviceDescription.setServiceHost(dataStream.readUTF());
                serviceDescription.setServicePort(dataStream.readInt());
                serviceDescription.setServiceURL(dataStream.readUTF());
            }
            else {
                throw new IOException("Received unknown protocol version: " + protocolVersion);
            }
        }
        catch (IOException ioe) {
            // This cannot happen here since we are reading from a byte array in memory!
        }
        finally {
            try {dataStream.close();} catch (IOException ioe) {/* This cannot fail either for the same reason. */}
        }

        return new DiscoveryAction(action, serviceDescription);
    }

    /**
     * Creates a DiscoveryAction instance for a publishing action.
     *
     * @param serviceDescription The service description to publish.
     */
    public static DiscoveryAction publishAction(ServiceDescription serviceDescription) {
        return new DiscoveryAction(DiscoveryAction.PUBLISH, serviceDescription);
    }

    /**
     * Creates a DiscoveryAction instance for an unpublishing action.
     *
     * @param serviceDescription The service description to unpublish.
     */
    public static DiscoveryAction unpublishAction(ServiceDescription serviceDescription) {
        return new DiscoveryAction(DiscoveryAction.UNPUBLISH, serviceDescription);
    }

    /**
     * Represents an acton to perform and the data to perform it on.
     */
    public static class DiscoveryAction {
        public static final int PUBLISH = 1;
        public static final int UNPUBLISH = 2;

        private int action = -1;
        private ServiceDescription serviceDescription;

        /**
         * Creates a new DiscoveryAction instance.
         *
         * @param action The action this instance represents.
         * @param serviceDescription The service description the action is for.
         */
        private DiscoveryAction(int action, ServiceDescription serviceDescription) {
            this.action = action;
            this.serviceDescription = serviceDescription;
        }

        /**
         * @return The action to perform.
         */
        public int getAction() {
            return this.action;
        }

        /**
         * @return The service description to perform the action on.
         */
        public ServiceDescription getServiceDescription() {
            return serviceDescription;
        }
    }
}
