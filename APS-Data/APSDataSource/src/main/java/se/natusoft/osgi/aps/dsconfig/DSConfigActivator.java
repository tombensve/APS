/* 
 * 
 * PROJECT
 *     Name
 *         APS Data Source
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         This bundle provides data source definitions configuration and a service to lookup
 *         the configured data source definitions with. These are not javax.sql.DataSource
 *         objects! This only provides the configuration data to setup a data source. Some
 *         other bundle can use this to configure a DataSource, a connection pool, JPA, etc
 *         from this.
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
 *         2012-07-17: Created!
 *         
 */
package se.natusoft.osgi.aps.dsconfig;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.data.jdbc.service.APSDataSourceDefService;
import se.natusoft.osgi.aps.dsconfig.service.APSDataSourceDefServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.Properties;

public class DSConfigActivator implements BundleActivator {
    //
    // Private Members
    //
    
    // Required Services
    
    // Provided Services
    
    /** The platform service. */
    private ServiceRegistration apsDataSourceDefServiceReg = null;

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


        Properties platformServiceProps = new Properties();
        platformServiceProps.put(Constants.SERVICE_PID, APSDataSourceDefServiceProvider.class.getName());

        APSDataSourceDefServiceProvider dataSourceServiceProvider =
                new APSDataSourceDefServiceProvider(this.logger);

        this.apsDataSourceDefServiceReg =
                context.registerService(
                        APSDataSourceDefService.class.getName(),
                        dataSourceServiceProvider,
                        platformServiceProps
                );
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.apsDataSourceDefServiceReg != null) {
            try {
                this.apsDataSourceDefServiceReg.unregister();
                this.apsDataSourceDefServiceReg = null;
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
        }

        if (this.logger != null) {
            this.logger.stop(context);
            this.logger = null;
        }
    }

}
