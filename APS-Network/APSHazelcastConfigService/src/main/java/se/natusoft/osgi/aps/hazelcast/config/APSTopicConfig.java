package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;

@APSConfigDescription(
        configId = "topics",
        description = "Provides Hazelcast topic configuration",
        version = "1.0.0"
)
public class APSTopicConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The name of the topic."
    )
    public APSConfigValue name;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Enable global ordering.",
            isBoolean = true
    )
    public APSConfigValue globalOrderingEnabled;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Select to enable statistics."
    )
    public APSConfigValue statisticsEnabled;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "A name of an message listener configured in 'listeners'."
    )
    public APSConfigValueList messageListeners;

}
