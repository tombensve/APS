package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;

@APSConfigDescription(
        configId = "stores",
        description = "Provides Hazelcast store configuration for queues or maps.",
        version = "1.0.0"
)
public class APSStoreConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Identifying name of this entry to reference in other configs."
    )
    public APSConfigValue name;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "A fully qualified name of a class implementing a store type."
    )
    public APSConfigValue storeClassName;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "A fully qualified name of a class implementing <type>StoreFactory."
    )
    public APSConfigValue storeFactoryClassName;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Enter properties for the store factory in 'name=value' format."
    )
    public APSConfigValueList properties;
}
