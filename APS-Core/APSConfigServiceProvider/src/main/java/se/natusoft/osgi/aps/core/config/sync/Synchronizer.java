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

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.api.net.groups.service.GroupMember;
import se.natusoft.osgi.aps.api.net.groups.service.Message;
import se.natusoft.osgi.aps.api.net.groups.service.MessageListener;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigAdminImpl;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Responsible for synchronizing with other installations.
 */
public class Synchronizer implements APSConfigMemoryStore.ConfigUpdateListener, APSConfigEnvStore.ConfigEnvUpdateListener, MessageListener {
    //
    // Private Members
    //

    APSLogger logger = null;

    private APSConfigEnvStore configEnvStore = null;

    private APSConfigMemoryStore configMemoryStore = null;

    private APSConfigPersistentStore configPersistentStore = null;

    private APSConfigAdminService configAdminService = null;

    private APSGroupsService groupsService = null;

    private GroupMember groupMember = null;

    //
    // Constructors
    //

    /**
     * Creates a new Synchronizer.
     *
     * @param logger The logger to log to.
     * @param configAdminService The local APSConfigAdminService instance.
     * @param configEnvStore The local APSConfigEnvStore.
     * @param configMemoryStore The memory store to listen for changes on.
     * @param configPersistentStore For persisting changes.
     * @param groupsService The APSGroupsService to sych with.
     */
    public Synchronizer(
            APSLogger logger,
            APSConfigAdminService configAdminService,
            APSConfigEnvStore configEnvStore,
            APSConfigMemoryStore configMemoryStore,
            APSConfigPersistentStore configPersistentStore,
            APSGroupsService groupsService
    ) {
        this.logger = logger;
        this.configAdminService = configAdminService;
        this.configEnvStore = configEnvStore;
        this.configMemoryStore = configMemoryStore;
        this.configPersistentStore = configPersistentStore;
        this.groupsService = groupsService;
    }

    //
    // Methods
    //

    public void start() throws IOException {
        this.groupMember = this.groupsService.joinGroup("aps-config-synchronizer");
        this.groupMember.addMessageListener(this);
        this.configEnvStore.addUpdateListener(this);
        this.configMemoryStore.addUpdateListener(this);
        sendFeedMe();
        this.logger.info("Started synchronizer!");
    }

    public void stop() throws IOException {
        this.configEnvStore.removeUpdateListener(this);
        this.configMemoryStore.removeUpdateListener(this);
        this.groupMember.removeMessageListener(this);
        this.groupsService.leaveGroup(this.groupMember);
        this.logger.info("Stopped synchronizer!");
    }

    /**
     * Sends a "FEED_ME" message to all other installation which will trigger then to send all their configuration which
     * will be received and merged by all other. The more installations the more messages will be received and in the end
     * all should have the same data.
     */
    private void sendFeedMe() {
        try {
            Message message = this.groupMember.createNewMessage();
            ObjectOutputStream msgStream = new ObjectOutputStream(message.getOutputStream());
            msgStream.writeObject(MessageType.FEED_ME);
            msgStream.close();
            MessageSendThread mst = new MessageSendThread(this.groupMember, message, this.logger, "Send of feed me message failed: ");
            mst.start();
        }
        catch (IOException ioe) {
            this.logger.error("Send of config env message failed: " + ioe.getMessage(), ioe);
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
            return Synchronizer.this.groupMember.createFromLocalTime(time).getNetTimeDate().getTime();
        }
    };

