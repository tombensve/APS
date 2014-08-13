/* 
 * 
 * PROJECT
 *     Name
 *         APS Discovery Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This is a simple discovery service to discover other services on the network.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2011-10-16: Created!
 *         
 */
package se.natusoft.osgi.aps.discovery.service.provider;

import se.natusoft.osgi.aps.api.core.platform.service.APSPlatformService;
import se.natusoft.osgi.aps.api.net.discovery.exception.APSDiscoveryPublishException;
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.api.net.groups.service.GroupMember;
import se.natusoft.osgi.aps.api.net.groups.service.Message;
import se.natusoft.osgi.aps.api.net.groups.service.MessageListener;
import se.natusoft.osgi.aps.discovery.model.ServiceDescriptions;
import se.natusoft.osgi.aps.discovery.service.protocol.DiscoveryProtocol;
import se.natusoft.osgi.aps.discovery.service.protocol.Protocol;
import se.natusoft.osgi.aps.discovery.service.protocol.Publish;
import se.natusoft.osgi.aps.discovery.service.protocol.UnPublish;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The discovery service Implementation.
 */
public class APSSimpleDiscoveryServiceProvider extends Thread implements APSSimpleDiscoveryService, MessageListener {
    //
    // Private Members
    //

    // Local service implementation data.

    /** The services published locally by clients of this service. */
    private ServiceDescriptions locallyPublishedServices = new ServiceDescriptions();

    /** The services published on other APSSimpleDiscoveryService instances and announced to us. */
    private ServiceDescriptions remotelyPublishedServices = new ServiceDescriptions();


    // Information we depend on.

    /** Our logger. */
    private APSLogger logger = null;

    /** This is used sending and receiving discovery messages. */
    private APSGroupsService groupsService = null;

    /** Our API to the group we join. */
    private GroupMember groupMember = null;

    /** The platform service. */
    private APSPlatformService platformService = null;

    private boolean running = false;

    //
    // Constructors
    //

