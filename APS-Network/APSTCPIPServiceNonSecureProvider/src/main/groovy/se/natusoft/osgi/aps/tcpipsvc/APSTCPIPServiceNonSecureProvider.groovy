/* 
 * 
 * PROJECT
 *     Name
 *         APS TCPIP Service NonSecure Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides a nonsecure implementation of APSTCPIPService.
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
import se.natusoft.osgi.aps.api.APSServiceProperties
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.TCPRequest
import se.natusoft.osgi.aps.api.net.tcpip.TCPListener
import se.natusoft.osgi.aps.api.net.tcpip.UDPListener
import se.natusoft.osgi.aps.tcpipsvc.ConnectionProvider.Direction
import se.natusoft.osgi.aps.tcpipsvc.ConnectionProvider.Type
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 * Provides an implementation of APSTCPIPService for nonsecure connections.
 */
@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name=APSServiceProperties.Security.Key, value = APSServiceProperties.Security.NonSecure)
        ]
)
@CompileStatic
@TypeChecked
class APSTCPIPServiceNonSecureProvider implements APSTCPIPService {
    //
    // Private Members
    //

    @Managed(name = "APSTCPIPServiceLogger", loggingFor = "aps-tcpip-service-nonsecure-provider")
    private APSLogger logger

    @Managed
    private APSConfigAdminService configAdminService

    @Managed
    private ConfigResolver configResolver = null;

    //
    // Methods
    //

    /**
     * Delay creating the config resolver until first client call. This way we don't have to thread
     * the service just so that we can access config without deadlocking startup.
     */
    private ConfigResolver getConfigResolver() {
        if (this.configResolver == null) {
            this.configResolver = new ConfigResolver(logger: this.logger)
        }
        return this.configResolver
    }

    /**
     * Sends UDP data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param data The data to send.
     *
     * @throws IOException The one and only!
     * @throws IllegalArgumentException on bad name.
     */
    @Override
    public void sendUDP(String name, byte[] data) throws IOException {
        // Do note that MulticastSender extends UDPSender!
        UDPSender sender = (UDPSender)this.configResolver.resolve(name, Direction.Write, Type.UDP)
        sender.send(data)
    }

    /**
     * Reads UDP data.
     *
     * @param name The name of a configuration specifying address and port or mulicast and port.
     * @param data The buffer to receive into.
     *
     * @return the data buffer.
     *
     * @throws IOException
     * @throws IllegalArgumentException on bad name.
     */
    @Override
    public DatagramPacket readUDP(String name, byte[] data) throws IOException {
        // Do note that MulticastReceiver extends UDPReceiver!
        UDPReceiver receiver =(UDPReceiver)this.configResolver.resolve(name, Direction.Read, Type.UDP)
        return receiver.read(data)
    }

    /**
     * Adds a listener for received udp data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param listener The listener to call back with messages.
     *
     * @throws IllegalArgumentException on bad name.
     */
    @Override
    public void addUDPListener(String name, UDPListener listener) {
        // Do note that MulticastReceiver extends UDPReceiver!
        UDPReceiver receiver =(UDPReceiver)this.configResolver.resolve(name, Direction.Read, Type.UDP)
        receiver.addListener(listener)
    }

    /**
     * Removes a listener for received udp data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param listener The listener to remove.
     *
     * @throws IllegalArgumentException on bad name.
     */
    @Override
    public void removeUDPListener(String name, UDPListener listener) {
        // Do note that MulticastReceiver extends UDPReceiver!
        UDPReceiver receiver =(UDPReceiver)this.configResolver.resolve(name, Direction.Read, Type.UDP)
        receiver.removeListener(listener)
    }

    /**
     * Sends a TCP request on a named TCP config.
     *
     * @param name The named config to send to.
     *
     * @return An OutputStream to write request to. **Do close this!**
     *
     * @throws IllegalArgumentException on bad name.
     */
    @Override
    public void sendTCPRequest(String name, TCPRequest request)  throws IOException {
        TCPSender sender = (TCPSender)this.configResolver.resolve(name, Direction.Write, Type.TCP)
        sender.send(request)
    }

    /**
     * Sets a listener for incoming TCP requests. There can only be one per name.
     *
     * @param name The named config to add listener for.
     * @param listener The listener to add.
     *
     * @throws IllegalArgumentException on bad name.
     */
    @Override
    public void setTCPRequestListener(String name, TCPListener listener) {
        TCPReceiver receiver = (TCPReceiver)this.configResolver.resolve(name, Direction.Read, Type.TCP)
        receiver.setListener(listener)
    }

    /**
     * Removes a listener for incoming TCP requests.
     *
     * @param name Thge named config to remove a listener for.
     * @param listener The listener to remove.
     *
     * @throws IllegalArgumentException on bad name.
     */
    @Override
    public void removeTCPRequestListener(String name, TCPListener listener) {
        TCPReceiver receiver = (TCPReceiver)this.configResolver.resolve(name, Direction.Read, Type.TCP)
        receiver.removeListener(listener)
    }

    /**
     * Returns true if there is a config for the specified name.
     *
     * @param name The name of the config to check.
     *
     * @throws IllegalArgumentException on bad name.
     */
    @Override
    public boolean hasConfig(String name) {
        return false
    }

    /**
     * Creates a config entry for a specific config name. The intention with this is to be able to
     * set a default config if none has been configured.
     *
     * Also note that this independent of TCP or UDP config. The multicast value is only relevant for UDP.
     *
     * This method is optional and if not implemented the supportsSetConfig() should return false.
     *
     * @param name The name of the config to set.
     * @param address The ip address to set.
     * @param port The port to set.
     * @param multicast Set true if this config is for multicast in which case the address is ignored and only the port is used.
     */
    @Override
    public void setConfig(String name, String address, int port, boolean multicast) {

    }

    /**
     * Returns true if setConfig() is supported.
     */
    @Override
    public boolean supportsSetConfig() {
        return true
    }
}
