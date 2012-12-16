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
package se.natusoft.osgi.aps.discovery.service.net;

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;
import se.natusoft.osgi.aps.discovery.model.ServiceDescriptions;
import se.natusoft.osgi.aps.discovery.service.event.DiscoveryEventProvidingBase;
import se.natusoft.osgi.aps.tools.APSLogger;

/**
 * Listens to multicast data and fires events when service descriptions are seen.
 */
public class APSAutoRefreshRemoteInstancesThread extends DiscoveryEventProvidingBase implements Runnable {
    //
    // Private Members
    //

    /** Thread will exit when this is false. */
    private boolean running = false;

    /** Our thread. */
    private Thread thread = null;

    /** The container of locally published services. */
    private ServiceDescriptions locallyPublishedServices;

    /**
     * The number of minutes between firing events about all locally registered services again.
     * <p/>
     * The point of this is that we announce services using non transport safe UDP, and in addition
     * to that a new instnance may have been started somewhere that has not yet received our
     * services.
     */
    private int autoFireAvailableEventIntervalMinutes;

    /** The logger to log to. */
    private APSLogger logger = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSMulticastDiscoveryListenerThread.
     *
     * @param locallyPublishedServices The container of locally published services.
     * @param autoFireAvailableEventIntervalMinutes The number of minutes between firing events about all locally registered services again.
     * @param logger The logger to log to.
     */
    public APSAutoRefreshRemoteInstancesThread(ServiceDescriptions locallyPublishedServices, int autoFireAvailableEventIntervalMinutes, APSLogger logger) {
        this.locallyPublishedServices = locallyPublishedServices;
        this.autoFireAvailableEventIntervalMinutes = autoFireAvailableEventIntervalMinutes;
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Starts the thread.
     */
    public void start() throws Exception {
        this.running = true;
        this.thread = new Thread(this);
        this.thread.setName("APSDiscoveryService Auto refresh remote instances thread");
        this.thread.start();
        this.logger.info("Started APSDiscoveryService:APSAutoRefreshRemoteInstancesThread!");
    }

    /**
     * Stops the thread.
     */
    public void stop() {
        if (this.running) {
            this.running = false;
            try {
                if (this.thread != null) {this.thread.join();}
            } catch (InterruptedException e) {
                // This is OK! We just want to wait for it to finnish.
            }
            this.logger.info("Stopped APSDiscoveryService:APSAutoRemoteRefreshInstancesThread!");
        }
    }

    /**
     * The main part of the thread.
     */
    @Override
    public void run() {
        int interval = this.autoFireAvailableEventIntervalMinutes * 1000 * 10;

        while (this.running) {
            int waitCount = 0;
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException ie) {
                // Do nothing.
            }
            waitCount += 5000;

            if (waitCount >= interval) {
                waitCount = 0;
                for (ServiceDescription sd : this.locallyPublishedServices.getAllServiceDescriptions()) {
                    super.fireAvailableEvent(sd);
                }
            }
        }
    }
}
