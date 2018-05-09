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
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.DatagramPacketListener
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequest
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequestListener
import se.natusoft.osgi.aps.tcpipsvc.ConnectionProvider.Direction
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider

/**
 * Provides an implementation of APSTCPIPService for nonsecure connections.
 */
@SuppressWarnings("GroovyUnusedDeclaration") // APSActivator instantiates this through reflection, and it will
                                             // only be called via the interface so IDEs will never see a
                                             // reference to this.
@OSGiServiceProvider
@CompileStatic
@TypeChecked
class APSTCPIPServiceProvider implements APSTCPIPService {
    //
    // Private Members
    //

    @Managed(name = "APSTCPIPServiceLogger", loggingFor = "aps-tcpip-service-provider")
    private APSLogger logger

    @Managed
    private ConnectionResolver connectionResolver

    private Map<URI, UDPReceiver> udpReceivers = Collections.synchronizedMap(new HashMap<URI, UDPReceiver>())

    private Map<URI, TCPReceiver> tcpReceivers = Collections.synchronizedMap(new HashMap<URI, TCPReceiver>())

    //
    // Methods
    //

    /**
     * Sends a block of data.
     *
     * @param connectionPoint Where to send it. Allows udp://... and multicast://...
     * @param content The data to send.
     *
     * @throws IOException on any communication problem.
     * @throws IllegalArgumentException on bad URI.
     */
    @Override
    void sendDataPacket(URI connectionPoint, byte[] content) throws IOException {
        // Do note that MulticastSender extends UDPSender!
        UDPSender sender = this.connectionResolver.resolve(connectionPoint, Direction.Write) as UDPSender
        sender.send(content)
    }

    /**
     * Adds a listener on incoming data packets. UDP and Multicast protocols are allowed here.
     *
     * @param connectionPoint Receive point to listen to.
     * @param dataPacketListener The listener to call when data arrives.
     *
     * @throws IllegalArgumentException on bad URI.
     */
    @Override
    void addDataPacketListener(URI connectionPoint, DatagramPacketListener dataPacketListener) throws IOException {
        UDPReceiver receiver = this.udpReceivers.get(connectionPoint)
        if (receiver == null) {
            receiver = this.connectionResolver.resolve(connectionPoint, Direction.Read) as UDPReceiver
            this.udpReceivers.put(connectionPoint, receiver)
        }

        receiver.addListener(dataPacketListener)
    }

    /**
     * Removes a previously added listener. UDP and Multicast protocols are allowed here.
     *
     * @param connectionPoint The receive point to remove listener for.
     * @param dataPacketListener The listener to remove.
     *
     * @throws IllegalArgumentException on bad URI.
     */
    @Override
    void removeDataPacketListener(URI connectionPoint, DatagramPacketListener dataPacketListener) throws IOException {
        UDPReceiver receiver = this.udpReceivers.get(connectionPoint)
        if (receiver != null) {
            receiver.removeListener(dataPacketListener)
        }
    }

    /**
     * Sends a TCP request
     *
     * @param connectionPoint Where to send the request.
     * @param request An implementation of StreamedRequest for writing the request and reading the response.
     *
     * @throws IOException on any communication problems.
     * @throws IllegalArgumentException on bad URI.
     */
    @Override
    void sendStreamedRequest(URI connectionPoint, StreamedRequest request) throws IOException {
        TCPSender sender = this.connectionResolver.resolve(connectionPoint, Direction.Write) as TCPSender
        sender.send(request)
    }

    /**
     * Sets a streamed request listener for a specific receive point.
     *
     * @param connectionPoint The receive point to set listener for.
     * @param streamedRequestListener The listener to set.
     *
     * @throws IllegalArgumentException on bad URI or if there already is a listener set for the receive point.
     */
    @Override
    void setStreamedRequestListener(URI connectionPoint, StreamedRequestListener streamedRequestListener) throws IOException {
        TCPReceiver receiver = this.tcpReceivers.get(connectionPoint)
        if (receiver == null) {
            receiver = this.connectionResolver.resolve(connectionPoint, Direction.Read) as TCPReceiver
            this.tcpReceivers.put(connectionPoint, receiver)
        }
        receiver.setListener(streamedRequestListener)
    }

    /**
     * Removes the streamed request listener for the specified receive point.
     *
     * @param connectionPoint The receive point to remove listener for.
     * @param streamedRequestListener The listener to remove.
     *
     * @throws IllegalArgumentException on bad URI.
     */
    @Override
    void removeStreamedRequestListener(URI connectionPoint, StreamedRequestListener streamedRequestListener) throws IOException {
        TCPReceiver receiver = this.tcpReceivers.get(connectionPoint)
        if (receiver != null) {
            receiver.removeListener()
        }
    }

    @BundleStop
    public void shutdown() {
        this.udpReceivers.keySet().each { URI connectionPoint ->
            UDPReceiver receiver = this.udpReceivers.get(connectionPoint)
            try {
                receiver.stop()
            }
            catch (Exception e) {
                this.logger.error("Failed to shutdown UDPReceiver(${connectionPoint})!", e)
            }
        }

        this.tcpReceivers.keySet().each { URI connectionPoint ->
            TCPReceiver receiver = this.tcpReceivers.get(connectionPoint)
            try {
                receiver.stop()
            }
            catch (Exception e) {
                this.logger.error("Failed to shutdown TCPReceiver(${connectionPoint})!", e)
            }
        }
    }
}
