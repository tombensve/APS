package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

@APSConfigDescription(
        configId = "group",
        description = "Provides Hazelcast group configuration",
        version = "1.0.0"
)
public class APSGroupConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The name of the group to join."
    )
    public APSConfigValue name;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The password for the group."
    )
    public APSConfigValue password;
}
