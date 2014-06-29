/* 
 * 
 * PROJECT
 *     Name
 *         APS Platform Service Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides a platform specific configuration and service providing platform instance
 *         specific information.
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
 *         2011-08-16: Created!
 *         
 */
package se.natusoft.osgi.aps.platform;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.core.platform.service.APSPlatformService;
import se.natusoft.osgi.aps.platform.service.provider.APSPlatformServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.Dictionary;
import java.util.Properties;

public class APSPlatformActivator implements BundleActivator {
    //
    // Private Members
    //
    
    // Required Services
    
    /** The APS Configuration Service. */
//    private APSServiceTracker<APSConfigService> configServiceTracker = null;

    /** The current config service. */
//    private APSConfigService configService = null;
    
    // Provided Services
    
    /** The platform service. */
    private ServiceRegistration platformServiceReg = null;

    // Other Members
    
    /** Our logger. */
    APSLogger logger = null;

    /** Our bundle context. */
    private BundleContext context = null;
    
    //
    // Bundle Start.
    //
    
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        this.logger = new APSLogger();
        this.logger.start(context);

//        this.configServiceTracker = new APSServiceTracker<APSConfigService>(context, APSConfigService.class, "forever");
        setupService();
//
//        this.configServiceTracker.onServiceAvailable(new OnServiceAvailable<APSConfigService>() {
//            public void onServiceAvailable(APSConfigService service, ServiceReference serviceReference) throws Exception {
//                service.registerConfiguration(APSPlatformConfig.class, false);
//            }
//        });
//
//        this.configServiceTracker.start();

    }

    private void setupService() throws Exception {
//        this.configService = configService;

        Dictionary platformServiceProps = new Properties();
        platformServiceProps.put(Constants.SERVICE_PID, APSPlatformServiceProvider.class.getName());
        APSPlatformServiceProvider platformServiceProvider = new APSPlatformServiceProvider();
        this.platformServiceReg = context.registerService(APSPlatformService.class.getName(), platformServiceProvider, platformServiceProps);
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
//        // When the service is leaving there is not much we can do about it, but in this case
//        // we are leaving and thus we cleanup after ourselves. Also note that we have registered
//        // with potentially more than one config service so we try to unregister with all currently
//        // available.
//        if (this.configService != null) {
//            this.configServiceTracker.withAllAvailableServices(new WithService<APSConfigService>() {
//                public void withService(APSConfigService service) throws Exception {
//                    try {
//                        service.unregisterConfiguration(APSPlatformConfig.class);
//                    }
//                    catch (RuntimeException re) {} // Make sure we try to unregister from all!
//                }
//            });
//        }
        takedownService(context);
    }

    private void takedownService(BundleContext context) {
        if (this.platformServiceReg != null) this.platformServiceReg.unregister();
//        this.configService = null;
//        if (this.configServiceTracker != null) this.configServiceTracker.stop(context);
        if (this.logger != null) this.logger.stop(context);

        this.platformServiceReg = null;
//        this.configServiceTracker = null;
        this.logger = null;
    }
}
