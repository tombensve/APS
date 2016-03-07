/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Edits configurations registered with the APSConfigurationService.
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
 *         2011-08-28: Created!
 *
 */
package se.natusoft.osgi.aps.apsconfigadminweb;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.apsadminweb.service.APSAdminWebService;
import se.natusoft.osgi.aps.apsadminweb.service.model.AdminWebReg;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable;
import se.natusoft.osgi.aps.tools.tracker.WithService;

public class APSConfigAdminWebActivator implements BundleActivator {
    //
    // Private Members
    //

    // Required Services

    /** The APSAdminWeb registry service. */
    private APSServiceTracker<APSAdminWebService> adminWebServiceTracker = null;

    // Provided Services


    // Other Members

    /** Our logger. */
    private APSLogger logger = null;

    /** Our APSAdminWeb registration. */
    private static final AdminWebReg ADMIN_WEB_REG =
            new AdminWebReg("Configuration", "1.0.0", "Administers APSConfigService configurations.", "/apsadminweb/config");

    //
    // Bundle Start.
    //

    @Override
    @SuppressWarnings("Duplicates")
    public void start(BundleContext context) throws Exception {
        this.logger = new APSLogger();
        this.logger.start(context);

        this.adminWebServiceTracker = new APSServiceTracker<>(context, APSAdminWebService.class);
        this.adminWebServiceTracker.setLogger(this.logger);
        this.adminWebServiceTracker.onServiceAvailable(new OnServiceAvailable<APSAdminWebService>() {
            public void onServiceAvailable(APSAdminWebService service, ServiceReference serviceReference) {
                service.registerAdminWeb(APSConfigAdminWebActivator.ADMIN_WEB_REG);
            }
        });
        this.adminWebServiceTracker.start();
    }

    //
    // Bundle Stop.
    //

    @Override
    @SuppressWarnings("Duplicates")
    public void stop(BundleContext context) throws Exception {
        this.adminWebServiceTracker.withAllAvailableServices(new WithService<APSAdminWebService>() {
            public void withService(APSAdminWebService service) {
                service.unregisterAdminWeb(APSConfigAdminWebActivator.ADMIN_WEB_REG);
            }
        });
        this.adminWebServiceTracker.stop(context);
        this.logger.stop(context);

        this.logger = null;
        this.adminWebServiceTracker = null;
    }

}
