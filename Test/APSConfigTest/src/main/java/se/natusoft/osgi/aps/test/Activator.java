/* 
 * 
 * PROJECT
 *     Name
 *         APS APSConfigTest
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This project is only for providing an APSConfigService configuration for testing on.
 *         It was made to test the APSConfigAdminWeb.
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
 *         2012-05-01: Created!
 *         
 */
package se.natusoft.osgi.aps.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService;
import se.natusoft.osgi.aps.test.config.APSTestConfig;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable;

public class Activator implements BundleActivator {
    //
    // Private Members
    //

    private APSServiceTracker<APSConfigService> configServiceTracker = null;

    //
    // Bundle Start.
    //
    
    @Override
    public void start(BundleContext context) throws Exception {

        this.configServiceTracker =
                new APSServiceTracker<APSConfigService>(context, APSConfigService.class, APSServiceTracker.NO_TIMEOUT);
        this.configServiceTracker.start();

        this.configServiceTracker.onServiceAvailable(new OnServiceAvailable<APSConfigService>() {
            @Override
            public void onServiceAvailable(APSConfigService service, ServiceReference serviceReference) throws Exception {
                service.registerConfiguration(APSTestConfig.class, true); // Setting forService=true as a test.
            }
        });

    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.configServiceTracker != null) {
            this.configServiceTracker.stop(context);
            this.configServiceTracker = null;
        }
    }
}
