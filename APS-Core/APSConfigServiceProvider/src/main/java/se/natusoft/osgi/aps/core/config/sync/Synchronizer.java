/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.9.2
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
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService;
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONNumber;
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject;
import se.natusoft.osgi.aps.api.misc.json.model.JSONString;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService;
import se.natusoft.osgi.aps.api.net.sync.service.APSSyncService;
import se.natusoft.osgi.aps.api.net.time.service.APSNetTimeService;
import se.natusoft.osgi.aps.core.config.api.APSConfigSyncMgmtService;
import se.natusoft.osgi.aps.core.config.config.APSConfigServiceConfig;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigAdminImpl;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Responsible for synchronizing with other installations.
 */
public class Synchronizer implements APSConfigSyncMgmtService, APSConfigMemoryStore.ConfigUpdateListener,
        APSConfigEnvStore.ConfigEnvUpdateListener, APSConfigChangedListener, APSSyncService.SyncGroup.Message.Listener,
APSSyncService.SyncGroup.ReSyncListener {
    //
    // Constants
    //

    private static final String SYNC_MESSAGE_TYPE = "messageType";
    private static final String SYNC_CONFIG_ID = "configId";
    private static final String SYNC_VERSION = "version";
    private static final String SYNC_DATA = "data";

    //
    // Private Members
    //

    /** The general service logger. We get this in the constructor. */
    APSLogger logger = null;

    /** The internal configuration environment storage.  */
    private APSConfigEnvStore configEnvStore = null;

    /** The internal configuration value storage. */
    private APSConfigMemoryStore configMemoryStore = null;

    /** The internal handler for accessing configurations on disk. */
    private APSConfigPersistentStore configPersistentStore = null;

    /** The implementation of APSConfigAdminService. */
    private APSConfigAdminService configAdminService = null;

    /** Our own config model. It is loaded in the constructor! */
    private APSConfigServiceConfig config = null;

    /** The APSSync service we use for synchronizing the configuration. */
    private APSSyncService syncService = null;

    /** The net time service for translating time between hosts. */
    private APSNetTimeService netTimeService = null;

    /** This is used for creating and reading sync data. */
    private APSJSONService jsonService;

    /** APSGroups member representing us. */
    private APSSyncService.SyncGroup syncGroup = null;

    /** This thread sends the freshest config timestamp out at intervals. */
    private ConfigTimestampSendingThread configTimestampSendingThread = null;

    /** The time of the last message received. */
    private Date lastGroupMsgTimestamp = null;

    /** Since we cannot use net time for the newest config timestamp passed with all config messages
        received we have to avoid triggering a constant "FEED_ME" message due to time difference, so
        the local newest config timestamp is calculated when this values is -1, otherwise the saved
        value in this field is used. When a newer timestamp is received it is saved here. */
    private long newestConfigTimestamp = -1;

    /** Determines if we are synchronizing or not. This is used to be able to turn on or off sync
        due to configuration change. A configuration change event is received on each save even
        if no values have been changed so we use this to determine the current state. */
    private boolean synchronizing = false;

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
     * @param syncService The APSSyncService to sync with.
     * @param netTimeService The APSNetTimeService to resolve time difference between hosts with.
     * @param jsonService The JSON service to use for creating and reading sync data.
     */
    public Synchronizer(
            APSLogger logger,
            APSConfigAdminService configAdminService,
            APSConfigService configService,
            APSConfigEnvStore configEnvStore,
            APSConfigMemoryStore configMemoryStore,
            APSConfigPersistentStore configPersistentStore,
            APSSyncService syncService,
            APSNetTimeService netTimeService,
            APSJSONService jsonService
    ) {
        this.logger = logger;
        this.configAdminService = configAdminService;
        this.configEnvStore = configEnvStore;
        this.configMemoryStore = configMemoryStore;
        this.configPersistentStore = configPersistentStore;
        this.syncService = syncService;
        this.netTimeService = netTimeService;
        this.jsonService = jsonService;

        this.config = configService.getConfiguration(APSConfigServiceConfig.class);
        this.config.addConfigChangedListener(this);
    }

    public void cleanup() {
        if (this.config != null) {
            this.config.removeConfigChangedListener(this);
        }
    }

    //
    // Methods
    //

    public void start() {
        if (this.config.synchronize.toBoolean()) {
            this.synchronizing = true;

            this.syncGroup = this.syncService.joinSyncGroup(this.config.synchronizationGroup.toString());
            this.syncGroup.addMessageListener(this);
            this.syncGroup.addReSyncListener(this);
            this.configEnvStore.addUpdateListener(this);
            this.configMemoryStore.addUpdateListener(this);
            this.syncGroup.reSyncAll();
            updateLastGroupMsgTimestamp();
            this.configTimestampSendingThread = new ConfigTimestampSendingThread();
            this.configTimestampSendingThread.start();
            this.logger.info("Started config synchronizer!");
        }
    }

    public void stop() {
        if (this.config.synchronize.toBoolean()) {
            this.synchronizing = false;

            if (this.configTimestampSendingThread != null) this.configTimestampSendingThread.stopThread();
            this.configEnvStore.removeUpdateListener(this);
            this.configMemoryStore.removeUpdateListener(this);
            if (this.syncGroup != null) {
                this.syncGroup.removeMessageListener(this);
                this.syncGroup.removeReSyncListener(this);
                this.syncGroup.leaveSyncGroup();
            }
            this.logger.info("Stopped config synchronizer!");
        }
    }

    /**
     * Event listener callback when event occurs.
     *
     * @param event information about the event.
     */
    @Override
    public void apsConfigChanged(APSConfigChangedEvent event) {
        // This will be called on save of the configuration even if nothing was actually changed!
        // Thereby we keep track or and check our current state.
        if (!this.synchronizing && this.config.synchronize.toBoolean()) {
            start();
        }
        else if (this.synchronizing && !this.config.synchronize.toBoolean()) {
            stop();
        }
    }

    /**
     * This finds the freshest of all config value timestamps. This is used to compare with same value
     * of received configurations to determine if we are behind in actuality.
     */
    private long resolveNewestConfigTimestamp() {
        if (this.newestConfigTimestamp == -1) {
            long newestConfigTimestamp = 0;

            for (APSConfigAdmin configAdmin : this.configMemoryStore.getAllConfigurations()) {
                for (APSConfigValueEditModel valueEditModel : configAdmin.getConfigModel().getValues()) {
                    for (APSConfigEnvironment configEnv : this.configEnvStore.getConfigEnvironments()) {
                        long ts = configAdmin.getConfigValueTimestamp(valueEditModel, configEnv);
                        if (ts > newestConfigTimestamp) {
                            newestConfigTimestamp = ts;
                        }
                    }
                }
            }
            this.newestConfigTimestamp = newestConfigTimestamp;
        }

        return this.newestConfigTimestamp;
    }

    private Date getLastGroupMsgTimestamp() {
        return this.lastGroupMsgTimestamp;
    }

    private void updateLastGroupMsgTimestamp() {
        this.lastGroupMsgTimestamp = new Date();
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
            return Synchronizer.this.netTimeService.localToNetTime(time);
        }
    };

    /** Converts from net to local time. */
    private final TimePropertyConverter Net_2_Local_Time_Converter = new TimePropertyConverter() {
        @Override
        public long convertTime(long time) {
            return Synchronizer.this.netTimeService.netToLocalTime(time);
        }
    };

    /**
     * Converts all time properties in a whole Properties set from local to net time or net to local time.
     *
     * @param props The properties to convert.
     * @param timePropMatcher The matcher to use for identifying time properties.
     * @param timePropConverter The converter to use for converting properties.
     * @return
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
     * Creates a synchronization JSON object containing the passed config env properties.
     *
     * The object will look like this:
     *
     *     {
     *         "messageType": "CONFIG_ENV",
     *         "data": {
     *             ...
     *         }
     *     }
     *
     * @param confenv The config env properties to sync.
     */
    private JSONObject createConfEnvJSON(Properties confenv) {
        JSONObject root = this.jsonService.createJSONObject();
        root.addValue(SYNC_MESSAGE_TYPE, this.jsonService.createJSONString(MessageType.CONFIG_ENV.name()));
        JSONObject configEnvData = this.jsonService.createJSONObject();
        for (String propName : confenv.stringPropertyNames()) {
            String value = confenv.getProperty(propName);

            configEnvData.addValue(propName, this.jsonService.createJSONString(value));
        }
        root.addValue(SYNC_DATA, configEnvData);

        return root;
    }

    /**
     * Send sync config update.
     *
     * @param configEnvStore The config env store holding the updated value(s).
     */
    public void syncSend(APSConfigEnvStore configEnvStore) {
        try {
            APSSyncService.SyncGroup.Message message = this.syncGroup.createMessage();
            JSONObject confEnvJSON =
                    createConfEnvJSON(convertPropTime(configEnvStore.getAsProperties(), Config_Env_Matcher, Local_2_Net_Time_Converter));
            OutputStream msgStream = message.getOutputStream();
            this.jsonService.writeJSON(msgStream, confEnvJSON, false);
            msgStream.close();

            //logger.debug(">>>>>> length: [" + message.getBytes().length + "]");
            this.syncGroup.sendMessage(message);
        }
        catch (IOException ioe) {
            this.logger.error("Send of config env message failed: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Receives updated local configuration.
     *
     * @param configuration The updated configuration.
     */
    @Override
    public void updated(APSConfigAdmin configuration) {
        if (configuration.getConfigId().equals(APSConfigServiceConfig.CONFIG_ID)) {
            stop();
            start();
        }
        syncSend(configuration);
    }

    /**
     * Creates a synchronization JSON object for a config.
     *
     * The object will look like this:
     *
     *     {
     *         "messageType": "CONFIG",
     *         "configId": "(id)",
     *         "version": "(version)",
     *         "data": {
     *             ...
     *         }
     *     }
     *
     * @param configId The id of the config to synchronize.
     * @param configVersion The version of the config to synchronize.
     * @param config The config values of the config to synchronize.
     */
    private JSONObject createConfigJSON(String configId, String configVersion, Properties config) {
        JSONObject root = this.jsonService.createJSONObject();
        root.addValue(SYNC_MESSAGE_TYPE, this.jsonService.createJSONString(MessageType.CONFIG.name()));
        root.addValue(SYNC_CONFIG_ID, this.jsonService.createJSONString(configId));
        root.addValue(SYNC_VERSION, this.jsonService.createJSONString(configVersion));

        JSONObject configData = this.jsonService.createJSONObject();
        for (String propName : config.stringPropertyNames()) {
            String value = config.getProperty(propName);

            configData.addValue(propName, this.jsonService.createJSONString(value));
        }
        root.addValue(SYNC_DATA, configData);

        return root;
    }
    /**
     * Sends a sync config update.
     *
     * @param configuration The updated configuration.
     */
    private void syncSend(APSConfigAdmin configuration) {
        try {
            APSSyncService.SyncGroup.Message message = this.syncGroup.createMessage();
            JSONObject configJSON = createConfigJSON(
                    configuration.getConfigId(),
                    configuration.getVersion(),
                    convertPropTime(
                            ((APSConfigAdminImpl) configuration).getConfigInstanceMemoryStore().getProperties(),
                            Config_Value_Matcher,
                            Local_2_Net_Time_Converter
                    )
            );
            OutputStream msgStream = message.getOutputStream();
            this.jsonService.writeJSON(msgStream, configJSON, false);
            msgStream.close();
            //System.out.print("SENDING:");
            //this.jsonService.writeJSON(System.out, configJSON, false);

            //logger.debug(">>>>>> length: [" + message.getBytes().length + "]");
            this.syncGroup.sendMessage(message);
        }
        catch (IOException ioe) {
            this.logger.error("Send of config value message failed: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Request that all data be sent again.
     *
     * @param group The group making the request.
     */
    @Override
    public void reSyncAll(APSSyncService.SyncGroup group) {
        syncSend(this.configEnvStore);
        for (APSConfigAdmin configAdmin : this.configAdminService.getAllConfigurations()) {
            syncSend(configAdmin);
        }
    }

    /**
     * This is a default implementation.
     */
    public class ErrorHandler implements JSONErrorHandler {

        /**
         * Warns about something.
         *
         * @param message The warning message.
         */
        @Override
        public void warning(String message) {
            logger.warn(message);
        }

        /**
         * Indicate failure.
         *
         * @param message The failure message.
         * @param cause   The cause of the failure. Can be null!
         * @throws RuntimeException This method must throw a RuntimeException.
         */
        @Override
        public void fail(String message, Throwable cause) throws RuntimeException {
            logger.error(message, cause);
        }
    }

    /**
     * Called when a message is received.
     *
     * @param message The received message.
     */
    @Override
    public void receiveMessage(APSSyncService.SyncGroup.Message message) {
        updateLastGroupMsgTimestamp();

//        logger.debug("<<<<<< length: [" + message.getBytes().length + "]");
        try {
            InputStream msgStream = message.getInputStream();
            JSONObject msg = (JSONObject)this.jsonService.readJSON(msgStream, new ErrorHandler());
            msgStream.close();
            MessageType messageType = MessageType.valueOf(msg.getValue(SYNC_MESSAGE_TYPE).toString());
            switch(messageType) {
                case CONFIG:
                    handleConfigValueMessage(msg);
                    break;

                case CONFIG_ENV:
                    handleConfigEnvMessage(msg);
                    break;

                case CONFIG_TIMESTAMP:
                    handleConfigTimestamp(msg);
            }
        }
        catch (IOException ioe) {
            this.logger.error("Failed to read synchronization message!", ioe);
        }
        catch (ClassNotFoundException cnfe) {
            this.logger.error("Message contained data of unknown type!", cnfe);
        }
    }

    /**
     * Handles a received config timestamp and triggers a re-sync if being behind.
     *
     * @param msgStream The message stream to read timestamp from.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void handleConfigTimestamp(JSONObject msg) throws IOException, ClassNotFoundException {
        long timeStamp = this.netTimeService.netToLocalTime((long)((JSONNumber)msg.getValue(SYNC_DATA)).toNumber());
        long localTimeStamp = resolveNewestConfigTimestamp();
        if (timeStamp > (localTimeStamp + 2000)) {
            this.syncGroup.reSyncAll();
        }
    }

    /**
     * Handles a received config value message.
     *
     * @param msgStream The received message stream.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void handleConfigValueMessage(JSONObject msg) throws IOException, ClassNotFoundException {
        String configId = msg.getValue(SYNC_CONFIG_ID).toString();
        String version = msg.getValue(SYNC_VERSION).toString();
        Properties props = new Properties();
        JSONObject data = (JSONObject)msg.getValue(SYNC_DATA);
        for (JSONString name : data.getValueNames()) {
            JSONValue value = data.getValue(name);
            props.put(name.toString(), value.toString());
        }
        props = convertPropTime(props, Config_Value_Matcher, Net_2_Local_Time_Converter);

        mergeConfigValues(configId, version, props);
    }

    /**
     * Merge received config properties with local.
     *
     * @param configId Id of received configuration.
     * @param version Version of received configuration.
     * @param msgProps The received configuration properties.
     */
    private void mergeConfigValues(String configId, String version, Properties msgProps) {
        APSConfigAdminImpl localConfigAdmin = null;

        synchronized (this.configAdminService) {
            localConfigAdmin = (APSConfigAdminImpl)this.configAdminService.getConfiguration(configId, version);
        }

        if (localConfigAdmin != null) {
            Map<String,String> listPropKeys = new HashMap<>();

            Properties localProps = localConfigAdmin.getConfigInstanceMemoryStore().getProperties();
            boolean updated = false;
            for (String propName : msgProps.stringPropertyNames()) {
                if (!propName.endsWith("_time")) {
                    String propValue = msgProps.getProperty(propName);

                    if (propName.endsWith("_size")) {
                        listPropKeys.put(propName.substring(0, propName.length() - 5), propValue);
                    }

                    String msgTSValue = msgProps.getProperty(propName + "_time");
                    long msgTS = msgTSValue != null ? Long.valueOf(msgTSValue) : 0;

                    String localTSValue = localProps.getProperty(propName + "_time");
                    long localTS = localTSValue != null ? Long.valueOf(localTSValue) : 0;

                    if ((msgTS > localTS || localTS == 0) && propValue != null) {
                        localProps.setProperty(propName, propValue);
                        if (msgTSValue != null) {
                            localProps.setProperty(propName + "_time", msgTSValue);
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
     * @param msgStream The received message stream.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void handleConfigEnvMessage(JSONObject msg) throws IOException, ClassNotFoundException {
        Properties props = new Properties();
        JSONObject data = (JSONObject)msg.getValue(SYNC_DATA);
        for (JSONString name : data.getValueNames()) {
            JSONValue value = data.getValue(name);
            props.put(name.toString(), value.toString());
        }

        //Properties msgProps = convertPropTime((Properties)msgStream.readObject(), Config_Env_Matcher, Net_2_Local_Time_Converter);
        mergeConfigEnvs(props);
    }

    /**
     * Merge received config env with local.
     * @param msgProps
     */
    private void mergeConfigEnvs(Properties msgProps) {
        Properties localEnvProps = null;

        synchronized (this.configEnvStore) {
            localEnvProps = this.configEnvStore.getAsProperties();
        }

        String localEnvsStr = localEnvProps.getProperty("envs");
        int localEnvs = localEnvsStr != null ? Integer.valueOf(localEnvsStr) : 0;

        String envsStr = msgProps.getProperty("envs");
        int envs = envsStr != null ? Integer.valueOf(envsStr).intValue() : 0;
        boolean updated = false;
        for (int i = 0; i < envs; i++) {
            String name = msgProps.getProperty("name_" + i);
            String time = msgProps.getProperty("time_" + i);
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
                localEnvProps.setProperty("desc_" + localEnvs, msgProps.getProperty("desc_" + i));
                localEnvProps.setProperty("time_" + localEnvs, "" + msgTs);
                localEnvProps.setProperty("envs", "" + localEnvs + 1);
                updated = true;
            }
            else {
                String localTime = localEnvProps.getProperty("time_" + localIx);
                long localTs = localTime != null ? Long.valueOf(localTime) : 0;

                if (msgTs > localTs || localTs == 0) {
                    localEnvProps.setProperty("name_" + localIx, name);
                    localEnvProps.setProperty("desc_" + localIx, msgProps.getProperty("desc_" + i));
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

    /**
     * Returns the time of the last sync message received.
     */
    @Override
    public String getLastMessageTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getLastGroupMsgTimestamp());
    }

    /**
     * Trigger this service to request updates from other nodes.
     * <p/>
     * A response or potential error message is returned.
     */
    @Override
    public String requestUpdate() {
        //this.syncGroup.reSyncAll();
        return "Requested re-sync of all config!";
    }

    //
    // Inner Classes
    //

    /**
     * Defines the different message types.
     */
    private enum MessageType {
        /** The message is a configuration environment.  */
        CONFIG_ENV,

        /** The message is a configuration. */
        CONFIG,

        /** A timestamp of the freshest config. */
        CONFIG_TIMESTAMP;
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

    /**
     * Sends messages with freshest config timestamp. This so that nodes can realize if they are behind.
     */
    private class ConfigTimestampSendingThread extends Thread {

        private boolean done = false;

        public synchronized void stopThread() {
            this.done = true;
        }

        private synchronized boolean isDone() {
            return this.done;
        }

        public void run() {
            while (!isDone()) {

                try {
                    Thread.sleep(1000 * 60);
                }
                catch (InterruptedException ie) {
                    Synchronizer.this.logger.error("Unexpected interrupt of sleep!", ie);
                }

                sendTimestampMessage();
            }
        }
    }

    private void sendTimestampMessage() {
        try {
            APSSyncService.SyncGroup.Message message = this.syncGroup.createMessage();
            JSONObject root = this.jsonService.createJSONObject();
            root.addValue(SYNC_MESSAGE_TYPE, this.jsonService.createJSONString(MessageType.CONFIG_TIMESTAMP.name()));
            root.addValue(SYNC_DATA, this.jsonService.createJSONNumber(this.netTimeService.localToNetTime(resolveNewestConfigTimestamp())));
            OutputStream msgStream = message.getOutputStream();
            this.jsonService.writeJSON(msgStream, root, false);
            msgStream.close();

//            ObjectOutputStream msgStream = new ObjectOutputStream(message.getOutputStream());
//            msgStream.writeObject(MessageType.CONFIG_TIMESTAMP);
//            msgStream.writeLong(this.netTimeService.localToNetTime(resolveNewestConfigTimestamp()));
//            msgStream.close();
            this.syncGroup.sendMessage(message);
        }
        catch (IOException ioe) {
            this.logger.error("Failed to send timestamp message!", ioe);
        }
    }
}
