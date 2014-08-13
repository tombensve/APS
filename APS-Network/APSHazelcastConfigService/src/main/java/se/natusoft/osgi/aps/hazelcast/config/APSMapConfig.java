package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;

@APSConfigDescription(
        configId = "maps",
        description = "Provides Hazelcast maps configuration",
        version = "1.0.0"
)
public class APSMapConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The name of the map."
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
            description = "The eviction procentage when max is reached."
    )
    public APSConfigValue evictionProcentage;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The maximumsize of the map."
    )
    public APSConfigValue maxSize;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The max idle seconds."
    )
    public APSConfigValue maxIdleSeconds;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Select to optimize queries."
    )
    public APSConfigValue optimizeQueries;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Select to read backup data."
    )
    public APSConfigValue readBackupData;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Select to enable statistics."
    )
    public APSConfigValue statisticsEnabled;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The time to live."
    )
    public APSConfigValue timeToLive;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "A name of a listener configured in 'listeners'. Must implement EntryListener."
    )
    public APSConfigValueList entryListeners;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The name of a store configured in 'stores'. This must implement MapStore."
    )
    public APSConfigValue mapStore;

}
