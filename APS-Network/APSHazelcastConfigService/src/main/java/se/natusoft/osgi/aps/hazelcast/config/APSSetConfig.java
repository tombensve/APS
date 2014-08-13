package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;

@APSConfigDescription(
        configId = "sets",
        description = "Provides Hazelcast set configuration",
        version = "1.0.0"
)
public class APSSetConfig extends APSCollectionCommoConfig {}
