/* 
 * 
 * PROJECT
 *     Name
 *         APS Discovery Service Provider
 *     
 *     Code Version
 *         1.0.0
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
 *         2013-02-03: Created!
 *         
 */
package se.natusoft.osgi.aps.discovery.service.protocol;

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescriptionProvider;
import se.natusoft.osgi.aps.api.net.groups.service.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Publishes a service.
 */
public class Publish  implements Protocol {
    //
    // Private Members
    //

    /** The service description to read or write. */
    private ServiceDescription serviceDescription = null;

    private String fromMember = null;

    //
    // Constructors
    //

    /**
     * Creates a new Publish.
     *
     * @param serviceDescription The description of the service to publish.
     */
    public Publish(ServiceDescription serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    /**
     * Creates a new Publish.
     */
    public Publish(Message message) {
        this.fromMember = message.getMemberId();
    }

    //
    // Methods
    //

    /**
     * @return The read service description.
     */
    public ServiceDescription getServiceDescription() {
        return this.serviceDescription;
    }

    /**
     * Reads from the stream into local data.
     *
     * @param dataStream The stream to read from.
     * @throws java.io.IOException
     */
    @Override
    public void read(DataInputStream dataStream) throws IOException {
        ServiceDescriptionProvider serviceDescriptionProvider = new ServiceDescriptionProvider();
        this.serviceDescription = serviceDescriptionProvider;
        serviceDescriptionProvider.setDescription(dataStream.readUTF());
        serviceDescriptionProvider.setServiceId(dataStream.readUTF());
        serviceDescriptionProvider.setVersion(dataStream.readUTF());
        serviceDescriptionProvider.setServiceHost(dataStream.readUTF());
        serviceDescriptionProvider.setServicePort(dataStream.readInt());
        serviceDescriptionProvider.setServiceURL(dataStream.readUTF());
    }

    /**
     * Writes from local data into specified stream.
     *
     * @param dataStream The stream to write to.
     * @throws java.io.IOException
     */
    @Override
    public void write(DataOutputStream dataStream) throws IOException {
        dataStream.writeInt(DiscoveryProtocol.PUBLISH);
        dataStream.writeUTF(serviceDescription.getDescription());
        dataStream.writeUTF(serviceDescription.getServiceId());
        dataStream.writeUTF(serviceDescription.getVersion());
        dataStream.writeUTF(serviceDescription.getServiceHost());
        dataStream.writeInt(serviceDescription.getServicePort());
        dataStream.writeUTF(serviceDescription.getServiceURL());
    }

    public String toString() {
        return "Publish: (From:" + this.fromMember + ") " + this.serviceDescription.getServiceId() + ":" + this.serviceDescription.getVersion();
    }
}
