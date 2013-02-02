/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         A more advanced configuration service that uses annotated interfaces to
 *         describe and provide access to configuration. It supports structured
 *         configuration models.
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
 *         2011-08-05: Created!
 *
 */
package se.natusoft.osgi.aps.core.config;

import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService;
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem;
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService;
import se.natusoft.osgi.aps.core.config.service.APSConfigAdminServiceProvider;
import se.natusoft.osgi.aps.core.config.service.APSConfigServiceExtender;
import se.natusoft.osgi.aps.core.config.service.APSConfigServiceProvider;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.core.config.store.APSFileTool;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable;
import se.natusoft.osgi.aps.tools.tracker.OnServiceLeaving;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

public class APSConfigServiceActivator implements BundleActivator {
    //
    // Provided services
    //

    /** The APS configuration admin service. */
    private ServiceRegistration configAdminService = null;

    /** The APS configuration service. */
    private ServiceRegistration configService = null;

    //
    // Required services
    //

    /** The standard OSGi ConfigAdmin servcie. */
    private APSServiceTracker<ConfigurationAdmin> configurationAdminTracker = null;

    /** The APS filesystem service used for storing config data. */
    private APSServiceTracker<APSFilesystemService> fsServiceTracker = null;

    //
    // Other Members
    //

    /** Our bundle context. */
    private BundleContext context = null;

    /** Logger for config admin service. */
    private APSLogger configAdminLogger = null;

    /** Logger for config service. */
    private APSLogger configLogger = null;

    /** This is set to true when all services have been csetup, and false when they have been taken down. */
    private boolean configured = false;

    /** This is needed for the extender handling. */
    private APSConfigServiceProvider configServiceProvider = null;

    /** The auto config extender. */
    private APSConfigServiceExtender configServiceExtender = null;

    //
    // Bundle Management
    //

    @Override
    public void start(final BundleContext context) throws Exception {
        this.context = context;

        // Setup logging
        this.configAdminLogger = new APSLogger(System.out);
        this.configAdminLogger.start(context);
        this.configAdminLogger.setLoggingFor("aps-config-service-provider(APSConfigAdminService)");
        this.configLogger = new APSLogger(System.out);
        this.configLogger.start(context);
        this.configLogger.setLoggingFor("aps-config-service-provider(APSConfigService)");

        // Setup ConfigurationAdmin
        this.configurationAdminTracker = new APSServiceTracker<ConfigurationAdmin>(context, ConfigurationAdmin.class, APSServiceTracker.LARGE_TIMEOUT);
        this.configurationAdminTracker.start();

        // Setup APSFilesystemService
        this.fsServiceTracker = new APSServiceTracker<APSFilesystemService>(context, APSFilesystemService.class, APSServiceTracker.LARGE_TIMEOUT);
        this.fsServiceTracker.setDebugLogger(new APSLogger(System.out));
        this.fsServiceTracker.start();

        this.fsServiceTracker.onActiveServiceAvailable(new OnServiceAvailable<APSFilesystemService>() {
            public void onServiceAvailable(APSFilesystemService fsService, ServiceReference serviceReference) throws Exception {
                setupServices(fsService);
            }
        });

        this.fsServiceTracker.onActiveServiceLeaving(new OnServiceLeaving<APSFilesystemService>() {
            public void onServiceLeaving(ServiceReference service, Class serviceAPI) throws Exception {
                takedownServices(APSConfigServiceActivator.this.context);
            }
        });
    }

    private void setupServices(APSFilesystemService fsService) throws Exception {
        // Create or get filesystem.
        APSFilesystem fs = null;
        try {
            fs = fsService.getFilesystem(APSConfigServiceProvider.class.getName());
        } catch (IOException ioe) {
            fs = fsService.createFilesystem(APSConfigServiceProvider.class.getName());
        }

        APSFileTool fileTool = new APSFileTool(fs);

        // Create the different config data stores.
        APSConfigEnvStore envStore = new APSConfigEnvStore(fileTool);
        APSConfigMemoryStore memoryStore = new APSConfigMemoryStore();
        APSConfigPersistentStore configStore = new APSConfigPersistentStore(fileTool, envStore, configLogger);

        // Register APSConfigAdminService
        APSConfigAdminServiceProvider configAdminProvider = new APSConfigAdminServiceProvider(this.configAdminLogger,
                memoryStore, envStore, configStore);
        Dictionary adminProps = new Properties();
        adminProps.put(Constants.SERVICE_PID, APSConfigAdminServiceProvider.class.getName());
        this.configAdminService = this.context.registerService(APSConfigAdminService.class.getName(), configAdminProvider,
                adminProps);
        this.configAdminLogger.setServiceReference(this.configAdminService.getReference());
        this.configAdminLogger.info("APSConfigAdminServiceProvider have been registered!");

        // Register APSConfigService
        this.configServiceProvider = new APSConfigServiceProvider(this.configLogger,
               this.configurationAdminTracker.getWrappedService(), memoryStore, envStore, configStore);
        Dictionary props = new Properties();
        props.put(Constants.SERVICE_PID, APSConfigServiceProvider.class.getName());
        this.configService = this.context.registerService(APSConfigService.class.getName(), this.configServiceProvider, props);
        this.configLogger.setServiceReference(this.configService.getReference());
        this.configLogger.info("APSConfigServiceProvider have been registered!");

        this.configured = true;

        this.configServiceExtender = new APSConfigServiceExtender(this.configServiceProvider, this.configLogger);
        this.context.addBundleListener(this.configServiceExtender);
        // Check already started bundles that we might have missed.
        for (Bundle bundle : this.context.getBundles()) {
            this.configServiceExtender.handleBundleStart(bundle);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        takedownServices(context);
    }

    private  void takedownServices(BundleContext context) {
        if (this.configServiceExtender != null) {
            this.context.removeBundleListener(this.configServiceExtender);
            this.configServiceExtender = null;
        }

        if (this.configured) {
            if (this.configAdminService != null) {this.configAdminService.unregister();}
            if (this.configAdminLogger != null) {
                this.configAdminLogger.info("APSConfigAdminServiceProvider have been unregistered!");
                this.configAdminLogger.stop(context);
            }
            if (this.configService != null) {this.configService.unregister();}
            if (this.configLogger != null) {
                this.configLogger.info("APSConfigServiceProvider havve been unregistered!");
                this.configLogger.stop(context);
            }
            this.fsServiceTracker.stop(context);
            this.configurationAdminTracker.stop(context);

            // Let these be garbage collected.
            this.configAdminService = null;
            this.configService = null;
            this.fsServiceTracker = null;
            this.configurationAdminTracker = null;
            this.configAdminLogger = null;
            this.configLogger = null;
        }
    }
}
