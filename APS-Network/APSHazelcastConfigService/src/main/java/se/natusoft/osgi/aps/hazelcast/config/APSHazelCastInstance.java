package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

@APSConfigDescription(
        configId = "instance",
        description = "Provides Hazelcast instance configuration",
        version = "1.0.0"
)
public class APSHazelCastInstance extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The name of the instance. Must be unique!"
    )
    public APSConfigValue name;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The path to a config file on the server or an http or ftp url. This makes out the base config. " +
                    "Anything else configured in this gui overrides or appends to what is in " +
                    "the config file. This can also be left blank."
    )
    public APSConfigValue configFile;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The port to use."
    )
    public APSConfigValue port;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Enable to autoincrement port.",
            isBoolean = true
    )
    public APSConfigValue autoIncrementPort;

    // Sub configs

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Interfaces config."
    )
    public APSInterfacesConfig interfaces;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Multicast config."
    )
    public APSMulticastConfig multicast;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "TCP-IP config."
    )
    public APSTCPIPConfig tcpip;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Group config."
    )
    public APSGroupConfig group;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Configures executors."
    )
    public APSConfigList<APSExecutorConfig> executors;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Configures listener instances."
    )
    public APSConfigList<APSListenerConfig> listeners;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Configures lists."
    )
    public APSConfigList<APSListConfig> lists;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Configures sets."
    )
    public APSConfigList<APSSetConfig> sets;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Configures maps."
    )
    public APSConfigList<APSMapConfig> maps;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Configures store instances."
    )
    public APSConfigList<APSStoreConfig> stores;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Configures queues."
    )
    public APSConfigList<APSQueueConfig> queues;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Configures topics."
    )
    public APSConfigList<APSTopicConfig> topics;
}
