/* 
 * 
 * PROJECT
 *     Name
 *         APS Administration Web Registration Service
 *     
 *     Code Version
 *         0.9.1
 *     
 *     Description
 *         The service for registering admin webs with aps-admin-web.
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
 *         2011-08-27: Created!
 *         
 */
package se.natusoft.osgi.aps.apsadminweb;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.apsadminweb.service.APSAdminWebService;
import se.natusoft.osgi.aps.apsadminweb.service.provider.APSAdminWebServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.Dictionary;
import java.util.Properties;

public class APSAdminWebServiceActivator implements BundleActivator {
    //
    // Provided services
    //
    
    /** The admin web registration service. */
    private ServiceRegistration adminWebService = null;
    
    //
    // Required services
    //


    //
    // Other Members
    //
    
    /** Logger for service. */
    private APSLogger logger = null;
    
    
    //
    // Bundle Management
    //
    
    @Override
    public void start(BundleContext context) throws Exception {
        // Setup logging
        this.logger = new APSLogger(System.out);
        this.logger.start(context);
        
        Dictionary props = new Properties();
        props.put(Constants.SERVICE_PID, APSAdminWebServiceProvider.class.getName());
        APSAdminWebServiceProvider adminWebServiceProvider = new APSAdminWebServiceProvider(this.logger);
        this.adminWebService = context.registerService(APSAdminWebService.class.getName(), adminWebServiceProvider, props);
        
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.adminWebService.unregister();
        this.logger.stop(context);

        // Let these be garbage collected.
        this.adminWebService = null;
        this.logger = null;
    }

}
