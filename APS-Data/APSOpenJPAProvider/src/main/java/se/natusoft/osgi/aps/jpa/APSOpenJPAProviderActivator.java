/* 
 * 
 * PROJECT
 *     Name
 *         APS OpenJPA Provider
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         Provides an implementation of APSJPAService using OpenJPA.
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
 *         2012-08-19: Created!
 *         
 */
package se.natusoft.osgi.aps.jpa;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.data.jpa.service.APSJPAService;
import se.natusoft.osgi.aps.jpa.service.APSOpenJPAServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.Dictionary;
import java.util.Properties;

public class APSOpenJPAProviderActivator implements BundleActivator {
    //
    // Private Members
    //

    private APSOpenJPAServiceProvider apsJPAServiceProvider = null;

    private ServiceRegistration apsOpenJPAServiceReg = null;

    private APSLogger logger = null;
    
    //
    // Bundle Start.
    //
    
    @Override
    public void start(BundleContext context) throws Exception {
        this.logger = new APSLogger(System.out);
        this.logger.start(context);

        Dictionary apsJPAServiceProps = new Properties();
        apsJPAServiceProps.put(Constants.SERVICE_PID, APSOpenJPAServiceProvider.class.getName());
        this.apsJPAServiceProvider = new APSOpenJPAServiceProvider(this.logger, context);
        this.apsOpenJPAServiceReg = context.registerService(APSJPAService.class.getName(), apsJPAServiceProvider, apsJPAServiceProps);

        context.addBundleListener(this.apsJPAServiceProvider);
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.apsOpenJPAServiceReg != null) {
            try {
                this.apsOpenJPAServiceReg.unregister();
                this.apsOpenJPAServiceReg = null;
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
        }

        if (this.apsJPAServiceProvider != null) {
            context.removeBundleListener(this.apsJPAServiceProvider);
            this.apsJPAServiceProvider.closeAll();
            this.apsJPAServiceProvider = null;
        }

        if (this.logger != null) {
            this.logger.stop(context);
            this.logger = null;
        }
    }

}
