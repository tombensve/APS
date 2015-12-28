package se.natusoft.osgi.aps.discoveryservice.config

import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.ManagedConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList

@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.net.default-discovery-svc",
        description = "Configuration for aps-default-discovery-service.",
        version =  "1.0.0",
        group = "network"
)
class DiscoveryConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this config when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the config to become managed before we try to access it. Our bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the config
     * by using the extender pattern. Even if the config service is running before us we might access the config
     * before the config service have had a change to manage the config. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<DiscoveryConfig> managed = new ManagedConfig<DiscoveryConfig>();


    @APSConfigItemDescription(
            description = "The name of the APSTCPIPService configuration to use for multicast discovery. This has to be a multicast configuration!",
            environmentSpecific = true
    )
    public APSConfigValue mcastDiscoveryConfigName

    @APSConfigItemDescription(
            description = "The name of the APSTCPIPService configuration to use for receiving other published services.",
            environmentSpecific = true
    )
    public APSConfigValue tcpReceiverConfigName

    @APSConfigItemDescription(
            description = "Named APSTCPIPService TCP configurations for publishing services.",
            environmentSpecific = true
    )
    public APSConfigValueList tcpPublishToConfigNames

    @APSConfigItemDescription(
            description = "Manually entered service entries.",
            environmentSpecific = true
    )
    public APSConfigList<ManualServiceEntry> manualServiceEntries


    @APSConfigDescription(
            configId = "svc-entry",
            description = "A manually configured service entry.",
            version = "1.0.0"
    )
    public static class ManualServiceEntry extends APSConfig {

        @APSConfigItemDescription(
                description = "A description of the service.",
                environmentSpecific = true
        )
        public APSConfigValue description

        @APSConfigItemDescription(
                description = "Service name / identifier.",
                environmentSpecific = true
        )
        public APSConfigValue serviceId

        @APSConfigItemDescription(
                description = "The version of the service.",
                environmentSpecific = true
        )
        public APSConfigValue version

        @APSConfigItemDescription(
                description = "The host the service resides on. This is for non web based services.",
                environmentSpecific = true
        )
        public APSConfigValue host

        @APSConfigItemDescription(
                description = "The port the service listens to. This is for non web based services.",
                environmentSpecific = true
        )
        public APSConfigValue port

        @APSConfigItemDescription(
                description = "The protocol type of the service. This is for non web based services.",
                environmentSpecific = true,
                validValues = ["TCP", "UDP", "MULTICAST"]
        )
        public APSConfigValue protocol

        @APSConfigItemDescription(
                description = "An URL pointing to the service. This should be specified if it is a web service only accessible via an URL.",
                environmentSpecific = true
        )
        public APSConfigValue url
    }
}
