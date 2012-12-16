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

import se.natusoft.osgi.aps.discovery.service.event.DiscoveryEventProvidingBase;
import se.natusoft.osgi.aps.discovery.service.event.ServiceDescriptionRemoteEvent;
import se.natusoft.osgi.aps.exceptions.APSException;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Listens to udp data and fires events when service descriptions are seen.
 */
public class APSUDPDiscoveryListenerThread extends DiscoveryEventProvidingBase implements Runnable {
    //
    // Private Members
    //

    /** The multicast address to listen to. */
    private String udpAddress = null;

    /** The targetPort to listen to. */
    private int port;

    /** The limit of consecutive read failures. */
    private int consecutiveReadFailureLimit;

    /** Thread will exit when this is false. */
    private boolean running = false;

    /** Our thread. */
    private Thread thread = null;

    private APSLogger logger = null;

    /** If this is set we had a failure at startup. */
    private Exception startFailure = null;

    /** This gets incremented on each read failure. */
    private int readFailureCount = 0;

    /** The UDP socket we will listen to. */
    private DatagramSocket dsocket = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSUDPDiscoveryListenerThread.
     *
     * @param udpAddress The multicast address to listen to.
     * @param port The targetPort to listen on.
     * @param consecutiveReadFailureLimit The limit of consecutive read failures.
     * @param logger The logger to log to.
     */
    public APSUDPDiscoveryListenerThread(String udpAddress, int port, int consecutiveReadFailureLimit, APSLogger logger) {
        this.udpAddress = udpAddress;
        this.port = port;
        this.consecutiveReadFailureLimit = consecutiveReadFailureLimit;
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Sets up the socket.
     *
     * @throws UnknownHostException
     * @throws SocketException
     */
    private void setupSocket() throws UnknownHostException, SocketException {
        InetAddress address = InetAddress.getByName(this.udpAddress);
        this.dsocket = new DatagramSocket(this.port, address);
        dsocket.setSoTimeout(5000);
        this.logger.info("Listening for UDP messages on " + address + ", targetPort " + this.port);

    }

    /**
     * Starts the thread.
     */
    public void start() throws APSException {
        this.running = true;
        try {
            setupSocket();
        }
        catch (UnknownHostException uhe) {
            throw new APSException("Failed to get address: " + this.udpAddress, uhe);
        }
        catch (SocketException se) {
            throw new APSException("Failed to create a DatagramSocket at address '" + this.udpAddress + "' and targetPort '" + this.port + "'!", se);
        }

        this.thread = new Thread(this);
        this.thread.setName("APSDiscoveryService UDP discovery listener thread");
        this.thread.start();

        this.logger.info("Started APSDiscoveryService:APSUDPDiscoveryListenerThread!");
    }

    /**
     * Stops the thread.
     */
    public void stop() {
        if (this.running) {
            this.running = false;
            try {
                if (this.thread != null) {this.thread.join();}
            } catch (InterruptedException e) {
                // This is OK! We just want to wait for it to finnish.
            }
            if (this.dsocket != null) {this.dsocket.close();}
            this.logger.info("Stopped APSDiscoveryService:APSUDPDiscoveryListenerThread!");
        }
    }

    /**
     * The main part of the thread.
     */
    @Override
    public void run() {
        byte[] buffer = new byte[1000];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (this.running) {
            try {
                dsocket.receive(packet);

                byte[] packetbuf = packet.getData();
                // We want an array of correct size
                byte[] recbuf = Arrays.copyOf(packetbuf, packet.getLength());

                ByteArrayInputStream bai = new ByteArrayInputStream(recbuf);
                ServiceDescriptionRemoteEvent sdRemoteEvent = new ServiceDescriptionRemoteEvent();
                try {
                    if (sdRemoteEvent.read(bai)) {
                        if (sdRemoteEvent.isAvailable()) {
                            fireAvailableEvent(sdRemoteEvent.getServiceDescription());
                        }
                        else {
                            fireLeavingEvent(sdRemoteEvent.getServiceDescription());
                        }
                    }
                }
                catch (IOException ioe) {
                    logger.error("Received unknown data or an incomplete service description!");

                    ++this.readFailureCount;
                    if (this.readFailureCount == this.consecutiveReadFailureLimit) {
                        this.logger.error("Failed to receive to many times in a row, stopping APSUDPDiscoveryListenerThread!");
                        this.running = false;
                        this.readFailureCount = 0;
                    }
                }

                this.readFailureCount = 0;
            }
            catch (SocketTimeoutException ste) {
                // This is OK, we want to check this.running reasonably often.
            }
            catch (SocketException se) {
                Exception fail = null;
                try {
                    setupSocket();
                    this.logger.warn("The datagram socket failed, but i was able to recreate it!");
                }
                catch (UnknownHostException uhe) {
                    fail = uhe;
                }
                catch (SocketException se2) {
                    fail = se2;
                }
                if (fail != null) {
                    this.logger.error("The udp socket has failed, and also failed to be recreated. Will shutdown the thread! " +
                    "Reason for failure: " + fail.getMessage());
                    this.running = false;
                }
            }
            catch (IOException ioe1) {
                this.logger.error("Failed UDP datagram receive on address '" + this.udpAddress + "', targetPort '" + this.port + "'!", ioe1);
                try {Thread.sleep(1000);} catch (InterruptedException ie) {}
                ++this.readFailureCount;
                if (this.readFailureCount == this.consecutiveReadFailureLimit) {
                    this.logger.error("Failed to receive to many times in a row, stopping APSUDPDiscoveryListenerThread!");
                    this.running = false;
                }
            }
        }
    }
}
