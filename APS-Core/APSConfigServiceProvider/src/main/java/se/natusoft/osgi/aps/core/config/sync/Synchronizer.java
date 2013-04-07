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
            msgStream.writeObject(configEnvStore.getAsProperties());
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
            msgStream.writeObject(((APSConfigAdminImpl)configuration).getConfigInstanceMemoryStore().getProperties());
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
        Properties props = (Properties)msgStream.readObject();

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
        Properties envProps = (Properties)msgStream.readObject();
        mergeConfigEnvs(envProps);
    }

    /**
     * Merge received config env with local.
     * @param envProps
     */
    private void mergeConfigEnvs(Properties envProps) {
        Properties localEnvProps = this.configEnvStore.getAsProperties();
        String localEnvsStr = localEnvProps.getProperty("envs");
        int localEnvs = localEnvsStr != null ? Integer.valueOf(localEnvsStr) : 0;

        String envsStr = envProps.getProperty("envs");
        int envs = envsStr != null ? Integer.valueOf(envsStr).intValue() : 0;
        boolean updated = false;
        for (int i = 0; i < envs; i++) {
            String name = envProps.getProperty("name_" + i);
            String time = envProps.getProperty("time_" + i);
            long ts = time != null ? Long.valueOf(time) : 0;

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
                localEnvProps.setProperty("desc_" + localEnvs, envProps.getProperty("desc_" + i));
                localEnvProps.setProperty("time_" + localEnvs, "" + ts);
                localEnvProps.setProperty("envs", "" + localEnvs + 1);
                updated = true;
            }
            else {
                String localTime = localEnvProps.getProperty("time_" + localIx);
                long localTs = localTime != null ? Long.valueOf(localTime) : 0;

                if (ts > localTs || localTs == 0) {
                    localEnvProps.setProperty("name_" + localIx, name);
                    localEnvProps.setProperty("desc_" + localIx, envProps.getProperty("desc_" + i));
                    localEnvProps.setProperty("time_" + localIx, "" + ts);
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

    private enum MessageType {
        /** The message is a configuration environment.  */
        CONFIG_ENV,

        /** The message is a configuration. */
        CONFIG,

        /** The message is a request to get all data. */
        FEED_ME
    }

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