    /**
     * Creates a new APSSimpleDiscoveryServiceProvider instance.
     *
     * @param groupsService An APSServiceTracker managed API to APSGroupsService.
     * @param platformService Provides information about the local platform installation.
     * @param logger To log to.
     */
    public APSSimpleDiscoveryServiceProvider(APSGroupsService groupsService, APSPlatformService platformService, APSLogger logger) {
        this.groupsService = groupsService;
        this.platformService = platformService;
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Checks if the thread should continue to run.
     */
    private synchronized boolean keepRunning() {
        return this.running;
    }

    /**
     * Terminate the thread.
     */
    public synchronized void terminate() {
        this.running = false;
    }

    /**
     * Connect and then evict non updated service descriptions regularly.
     */
    public void run() {
        connect();

        this.running = true;

        int evictCount = 0;
        int evictAtCount = 2; // 10 seconds
        int republishCount = 0;
        int republishAtCount = 60; // 5 minutes

        while (keepRunning()) {
            if (republishCount >= republishAtCount) {
                republishCount = 0;
                republish();
            }
            else {
                ++republishCount;
            }

            if (evictCount >= evictAtCount) {
                evictCount = 0;
                this.remotelyPublishedServices.evictOld();
            }
            else {
                ++evictCount;
            }

            try {Thread.sleep(5000);} catch (InterruptedException ie) {}
        }

        disconnect();
    }

    /**
     * Republished all local services.
     */
    private synchronized void republish() {
        try {
            for (ServiceDescription sd : this.locallyPublishedServices.getAllServiceDescriptions()) {
                Message message = this.groupMember.createNewMessage();
                Publish publish = new Publish(sd);
                DiscoveryProtocol.write(message, publish);
                this.groupMember.sendMessage(message);
            }
        }
        catch (IOException ioe) {
            this.logger.error("Problems during republish!", ioe);
        }
    }

    /**
     * Since we cannot do joinGroup() during bundle activation in startup thread due to potential deadlock we have to do it
     * in a separate thread.
     */
    private void connect() {
        this.logger.info("Connecting ...");
        // We need the platform type so that we don't mix up dev-test, system-test, acceptance-test and production
        // installations. Well, production should be on a separate net of course, but the others might collide
        // otherwise.
        String discoveryGroup = "aps-discovery-" + this.platformService.getPlatformDescription().getType();
        try {
            this.groupMember = this.groupsService.joinGroup(discoveryGroup);
            this.logger.info("Joined group '" + discoveryGroup + "'!");
            this.groupMember.addMessageListener(this);
        }
        catch (IOException ioe) {
            this.logger.error("Failed to join group! Discovery will not work! [" + discoveryGroup + "]", ioe);
        }
        try {Thread.sleep(2000);} catch (InterruptedException ie) {}

        this.logger.info("Connected!");
    }

    /**
     * Used by activator on shutdown.
     *
     * @throws IOException
     */
    private void disconnect() {
        try {
            if (this.groupMember != null) {
                this.groupMember.removeMessageListener(this);
                this.groupsService.leaveGroup(this.groupMember);
            }
            this.logger.info("Disconnected!");
        }
        catch (IOException ioe) {
            this.logger.error("Failed disconnect!", ioe);
        }
    }

    /**
     * Returns all remotely discovered services.
     */
    @Override
    public synchronized List<ServiceDescription> getRemotelyDiscoveredServices() {
        List<ServiceDescription> list = new LinkedList<>();
        list.addAll(this.remotelyPublishedServices.getAllServiceDescriptions());
        return list;
    }

    /**
     * Returns the locally registered services.
     */
    @Override
    public List<ServiceDescription> getLocallyRegisteredServices() {
        List<ServiceDescription> list = new LinkedList<>();
        list.addAll(this.locallyPublishedServices.getAllServiceDescriptions());
        return list;
    }

    /**
     * Returns all known services, both locally registered and remotely discovered.
     */
    @Override
    public synchronized List<ServiceDescription> getAllServices() {
        List<ServiceDescription> allServices = new LinkedList<>();
        allServices.addAll(getRemotelyDiscoveredServices());
        allServices.addAll(getLocallyRegisteredServices());
        return allServices;
    }

    /**
     * Returns all discovered services with the specified id.
     *
     * @param serviceId The id of the service to get.
     * @param version The version of the service to get.
     */
    @Override
    public synchronized List<ServiceDescription> getService(String serviceId, String version) {
        return this.remotelyPublishedServices.getServiceDescriptions(serviceId, version);
    }

    /**
     * Publishes a local service. This will announce it to other known APSSimpleDiscoveryService instances.
     *
     * @param serviceDescription The description of the service to publish.
     */
    @Override
    public synchronized void publishService(ServiceDescription serviceDescription) throws APSDiscoveryPublishException {
        try {
            Message message = this.groupMember.createNewMessage();
            Publish publish = new Publish(serviceDescription);
            DiscoveryProtocol.write(message, publish);
            this.groupMember.sendMessage(message);
            this.locallyPublishedServices.addServiceDescription(serviceDescription);
        }
        catch (IOException ioe) {
            throw new APSDiscoveryPublishException("Failed to publish service description with id '" +
                    serviceDescription.getServiceId() + "'!", ioe);
        }
    }

    /**
     * Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this
     * service is no longer available.
     *
     * @param serviceDescription The service to unpublish.
     */
    @Override
    public synchronized void unpublishService(ServiceDescription serviceDescription) {
        try {
            Message message = this.groupMember.createNewMessage();
            UnPublish unPublish = new UnPublish(serviceDescription);
            DiscoveryProtocol.write(message, unPublish);
            this.groupMember.sendMessage(message);
            this.locallyPublishedServices.removeServiceDescription(serviceDescription);
        }
        catch (IOException ioe) {
            throw new APSDiscoveryPublishException("Failed to unpublish service description with id '" +
                    serviceDescription.getServiceId() + "'!", ioe);
        }
    }

    /**
     * Notification of received message. This implements MessageListener.
     *
     * @param message The received message.
     */
    @Override
    public synchronized void messageReceived(Message message) {
        try {
            Protocol protocol = DiscoveryProtocol.read(message);

            if (Publish.class.isAssignableFrom(protocol.getClass())) {
                this.remotelyPublishedServices.addServiceDescription(((Publish)protocol).getServiceDescription());
            }
            else if (UnPublish.class.isAssignableFrom(protocol.getClass())) {
                this.remotelyPublishedServices.removeServiceDescription(((UnPublish)protocol).getServiceDescription());
            }
        }
        catch (IOException ioe) {
            this.logger.error("Failed to read received message!", ioe);
        }
    }
}
