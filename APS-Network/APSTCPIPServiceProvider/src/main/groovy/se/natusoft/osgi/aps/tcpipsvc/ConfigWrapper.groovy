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
import se.natusoft.osgi.aps.api.net.tcpip.NetworkConfig
import se.natusoft.osgi.aps.exceptions.APSConfigException
import se.natusoft.osgi.aps.tcpipsvc.config.NamedConfig
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig

/**
 * Wraps a config entry, providing fresh config on each access.
 */
@CompileStatic
@TypeChecked
class ConfigWrapper {
    //
    // Properties
    //

    /** The name of the config for this instance. */
    String name

    /** A cached config instance. */
    NetworkConfig config

    //
    // Private Members
    //


    //
    // Methods
    //

    /**
     * Gets and validates the config for the named entry.
     */
    private NetworkConfig getConfig() {

        if (this.config == null) {
            // Do note that each APSConfigValue have a pointer to the real configuration value in memory
            // and any update of the value will be immediately reflected since it is not holding a copy!
            NamedConfig nconfig = (NamedConfig) TCPIPConfig.managed.get().namedConfigs.find { NamedConfig nc ->
                nc.name.string == this.name
            }
            if (nconfig != null) {
                this.config = new NamedConfigWrapper(nconfig)
            }
            else {
                throw new APSConfigException("APSTCPIPService: There is no valid config named '" + name + "'!")
            }
        }
        return this.config
    }

    /**
     * The hostname of the config.
     *
     * @throws APSConfigException on bad config.
     */
    String getHost() {
        return getConfig().address
    }

    /**
     * The multicast address of the config.
     *
     * @throws APSConfigException on bad config.
     */
    String getMulticastAddress() {
        String mcast = getConfig().address
        if (mcast.trim().isEmpty()) {
            mcast = "all-systems.mcast.net"
        }
        return mcast
    }

    /**
     * The port of the config.
     *
     * @throws APSConfigException on bad config.
     */
    int getPort() {
        return getConfig().port
    }

    /**
     * The type of the config.
     *
     * @throws APSConfigException on bad config.
     */
    String getType() {
        return getConfig().type
    }

    /**
     * Returns true if this service should be secure when possible.
     */
    boolean isSecure() {
        return getConfig().secure
    }

    /**
     * Returns the size of the byte buffer to create.
     */
    static int getByteBufferSize() {
        return TCPIPConfig.managed.get().byteBufferSize.int
    }
}

/**
 * Provides the NetworkConfig API by wrapping a NamedConfig entry.
 */
@CompileStatic
@TypeChecked
class NamedConfigWrapper implements NetworkConfig {
    private NamedConfig config

    public NamedConfigWrapper(NamedConfig config) {
        this.config = config
    }

    /**
     * Returns the name of the configuration.
     */
    @Override
    String getName() {
        return this.config.name
    }

    /**
     * Returns the type of this configuration.
     */
    @Override
    NetworkConfig.Type getType() {
        return NetworkConfig.Type.valueOf(this.config.type.string)
    }

    /**
     * Returns the IP address to listen or send to.
     */
    @Override
    String getAddress() {
        return this.config.address.string
    }

    /**
     * Returns the port to listen or send to.
     */
    @Override
    int getPort() {
        return this.config.port.int
    }

    /**
     * If true and security service is available it will be used.
     */
    @Override
    boolean isSecure() {
        return this.config.secure.boolean
    }
}
