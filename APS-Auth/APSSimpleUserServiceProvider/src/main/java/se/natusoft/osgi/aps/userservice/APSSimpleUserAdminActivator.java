/* 
 * 
 * PROJECT
 *     Name
 *         APSSimpleUserServiceProvider
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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-07-15: Created!
 *         
 */
package se.natusoft.osgi.aps.userservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserService;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserServiceAdmin;
import se.natusoft.osgi.aps.api.data.jdbc.service.APSDataSourceDefService;
import se.natusoft.osgi.aps.api.data.jpa.service.APSJPAService;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.userservice.service.APSSimpleUserServiceProvider;

import java.util.Properties;

public class APSSimpleUserAdminActivator implements BundleActivator {
    //
    // Private Members
    //

    // Required Services

    /** The data source definition service. */
    private APSServiceTracker<APSDataSourceDefService> apsDataSourceDefServiceTracker = null;

    /** The APS data source definition service. */
    private APSServiceTracker<APSJPAService> apsJPAServiceTracker = null;

    // Provided Services

    /** The platform service. */
    private ServiceRegistration simpleUserServiceReg = null;

    private ServiceRegistration simpleUserServiceAdminReg = null;

    // Other Members

    /** Our logger. */
    private APSLogger logger = null;

    //
    // Bundle Start.
    //

    @Override
    public void start(BundleContext context) throws Exception {
        this.logger = new APSLogger(System.out);
        this.logger.start(context);

        this.apsDataSourceDefServiceTracker = new APSServiceTracker<APSDataSourceDefService>(
                context,
                APSDataSourceDefService.class,
                APSServiceTracker.LARGE_TIMEOUT
        );
        this.apsDataSourceDefServiceTracker.start();

        this.apsJPAServiceTracker = new APSServiceTracker<APSJPAService>(
                context,
                APSJPAService.class,
                APSServiceTracker.LARGE_TIMEOUT
        );
        this.apsJPAServiceTracker.start();

        APSDataSourceDefService dataSourceDefService = this.apsDataSourceDefServiceTracker.getWrappedService();
        APSJPAService apsJPAService = this.apsJPAServiceTracker.getWrappedService();

        Properties serviceProps = new Properties();
        serviceProps.put(Constants.SERVICE_PID, APSSimpleUserServiceProvider.class.getName());
        APSSimpleUserServiceProvider apsSimpleUserServiceProvider =
                new APSSimpleUserServiceProvider(context, this.logger, dataSourceDefService, apsJPAService);

        this.simpleUserServiceReg = context.registerService(
                APSSimpleUserService.class.getName(),
                apsSimpleUserServiceProvider,
                serviceProps
        );

        this.simpleUserServiceAdminReg = context.registerService(
                APSSimpleUserServiceAdmin.class.getName(),
                apsSimpleUserServiceProvider,
                serviceProps
        );
    }

    //
    // Bundle Stop.
    //

    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.simpleUserServiceReg != null) {
            try {
                this.simpleUserServiceReg.unregister();
                this.simpleUserServiceReg = null;
            } catch (IllegalStateException ise) { /* This is OK! */ }
        }

        if (this.simpleUserServiceAdminReg != null) {
            try {
                this.simpleUserServiceAdminReg.unregister();
                this.simpleUserServiceAdminReg = null;
            } catch (IllegalStateException ise) { /* This is OK! */ }
        }

        if (this.apsDataSourceDefServiceTracker != null) {
            this.apsDataSourceDefServiceTracker.stop(context);
            this.apsDataSourceDefServiceTracker = null;
        }

        if (this.apsJPAServiceTracker != null) {
            this.apsJPAServiceTracker.stop(context);
            this.apsJPAServiceTracker = null;
        }

        if (this.logger != null) {
            this.logger.stop(context);
            this.logger = null;
        }
    }

}
