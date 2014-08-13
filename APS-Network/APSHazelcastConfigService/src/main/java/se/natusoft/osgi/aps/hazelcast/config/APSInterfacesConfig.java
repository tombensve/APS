package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;

@APSConfigDescription(
        configId = "interfaces",
        description = "Provides Hazelcast interface configuration",
        version = "1.0.0"
)
public class APSInterfacesConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Enable/disable config."
    )
    public APSConfigValue configEnabled;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Network interfaces. Ex: 10.3.16.* / 10.3.10.4-18"
    )
    public APSConfigValueList interfaces;
}
