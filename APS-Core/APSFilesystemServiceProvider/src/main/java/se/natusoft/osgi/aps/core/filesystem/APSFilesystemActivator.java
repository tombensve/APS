/* 
 * 
 * PROJECT
 *     Name
 *         APS Filesystem Service Provider
 *     
 *     Code Version
 *         0.11.0
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
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService;
import se.natusoft.osgi.aps.core.filesystem.service.APSFilesystemServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

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
        
        String fsRoot = getFSRoot(context);

        // Register APSFilesystemService implementation.
        Dictionary props = new Properties();
        props.put(Constants.SERVICE_PID, FS_SVC_PID);  
        APSFilesystemServiceProvider fsProvider = new APSFilesystemServiceProvider(fsRoot, logger);
        this.filesytemService = context.registerService(APSFilesystemService.class.getName(), fsProvider, props);
        
        // Allow the logger to pass the service reference to the log service to identify it as logger.
        logger.setServiceReference(this.filesytemService.getReference());
        
        logger.info("APSFilesystemService Activator: Bundle started with filesystem root: " + fsRoot);
    }

    /**
     * Returns the file system root.
     * @param context
     */
    private String getFSRoot(BundleContext context) {
        String fsRoot = System.getProperty(APSFilesystemService.CONF_APS_FILESYSTEM_ROOT);
        if (fsRoot == null) {
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                fsRoot = userHome + File.separator + ".apsHome" + File.separator + "filesystems";
                this.logger.info("The system property '" + APSFilesystemService.CONF_APS_FILESYSTEM_ROOT +
                        "' was not found so we look in '" + fsRoot + "' instead!");
            }
            else {
                fsRoot = context.getDataFile(".").getAbsolutePath();
                this.logger.error("The '" + APSFilesystemService.CONF_APS_FILESYSTEM_ROOT + "' system property " +
                        "was not found and system property 'user.home' was not found either so I default to " +
                        "the file path provided by the bundle context: '" + fsRoot + "'! PLEASE NOTE THAT THIS " +
                        "PATH IS NOT VERY PERSISTENT!");
            }
        }

        return fsRoot;
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
