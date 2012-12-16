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
 *         It supports both multicast and UDP connections.
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
package se.natusoft.osgi.aps.discovery.service.event;

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for discovery classes handling discovery events.
 */
public class DiscoveryEventProvidingBase {

    //
    // Private Members
    //

    /** Listeners for local services. */
    private List<DiscoveryEventListener> eventlisteners = new ArrayList<DiscoveryEventListener>();


    //
    // Constructors
    //

    /**
     * Creates a new DiscoveryEventProvidingBase.
     */
    public DiscoveryEventProvidingBase() {}

    //
    // Methods
    //

    /**
     * Adds an event listener for local discovery events.
     *
     * @param discoveryEventListener The listener to add.
     */
    public void addDiscoveryEventListener(DiscoveryEventListener discoveryEventListener) {
        this.eventlisteners.add(discoveryEventListener);
    }

    /**
     * Removes a previously added event listener.
     *
     * @param discoveryEventListener The event listener to remove.
     */
    public void removeDiscoveryEventListener(DiscoveryEventListener discoveryEventListener) {
        this.eventlisteners.remove(discoveryEventListener);
    }


    /**
     * Fires an service available event.
     *
     * @param serviceDescription The available service to fire.
     */
    protected void fireAvailableEvent(ServiceDescription serviceDescription) {
        for (DiscoveryEventListener listener : this.eventlisteners) {
            listener.serviceAvailable(serviceDescription);
        }
    }

    /**
     * Fires a service leaving event.
     *
     * @param serviceDescription The leaving service to fire.
     */
    protected void fireLeavingEvent(ServiceDescription serviceDescription) {
        for (DiscoveryEventListener listener : this.eventlisteners) {
            listener.serviceLeaving(serviceDescription);
        }
    }
}
