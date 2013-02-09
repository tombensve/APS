/* 
 * 
 * PROJECT
 *     Name
 *         APS Filesystem Service Provider
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         Provides access to a service/application private filesystem that remains until the
 *         service/application specifically deletes it. This is independent of the OSGi server
 *         it is running in (if configured).
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
 *         2011-08-03: Created!
 *         
 */
package se.natusoft.osgi.aps.core.filesystem;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService;
import se.natusoft.osgi.aps.core.filesystem.service.APSFilesystemServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException;

import java.io.File;
import java.util.Dictionary;
import java.util.Properties;

/**
 * APSFilesystemActivator for filesystem service.
 */
public class APSFilesystemActivator implements BundleActivator {
    //
    // Constants
    //
    
    /** The Pid of the filesytem service. */
    private static final String FS_SVC_PID = APSFilesystemServiceProvider.class.getName();
    
    //
    // Required Services
    //
    
    //
    // Provided Services
    //
    
    /** The published APSFilesystemService.. */
    private ServiceRegistration filesytemService = null;
    
    //
    // Other Members
    //
    
    /** The LogService utility wrapper. */
    private APSLogger logger = null;
    
    //
    // Methods
    //
    

    /**
     * Starts the bundle.
     * 
     * @param context The bundle context.
     * 
     * @throws Exception on failure to start the bundle.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        
        // Initialize logging
        this.logger = new APSLogger();
        this.logger.start(context);
        
        // Initialize ConfigurationAdmin.
        APSServiceTracker<ConfigurationAdmin> configAdminTracker = new APSServiceTracker<ConfigurationAdmin>(context, ConfigurationAdmin.class, "5 seconds");
        configAdminTracker.start();

        // Create a default configuration if none exist.
        Dictionary initConf = null;
        try {
            ConfigurationAdmin configAdmin = configAdminTracker.allocateService();
            Configuration conf = configAdmin.getConfiguration(FS_SVC_PID);
            if (conf == null) {
                conf = configAdmin.createFactoryConfiguration(FS_SVC_PID);
                initConf = getDefaultConfigProps(context.getDataFile("."));
                conf.update(initConf);
            }
            else {
                Dictionary defaultProps = conf.getProperties();
                if (defaultProps == null || defaultProps.get(APSFilesystemService.CONF_APS_FILESYSTEM_ROOT) == null) {
                    initConf = getDefaultConfigProps(context.getDataFile("."));
                    conf.update(initConf);
                }
                else {
                    initConf = defaultProps;
                }
            }
        }
        catch (APSNoServiceAvailableException nsae) {
            initConf = getDefaultConfigProps(context.getDataFile("."));
        }
        finally {
            configAdminTracker.releaseService();
            configAdminTracker.stop(context);
        }
        String fsRoot = (String)initConf.get(APSFilesystemService.CONF_APS_FILESYSTEM_ROOT);

        // Register APSFilesystemService implementation.
        Dictionary props = new Properties();
        props.put(Constants.SERVICE_PID, FS_SVC_PID);  
        APSFilesystemServiceProvider fsProvider = new APSFilesystemServiceProvider(fsRoot, logger);
        this.filesytemService = context.registerService(APSFilesystemService.class.getName(), fsProvider, props);
        
        // Allow the logger to pass the service reference to the log service to identify it as logger.
        logger.setServiceReference(this.filesytemService.getReference());
        
        logger.info("APSFilesystemService Activator: Bundle started!");
    }

    /**
     * Returns the default configuraiton properties.
     */
    private Dictionary getDefaultConfigProps(File frameworkFileArea) {
        Properties props = new Properties();

        if (System.getProperty(APSFilesystemService.CONF_APS_FILESYSTEM_ROOT) != null) {
            props.setProperty(APSFilesystemService.CONF_APS_FILESYSTEM_ROOT, System.getProperty(APSFilesystemService.CONF_APS_FILESYSTEM_ROOT));
        }
        else {
            // This is a bad default!
            //props.setProperty(APSFilesystemService.CONF_APS_FILESYSTEM_ROOT, frameworkFileArea.getAbsolutePath());

            // This is a safer default!
            props.setProperty(APSFilesystemService.CONF_APS_FILESYSTEM_ROOT, System.getProperty("user.home") +
                    File.pathSeparator + ".aps" + File.pathSeparator + "apsfs");
            File fsDir = new File(props.getProperty(APSFilesystemService.CONF_APS_FILESYSTEM_ROOT));
            fsDir.mkdirs();
        }
        return props;
    }
    
    /**
     * Stops the bundle.
     * 
     * @param context The bundle context.
     * 
     * @throws Exception On any failure to stop the bundle.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        
        this.filesytemService.unregister();

        this.logger.info("APSFilesystemService Activator: Bundle stopped!");
        this.logger.stop(context);

        this.filesytemService = null;
        this.logger = null;
    }
}
