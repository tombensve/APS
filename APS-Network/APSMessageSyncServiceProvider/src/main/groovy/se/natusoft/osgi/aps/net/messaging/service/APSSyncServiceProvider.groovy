/*
 *
 * PROJECT
 *     Name
 *         APS Message Sync Service Provider
 *
 *     Code Version
 *         1.0.0
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
 *         2015-01-09: Created!
 *
 */
package se.natusoft.osgi.aps.net.messaging.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.messages.APSBinaryMessage
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.api.net.messaging.service.APSSyncService
import se.natusoft.osgi.aps.api.net.messaging.types.APSCommonDateTime
import se.natusoft.osgi.aps.api.net.messaging.types.APSReSyncEvent
import se.natusoft.osgi.aps.api.net.messaging.types.APSSyncDataEvent

import se.natusoft.osgi.aps.api.net.messaging.types.APSSyncEvent
import se.natusoft.osgi.aps.codedoc.Implements
import se.natusoft.osgi.aps.net.messaging.config.SyncServiceConfig
import se.natusoft.osgi.aps.net.messaging.messages.CommonTimeRequestMessage
import se.natusoft.osgi.aps.net.messaging.messages.CommonTimeValueMessage
import se.natusoft.osgi.aps.net.messaging.messages.ResyncMessage
import se.natusoft.osgi.aps.net.messaging.messages.SyncDataMessage
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStart
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider
import se.natusoft.osgi.aps.tools.apis.APSActivatorSearchCriteriaProvider
import se.natusoft.osgi.aps.tools.apis.APSActivatorServiceSetupProvider
import se.natusoft.osgi.aps.tools.apis.ServiceSetup

/**
 * This is an implementation of APSSyncService that uses APSMessageClusterService
 * to sync. Each instance represents one cluster member.
 *
 * Each service instance will be registered with the following properties:
 *
 *     aps-sync-provider=aps-message-service-sync-provider
 *     aps-sync-instance-name=<configured instance name>
 */
