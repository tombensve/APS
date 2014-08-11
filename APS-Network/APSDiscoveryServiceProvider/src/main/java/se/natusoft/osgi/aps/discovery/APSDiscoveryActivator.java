/*
 *
 * PROJECT
 *     Name
 *         APS Discovery Service Provider
 *     
 *     Code Version
 *         0.11.0
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
package se.natusoft.osgi.aps.discovery;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.core.platform.service.APSPlatformService;
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.discovery.service.provider.APSSimpleDiscoveryServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.APSServiceTracker;

import java.util.Dictionary;
import java.util.Properties;

public class APSDiscoveryActivator implements BundleActivator {
    //
    // Private Members
    //

    /** The bundle logger. */
    private APSLogger logger = null;

    /** The service we publish. */
    private ServiceRegistration discoveryServiceReg = null;

    /** The published discovery service instance. We need to save it so that we can use in to cleanup in stop(). */
    private APSSimpleDiscoveryServiceProvider discoveryService = null;

    // Services we depend on

    private APSServiceTracker<APSPlatformService> platformServiceTracker = null;

    private APSServiceTracker<APSGroupsService> groupsServiceTracker = null;

    //
    // Bundle Start.
    //

    @Override
    public void start(BundleContext context) throws Exception {

        // This will log to the OSGi LogService if it is found otherwise it will log to System.out.
        // Not all OSGi servers provide the LogService.
        this.logger = new APSLogger(System.out);
        this.logger.setLoggingFor("APSDiscoveryService");
        this.logger.start(context);

        this.platformServiceTracker =
                new APSServiceTracker<>(context, APSPlatformService.class, APSServiceTracker.LARGE_TIMEOUT);
        this.platformServiceTracker.start();

        this.groupsServiceTracker =
                new APSServiceTracker<>(context, APSGroupsService.class, APSServiceTracker.LARGE_TIMEOUT);
        this.groupsServiceTracker.start();

        // Register our discovery service as an OSGi service.
        this.discoveryService =
                new APSSimpleDiscoveryServiceProvider(
                        this.groupsServiceTracker.getWrappedService(),
                        this.platformServiceTracker.getWrappedService(),
                        this.logger
                );
        this.discoveryService.start();

        Dictionary serviceProps = new Properties();
        serviceProps.put(Constants.SERVICE_PID, APSSimpleDiscoveryServiceProvider.class.getName());
        this.discoveryServiceReg = context.registerService(
                APSSimpleDiscoveryService.class.getName(),
                this.discoveryService,
                serviceProps
        );

        this.logger.setServiceReference(this.discoveryServiceReg.getReference());

        this.logger.info("Started!");
    }

    //
    // Bundle Stop.
    //

    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.discoveryServiceReg != null) {
            this.discoveryServiceReg.unregister();
        }

        if (this.discoveryService != null) {
            this.discoveryService.terminate();
            this.discoveryService = null;
        }

        if (this.platformServiceTracker != null) {
            this.platformServiceTracker.stop(context);
            this.platformServiceTracker = null;
        }

        if (this.groupsServiceTracker != null) {
            this.groupsServiceTracker.stop(context);
            this.groupsServiceTracker = null;
        }
    }


}
