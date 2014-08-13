package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

@APSConfigDescription(
        configId = "listeners",
        description = "Provides Hazelcast listener configuration",
        version = "1.0.0"
)
public class APSListenerConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Identifying name of this config entry for reference in other configs."
    )
    public APSConfigValue name;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "A fully qualified name of a class that implements the relevant listener interface."
    )
    public APSConfigValue implementationClassName;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Select to include values in the events sent to the listener."
    )
    public APSConfigValue includeValue;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "This listener is local (does not apply to all types of listeners!)"
    )
    public APSConfigValue local;
}
