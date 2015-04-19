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
import se.natusoft.osgi.aps.exceptions.APSConfigException
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

    //
    // Private Members
    //

    /** A cached config instance. */
    private TCPIPConfig.NamedConfig config

    //
    // Methods
    //

    /**
     * Gets and validates the config for the named entry.
     */
    private TCPIPConfig.NamedConfig getConfig() {
        if (this.config == null) {
            // Do note that each APSConfigValue have a pointer to the real configuration value in memory
            // and any update of the value will be immediately reflected since it is not holding a copy!
            this.config =
                    (TCPIPConfig.NamedConfig) TCPIPConfig.managed.get().namedConfigs.find { TCPIPConfig.NamedConfig ce ->
                        ce.name.string == this.name
                    }
            if (this.config == null) {
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
    public String getHost() {
        return getConfig().address.string
    }

    /**
     * The multicast address of the config.
     *
     * @throws APSConfigException on bad config.
     */
    public String getMulticastAddress() {
        String mcast = getConfig().address.string
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
    public int getPort() {
        return getConfig().port.int
    }

    /**
     * The type of the config.
     *
     * @throws APSConfigException on bad config.
     */
    public String getType() {
        return getConfig().type.string
    }

    /**
     * Returns true if this service should be secure when possible.
     */
    public boolean isSecure() {
        return getConfig().secure.boolean
    }

    /**
     * Returns the size of the byte buffer to create.
     */
    public int getByteBufferSize() {
        return TCPIPConfig.managed.get().byteBufferSize.int
    }
}
