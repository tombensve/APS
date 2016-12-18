package se.natusoft.osgi.aps.net.messaging.topics.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.ManagedConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList

@CompileStatic
@TypeChecked
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.net.messaging.topics",
        description = "Configuration for messaging topics.",
        version =  "1.0.0",
        group = "network"
)
class TopicConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this config when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the config to become managed before we try to access it. Our bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the config
     * by using the extender pattern. Even if the config service is running before us we might access the config
     * before the config service have had a change to manage the config. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<TopicConfig> managed = new ManagedConfig<TopicConfig>()


    @APSConfigItemDescription(
            description = "A JSON object conforming to the APSMessageTopics documentation.",
            environmentSpecific = true
    )
    public APSConfigValue topicConfigJson

}
