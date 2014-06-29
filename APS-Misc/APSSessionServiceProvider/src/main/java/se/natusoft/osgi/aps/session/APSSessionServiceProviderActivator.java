/* 
 * 
 * PROJECT
 *     Name
 *         APS Session Service Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides an OSGi server wide session.
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
 *         2012-09-08: Created!
 *         
 */
package se.natusoft.osgi.aps.session;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.misc.session.APSSessionService;
import se.natusoft.osgi.aps.session.service.APSSessionServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.Dictionary;
import java.util.Properties;

public class APSSessionServiceProviderActivator implements BundleActivator {
    //
    // Private Members
    //
    
    // Provided Services
    
    /** The session service. */
    private ServiceRegistration sessionServiceReg = null;

    // Other Members
    
    /** Our logger. */
    private APSLogger logger = null;
    
    //
    // Bundle Start.
    //
    
    @Override
    @SuppressWarnings("unchecked")
    public void start(BundleContext context) throws Exception {
        this.logger = new APSLogger(System.out);
        this.logger.start(context);

        Dictionary platformServiceProps = new Properties();
        platformServiceProps.put(Constants.SERVICE_PID, APSSessionServiceProvider.class.getName());
        APSSessionServiceProvider sessionServiceProvider = new APSSessionServiceProvider(this.logger);
        this.sessionServiceReg = context.registerService(APSSessionService.class.getName(), sessionServiceProvider, platformServiceProps);
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.sessionServiceReg != null) {
            try {
                this.sessionServiceReg.unregister();
                this.sessionServiceReg = null;
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
        }

        if (this.logger != null) {
            this.logger.stop(context);
            this.logger = null;
        }
    }

}
