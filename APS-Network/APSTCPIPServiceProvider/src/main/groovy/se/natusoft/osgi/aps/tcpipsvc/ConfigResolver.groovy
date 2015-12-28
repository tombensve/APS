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
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener
import se.natusoft.osgi.aps.exceptions.APSConfigException
import se.natusoft.osgi.aps.exceptions.APSRuntimeException
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.tcpipsvc.security.TCPSecurityHandler
import se.natusoft.osgi.aps.tcpipsvc.security.UDPSecurityHandler
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.util.APSObject

/**
 * Resolves named configuration and creates configured network providers.
 */
@CompileStatic
@TypeChecked
class ConfigResolver extends APSObject implements APSConfigChangedListener {
    //
    // Properties
    //

    APSLogger logger

    /** TCP security handler that is possibly secure if an APSTCPSecurityService is available. */
    TCPSecurityHandler tcpSecurityHandler

    /** UDP security handler tha is possibly secure if an APSUDPSecurityService is available. */
    UDPSecurityHandler udpSecurityHandler

    //
    // Private Members
    //

    /** Cached connection providers. */
    private Map<String/*name*/, ConnectionProvider> connectionProviders = new HashMap<>()

    //
    // Methods
    //

    @Override
    protected void delayedInit() {
        TCPIPConfig.managed.get().addConfigChangedListener(this)
    }

    /**
     * Returns a list of names matching the specified regexp criteria.
     *
     * @param regexp The regexp to get names for.
     */
    public List<String> getNames(String regexp) {
        List<String> result = new LinkedList<>()

        this.connectionProviders.keySet().each { String key ->
            if (key.matches(regexp)) result += key
        }

        return result
    }

    /**
     * Resolves a configuration name returning a ConnectionProvider.
     *
     * @param name The name of the configuration to get a ConnectionProvider for.
     * @param direction The required direction of the provider to return.
     * @param expectedType A config validation. If config entry is not of this type an exception is thrown.
     *
     * @throws APSConfigException on missing or miss-matching config.
     */
    public synchronized ConnectionProvider resolve(String name, ConnectionProvider.Direction direction, ConnectionProvider.Type expectedType) {
        init()

        if (name == null) throw new IllegalArgumentException("name argument is required! It cannot be null!")
        if (direction == null) throw new IllegalArgumentException("direction argument is required! It cannot be null!")
        if (expectedType == null) throw new IllegalArgumentException("expectedType argument is required! It cannot be null!")

        String cacheKey = name + direction.name()
        if (this.connectionProviders.containsKey(cacheKey)) {
            return this.connectionProviders.get(cacheKey)
        }

        ConnectionProvider connectionProvider = null
        ConfigWrapper config = new ConfigWrapper(name: name) // Throws APSConfigException on bad name!

        ConnectionProvider.Type configType = null;
        try {
            configType = ConnectionProvider.Type.valueOf(config.type)
        }
        catch (IllegalArgumentException iae) {
            this.logger.error("Invalid config type: '${config.type}'!", iae)
            throw new APSRuntimeException("Bad protocol type from config: '${config.type}'", iae);
        }

        // Allow UDP as expected type for multicast by converting expected type to multicast.
        if (configType == ConnectionProvider.Type.Multicast && expectedType == ConnectionProvider.Type.UDP) {
            expectedType = ConnectionProvider.Type.Multicast
        }

        if (expectedType != configType) {
            throw new APSConfigException("Expected config entry of type '" + expectedType.name() + "', but got type '" + config.type + "'!")
        }

        switch (configType) {
            case ConnectionProvider.Type.TCP:
                if (direction == ConnectionProvider.Direction.Write) {
                    connectionProvider = new TCPSender(config: config, logger: logger, securityHandler: this.tcpSecurityHandler)
                }
                else {
                    connectionProvider = new TCPReceiver(config: config, logger: logger, securityHandler: this.tcpSecurityHandler)
                }
                connectionProvider.start()
                this.connectionProviders.put(cacheKey, connectionProvider)
                break

            case ConnectionProvider.Type.UDP:
                if (direction == ConnectionProvider.Direction.Write) {
                    connectionProvider = new UDPSender(config: config, logger: logger, securityHandler: udpSecurityHandler)
                }
                else {
                    connectionProvider = new UDPReceiver(config: config, logger: logger, securityHandler: udpSecurityHandler)
                }
                connectionProvider.start()
                this.connectionProviders.put(cacheKey, connectionProvider)
                break

            case ConnectionProvider.Type.Multicast:
                if (direction == ConnectionProvider.Direction.Write) {
                    connectionProvider = new MulticastSender(config: config, logger: logger, securityHandler: udpSecurityHandler)
                }
                else {
                    connectionProvider = new MulticastReceiver(config: config, logger: logger, securityHandler: udpSecurityHandler)
                }
                connectionProvider.start()
                this.connectionProviders.put(cacheKey, connectionProvider)
                break
        }

        return connectionProvider
    }

    @BundleStop
    public void cleanup() {
        TCPIPConfig.managed.get().removeConfigChangedListener(this)
        this.connectionProviders.keySet().each { String key ->
            ConnectionProvider connectionProvider = this.connectionProviders.get(key)
            connectionProvider.stop()
        }
    }

    /**
     * Event listener callback when event occurs.
     *
     * @param event information about the event.
     */
    @Override
    synchronized void apsConfigChanged(APSConfigChangedEvent event) {
        if (TCPIPConfig.managed.get().respondToConfigUpdates.boolean) {
            this.connectionProviders.keySet().each { String key ->
                ConnectionProvider provider = this.connectionProviders.get(key)
                provider.configChanged()
            }
        }
    }
}
