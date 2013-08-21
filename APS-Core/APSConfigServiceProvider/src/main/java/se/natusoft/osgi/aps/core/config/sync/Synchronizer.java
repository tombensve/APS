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
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.api.net.groups.service.GroupMember;
import se.natusoft.osgi.aps.api.net.groups.service.Message;
import se.natusoft.osgi.aps.api.net.groups.service.MessageListener;
import se.natusoft.osgi.aps.core.config.api.APSConfigSyncMgmtService;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigAdminImpl;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Responsible for synchronizing with other installations.
 */
public class Synchronizer implements APSConfigSyncMgmtService, APSConfigMemoryStore.ConfigUpdateListener, APSConfigEnvStore.ConfigEnvUpdateListener, MessageListener {
    //
    // Constants
    //

    private static final String NEWEST_CONFIG_TIMESTAMP = "newest-config-timestamp";


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

    private LongTimeNoSeeThread longTimeNoSeeThread = null;

    private Date lastGroupMsgTimestamp = null;

    private long newestConfigTimestamp = -1;

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
        Properties memberProps = new Properties();
        memberProps.setProperty(NEWEST_CONFIG_TIMESTAMP, "" + resolveNewestConfigTimestamp());
        this.groupMember = this.groupsService.joinGroup("aps-config-synchronizer", memberProps);
        this.groupMember.addMessageListener(this);
        this.configEnvStore.addUpdateListener(this);
        this.configMemoryStore.addUpdateListener(this);
        sendFeedMe();
        updateLastGroupMsgTimestamp();
        this.longTimeNoSeeThread = new LongTimeNoSeeThread();
        this.longTimeNoSeeThread.start();
        this.logger.info("Started synchronizer!");
    }

    public void stop() throws IOException {
        if (this.longTimeNoSeeThread != null) this.longTimeNoSeeThread.stopThread();
        this.configEnvStore.removeUpdateListener(this);
        this.configMemoryStore.removeUpdateListener(this);
        if (this.groupMember != null) this.groupMember.removeMessageListener(this);
        this.groupsService.leaveGroup(this.groupMember);
        this.logger.info("Stopped synchronizer!");
    }

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

    // ---- FEED_ME handling start ---- //

    /**
     * Sends a "FEED_ME" message to all other installation which will trigger then to send all their configuration which
     * will be received and merged by all other. The more installations the more messages will be received and in the end
     * all should have the same data.
     */
    private void sendFeedMe() {
        this.sendFeedMeThread = new SendFeedMeThread();
        this.sendFeedMeThread.start();
    }

    private SendFeedMeThread sendFeedMeThread = null;

    private void cancelFeedMe() {
        if (this.sendFeedMeThread != null) {
            this.sendFeedMeThread.cancel();
        }
    }

    /**
     * This sends the FEED_ME message in a separate thread with some delay.
     */
    private class SendFeedMeThread extends Thread {
        private boolean run = true;

        public synchronized void cancel() {
            this.run = false;
        }

        private synchronized boolean isRun() {
            return this.run;
        }

        public void run() {
            try {
                Random random = new Random(new Date().getTime());
                try {
                    // Wait between 5 to 15 seconds. This is an attempt to avoid several nodes sending a "feed me"
                    // simultaneously. When a "feed me" message is received cancel is done on this thread.
                    Thread.sleep(random.nextInt(10000) + 5000);
                }
                catch (InterruptedException ie) {
                    Synchronizer.this.logger.error("Unexpectedly interrupted from sleep!", ie);
                }

                if (isRun()) {
                    _sendFeedMe();
                }
            }
            catch (Exception e) {
                Synchronizer.this.logger.error("Failed to send FEED_ME message!", e);
            }
        }
    }

    /**
     * Sends a "FEED_ME" message to all other installation which will trigger then to send all their configuration which
     * will be received and merged by all other. The more installations the more messages will be received and in the end
     * all should have the same data.
     */
    private void _sendFeedMe() {
        try {
            Message message = this.groupMember.createNewMessage();
            ObjectOutputStream msgStream = new ObjectOutputStream(message.getOutputStream());
            msgStream.writeObject(MessageType.FEED_ME);
            msgStream.close();
            MessageSendThread mst = new MessageSendThread(this.groupMember, message, this.logger, "Send of feed me message failed: ");
            mst.start();
            this.logger.info("Sent 'FEED_ME' message!");
        }
        catch (IOException ioe) {
            this.logger.error("Send of config env message failed: " + ioe.getMessage(), ioe);
        }
    }

    // ---- FEED_ME handling end ---- //

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
        updateLastGroupMsgTimestamp();

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
                        // If some other node came first with a "feed me" then we don't need to do it too
                        // since whoever sent it will not have the latest and we will receive the updates
                        // from the other nodes too.
                        cancelFeedMe();

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
    private void handleConfigEnvMessage(ObjectInputStream msgStream) throws IOException, ClassNotFoundException {
        Properties msgProps = convertPropTime((Properties)msgStream.readObject(), Config_Env_Matcher, Net_2_Local_Time_Converter);
        mergeConfigEnvs(msgProps);
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
     * Send all config data to all members.
     */
    private void handleFeedMeMessage() {
        updated(this.configEnvStore);
        for (APSConfigAdmin configAdmin : this.configAdminService.getAllConfigurations()) {
            updated(configAdmin);
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
        try {
            sendFeedMe();
            return "Sent request to be updated!";
        }
        catch (Exception e) {
            return "ERROR: " + e.getMessage();
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

    /**
     * Sends a new FEED_ME message if we have older config than other members.
     */
    private class LongTimeNoSeeThread extends Thread {

        private boolean done = false;

        public synchronized void stopThread() {
            this.done = true;
        }

        private synchronized boolean isDone() {
            return this.done;
        }

        public void run() {
            int count = 0;
            while (!isDone()) {

                try {
                    Thread.sleep(1000 * 15);
                }
                catch (InterruptedException ie) {
                    Synchronizer.this.logger.error("Unexpected interrupt of sleep!", ie);
                }
                ++count;

                if (count >= 20 && !isDone()) {
                    count = 0;
                    long localNewestTimestamp = resolveNewestConfigTimestamp();
                    long newestTimestamp = 0;

                    for (Properties memberProps : Synchronizer.this.groupMember.getMembersUserProperties()) {
                        String val = memberProps.getProperty(NEWEST_CONFIG_TIMESTAMP);
                        if (val != null) {
                            try {
                                long ts = Long.valueOf(val);
                                if (ts > newestTimestamp) {
                                    newestTimestamp = ts;
                                }
                            }
                            catch (NumberFormatException nfe) {}
                        }
                    }

                    if (newestTimestamp > localNewestTimestamp) {
                        // This to avoid this always happening due to time difference. Since the timestamp is
                        // calculated before we join our group and this value is passed on join we cannot use
                        // net time to remove time differences!
                        Synchronizer.this.newestConfigTimestamp = newestTimestamp;
                        Synchronizer.this.logger.info("Found fresher config at other members!");
                        sendFeedMe();
                    }
                }
            }
        }
    }
}
