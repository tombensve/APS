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
package se.natusoft.osgi.aps.discovery.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration for the APS discovery service.
 */
@APSConfigDescription(
    configId="se.natusoft.osgi.aps.discovery",
    group = "network",
    description="Adresses and ports to use for discovery.",
    version="1.0.0"
)
public class APSDiscoveryServiceConfig extends APSConfig {

    /** This config is auto-managed and will be accessed through this. */
    public static ManagedConfig<APSDiscoveryServiceConfig> mc = new ManagedConfig<APSDiscoveryServiceConfig>();


    @APSConfigItemDescription(description = "The multicast address to use.", defaultValue = {@APSDefaultValue(value="228.31.32.33")})
    public APSConfigValue multicastAddress;

    @APSConfigItemDescription(description = "The multicast target port to use.", defaultValue = {@APSDefaultValue(value="14000")})
    public APSConfigValue multicastPort;

    @APSConfigItemDescription(description = "The UDP address to listen to. This is for enabling discovery between networks where multicast doesn't reach. " +
                                 "Defaults to \"auto\" which auto resolves the local address. " +
                                 "Specify an address if you have multiple network interfaces with different addresses and want to use one of those. " +
                                 "Set to blank or \"disable\" to disable!", defaultValue={@APSDefaultValue(value="auto")})
    public APSConfigValue udpLocalListenAddress;

    @APSConfigItemDescription(description = "The UDP targetPort to listen to.", defaultValue = {@APSDefaultValue(value="14001")})
    public APSConfigValue udpLocalListenPort;

    @APSConfigItemDescription(description = "The number of consecutive read failures allowed.", defaultValue = {@APSDefaultValue(value = "10")})
    public APSConfigValue consecutiveReadFailureLimit;

    @APSConfigItemDescription(description = "A list of target discovery services to announce ourself to.")
    public APSConfigList<APSUDPRemoteDestinationDiscoveryServiceConfig> udpTargetDiscoveryServices;

    /**
     * Configuration of static UDP discovery service instance elsewhere on the net
     * that services will be announced to.
     * <p/>
     * This is a sub config model of APSDiscoveryServiceConfig!
     */
    @APSConfigDescription(
        configId="se.natusoft.osgi.aps.discovery.static.services",
        description="Address and targetPort for known discovery service instance at static address and port outside of local net where multicast doesn't work.",
        version="1.0.0"
    )
    public static class APSUDPRemoteDestinationDiscoveryServiceConfig extends APSConfig {

        @APSConfigItemDescription(description = "The targetHost where a known discovery service runs.")
        public APSConfigValue targetHost;

        @APSConfigItemDescription(description = "The targetPort where the known discovery service listens to.")
        public APSConfigValue targetPort;
    }
}
