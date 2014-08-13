package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;

public class APSCollectionCommoConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The name of the collection."
    )
    public APSConfigValue name;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The number of asynchronous backups."
    )
    public APSConfigValue asyncBackupCount;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The number of backups."
    )
    public APSConfigValue backupCount;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The maximum size of the collection."
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
            description = "The item listeners to register on this collection. Use names configured in " +
                    "'listeners' config."
    )
    public APSConfigValueList itemListeners;

}
