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
package se.natusoft.osgi.aps.discovery;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.discovery.service.net.StartupManager;

public class APSDiscoveryActivator implements BundleActivator {
    //
    // Private Members
    //

    /** The bundles context. */
    private BundleContext context = null;

    /** Starts, refreshes and stops the whole thing. */
    private StartupManager startupManager = null;

    //
    // Bundle Start.
    //

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        // This manages the startup in a separate thread since it has dependencies that need to become available
        // before it can start. The StartupManager will also adapt to changes in the configuration while running.
        // It is however only the startup procedure that is running in the thread which dies after startup is done.
        this.startupManager = new StartupManager(this.context);
        this.startupManager.startup();
    }

    //
    // Bundle Stop.
    //

    @Override
    public void stop(BundleContext context) throws Exception {
        this.startupManager.shutdown();
    }


}
