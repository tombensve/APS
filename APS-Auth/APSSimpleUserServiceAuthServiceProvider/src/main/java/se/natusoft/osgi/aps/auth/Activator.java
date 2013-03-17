/* 
 * 
 * PROJECT
 *     Name
 *         APSSimpleUserServiceAuthServiceProvider
 *     
 *     Code Version
 *         0.9.1
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
 *         2013-02-21: Created!
 *         
 */
package se.natusoft.osgi.aps.auth;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.auth.user.APSAuthService;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserService;
import se.natusoft.osgi.aps.auth.service.APSSimpleUserServiceAuthServiceProvider;
import se.natusoft.osgi.aps.tools.APSServiceTracker;

import java.util.Dictionary;
import java.util.Properties;

public class Activator implements BundleActivator {
    //
    // Private Members
    //
    
    // Required Services
    
    /** The APSSimpleUserService service tracker. */
    private APSServiceTracker<APSSimpleUserService> simpleUserServiceTracker = null;
    
    
    // Provided Services
    
    /** The platform service. */
    private ServiceRegistration serviceReg = null;


    //
    // Bundle Start.
    //
    
    @Override
    public void start(BundleContext context) throws Exception {

        this.simpleUserServiceTracker = new APSServiceTracker<APSSimpleUserService>(
                context,
                APSSimpleUserService.class,
                APSServiceTracker.LARGE_TIMEOUT
        );
        this.simpleUserServiceTracker.start();
        APSSimpleUserService simpleUserService = this.simpleUserServiceTracker.getWrappedService();

        Dictionary platformServiceProps = new Properties();
        platformServiceProps.put(Constants.SERVICE_PID, APSSimpleUserServiceAuthServiceProvider.class.getName());
        APSSimpleUserServiceAuthServiceProvider serviceProvider = new APSSimpleUserServiceAuthServiceProvider(simpleUserService);
        this.serviceReg = context.registerService(APSAuthService.class.getName(),
                serviceProvider, platformServiceProps);
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.serviceReg != null) {
            try {
                this.serviceReg.unregister();
                this.serviceReg = null;
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
        }
    }

}
