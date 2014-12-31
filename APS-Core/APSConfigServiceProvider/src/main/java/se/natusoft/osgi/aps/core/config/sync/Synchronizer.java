/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         A more advanced configuration service that uses annotated interfaces to
 *         describe and provide access to configuration. It supports structured
 *         configuration models.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2013-08-02: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.sync;

import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService;
//import se.natusoft.osgi.aps.api.net.sharing.service.APSSyncService;
//import se.natusoft.osgi.aps.api.net.time.service.APSNetTimeService;
import se.natusoft.osgi.aps.api.net.messaging.types.APSCluster;
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage;
import se.natusoft.osgi.aps.core.config.config.APSConfigServiceConfig;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigAdminImpl;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Responsible for synchronizing with other installations.
 */
public class Synchronizer implements APSConfigMemoryStore.ConfigUpdateListener,
        APSConfigEnvStore.ConfigEnvUpdateListener, APSConfigChangedListener, APSCluster.Listener {

    //
    // Private Members
    //

    /** The general service logger. We get this in the constructor. */
    APSLogger logger;

    /** The internal configuration environment storage.  */
    private final APSConfigEnvStore configEnvStore;

    /** The internal configuration value storage. */
    private final APSConfigMemoryStore configMemoryStore;

    /** The internal handler for accessing configurations on disk. */
    private final APSConfigPersistentStore configPersistentStore;

    /** The implementation of APSConfigAdminService. */
    private final APSConfigAdminService configAdminService;

    /** Our own config model. It is loaded in the constructor! */
    private APSConfigServiceConfig config;

    /** The cluster we sync on. */
    private APSCluster syncCluster;

    /**
     * Determines if we are synchronizing or not. This is used to be able to turn on or off sync
     * due to configuration change. A configuration change event is received on each save even
     * if no values have been changed so we use this to determine the current state.
     */
    private boolean synchronizing = false;

    //
    // Internal Support Classes
    //

    /**
     * This is a transport object for synchronizing configuration data.
     */
    public static class ConfigSync implements Serializable {

        public ConfigSync(ContentType contentType) {
            if (contentType == null) {
                throw new APSRuntimeException("contentType arg cannot be null!");
            }
            this.contentType = contentType;
        }

        /** The content type of this sync object. */
        public ContentType contentType = ContentType.CONFIG;

        /** The configuration id when contentType is CONFIG. */
        public String configId;

        /** The version of the config to synchronize. */
        public String version;

        /** The actual config or config environment properties. */
        public Properties content;

        /**
         * Content identification.
         */
        private static enum ContentType implements Serializable {
            CONFIG,
            CONFIG_ENV
        }
    }

    /**
     * API for identifying time properties.
     */
    private interface TimePropertyMatcher {
        /**
         * Should return true if the specified property name is a time property.
         *
         * @param propName The property to check.
         */
        public boolean isTimeProperty(String propName);
    }
    /**
     * API for converting time back and forth between local and net time.
     */
    private interface TimePropertyConverter {
        /**
         * Converts the passed time and returns the converted time.
         *
         * @param time The time value to convert.
         */
        public long convertTime(long time);
    }

    //
    // Constructors
    //

    /**
     * Creates a new Synchronizer.
     *
     * @param logger The logger to log to.
     * @param configAdminService The local APSConfigAdminService instance.
     * @param configService The local APSConfigService instance.
     * @param configEnvStore The local APSConfigEnvStore.
     * @param configMemoryStore The memory store to listen for changes on.
     * @param configPersistentStore For persisting changes.
     * @param syncCluster The cluster to sync on.
     */
    public Synchronizer(
            APSLogger logger,
            APSConfigAdminService configAdminService,
            APSConfigService configService,
            APSConfigEnvStore configEnvStore,
            APSConfigMemoryStore configMemoryStore,
            APSConfigPersistentStore configPersistentStore,
            APSCluster syncCluster
    ) {
        this.logger = logger;
        this.configAdminService = configAdminService;
        this.configEnvStore = configEnvStore;
        this.configMemoryStore = configMemoryStore;
        this.configPersistentStore = configPersistentStore;
        this.syncCluster = syncCluster;

        this.config = configService.getConfiguration(APSConfigServiceConfig.class);
        this.config.addConfigChangedListener(this);
    }

    /**
     * Cleans up. Activator calls this on shutdown.
     */
    public void cleanup() {
        if (this.config != null) {
            this.config.removeConfigChangedListener(this);
        }
    }

    //
    // Methods
    //

    /**
     * Starts synchronizing.
     */
    public void start() {
        if (this.config.synchronize.toBoolean()) {
            this.synchronizing = true;

            this.syncCluster.addMessageListener(this);

            this.configEnvStore.addUpdateListener(this);
            this.configMemoryStore.addUpdateListener(this);
            this.logger.info("Started config synchronizer!");
        }
    }

    /**
     * Stops synchronizing.
     */
    public void stop() {
        if (this.config.synchronize.toBoolean()) {
            this.synchronizing = false;

            this.configEnvStore.removeUpdateListener(this);
            this.configMemoryStore.removeUpdateListener(this);
            this.syncCluster.
//            if (this.syncGroup != null) {
//                this.syncGroup.leaveSyncGroup();
//            }
            this.logger.info("Stopped config synchronizer!");
        }
    }

    /**
     * This is called when a messaging is received.
     *
     * @param message The received message.
     */
    @Override
    public void messageReceived(APSMessage message) {

    }

    /**
     * Event listener callback when event occurs.
     *
     * @param event information about the event.
     */
    @Override
    public void apsConfigChanged(APSConfigChangedEvent event) {
        // This will be called on save of the configuration even if nothing was actually changed!
        // Thereby we keep track of and check our current state.
        if (!this.synchronizing && this.config.synchronize.toBoolean()) {
            start();
        }
        else if (this.synchronizing && !this.config.synchronize.toBoolean()) {
            stop();
        }
    }

    /** Matches time properties for config env properties. */
    private static final TimePropertyMatcher Config_Env_Matcher = new TimePropertyMatcher() {
        @Override
        public boolean isTimeProperty(String propName) {
            return propName.startsWith("time_");
        }
    };

    /** Matches time properties for config value properties. */
    private static final TimePropertyMatcher Config_Value_Matcher = new TimePropertyMatcher() {
        @Override
        public boolean isTimeProperty(String propName) {
            return propName.endsWith("_time");
        }
    };

    /** Converts from local to net time. */
    private final TimePropertyConverter Local_2_Net_Time_Converter = new TimePropertyConverter() {
        @Override
        public long convertTime(long time) {
//            return Synchronizer.this.netTimeService.localToNetTime(time);
            return -1;
        }
    };

    /** Converts from net to local time. */
    private final TimePropertyConverter Net_2_Local_Time_Converter = new TimePropertyConverter() {
        @Override
        public long convertTime(long time) {
//            return Synchronizer.this.netTimeService.netToLocalTime(time);
            return -1;
        }
    };

    /**
     * Converts all time properties in a whole Properties set from local to net time or net to local time.
     *
     * @param props The properties to convert.
     * @param timePropMatcher The matcher to use for identifying time properties.
     * @param timePropConverter The converter to use for converting properties.
     */
    private Properties convertPropTime(Properties props, TimePropertyMatcher timePropMatcher, TimePropertyConverter timePropConverter) {
        Properties newProps = new Properties();

        for (String propName : props.stringPropertyNames()) {
            String propValue = props.getProperty(propName);
            newProps.setProperty(propName, propValue);

            if (timePropMatcher.isTimeProperty(propName)) {
                long value = Long.valueOf(propValue);
                newProps.setProperty(propName, "" + timePropConverter.convertTime(value));
            }
        }

        return newProps;
    }


    /**
     * Receives updated configuration.
     *
     * @param configEnvStore The config env store holding the updated value(s).
     */
    @Override
    public void updated(APSConfigEnvStore configEnvStore) {
       syncSend(configEnvStore);
    }

    /**
     * Send sync config update.
     *
     * @param configEnvStore The config env store holding the updated value(s).
     */
    public void syncSend(APSConfigEnvStore configEnvStore) {
        try {
            ConfigSync configSync = new ConfigSync(ConfigSync.ContentType.CONFIG_ENV);
            configSync.content = convertPropTime(configEnvStore.getAsProperties(), Config_Env_Matcher, Local_2_Net_Time_Converter);

//            this.syncGroup.sync(configSync);
        }
        catch (Exception e) {
            this.logger.error("Send of config env message failed: " + e.getMessage(), e);
        }
    }

    /**
     * Receives updated local configuration.
     *
     * @param configuration The updated configuration.
     */
    @Override
    public void updated(APSConfigAdmin configuration) {
        syncSend(configuration);
    }

    /**
     * Sends a sync config update.
     *
     * @param configuration The updated configuration.
     */
    private void syncSend(APSConfigAdmin configuration) {
        try {
            ConfigSync configSync = new ConfigSync(ConfigSync.ContentType.CONFIG);
            configSync.configId = configuration.getConfigId();
            configSync.version = configuration.getVersion();
            configSync.content = convertPropTime(
                    ((APSConfigAdminImpl) configuration).getConfigInstanceMemoryStore().getProperties(),
                    Config_Value_Matcher,
                    Local_2_Net_Time_Converter
            );

//            this.syncGroup.sync(configSync);

        }
        catch (Exception e) {
            this.logger.error("Send of config value message failed: " + e.getMessage(), e);
        }
    }

    /**
     * Called for each new synchronization data available.
     *
     * @param syncData The received synchronization data.
     */
//    @Override
    public void synced(ConfigSync syncData) {
        switch(syncData.contentType) {
            case CONFIG:
                handleConfigValueMessage(syncData);
                break;

            case CONFIG_ENV:
                handleConfigEnvMessage(syncData);
                break;
        }
    }

    /**
     * If this gets called then this synchronization member should resync all of its information.
     */
//    @Override
    public void updateOthers() {
        syncSend(this.configEnvStore);

        for (APSConfigAdmin configAdmin : this.configMemoryStore.getAllConfigurations()) {
            syncSend(configAdmin);
        }
    }

    /**
     * Handles a received config value message.
     *
     * @param sync The received sync.
     */
    private void handleConfigValueMessage(ConfigSync sync) {
        String configId = sync.configId;
        String version = sync.version;
        Properties props = convertPropTime(sync.content, Config_Value_Matcher, Net_2_Local_Time_Converter);

        mergeConfigValues(configId, version, props);
    }

    /**
     * Merge received config properties with local.
     *
     * @param configId Id of received configuration.
     * @param version Version of received configuration.
     * @param syncProps The received configuration properties.
     */
    private void mergeConfigValues(String configId, String version, Properties syncProps) {
        APSConfigAdminImpl localConfigAdmin;

        synchronized (this.configAdminService) {
            localConfigAdmin = (APSConfigAdminImpl)this.configAdminService.getConfiguration(configId, version);
        }

        if (localConfigAdmin != null) {
            Map<String,String> listPropKeys = new HashMap<>();

            Properties localProps = localConfigAdmin.getConfigInstanceMemoryStore().getProperties();
            boolean updated = false;
            for (String propName : syncProps.stringPropertyNames()) {
                if (!propName.endsWith("_time")) {
                    String propValue = syncProps.getProperty(propName);

                    if (propName.endsWith("_size")) {
                        listPropKeys.put(propName.substring(0, propName.length() - 5), propValue);
                    }

                    String syncTSValue = syncProps.getProperty(propName + "_time");
                    long syncTS = syncTSValue != null ? Long.valueOf(syncTSValue) : 0;

                    String localTSValue = localProps.getProperty(propName + "_time");
                    long localTS = localTSValue != null ? Long.valueOf(localTSValue) : 0;

                    if ((syncTS > localTS || localTS == 0) && propValue != null) {
                        localProps.setProperty(propName, propValue);
                        if (syncTSValue != null) {
                            localProps.setProperty(propName + "_time", syncTSValue);
                        }
                        updated = true;
                    }
                }
            }

            // Remove list properties that are no longer valid due to size been decreased.
            for (String listEntryKey : listPropKeys.keySet()) {
                int size = Integer.valueOf(listPropKeys.get(listEntryKey));
                for (String localPropKey : localProps.stringPropertyNames()) {
                    if (localPropKey.startsWith(listEntryKey)) {
                        String[] parts = localPropKey.substring(listEntryKey.length() + 1).split("_");
                        if (parts.length > 0 && !parts[0].equals("size")) {
                            if (Character.isDigit(parts[0].charAt(0))) {
                                int ix = Integer.valueOf(parts[0]);
                                if (ix >= size) {
                                    localProps.remove(localPropKey);
                                }
                            }
                        }
                    }
                }
            }

            // We go directly to the underlaying store rather than use the APSConfigAdmin service API
            // since that API would trigger a configuration update event.
            synchronized (this.configPersistentStore) {
                if (updated) this.configPersistentStore.saveConfiguration(localConfigAdmin);
            }
        }
        else {
            this.logger.warn("Received update for configuration '" + configId + ":" + version + "' but I don't have that configuration " +
                "registered locally!");
        }
    }

    /**
     * Handles a received config env message.
     *
     * @param sync The received message.
     */
    private void handleConfigEnvMessage(ConfigSync sync) {
        mergeConfigEnvs(convertPropTime(sync.content, Config_Env_Matcher, Net_2_Local_Time_Converter));
    }

    /**
     * Merge received config env with local.
     * @param syncProps The synchronized properties.
     */
    private void mergeConfigEnvs(Properties syncProps) {
        Properties localEnvProps;

        synchronized (this.configEnvStore) {
            localEnvProps = this.configEnvStore.getAsProperties();
        }

        String localEnvsStr = localEnvProps.getProperty("envs");
        int localEnvs = localEnvsStr != null ? Integer.valueOf(localEnvsStr) : 0;

        String envsStr = syncProps.getProperty("envs");
        int envs = envsStr != null ? Integer.valueOf(envsStr) : 0;
        boolean updated = false;
        for (int i = 0; i < envs; i++) {
            String name = syncProps.getProperty("name_" + i);
            String time = syncProps.getProperty("time_" + i);
            long msgTs = time != null ? Long.valueOf(time) : 0;

            int localIx = -1;
            for (int ix = 0; ix < localEnvs; ix++) {
                String localName = localEnvProps.getProperty("name_" + ix);
                if (localName != null && localName.equals(name)) {
                    localIx = ix;
                    break;
                }
            }

            if (localIx == -1) {
                localEnvs += 1;
                localEnvProps.setProperty("name_" + localEnvs, name);
                localEnvProps.setProperty("desc_" + localEnvs, syncProps.getProperty("desc_" + i));
                localEnvProps.setProperty("time_" + localEnvs, "" + msgTs);
                localEnvProps.setProperty("envs", "" + localEnvs + 1);
                updated = true;
            }
            else {
                String localTime = localEnvProps.getProperty("time_" + localIx);
                long localTs = localTime != null ? Long.valueOf(localTime) : 0;

                if (msgTs > localTs || localTs == 0) {
                    localEnvProps.setProperty("name_" + localIx, name);
                    localEnvProps.setProperty("desc_" + localIx, syncProps.getProperty("desc_" + i));
                    localEnvProps.setProperty("time_" + localIx, "" + msgTs);
                    updated = true;
                }
            }
        }

        if (updated) {
            synchronized (this.configEnvStore) {
                this.configEnvStore.setFromProperties(localEnvProps);
                this.configEnvStore.saveConfigEnvironments();
            }
        }
    }
}
