package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;

@APSConfigDescription(
        configId = "queues",
        description = "Provides Hazelcast queues configuration",
        version = "1.0.0"
)
public class APSQueueConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The name of the queue."
    )
    public APSConfigValue name;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The asynchronous backup count."
    )
    public APSConfigValue asyncBackupCount;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The backup count."
    )
    public APSConfigValue backupCount;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Time to live for empty queues."
    )
    public APSConfigValue emptyQueueTtl;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The maximumsize of the queue."
    )
    public APSConfigValue maxSize;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Select to enable statistics."
    )
    public APSConfigValue statisticsEnabled;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "A name of an item listener configured in 'listeners'."
    )
    public APSConfigValueList itemListeners;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The name of a store configured in 'stores'. The pointed out config must implement QueueStore."
    )
    public APSConfigValue queueStore;

}
