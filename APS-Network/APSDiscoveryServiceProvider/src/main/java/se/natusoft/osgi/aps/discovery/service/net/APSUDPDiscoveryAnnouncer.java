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
package se.natusoft.osgi.aps.discovery.service.net;

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;
import se.natusoft.osgi.aps.discovery.service.event.DiscoveryEventListener;
import se.natusoft.osgi.aps.discovery.service.event.ServiceDescriptionRemoteEvent;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * This manages multicast announcements.
 */
public class APSUDPDiscoveryAnnouncer implements DiscoveryEventListener {
    //
    // Private Members
    //

    /** The address to communicate on. */
    private InetAddress address = null;

    /** The targetPort to communicate on. */
    private int port;

    /** The logger to log to. */
    private APSLogger logger;

    /** Our datagram socket. */
    private DatagramSocket dsocket = null;


    //
    // Constructors
    //

    /**
     * Creates a new APSMulticastDiscoveryAnnouncer instance.
     *
     * @param udpAddress The address to comminicate on.
     * @param port The targetPort to multicast on.
     * @param logger The logger to log on.
     */
    public APSUDPDiscoveryAnnouncer(String udpAddress, int port, APSLogger logger) throws IOException {
        this.address = InetAddress.getByName(udpAddress);
        this.port = port;
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Starts announcer.
     *
     * @throws java.io.IOException
     */
    public void start() throws IOException {
        if (this.dsocket == null) {
            this.dsocket = new DatagramSocket();
        }
        this.logger.info("Started APSDiscoveryService:APSUDPDiscoveryAnnouncer(" + hashCode() + ")");
    }

    /**
     * Stops announcer.
     */
    public void stop() {
        if (this.dsocket != null) {
            this.dsocket.close();
            this.dsocket = null;
        }
        this.logger.info("Stopped APSDiscoveryService:APSUDPDiscoveryAnnouncer(" + hashCode() + ")");
    }

    /**
     * Sends a packet with error handling.
     *
     * @param packet
     * @throws IOException
     */
    private void sendPacket(DatagramPacket packet) throws IOException {
        try {
            this.dsocket.send(packet);
        }
        catch (SocketException se) {
            Exception fail = null;
            try {
                this.dsocket = new DatagramSocket();
                this.dsocket.send(packet);
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

    //
    // DiscoveryEventListener Implementation Methods
    //

    /**
     * Announces that a new service is available.
     *
     * @param serviceDescription The description of the new service.
     */
    @Override
    public synchronized void serviceAvailable(ServiceDescription serviceDescription) {
        if (this.dsocket != null) {
            ServiceDescriptionRemoteEvent sdRemoteEvent = new ServiceDescriptionRemoteEvent(serviceDescription);
            sdRemoteEvent.setAvailable(true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                sdRemoteEvent.write(baos);
                byte[] bytes = baos.toByteArray();
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, this.address, this.port);
                sendPacket(packet);
            }
            catch (IOException ioe) {
                this.logger.error("Failed to announce '" + serviceDescription + "'!", ioe);
            }
        }
    }

    /**
     * Announces that an old service is leaving.
     *
     * @param serviceDescription The description of the leaving service.
     */
    @Override
    public synchronized void serviceLeaving(ServiceDescription serviceDescription) {
        if (this.dsocket != null) {
            ServiceDescriptionRemoteEvent sdRemoteEvent = new ServiceDescriptionRemoteEvent(serviceDescription);
            sdRemoteEvent.setAvailable(false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                sdRemoteEvent.write(baos);
                byte[] bytes = baos.toByteArray();
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, this.address, this.port);
                sendPacket(packet);
            }
            catch (IOException ioe) {
                this.logger.error("Failed to announce '" + serviceDescription + "'!", ioe);
            }
        }
    }
}
