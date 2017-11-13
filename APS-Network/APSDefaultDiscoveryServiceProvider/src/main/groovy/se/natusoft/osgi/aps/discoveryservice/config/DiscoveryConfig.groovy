package se.natusoft.osgi.aps.discoveryservice.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.configold.APSConfig
import se.natusoft.osgi.aps.api.core.configold.ManagedConfig
import se.natusoft.osgi.aps.api.core.configold.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.configold.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.configold.model.APSConfigList
import se.natusoft.osgi.aps.api.core.configold.model.APSConfigValue
import se.natusoft.osgi.aps.api.core.configold.model.APSConfigValueList

@CompileStatic
@TypeChecked
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.net.default-discovery-svc",
        description = "Configuration for aps-default-discovery-service.",
        version =  "1.0.0",
        group = "network"
)
class DiscoveryConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this configold when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the configold to become managed before we try to access it. Our bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the configold
     * by using the extender pattern. Even if the configold service is running before us we might access the configold
     * before the configold service have had a change to manage the configold. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<DiscoveryConfig> managed = new ManagedConfig<DiscoveryConfig>();


    @APSConfigItemDescription(
            description = "A 'multicast://host:port' URI or blank for no multicast.",
            environmentSpecific = true
    )
    public APSConfigValue multicastConnectionPoint

    @APSConfigItemDescription(
            description = "A 'tcp://host:port' URI or blank for no TCP.",
            environmentSpecific = true
    )
    public APSConfigValue tcpReceiverConnectionPoint

    @APSConfigItemDescription(
            description = "A list of 'tcp://host:port' URIs, one for each other discovery service to inform. Empty list for none.",
            environmentSpecific = true
    )
    public APSConfigValueList tcpPublishToConnectionPoints

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
                description = "Enter 'name=value' on each line.",
                environmentSpecific = true
        )
        public APSConfigValueList propertyList

    }
}
