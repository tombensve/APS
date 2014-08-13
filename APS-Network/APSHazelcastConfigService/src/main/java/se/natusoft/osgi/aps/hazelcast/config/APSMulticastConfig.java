package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

@APSConfigDescription(
        configId = "multicast",
        description = "Provides Hazelcast multicast configuration",
        version = "1.0.0"
)
public class APSMulticastConfig extends APSConfig {


    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("false"),
            isBoolean = true,
            description = "Enable/disable config."
    )
    public APSConfigValue configEnabled;

    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("224.2.2.3"),
            description = "The multicast group address to use. Default 224.2.2.3."
    )
    public APSConfigValue group;

    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("54327"),
            description = "The port to use. Default 54327."
    )
    public APSConfigValue port;

    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("2"),
            description = "The timeout to use. Default 2 seconds."
    )
    public APSConfigValue timeout;

    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("32"),
            description = "The 'time to live' value. Default 32."
    )
    public APSConfigValue timeToLive;

}
