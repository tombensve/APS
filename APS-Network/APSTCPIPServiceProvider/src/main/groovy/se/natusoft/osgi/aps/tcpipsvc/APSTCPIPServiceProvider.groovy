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
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.NetworkConfig
import se.natusoft.osgi.aps.api.net.tcpip.TCPListener
import se.natusoft.osgi.aps.api.net.tcpip.TCPRequest
import se.natusoft.osgi.aps.api.net.tcpip.UDPListener
import se.natusoft.osgi.aps.tcpipsvc.ConnectionProvider.Direction
import se.natusoft.osgi.aps.tcpipsvc.meta.APSTCPIPServiceMetaData
import se.natusoft.osgi.aps.tcpipsvc.security.TCPSecurityHandler
import se.natusoft.osgi.aps.tcpipsvc.security.UDPSecurityHandler
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

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
    private TCPSecurityHandler tcpSecurityHandler

    @Managed
    private UDPSecurityHandler updSecurityHandler

    @Managed(name = "META-DATA")
    private APSTCPIPServiceMetaData metaData

    /** The real ConfigResolver instance. This is accessed via the getConfigResolver() method, which will delay its creation. */
    private ConfigResolver _configResolver = null

    //
    // Methods
    //

    /**
     * Delay creating the config resolver until first client call. This way we don't have to thread
     * the service just so that we can access config without deadlocking startup.
     */
    private ConfigResolver getConfigResolver() {
        if (this._configResolver == null) {
            this._configResolver = new ConfigResolver(
                    logger: this.logger,
                    tcpSecurityHandler: this.tcpSecurityHandler,
                    udpSecurityHandler: this.updSecurityHandler
            )
        }
        return this._configResolver
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
    void sendUDP(String name, byte[] data) throws IOException {
        this.logger.debug("sendUDP - name:${name}, data-size: ${data.length}")
        // Do note that MulticastSender extends UDPSender!
        UDPSender sender = (UDPSender)this.configResolver.resolve(name, Direction.Write, NetworkConfig.Type.UDP)
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
    DatagramPacket readUDP(String name, byte[] data) throws IOException {
        this.logger.debug("readUDP - name:${name}, data-size: ${data.length}")
        // Do note that MulticastReceiver extends UDPReceiver!
        UDPReceiver receiver =(UDPReceiver)this.configResolver.resolve(name, Direction.Read, NetworkConfig.Type.UDP)
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
    void addUDPListener(String name, UDPListener listener) {
        this.logger.debug("addUDPListener - name:${name}, listener:${listener}")
        // Do note that MulticastReceiver extends UDPReceiver!
        UDPReceiver receiver =(UDPReceiver)this.configResolver.resolve(name, Direction.Read, NetworkConfig.Type.UDP)
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
    void removeUDPListener(String name, UDPListener listener) {
        this.logger.debug("removeUDPListener - name:${name}, listener:${listener}")
        // Do note that MulticastReceiver extends UDPReceiver!
        UDPReceiver receiver =(UDPReceiver)this.configResolver.resolve(name, Direction.Read, NetworkConfig.Type.UDP)
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
    void sendTCPRequest(String name, TCPRequest request)  throws IOException {
        this.logger.debug("sendTCPRequest - name:${name}, request:${request}")
        TCPSender sender = (TCPSender)this.configResolver.resolve(name, Direction.Write, NetworkConfig.Type.TCP)
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
    void setTCPRequestListener(String name, TCPListener listener) {
        this.logger.debug("setTcpRequestListener - name:${name}, listener:${listener}")
        TCPReceiver receiver = (TCPReceiver)this.configResolver.resolve(name, Direction.Read, NetworkConfig.Type.TCP)
        receiver.setListener(listener)
    }

    /**
     * Removes a listener for incoming TCP requests.
     *
     * @param name Thge named config to remove a listener for.
     *
     * @throws IllegalArgumentException on bad name.
     */
    @Override
    void removeTCPRequestListener(String name) {
        this.logger.debug("removeTcpRequestListener - name:${name}")
        TCPReceiver receiver = (TCPReceiver)this.configResolver.resolve(name, Direction.Read, NetworkConfig.Type.TCP)
        receiver.removeListener()
    }

    /**
     * Returns a list of names matching the specified regexp.
     *
     * @param regexp The regexp to get names for.
     *
     * @return A list of the matching names. If none were found the list will be empty.
     */
    @Override
    List<String> getNames(String regexp) {
        return this.configResolver.getNames(regexp)
    }

    /**
     * Adds a network configuration in addition to those configured in standard APS configuration.
     *
     * Do note that the name in the config must be unique!
     *
     * @param networkConfig The network config to register.
     */
    @Override
    void addServiceConfig(NetworkConfig networkConfig) {
        this.configResolver.addServiceConfig(networkConfig)
    }

    /**
     * Removes a previously added network config by its name.
     *
     * @param name The name in the registered config to delete.
     */
    @Override
    void removeServiceConfig(String name) {
        this.configResolver.removeServiceConfig(name)
    }
}
