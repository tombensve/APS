/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Service Provider
 *     
 *     Code Version
 *         0.9.1
 *     
 *     Description
 *         Provides an implementation of aps-apis:se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService
 *         using aps-json-lib as JSON parser/creator.
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
 *         2012-01-22: Created!
 *         
 */
package se.natusoft.osgi.aps.json;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService;
import se.natusoft.osgi.aps.json.service.APSJSONServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.Dictionary;
import java.util.Properties;

public class APSJSONServiceActivator implements BundleActivator {
    //
    // Private Members
    //
    
    // Required Services

    // Provided Services
    
    /** The JSONService service. */
    private ServiceRegistration jsonServiceReg = null;

    /** The JSONExtendedService service. */
    private ServiceRegistration jsonExtendedServiceReg = null;

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

        APSJSONServiceProvider jsonService = new APSJSONServiceProvider();

        Dictionary jsonServiceProps = new Properties();
        jsonServiceProps.put(Constants.SERVICE_PID, APSJSONServiceProvider.class.getName());
        this.jsonServiceReg =
                context.registerService(APSJSONService.class.getName(), jsonService, jsonServiceProps);

        Dictionary jsonExtServiceProps = new Properties();
        jsonExtServiceProps.put(Constants.SERVICE_PID, APSJSONServiceProvider.class.getName() + "Extended");
        this.jsonExtendedServiceReg =
                context.registerService(APSJSONExtendedService.class.getName(), jsonService, jsonExtServiceProps);
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.jsonServiceReg != null) {
            try {
                this.jsonServiceReg.unregister();
                this.jsonServiceReg = null;
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
        }
        if (this.jsonExtendedServiceReg != null) {
            try {
                this.jsonExtendedServiceReg.unregister();
                this.jsonExtendedServiceReg = null;
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
        }

        if (this.logger != null) {
            this.logger.stop(context);
            this.logger = null;
        }
    }

}
