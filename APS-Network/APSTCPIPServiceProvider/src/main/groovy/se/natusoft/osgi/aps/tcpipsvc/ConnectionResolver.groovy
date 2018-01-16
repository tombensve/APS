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
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.exceptions.APSConfigException
import se.natusoft.osgi.aps.tcpipsvc.config.NamedDestinationsConfig
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.tcpipsvc.security.TCPSecurityHandler
import se.natusoft.osgi.aps.tcpipsvc.security.UDPSecurityHandler
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed

/**
 * Resolves named configuration and creates configured network providers.
 */
@CompileStatic
@TypeChecked
class ConnectionResolver {
    //
    // Private Members
    //

    @Managed(name = "APSTCPIPServiceLogger", loggingFor = "aps-tcpip-service-provider")
    private APSLogger logger

    @Managed
    private TCPSecurityHandler tcpSecurityHandler

    @Managed
    private UDPSecurityHandler udpSecurityHandler

    //
    // Methods
    //

    /**
     * Resolves a configuration name returning a ConnectionProvider.
     *
     * @param name The name of the configuration to get a ConnectionProvider for.
     * @param direction The required direction of the provider to return.
     * @param expectedType A configold validation. If configold entry is not of this type an exception is thrown.
     *
     * @throws APSConfigException on missing or miss-matching configold.
     */
    public synchronized ConnectionProvider resolve(@NotNull URI connectionPoint, @NotNull ConnectionProvider.Direction direction)
            throws IOException {

        if (connectionPoint == null) throw new IllegalArgumentException("'connectionPoint' argument is required! It cannot be null!")
        if (direction == null) throw new IllegalArgumentException("'direction' argument is required! It cannot be null!")

        ConnectionProvider connectionProvider

        switch (connectionPoint.scheme.toLowerCase()) {
            case "tcp":
                if (direction == ConnectionProvider.Direction.Write) {
                    connectionProvider = new TCPSender(
                            connectionPoint: connectionPoint,
                            logger: logger,
                            securityHandler: this.tcpSecurityHandler
                    )
                }
                else {
                    connectionProvider = new TCPReceiver(
                            connectionPoint: connectionPoint,
                            logger: logger,
                            securityHandler: this.tcpSecurityHandler
                    )
                }
                connectionProvider.start() // <-- Usage of connectionProvider.
                break

            case "udp":
                if (direction == ConnectionProvider.Direction.Write) {
                    connectionProvider = new UDPSender(
                            connectionPoint: connectionPoint,
                            logger: logger,
                            securityHandler: udpSecurityHandler
                    )
                }
                else {
                    connectionProvider = new UDPReceiver(
                            connectionPoint: connectionPoint,
                            logger: logger,
                            securityHandler: udpSecurityHandler
                    )
                }
                connectionProvider.start()
                break

            case "multicast":
                if (direction == ConnectionProvider.Direction.Write) {
                    connectionProvider = new MulticastSender(
                            connectionPoint: connectionPoint,
                            logger: logger,
                            securityHandler: udpSecurityHandler
                    )
                }
                else {
                    connectionProvider = new MulticastReceiver(
                            connectionPoint: connectionPoint,
                            logger: logger,
                            securityHandler: udpSecurityHandler
                    )
                }
                connectionProvider.start()
                break
            case "named":
                String resolvedURIStr = lookupNamed(connectionPoint.host)
                if (resolvedURIStr == null) {
                    throw new IOException("Named connectionpoint '${connectionPoint.host}' is not configured!")
                }
                URI resolvedURI = new URI(resolvedURIStr)
                connectionProvider = resolve(resolvedURI, direction)
                break;
            default:
                throw new IOException(
                        "Connection point URI (${connectionPoint}) is not valid! Only 'tcp://', 'udp://', 'multicast://, and named://' " +
                                "are allowed!"
                )
        }

        return connectionProvider // <-- Usage of connectionProvider.
    }

    private static String lookupNamed(String name) {
        // Groovy .each { ... } does not seem to work on an Iterable!
        for (NamedDestinationsConfig ndc : TCPIPConfig.managed.get().namedDestinations) {
            if (ndc.destName.string == name) {
                return ndc.destURI.string
            }
        }

        return null
    }
}
