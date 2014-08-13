package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;

@APSConfigDescription(
        configId = "tcpip",
        description = "Provides Hazelcast tcpip configuration",
        version = "1.0.0"
)
public class APSTCPIPConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("false"),
            isBoolean = true,
            description = "Enable/disable config."
    )
    public APSConfigValue tcpipConfigEnabled;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The required member."
    )
    public APSConfigValue tcpipRequiredMember;


    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "List of additional members."
    )
    public APSConfigValueList tcpipMembers;
}
