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
 *         It supports both multicast and UDP connections.
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
 *         2011-10-16: Created!
 *         
 */
package se.natusoft.osgi.aps.discovery.service.event;

import se.natusoft.osgi.aps.api.core.platform.model.PlatformDescription;
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;

import java.io.*;

/**
 * An event that is sent over the network.
 */
public class ServiceDescriptionRemoteEvent {
    //
    // Constants
    //

    /** The version of the discovery protocol. */
    private static final int PROTOCOL_VERSION = 1;

    //
    // Private Members
    //

    /** True if the event announces availability, false if it announces leaving. */
    private boolean available;

    /** The service description. */
    private final ServiceDescription serviceDescription;

    //
    // Constructors
    //

    /**
     * Creates a new ServiceDescriptionRemoteEvent.
     *
     * @param serviceDescription The service description to announce.
     */
    public ServiceDescriptionRemoteEvent(final ServiceDescription serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    /**
     * Creates a new ServiceDescriptionRemoteEvent.
     */
    public ServiceDescriptionRemoteEvent() {
        this.serviceDescription = new ServiceDescription();
    }

    //
    // Methods
    //

    /**
     * Returns the service description of this event.
     */
    public ServiceDescription getServiceDescription() {
        return this.serviceDescription;
    }

    /**
     * Returns true if this event announces availability, false if it announces leaving.
     */
    public boolean isAvailable() {
        return this.available;
    }

    /**
     * Set to true if this event announces availability, false if it announces leaving.
     *
     * @param available The availability to set.
     */
    public void setAvailable(final boolean available) {
        this.available = available;
    }

    /**
     * Writes this object to the specified OutputStream.
     *
     * @param outStream The stream to write to.
     *
     * @throws java.io.IOException On any failure to write.
     */
    public void write(final OutputStream outStream) throws IOException {
        final DataOutputStream out = new DataOutputStream(outStream);
        try {
            out.writeUTF("APSDSC");
            out.writeInt(PROTOCOL_VERSION);
            out.writeBoolean(this.available);
            out.writeUTF(this.serviceDescription.getPlatformDescription().getIdentifier());
            out.writeUTF(this.serviceDescription.getPlatformDescription().getType());
            out.writeUTF(this.serviceDescription.getPlatformDescription().getDescription());
            out.writeUTF(this.serviceDescription.getDescription());
            out.writeUTF(this.serviceDescription.getServiceId());
            out.writeUTF(this.serviceDescription.getVersion());
            out.writeUTF(this.serviceDescription.getServiceHost());
            out.writeInt(this.serviceDescription.getServicePort());
            out.writeUTF(this.serviceDescription.getServiceURL() == null ? "" : this.serviceDescription.getServiceURL());
            out.flush();
        }
        finally {
            out.close();
        }
    }

    /**
     * Reads this object from the specified InputStream.
     *
     * @param inStream The stream to read from.
     *
     * @throws IOException On any failure to read.
     */
    public boolean read(final InputStream inStream) throws IOException {
        final DataInputStream in = new DataInputStream(inStream);
        boolean result = false;
        try {
            final String id = in.readUTF();
            if ("APSDSC".equals(id)) {
                final int protocolVersion = in.readInt();
                if (protocolVersion == PROTOCOL_VERSION) {
                    this.available = in.readBoolean();
                    PlatformDescription pd = new PlatformDescription();
                    pd.setIdentifier(in.readUTF());
                    pd.setType(in.readUTF());
                    pd.setDescription(in.readUTF());
                    this.serviceDescription.internalSetPlatformDescription(pd);
                    this.serviceDescription.setDescription(in.readUTF());
                    this.serviceDescription.setServiceId(in.readUTF());
                    this.serviceDescription.setVersion(in.readUTF());
                    this.serviceDescription.setServiceHost(in.readUTF());
                    this.serviceDescription.setServicePort(in.readInt());
                    this.serviceDescription.setServiceURL(in.readUTF());
                    if (this.serviceDescription.getServiceURL().trim().length() == 0) {
                        this.serviceDescription.setServiceURL(null);
                    }
                }
                result = true;
            }
        }
        finally {
            in.close();
        }

        return result;
    }

}
