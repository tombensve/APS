package se.natusoft.osgi.aps.net.messaging.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.ManagedConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue

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
@CompileStatic
@TypeChecked
class ClusterServiceConfig extends APSConfig {

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
    public APSConfigList<Cluster> clusters

    @APSConfigDescription(
            configId = "cluster",
            version = "1.0.0",
            description = "This represents one cluster definition."
    )
    @CompileStatic
    @TypeChecked
    public static final class Cluster extends APSConfig {

        @APSConfigItemDescription(description = "The name of the cluster definition.", environmentSpecific = true)
        public APSConfigValue name

        @APSConfigItemDescription(
                description = "This cluster should be discovered using multicast",
                isBoolean = true,
                environmentSpecific = true
        )
        public APSConfigValue multicastDiscovery

        @APSConfigItemDescription(
                description = "If members are not discovered by multicast then they need to be added here.",
                environmentSpecific = true
        )
        public APSConfigList<ClusterMember> tcpMembers

    }

    @APSConfigDescription(
            configId = "member",
            version = "1.0.0",
            description = "This represents one cluster member."
    )
    @CompileStatic
    @TypeChecked
    public static final class ClusterMember extends APSConfig {

        @APSConfigItemDescription(description = "Documentative name of the cluster member.", environmentSpecific = true)
        public APSConfigValue name

        @APSConfigItemDescription(description = "The host of the cluster member.", environmentSpecific = true)
        public APSConfigValue hostname

        @APSConfigItemDescription(description = "The port of the cluster member.", environmentSpecific = true)
        public APSConfigValue port
    }
}
