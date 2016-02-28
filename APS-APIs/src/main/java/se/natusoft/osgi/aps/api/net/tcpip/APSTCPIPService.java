/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *         2016-02-27: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.tcpip;

import se.natusoft.osgi.aps.api.APSServiceProperties;

import java.io.IOException;
import java.net.URI;

/**
 * This service provides the following functions:
 *
 * * TCP, UDP & Multicast network traffic.
 *
 * * Makes use of APSTCPSecurityService and APSUDPSecurityService if security is selected in configuration
 *   and above mentioned services are available.
 *
 * * Decoupling from java.net classes allowing for test implementations where network traffic can be
 *   simulated in unit-tests without causing problems when run concurrently in a CI server.
 *
 * * Easy to use.
 *
 * ## URIs
 *
 * Following are examples of connection point URIs:
 *
 *     tcp://localhost:8888
 *     tcp://192.168.1.60:9876
 *     tcp://some.server:10123
 *     tcp://some.server:11111#secure
 *     udp://bad.imagination.net:7519
 *     udp://bad.imagination.net:8620#secure
 *     multicast://all-systems.mcast.net:7654
 *     multicast://all-systems.mcast.net:7654#secure
 *
 * When the #secure URI fragment is used the APSTCPSecurityService or the APSUDPSecurityService will be used.
 * If those services are not available when the #secure fragment is specified then the call will fail.
 */
public interface APSTCPIPService extends APSServiceProperties {

    /** This can be used for remove DataBlockListener. */
    DatagramPacketListener ALL_LISTENERS = null;

    /**
     * Sends a block of data. UDP and Multicast protocols are allowed here.
     *
     * @param connectionPoint Where to send it.
     * @param content The data to send.
     *
     * @throws IOException on any communication problem, and when security is requested and no APSUDPSecurityService is available.
     * @throws IllegalArgumentException on bad URI.
     */
    void sendDataPacket(URI connectionPoint, byte[] content) throws IOException;

    /**
     * Adds a listener on incoming data packets. UDP and Multicast protocols are allowed here.
     *
     * @param connectionPoint Receive point to listen to.
     * @param datagramPacketListener The listener to call when data arrives.
     *
     * @throws IllegalArgumentException on bad URI.
     */
    void addDataPacketListener(URI connectionPoint, DatagramPacketListener datagramPacketListener) throws IOException;

    /**
     * Removes a previously added listener. UDP and Multicast protocols are allowed here.
     *
     * @param connectionPoint The receive point to remove listener for.
     * @param datagramPacketListener The listener to remove.
     *
     * @throws IllegalArgumentException on bad URI.
     */
    void removeDataPacketListener(URI connectionPoint, DatagramPacketListener datagramPacketListener) throws IOException;

    /**
     * Sends a TCP request
     *
     * @param connectionPoint Where to send the request.
     * @param request An implementation of StreamedRequest for writing the request and reading the response.
     *
     * @throws IOException on any communication problems, and when security is requested and no APSTCPSecurityService is available.
     * @throws IllegalArgumentException on bad URI.
     */
    void sendStreamedRequest(URI connectionPoint, StreamedRequest request) throws IOException;

    /**
     * Sets a streamed request listener for a specific receive point.
     *
     * @param connectionPoint The receive point to set listener for.
     * @param streamedRequestListener The listener to set.
     *
     * @throws IllegalArgumentException on bad URI or if there already is a listener set for the receive point.
     */
    void setStreamedRequestListener(URI connectionPoint, StreamedRequestListener streamedRequestListener) throws IOException;

    /**
     * Removes the streamed request listener for the specified receive point.
     *
     * @param connectionPoint The receive point to remove listener for.
     * @param streamedRequestListener The listener to remove.
     *
     * @throws IllegalArgumentException on bad URI.
     */
    void removeStreamedRequestListener(URI connectionPoint, StreamedRequestListener streamedRequestListener) throws IOException;

}
