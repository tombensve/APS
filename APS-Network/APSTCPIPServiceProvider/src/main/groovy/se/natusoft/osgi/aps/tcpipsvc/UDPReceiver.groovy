/*
 *
 * PROJECT
 *     Name
 *         APS TCPIP Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides an implementation of APSTCPIPService. This service does not provide any security of its own,
 *         but makes use of APSTCPSecurityService, and APSUDPSecurityService when available and configured for
 *         security.
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
 *     tommy ()
 *         Changes:
 *         2015-04-11: Created!
 *
 */
package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.tcpip.DatagramPacketListener
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.tcpipsvc.security.UDPSecurityHandler
import se.natusoft.osgi.aps.util.APSLogger

/**
 * ConnectionProvider for reading UDP data.
 */
@CompileStatic
@TypeChecked
class UDPReceiver implements ConnectionProvider {
    //
    // Constants
    //

    protected static final int RECEIVE_TIMEOUT = 3000 // 3 seconds.

    //
    // Properties
    //

    /** The connection point for this receiver. */
    URI connectionPoint

    /** A logger to log to. */
    APSLogger logger

    /** The security handler to use. */
    UDPSecurityHandler securityHandler

    //
    // Private Members
    //

    /** The UDP socket we listen to. */
    protected DatagramSocket socket = null

    /** The registered listeners. */
    protected List<DatagramPacketListener> listeners = new LinkedList<>()

    /** This is started when a listener is added. */
    protected UDPReceiverThread receiverThread = null

    /** Indicates the active state of this ConnectionProvider. */
    private synchronized boolean active = false

    //
    // Methods
    //

    /**
     * Starts the provider.
     *
     * @throws IOException
     */
    @Override
    void start() throws IOException {
        this.active = true
        if (!this.listeners.empty) connect()
    }

    /**
     * Stops the provider.
     *
     * @throws IOException
     */
    @Override
    void stop() throws IOException {
        this.active = false
        disconnect()
    }

    /**
     * Returns the direction of the connection.
     */
    @Override
    ConnectionProvider.Direction getDirection() {
        ConnectionProvider.Direction.Read
    }

    /**
     * Creates the socket.
     */
    protected void createSocket() {
        this.socket = new DatagramSocket(new InetSocketAddress(this.connectionPoint.host, this.connectionPoint.port))
        this.socket.soTimeout = RECEIVE_TIMEOUT
        this.socket.reuseAddress = true
        this.socket.broadcast = false
    }

    /**
     * Connects the socket.
     *
     * @throws IOException
     */
    protected void connect() throws IOException {
        if (this.active) {
            if (this.socket == null) {
                createSocket()
            }
            if (!this.listeners.empty && this.receiverThread == null) {
                this.receiverThread = new UDPReceiverThread(listeners: this.listeners, logger: this.logger,
                        connectionPoint: this.connectionPoint, receiver: this)
                this.receiverThread.start()
                this.logger.info("UDP Receiver for port ${this.connectionPoint.port} was started!")
            }
        }
    }

    /**
     * Disconnects the socket and stops the receiver thread.
     */
    protected void disconnect() {
        if (this.socket != null) {
            if (this.receiverThread != null) {
                try {
                    this.receiverThread.stopThread()
                    this.receiverThread.join(RECEIVE_TIMEOUT + 1000)
                    this.receiverThread = null
                }
                catch (InterruptedException ie) {
                    this.logger.warn("UDP Receiver thread did not stop in reasonable time!", ie)
                }
            }
            this.socket.disconnect()
            this.socket = null
            this.logger.info("UDP Receiver for port ${this.connectionPoint.port} was stopped!")
        }
    }

    /**
     * Adds a listener.
     *
     * @param listener The listener to add.
     */
    public synchronized void addListener(DatagramPacketListener listener) {
        this.listeners += listener
        if (this.listeners.size() == 1) {
            connect()
        }
    }

    /**
     * Removes a listener.
     *
     * @param listener The listener to remove.
     */
    public synchronized void removeListener(DatagramPacketListener listener) {
        if (listener == null) {
            this.listeners.clear()
        }
        else {
            this.listeners -= listener
        }
        if (this.listeners.size() == 0) {
            disconnect()
        }
    }

    /**
     * Reads from the UDP socket.
     *
     * @param data The byte buffer to read into.
     *
     * @return A DatagramPacket containing the data.
     *
     * @throws IOException
     */
    public DatagramPacket read(byte[] data) throws IOException {
        if (this.active) {
            connect()
            DatagramPacket dp = new DatagramPacket(data, data.length)
            this.socket.receive(dp)
            if (this.connectionPoint.getFragment() == "secure") {
                this.securityHandler.unsecure(this.connectionPoint, dp)
            }
            return dp
        }

        return null
    }

}

//
// Support Classes
//

// NOTE: The following was originally an inner class of UDPReceiver. Groovy language constructs however fail
// at runtime when they are part of an inner class, but works fine when part of a top level class.
//
// This is fixed in version 2.4.3 of groovy, but I've decided to not move these back as inner classes.

/**
 * Listens to traffic on the UDP socket and calls listeners with packets received.
 */
@CompileStatic
@TypeChecked
public class UDPReceiverThread extends Thread {
    //
    // Properties
    //

    List<DatagramPacketListener> listeners

    APSLogger logger

    URI connectionPoint

    UDPReceiver receiver

    //
    // Private Members
    //

    private boolean running = false

    //
    // Methods
    //

    public synchronized void stopThread() {
        this.running = false
    }

    private synchronized boolean isRunning() {
        return this.running
    }

    private synchronized void startRunning() {
        this.running = true
    }

    /**
     * Gets passed logger or creates a local logger if none have been provided.
     */
    public APSLogger getSafeLogger() {
        if (this.logger == null) {
            this.logger = new APSLogger(System.out)
        }
        return this.logger
    }

    public void run() {
        setName("APSTCPIPService: TCP ReceiverThread[" + this.connectionPoint+ "]")

        byte[] data = new byte[TCPIPConfig.managed.get().byteBufferSize.int]
        startRunning()

        safeLogger.info("UDPReceiverThread(" + this.connectionPoint + "): Starting up!")

        while (isRunning()) {
            try {
                DatagramPacket dp = receiver.read(data)

                this.listeners.each { DatagramPacketListener listener ->
                    try {
                        listener.dataBlockReceived(this.connectionPoint, dp)
                    }
                    catch (Exception e) {
                        safeLogger.error("Listener call failed for '" + this.connectionPoint + "'! (" + listener + ")", e)
                    }
                }
            }
            catch (SocketTimeoutException ignore) {
                // This not only will happen, it must happen or we cannot shut down the thread!
            }
        }

        safeLogger.info("UDPReceiverThread(" + this.connectionPoint + "): Shutting down!")
    }
}

