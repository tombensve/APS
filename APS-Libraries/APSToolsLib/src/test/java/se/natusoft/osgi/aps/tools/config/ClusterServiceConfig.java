/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides a library of utilities, among them APSServiceTracker used by all other APS bundles.
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
 *         2017-01-05: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Cluster config.
 */
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.messaging.tcpclusterservice",
        description = "This provides configuration for aps-tcp-cluster-service-provider.",
        version = "1.0.0",
        // APSConfigAdminWeb GUI tree placement.
        group = "network.messaging"
)
public class ClusterServiceConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this config when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the config to become managed before we try to access it. Our bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the config
     * by using the extender pattern. Even if the config service is running before us we might access the config
     * before the config service have had a change to manage the config. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<ClusterServiceConfig> managed = new ManagedConfig<ClusterServiceConfig>();

    @APSConfigItemDescription(
            description = "Defines named clusters. There will be one APSClusterService registered per cluster.",
            environmentSpecific = true
    )
    public APSConfigList<Cluster> clusters;

    @APSConfigDescription(
            configId = "cluster",
            version = "1.0.0",
            description = "This represents one cluster definition."
    )
    public static final class Cluster extends APSConfig {

        @APSConfigItemDescription(description = "The name of the cluster definition.", environmentSpecific = true)
        public APSConfigValue name;

        @APSConfigItemDescription(
                description = "This cluster should be discovered using multicast",
                isBoolean = true,
                environmentSpecific = true
        )
        public APSConfigValue multicastDiscovery;

        @APSConfigItemDescription(
                description = "If members are not discovered by multicast then they need to be added here.",
                environmentSpecific = true
        )
        public APSConfigList<ClusterMember> tcpMembers;

    }

    @APSConfigDescription(
            configId = "member",
            version = "1.0.0",
            description = "This represents one cluster member."
    )
    public static final class ClusterMember extends APSConfig {

        @APSConfigItemDescription(description = "Documentative name of the cluster member.", environmentSpecific = true)
        public APSConfigValue name;

        @APSConfigItemDescription(description = "The host of the cluster member.", environmentSpecific = true)
        public APSConfigValue hostname;

        @APSConfigItemDescription(description = "The port of the cluster member.", environmentSpecific = true)
        public APSConfigValue port;
    }
}
