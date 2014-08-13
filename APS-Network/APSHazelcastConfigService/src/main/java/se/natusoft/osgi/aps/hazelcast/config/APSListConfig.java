package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;

@APSConfigDescription(
        configId = "lists",
        description = "Provides Hazelcast list configuration",
        version = "1.0.0"
)
public class APSListConfig extends APSCollectionCommoConfig {}