    /** Converts from net to local time. */
    private final TimePropertyConverter Net_2_Local_Time_Converter = new TimePropertyConverter() {
        @Override
        public long convertTime(long time) {
            return Synchronizer.this.groupMember.createFromNetTime(time).getLocalTimeDate().getTime();
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
            newProps.put(propName, propValue);

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
        try {
            Message message = this.groupMember.createNewMessage();
            ObjectOutputStream msgStream = new ObjectOutputStream(message.getOutputStream());
            msgStream.writeObject(MessageType.CONFIG_ENV);
            msgStream.writeObject(convertPropTime(configEnvStore.getAsProperties(), Config_Env_Matcher, Local_2_Net_Time_Converter));
            msgStream.close();
            MessageSendThread mst = new MessageSendThread(this.groupMember, message, this.logger, "Send of config env message failed: ");
            mst.start();
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
        try {
            Message message = this.groupMember.createNewMessage();
            ObjectOutputStream msgStream = new ObjectOutputStream(message.getOutputStream());
            msgStream.writeObject(MessageType.CONFIG);
            msgStream.writeUTF(configuration.getConfigId());
            msgStream.writeUTF(configuration.getVersion());
            msgStream.writeObject(
                convertPropTime(
                        ((APSConfigAdminImpl) configuration).getConfigInstanceMemoryStore().getProperties(),
                        Config_Value_Matcher,
                        Local_2_Net_Time_Converter
                )
            );
            msgStream.close();
            MessageSendThread mst = new MessageSendThread(this.groupMember, message, this.logger, "Send of config value message failed: ");
            mst.start();
        }
        catch (IOException ioe) {
            this.logger.error("Send of config value message failed: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Receive of configuration from other installation.
     *
     * @param message The received message.
     */
    @Override
    public void messageReceived(Message message) {
        this.logger.info("Received message from '" + message.getMemberId() + "' with id '" + message.getId() + "'!");
        if (!message.getMemberId().equals(this.groupMember.getMemberId())) {
            ObjectInputStream msgStream = null;

            try {
                msgStream = new ObjectInputStream(message.getInputStream());
                MessageType messageType = (MessageType)msgStream.readObject();
                switch(messageType) {
                    case CONFIG:
                        handleConfigValueMessage(msgStream);
                        break;

                    case CONFIG_ENV:
                        handleConfigEnvMessage(msgStream);
                        break;

                    case FEED_ME:
                        handleFeedMeMessage();
                }
            }
            catch (IOException ioe) {
                this.logger.debug("Failed to read message with id '" + message.getId() + "' from member '" + message.getMemberId() + "':" +
                ioe.getMessage(), ioe);
            }
            catch (ClassNotFoundException cnfe) {
                this.logger.debug("Failed to read message with id '" + message.getId() + "' from member '" + message.getMemberId() + "':" +
                        cnfe.getMessage(), cnfe);
            }
            finally {
                if (msgStream != null) {
                    try {
                        msgStream.close();
                    }
                    catch (IOException ioe2) {
                        this.logger.error("Failed to close received message!", ioe2);
                    }
                }
            }
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
    private void handleConfigValueMessage(ObjectInputStream msgStream) throws IOException, ClassNotFoundException {
        String configId = msgStream.readUTF();
        String version = msgStream.readUTF();
        Properties props = convertPropTime((Properties)msgStream.readObject(), Config_Value_Matcher, Net_2_Local_Time_Converter);

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
        APSConfigAdminImpl localConfigAdmin = (APSConfigAdminImpl)this.configAdminService.getConfiguration(configId, version);
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
                    long msgTS = msgTSValue != null ? Long.valueOf(msgTSValue).longValue() : 0;

                    String localTSValue = localProps.getProperty(propName + "_time");
                    long localTS = localTSValue != null ? Long.valueOf(localTSValue).longValue() : 0;

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

            if (updated) this.configPersistentStore.saveConfiguration(localConfigAdmin);
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
    private void handleConfigEnvMessage(ObjectInputStream msgStream) throws IOException, ClassNotFoundException {
        Properties msgProps = convertPropTime((Properties)msgStream.readObject(), Config_Env_Matcher, Net_2_Local_Time_Converter);
        mergeConfigEnvs(msgProps);
    }

    /**
     * Merge received config env with local.
     * @param msgProps
     */
    private void mergeConfigEnvs(Properties msgProps) {
        Properties localEnvProps = this.configEnvStore.getAsProperties();
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
            this.configEnvStore.setFromProperties(localEnvProps);
            this.configEnvStore.saveConfigEnvironments();
        }
    }

    /**
     * Send all config data to all members.
     */
    private void handleFeedMeMessage() {
        updated(this.configEnvStore);
        for (APSConfigAdmin configAdmin : this.configAdminService.getAllConfigurations()) {
            updated(configAdmin);
        }
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

        /** The message is a request to get all data. */
        FEED_ME
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
     * Sends messages off in the background.
     */
    private class MessageSendThread extends Thread {
        //
        // Private Members
        //

        private GroupMember groupMember = null;
        private Message message = null;
        private APSLogger logger = null;
        private String errorText = null;

        //
        // Constructors
        //

        public MessageSendThread(GroupMember groupMember, Message message, APSLogger logger, String errorText) {
            this.groupMember = groupMember;
            this.message = message;
            this.logger = logger;
            this.errorText = errorText;
        }

        public void run() {
            try {
                this.groupMember.sendMessage(this.message);
            }
            catch (IOException ioe ) {
                this.logger.error(this.errorText + ioe.getMessage(), ioe);
            }
        }
    }
}