@CompileStatic
@TypeChecked
@OSGiServiceProvider(serviceSetupProvider = SetupProvider.class, threadStart = true)
public class APSSyncServiceProvider implements APSActivatorSearchCriteriaProvider.SearchCriteriaProviderFactory, APSSyncService,
        APSMessageService.APSMessageListener {

    public static class SetupProvider implements APSActivatorServiceSetupProvider {

        /**
         * Provides setup for each instance to create and register.
         *
         * Each returned Setup instance will result in one registered service instance.
         *
         * Each Properties instance should contain a common property with different values
         * that can be searched for during service lookup/tracking.
         */
        @SuppressWarnings("UnnecessaryQualifiedReference") // This warning is BS and have been reported to JetBrains.
        @Override
        List<ServiceSetup> provideServiceInstancesSetup() {
            List<ServiceSetup> instances = new LinkedList<>()

            SyncServiceConfig.managed.get().instances.each { SyncServiceConfig.SyncInstance syncInstance ->
                ServiceSetup setup = new ServiceSetup(
                        serviceInstance: new APSSyncServiceProvider(messageInstanceName: syncInstance.messageInstanceName.string)
                )
                instances += setup

                setup.props = [
                        "${APSSyncService.SYNC_INSTANCE_NAME}": syncInstance.name.string,
                        "${APSSyncService.SYNC_PROVIDER}": "aps-message-service-sync-provider"
                ]

                setup.serviceAPIs += APSSyncService.class.name
            }

            return instances
        }
    }

    public class SearchCriteriaProvider implements APSActivatorSearchCriteriaProvider {

        /**
         * This should return a String starting with '(' and ending with ')'. The final ServiceListener
         * criteria will be (&(objectClass=MyService)_providedSearchCriteria()_)
         *
         * Whatever is returned it will probably  reference a property and a value that the service you
         * are looking for where registered with.
         */
        @Override
        String provideSearchCriteria() {
            return "(" + APSMessageService.APS_MESSAGE_SERVICE_INSTANCE_NAME + "=" + messageInstanceName + ")"
        }
    }

    /**
     * Returns an instance of an APSActivatorSearchCriteriaProvider.
     */
    @Override
    APSActivatorSearchCriteriaProvider createSearchCriteriaProvider() {
        return new SearchCriteriaProvider()
    }

    //
    // Private Members
    //

    /** Handles common date and time. */
    private APSCommonDateTime.Default commonDateTime = new APSCommonDateTime.Default()

    /** The logger to log to. */
    @Managed(loggingFor = "aps-message-sync-service-provider")
    private APSLogger logger

    /** The message service to use. */
    @OSGiService(timeout = "30 seconds", searchCriteriaProvider = SearchCriteriaProvider.class)
    private APSMessageService messageService

    /** Our time sender thread so that we can join it on shutdown. */
    private Thread timeSenderThread

    /** Our master status. */
    private boolean master = false;

    /** A flag used to see if we have received a common time from someone else. */
    private boolean commonTimeValueReceived = false;

    /** The last time we received a time value. */
    private long lastTimeValueTime = 0

    /** The time sending thread will be running as long as this is true. */
    private boolean timeSenderRunning = true

    /** Our sync listeners. */
    private List<APSSyncService.APSSyncListener> syncListeners = new LinkedList<>()

    //
    // Properties
    //

    /** The name of the APSMessageService instance to use. */
    String messageInstanceName

    //
    // Methods
    //

    @BundleStart(thread = true)
    public void startup() {
        this.messageService.addMessageListener(this)

        this.commonTimeValueReceived = false
        this.messageService.sendMessage(new CommonTimeRequestMessage())
        Thread.sleep(1000 * 15)

        if (!this.commonTimeValueReceived) {
            this.master = true
            sendTimeMessage()
            this.logger.info("Made myself master at startup due to lack of replies from others!")
        }

        this.timeSenderThread = Thread.start {
            timeSender()
        }
        this.timeSenderThread.name = "aps-sync-time-sender-thread"
    }

    @BundleStop
    public void shutdown() {
        this.messageService.removeMessageListener(this)
        stopTimeSender()
        try {
            this.timeSenderThread.join(10000)
        }
        catch (InterruptedException ie) {
            this.logger.error("The time sender thread did not stop within 10 seconds!", ie)
        }
    }

    /**
     * Returns the network common DateTime that is independent of local machine times.
     */
    @Override
    @Implements(APSSyncService.class)
    public APSCommonDateTime getCommonDateTime() {
        return this.commonDateTime
    }

    /**
     * Synchronizes data.
     *
     * @param syncEvent The sync event to send.
     * @throws APSMessagingException on failure.
     */
    @Override
    @Implements(APSSyncService.class)
    public void syncData(APSSyncDataEvent syncEvent) throws APSMessagingException {
        this.messageService.sendMessage(new SyncDataMessage(syncEvent: syncEvent))
    }

    /**
     * Makes all members resync everything.
     */
    @Override
    @Implements(APSSyncService.class)
    public void resync() {
        this.messageService.sendMessage(new ResyncMessage(resyncEvent: new APSReSyncEvent.Default(ResyncMessage.ALL_KEYS)))
    }

    /**
     * Makes all members resync the specified key.
     *
     * @param key The key to resync.
     */
    @Override
    @Implements(APSSyncService.class)
    public void resync(String key) {
        this.messageService.sendMessage(new ResyncMessage(resyncEvent: new APSReSyncEvent.Default(key)))
    }

    /**
     * Adds a synchronization listener.
     *
     * @param listener The listener to add.
     */
    @Override
    @Implements(APSSyncService.class)
    public synchronized void addSyncListener(APSSyncService.APSSyncListener listener) {
        this.syncListeners.add(listener)
    }

    /**
     * Removes a synchronization listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    @Implements(APSSyncService.class)
    public synchronized void removeSyncListener(APSSyncService.APSSyncListener listener) {
        this.syncListeners.remove(listener)
    }

    /**
     * Sends an APSSyncEvent to all listeners.
     *
     * @param syncEvent The sync event to send.
     */
    private void sendToListeners(APSSyncEvent syncEvent) {
        int count = 1
        this.syncListeners.each { APSSyncService.APSSyncListener listener ->
            // Since we are calling code we have no control over and don't know what it does, we play is safe.
            // Every listener will get its callback even if one or more does something stupid and puts itself
            // in a deadlock or something else very time consuming. For example do a REST call to fetch 20000+ lines
            // of data just to count them. (Yes, that example came from reality! The person that did this also had
            // access to the REST service code which had access to the database, but he thought it better to fetch
            // all data to the client just to count it rather than adding a service that asked the database for
            // the answer :-))
            //
            // So we protect ourselves and each callback from the idiots out there, they do exist!
            Thread.start {
                try {
                    listener.syncDataReceived(syncEvent)
                }
                catch (Throwable t) {
                    this.logger.error("Failed to update listener!", t)
                }
            }.name = "sync-service-update-listeners-thread-" + count++
        }
    }

    /**
     * This is called when a message is received.
     *
     * @param message The received message.
     */
    @Override
    @Implements(APSMessageService.APSMessageListener.class)
    void messageReceived(byte[] message) {
        String type = null
        if (SyncServiceConfig.managed.get().validateSenderUUID.boolean) {
            type = APSBinaryMessage.getType(message, this.messageService.providerUUID)
        }
        else {
            type = APSBinaryMessage.getType(message)
        }
        switch(type) {
            case CommonTimeValueMessage.MESSAGE_TYPE:
                CommonTimeValueMessage commonTimeValueMessage = new CommonTimeValueMessage(bytes: message)
                this.lastTimeValueTime = this.commonDateTime.currentLocalDateTime
                this.commonTimeValueReceived = true
                this.master = false
                this.commonDateTime.update(commonTimeValueMessage.dateTime)
                break

            case CommonTimeRequestMessage.MESSAGE_TYPE:
                if (this.master) {
                    sendTimeMessage()
                }
                break

            case SyncDataMessage.MESSAGE_TYPE:
                SyncDataMessage syncDataMessage = new SyncDataMessage(bytes: message)
                sendToListeners(syncDataMessage.syncEvent)
                break

            case ResyncMessage.MESSAGE_TYPE:
                ResyncMessage resyncMessage = new ResyncMessage(bytes: message)
                sendToListeners(resyncMessage.resyncEvent)
                break

            default:
                this.logger.error("Unknown message type received: " + type)
        }
    }

    /**
     * Sends a new common time message.
     */
    private synchronized void sendTimeMessage() {
        this.messageService.sendMessage(new CommonTimeValueMessage(dateTime: this.commonDateTime.currentCommonDateTime))
    }

    /**
     * Stops the time sender thread.
     */
    private synchronized stopTimeSender() {
        this.timeSenderRunning = false
    }

    /**
     * Used by the time sender loop to check if it should continue to run.
     */
    private synchronized runTimeSender() {
        return this.timeSenderRunning
    }

    /**
     * Runs the time sender.
     */
    private void timeSender() {
        long send = 0
        this.logger.info("Starting time sender!")
        while (runTimeSender()) {
            checkIfTimeValueHaveBeenReceived()
            Thread.sleep(1000 * 5)
            ++send
            if (send >= 60) {
                send = 0
                if (this.master) {
                    sendTimeMessage()
                }
            }
        }
        this.logger.info("Time sender stopped!")
    }

    /**
     * If we don't receive time in reasonable time we want to fight for mastership.
     */
    private void checkIfTimeValueHaveBeenReceived() {
        long now = this.commonDateTime.currentLocalDateTime
        if (this.lastTimeValueTime != 0 && (now - this.lastTimeValueTime) > ((1000 * 60 * 5) + 20000)) {
            // We have not received any time value for 5.20 seconds. It should be sent out every 5 by the master.
            // So we do a sleep for a random time and then send a time message ourselves. The random
            // wait inhibits all members to fight for the mastership at the same time. The member with
            // the shortest random sleep time will become the new master.
            Random random = new Random(now)
            Thread.sleep(random.nextInt(5000))

            now = this.commonDateTime.currentLocalDateTime
            if (this.lastTimeValueTime != 0 && (now - this.lastTimeValueTime) > ((1000 * 60 * 5) + 15000)) {
                sendTimeMessage()
                this.master = true
                this.logger.info("Became master!")
            }
        }
    }
}
