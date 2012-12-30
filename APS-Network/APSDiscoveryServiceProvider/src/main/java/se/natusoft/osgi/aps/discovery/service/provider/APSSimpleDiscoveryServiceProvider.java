/* 
 * 
 * PROJECT
 *     Name
 *         APS Discovery Service Provider
 *     
 *     Code Version
 *         0.9.0
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
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The discovery service Implementation.
 */
public class APSSimpleDiscoveryServiceProvider implements APSSimpleDiscoveryService, MessageListener {
    //
    // Private Members
    //

    // Local service implementation data.

    /** The services published locally by clients of this service. */
    private ServiceDescriptions locallyPublishedServices = null;

    /** The services published on other APSSimpleDiscoveryService instances and announced to us. */
    private ServiceDescriptions remotelyPublishedServices = null;

    /** This is false until setup() has been done. This is so that setup() will only run once. */
    private boolean isSetup = false;


    // Information we depend on.

    /** Our logger. */
    private APSLogger logger = null;

    /** This is used sending and receiving discovery messages. */
    private APSGroupsService groupsService = null;

    /** Our API to the group we join. */
    private GroupMember groupMember = null;

    /** The platform service. */
    private APSPlatformService platformService = null;

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
     * Since we cannot do joinGroup() during bundle activation due to potential deadlock we have to do it at first
     * service call.
     */
    private void setup() {
        if (!this.isSetup) {
            // We need the platform type so that we don't mix up dev-test, system-test, acceptance-test and production
            // installations. Well, production should be on a separate net of course, but the others might collide
            // otherwise.
            String discoveryGroup = "aps-discovery-" + this.platformService.getPlatformDescription().getType();
            try {
                this.groupMember = this.groupsService.joinGroup(discoveryGroup);
                this.isSetup = true;
                this.logger.info("Joined group '" + discoveryGroup + "'!");
                this.groupMember.addMessageListener(this);
            }
            catch (IOException ioe) {
                this.logger.error("Failed to join group! Discovery will not work! [" + discoveryGroup + "]", ioe);
            }
        }
    }

    /**
     * Used by activator on shutdown.
     *
     * @throws IOException
     */
    public void cleanup() throws IOException {
        if (this.isSetup) {
            this.groupMember.removeMessageListener(this);
            this.groupsService.leaveGroup(this.groupMember);
        }
    }

    /**
     * Returns all remotely discovered services.
     */
    @Override
    public List<ServiceDescription> getRemotelyDiscoveredServices() {
        setup();
        return this.remotelyPublishedServices.getAllServiceDescriptions();
    }

    /**
     * Returns the locally registered services.
     */
    @Override
    public List<ServiceDescription> getLocallyRegisteredServices() {
        setup();
        return this.locallyPublishedServices.getAllServiceDescriptions();
    }

    /**
     * Returns all known services, both locally registered and remotely discovered.
     */
    @Override
    public List<ServiceDescription> getAllServices() {
        setup();
        List<ServiceDescription> allServices = new LinkedList<ServiceDescription>();
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
    public List<ServiceDescription> getService(String serviceId, String version) {
        setup();
        return this.remotelyPublishedServices.getServiceDescriptions(serviceId, version);
    }

    /**
     * Publishes a local service. This will announce it to other known APSSimpleDiscoveryService instances.
     *
     * @param serviceDescription The description of the service to publish.
     */
    @Override
    public void publishService(ServiceDescription serviceDescription) throws APSDiscoveryPublishException {
        setup();
        try {
            Message message = this.groupMember.createNewMessage();
            DiscoveryProtocol.writeDiscoveryAction(message, DiscoveryProtocol.publishAction(serviceDescription));
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
    public void unpublishService(ServiceDescription serviceDescription) {
        setup();
        try {
            Message message = this.groupMember.createNewMessage();
            DiscoveryProtocol.writeDiscoveryAction(message, DiscoveryProtocol.unpublishAction(serviceDescription));
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
    public void messageReceived(Message message) {
        DiscoveryProtocol.DiscoveryAction discoveryAction = DiscoveryProtocol.readDiscoveryAction(message);
        if (discoveryAction.getAction() == DiscoveryProtocol.DiscoveryAction.PUBLISH) {
            this.remotelyPublishedServices.addServiceDescription(discoveryAction.getServiceDescription());
        }
        else if (discoveryAction.getAction() == DiscoveryProtocol.DiscoveryAction.UNPUBLISH) {
            this.remotelyPublishedServices.removeServiceDescription(discoveryAction.getServiceDescription());
        }
        else {
            this.logger.error("Unknown discovery action received: " + discoveryAction.getAction());
        }
    }
}
