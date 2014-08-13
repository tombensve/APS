package se.natusoft.osgi.aps.hazelcast.service;

import com.hazelcast.config.*;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.hazelcast.api.HazelcastConfigService;
import se.natusoft.osgi.aps.hazelcast.config.*;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;

import java.io.IOException;
import java.util.Properties;

/**
 * Provides implementation of HazelcastConfigService.
 */
@OSGiServiceProvider(serviceAPIs = {HazelcastConfigService.class})
public class HazelcastConfigServiceProvider implements HazelcastConfigService {

    //
    // Private Members
    //

    @Managed(name = "HazelcastConfigServiceProvider", loggingFor = "HazelcastConfigServiceProvider")
    private APSLogger logger;

    //
    // Constructors
    //

    public HazelcastConfigServiceProvider() {}

    //
    // Methods
    //

    /**
     * Returns the named configuration instance or null if the name is not defined.
     *
     * @param name The name of the configuration to get.
     */
    @Override
    public Config getConfigInstance(String name) {
        APSHazelCastConfig apsHzConf = APSHazelCastConfig.managed.get();

        APSHazelCastInstance namedInst = null;

        // Lookup the named instance.
        for (APSHazelCastInstance hzInst : apsHzConf.instances) {
            if (hzInst.name.toString().equals(name)) {
                namedInst = hzInst;
                break;
            }
        }

        // Not found, return null.
        if (namedInst == null) {
            this.logger.error("Nonexistent config was looked up: " + name);
            return null;
        }

        // OK, we have it, copy data over to real Hazelcast Config.
        Config hzConfig = null;

        // Start with config file if one has been provided.
        if (namedInst.configFile.toString().trim().length() > 0) {
            try {
                if (namedInst.configFile.toString().startsWith("http:") || namedInst.configFile.toString().startsWith("ftp:")) {
                    hzConfig = new UrlXmlConfig(namedInst.configFile.toString());
                } else {
                    hzConfig = new FileSystemXmlConfig(namedInst.configFile.toString());
                }
            }
            catch (IOException ioe) {/*OK*/}
        }

        // default to empty config if we didn't get any config file.
        if (hzConfig == null) {
            this.logger.info("No config file was provided for '" + name + "', starting with empty config.");
            hzConfig =  new Config(name);
        }

        NetworkConfig hzNetConf = hzConfig.getNetworkConfig();
        {
            if (!namedInst.port.isEmpty()) hzNetConf.setPort(namedInst.port.toInt());
            if (!namedInst.autoIncrementPort.isEmpty()) hzNetConf.setPortAutoIncrement(namedInst.autoIncrementPort.toBoolean());
        }

        // Interfaces
        APSInterfacesConfig apsIf = namedInst.interfaces;
        InterfacesConfig hzIf = hzNetConf.getInterfaces();
        {
            hzIf.setEnabled(apsIf.configEnabled.toBoolean());
            for (APSConfigValue intf : apsIf.interfaces) {
                hzIf.getInterfaces().add(intf.toString());
            }
        }

        // Multicast
        APSMulticastConfig apsMulticast = namedInst.multicast;
        MulticastConfig hzMulticast = hzNetConf.getJoin().getMulticastConfig();
        {
            if (!apsMulticast.configEnabled.isEmpty()) hzMulticast.setEnabled(apsMulticast.configEnabled.toBoolean());
            if (!apsMulticast.group.isEmpty()) hzMulticast.setMulticastGroup(apsMulticast.group.toString());
            if (!apsMulticast.port.isEmpty()) hzMulticast.setMulticastPort(apsMulticast.port.toInt());
            if (!apsMulticast.timeout.isEmpty()) hzMulticast.setMulticastTimeoutSeconds(apsMulticast.timeout.toInt());
            if (!apsMulticast.timeToLive.isEmpty()) hzMulticast.setMulticastTimeToLive(apsMulticast.timeToLive.toInt());
        }

        // TCPIP
        APSTCPIPConfig apsTcp = namedInst.tcpip;
        TcpIpConfig hzTcp = hzNetConf.getJoin().getTcpIpConfig();
        {
            if (!apsTcp.tcpipConfigEnabled.isEmpty()) hzTcp.setEnabled(apsTcp.tcpipConfigEnabled.toBoolean());
            if (!apsTcp.tcpipRequiredMember.isEmpty()) hzTcp.setRequiredMember(apsTcp.tcpipRequiredMember.toString());
            for (APSConfigValue member : apsTcp.tcpipMembers) {
                hzTcp.addMember(member.toString());
            }
        }

        // Group
        APSGroupConfig apsGroup = namedInst.group;
        GroupConfig hzgroup = hzConfig.getGroupConfig();
        {
            if (!apsGroup.name.isEmpty()) hzgroup.setName(apsGroup.name.toString());
            if (!apsGroup.password.isEmpty()) hzgroup.setPassword(apsGroup.password.toString());
        }

        // Executors
        for (APSExecutorConfig apsExec : namedInst.executors) {
            ExecutorConfig hzExec = new ExecutorConfig();
            if (!apsExec.name.isEmpty()) hzExec.setName(apsExec.name.toString());
            if (!apsExec.poolSize.isEmpty()) hzExec.setPoolSize(apsExec.poolSize.toInt());
            if (!apsExec.queueCapacity.isEmpty()) hzExec.setQueueCapacity(apsExec.queueCapacity.toInt());
            if (!apsExec.statisticsEnabled.isEmpty()) hzExec.setStatisticsEnabled(apsExec.statisticsEnabled.toBoolean());
            hzConfig.addExecutorConfig(hzExec);
        }

        // Lists
        for (APSListConfig apsList : namedInst.lists) {
            ListConfig hzList = new ListConfig();
            if (apsList.name.isEmpty()) {
                this.logger.error("Found unnamed list config! Skipping this!");
                continue;
            }
            hzList.setName(apsList.name.toString());
            if (!apsList.asyncBackupCount.isEmpty()) hzList.setAsyncBackupCount(apsList.asyncBackupCount.toInt());
            if (!apsList.backupCount.isEmpty()) hzList.setBackupCount(apsList.backupCount.toInt());
            if (!apsList.maxSize.isEmpty()) hzList.setMaxSize(apsList.maxSize.toInt());
            if (!apsList.statisticsEnabled.isEmpty()) hzList.setStatisticsEnabled(apsList.statisticsEnabled.toBoolean());
            for (APSConfigValue apsListener : apsList.itemListeners) {
                APSListenerConfig apsLc = lookupListener(namedInst, apsListener.toString());
                if (apsLc != null) {
                    ItemListenerConfig ilc = new ItemListenerConfig();
                    ilc.setClassName(apsLc.implementationClassName.toString());
                    ilc.setIncludeValue(apsLc.includeValue.toBoolean());
                    hzList.addItemListenerConfig(ilc);
                } else {
                    this.logger.error("Undefined listener referenced: " + apsListener);
                }
            }

            hzConfig.addListConfig(hzList);
        }

        // Sets
        for (APSSetConfig apsSet : namedInst.sets) {
            SetConfig hzSet = new SetConfig();
            if (apsSet.name.isEmpty()) {
                this.logger.error("Found unnamed set config! Skipping this!");
                continue;
            }
            hzSet.setName(apsSet.name.toString());
            if (!apsSet.asyncBackupCount.isEmpty()) hzSet.setAsyncBackupCount(apsSet.asyncBackupCount.toInt());
            if (!apsSet.backupCount.isEmpty()) hzSet.setBackupCount(apsSet.backupCount.toInt());
            if (!apsSet.maxSize.isEmpty()) hzSet.setMaxSize(apsSet.maxSize.toInt());
            if (!apsSet.statisticsEnabled.isEmpty()) hzSet.setStatisticsEnabled(apsSet.statisticsEnabled.toBoolean());
            for (APSConfigValue apsListener : apsSet.itemListeners) {
                APSListenerConfig apsLc = lookupListener(namedInst, apsListener.toString());
                if (apsLc != null) {
                    ItemListenerConfig ilc = new ItemListenerConfig();
                    ilc.setClassName(apsLc.implementationClassName.toString());
                    ilc.setIncludeValue(apsLc.includeValue.toBoolean());
                    hzSet.addItemListenerConfig(ilc);
                } else {
                    this.logger.error("Undefined listener referenced: " + apsListener);
                }
            }

            hzConfig.addSetConfig(hzSet);
        }

        // Maps
        for (APSMapConfig apsMap : namedInst.maps) {
            MapConfig hzMap = new MapConfig();
            if (apsMap.name.isEmpty()) {
                this.logger.error("Found unnamed set config! Skipping this!");
                continue;
            }
            hzMap.setName(apsMap.name.toString());
            if (!apsMap.asyncBackupCount.isEmpty()) hzMap.setAsyncBackupCount(apsMap.asyncBackupCount.toInt());
            if (!apsMap.backupCount.isEmpty()) hzMap.setBackupCount(apsMap.backupCount.toInt());
            if (!apsMap.maxSize.isEmpty()) {
                MaxSizeConfig msc = new MaxSizeConfig();
                msc.setSize(apsMap.maxSize.toInt());
                hzMap.setMaxSizeConfig(msc);
            }
            if (!apsMap.statisticsEnabled.isEmpty()) hzMap.setStatisticsEnabled(apsMap.statisticsEnabled.toBoolean());
            if (!apsMap.evictionProcentage.isEmpty()) hzMap.setEvictionPercentage(apsMap.evictionProcentage.toInt());
            if (!apsMap.maxIdleSeconds.isEmpty()) hzMap.setMaxIdleSeconds(apsMap.maxIdleSeconds.toInt());
            if (!apsMap.optimizeQueries.isEmpty()) hzMap.setOptimizeQueries(apsMap.optimizeQueries.toBoolean());
            if (!apsMap.readBackupData.isEmpty()) hzMap.setReadBackupData(apsMap.readBackupData.toBoolean());
            if (!apsMap.timeToLive.isEmpty()) hzMap.setTimeToLiveSeconds(apsMap.timeToLive.toInt());
            for (APSConfigValue apsListener : apsMap.entryListeners) {
                APSListenerConfig apsLc = lookupListener(namedInst, apsListener.toString());
                if (apsLc != null) {
                    EntryListenerConfig elc = new EntryListenerConfig();
                    elc.setClassName(apsLc.implementationClassName.toString());
                    elc.setIncludeValue(apsLc.includeValue.toBoolean());
                    hzMap.addEntryListenerConfig(elc);
                } else {
                    this.logger.error("Undefined listener referenced: " + apsListener);
                }
            }
            APSStoreConfig store = lookupStore(namedInst, apsMap.mapStore.toString());
            if (store != null) {
                MapStoreConfig msc = new MapStoreConfig();
                if (!store.storeFactoryClassName.isEmpty()) {
                    msc.setFactoryClassName(store.storeFactoryClassName.toString());
                }
                else {
                    msc.setClassName(store.storeClassName.toString());
                }
                Properties props = new Properties();
                for (APSConfigValue propLine : store.properties) {
                    String[] nameValue = propLine.toString().split("=");
                    if (nameValue.length == 2) {
                        props.setProperty(nameValue[0], nameValue[1]);
                    }
                    else {
                        this.logger.error("Bad property line: " + propLine);
                    }
                }
                msc.setProperties(props);
                hzMap.setMapStoreConfig(msc);
            }
            else {
                this.logger.error("Non defined store referenced: " + apsMap.mapStore.toString());
            }

            hzConfig.addMapConfig(hzMap);
        }

        // Queues
        for (APSQueueConfig apsQueue : namedInst.queues) {
            QueueConfig hzQueue = new QueueConfig();

            if (!apsQueue.name.isEmpty()) {
                hzQueue.setName(apsQueue.name.toString());
                if (!apsQueue.asyncBackupCount.isEmpty()) hzQueue.setAsyncBackupCount(apsQueue.asyncBackupCount.toInt());
                if (!apsQueue.backupCount.isEmpty()) hzQueue.setBackupCount(apsQueue.backupCount.toInt());
                if (!apsQueue.emptyQueueTtl.isEmpty()) hzQueue.setEmptyQueueTtl(apsQueue.emptyQueueTtl.toInt());
                if (!apsQueue.maxSize.isEmpty()) hzQueue.setMaxSize(apsQueue.maxSize.toInt());
                if (!apsQueue.statisticsEnabled.isEmpty()) hzQueue.setStatisticsEnabled(apsQueue.statisticsEnabled.toBoolean());
                APSStoreConfig store = lookupStore(namedInst, apsQueue.queueStore.toString());
                if (store != null) {
                    QueueStoreConfig qsc = new QueueStoreConfig();
                    if (!store.storeFactoryClassName.isEmpty()) {
                        qsc.setFactoryClassName(store.storeFactoryClassName.toString());
                    }
                    else {
                        qsc.setClassName(store.storeClassName.toString());
                    }
                    Properties props = new Properties();
                    for (APSConfigValue propLine : store.properties) {
                        String[] nameValue = propLine.toString().split("=");
                        if (nameValue.length == 2) {
                            props.setProperty(nameValue[0], nameValue[1]);
                        }
                        else {
                            this.logger.error("Bad property line: " + propLine);
                        }
                    }
                    qsc.setProperties(props);
                    hzQueue.setQueueStoreConfig(qsc);
                }
                for (APSConfigValue itemListener : apsQueue.itemListeners) {
                    APSListenerConfig apsLc = lookupListener(namedInst, itemListener.toString());
                    if (apsLc != null) {
                        ItemListenerConfig ilc = new ItemListenerConfig();
                        ilc.setClassName(apsLc.name.toString());
                        ilc.setIncludeValue(apsLc.includeValue.toBoolean());
                        hzQueue.addItemListenerConfig(ilc);
                    }
                    else {
                        this.logger.error("Undefined listener referenced: " + itemListener);
                    }
                }
            }
            else {
                this.logger.error("Unnamed queue defined!");
            }

            hzConfig.addQueueConfig(hzQueue);
        }

        // Topics
        for (APSTopicConfig apsTopic : namedInst.topics) {
            TopicConfig hzTopic = new TopicConfig();

            if (apsTopic.name.isEmpty()) {
                hzTopic.setName(apsTopic.name.toString());

                if (!apsTopic.globalOrderingEnabled.isEmpty()) hzTopic.setGlobalOrderingEnabled(apsTopic.globalOrderingEnabled.toBoolean());
                if (!apsTopic.statisticsEnabled.isEmpty()) hzTopic.setStatisticsEnabled(apsTopic.statisticsEnabled.toBoolean());
                for (APSConfigValue itemListener : apsTopic.messageListeners) {
                    APSListenerConfig apsLc = lookupListener(namedInst, itemListener.toString());
                    if (apsLc != null) {
                        ListenerConfig lc = new ListenerConfig();
                        lc.setClassName(apsLc.name.toString());
                        hzTopic.addMessageListenerConfig(lc);
                    }
                    else {
                        this.logger.error("Undefined listener referenced: " + itemListener);
                    }
                }
            }
            else {
                this.logger.error("Unnamed topic defined!");
            }

            hzConfig.addTopicConfig(hzTopic);
        }

        return hzConfig;
    }

    /**
     * Looks up a listener from the config.
     *
     * @param inst The instance to search.
     * @param name The name of the listener to get.
     */
    private APSListenerConfig lookupListener(APSHazelCastInstance inst, String name) {
        APSListenerConfig found = null;
        for (APSListenerConfig lc : inst.listeners) {
            if (lc.name.toString().equals(name)) {
                found = lc;
                break;
            }
        }

        return found;
    }

    /**
     * Looks up a store from the config.
     *
     * @param inst The instance to search.
     * @param name The name of the store to get.
     */
    private APSStoreConfig lookupStore(APSHazelCastInstance inst, String name) {
        APSStoreConfig found = null;
        for (APSStoreConfig sc : inst.stores) {
            if (sc.name.toString().equals(name)) {
                found = sc;
                break;
            }
        }
        return found;
    }
}
