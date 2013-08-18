/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.9.2
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
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.core.config.api.APSConfigSyncMgmtService;
import se.natusoft.osgi.aps.core.config.service.APSConfigAdminServiceProvider;
import se.natusoft.osgi.aps.core.config.service.APSConfigServiceExtender;
import se.natusoft.osgi.aps.core.config.service.APSConfigServiceProvider;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.core.config.store.APSFileTool;
import se.natusoft.osgi.aps.core.config.sync.Synchronizer;
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

    /** Publish a bundle provided APSConfigSyncMgmtService. */
    private ServiceRegistration syncMgmtService = null;

    //
    // Required services
    //

    /** The standard OSGi ConfigAdmin servcie. */
    private APSServiceTracker<ConfigurationAdmin> configurationAdminTracker = null;

    /** The APS filesystem service used for storing config data. */
    private APSServiceTracker<APSFilesystemService> fsServiceTracker = null;

    /** Tracker for the APSGroupsService. */
    private APSServiceTracker<APSGroupsService> groupsServiceTracker = null;

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

    /** Provider of APSConfigAdmin service. Also needed by the synchronizer. */
    private APSConfigAdminServiceProvider configAdminProvider = null;

    /** Storage for configuration environments. Also needed by the synchronizer. */
    private APSConfigEnvStore envStore = null;

    /** In memory configuration storage. Also needed by the synchronizer. */
    private APSConfigMemoryStore memoryStore = null;

    /** Persistent configuration storage. Also needed by the synchronizer. */
    private APSConfigPersistentStore configStore = null;

    /** Used for sychronizing between installations. */
    private Synchronizer synchronizer = null;

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
        this.envStore = new APSConfigEnvStore(fileTool);
        this.memoryStore = new APSConfigMemoryStore();
        this.configStore = new APSConfigPersistentStore(fileTool, envStore, configLogger);

        // Register APSConfigAdminService
        this.configAdminProvider = new APSConfigAdminServiceProvider(this.configAdminLogger,
                memoryStore, envStore, configStore);
        Dictionary adminProps = new Properties();
        adminProps.put(Constants.SERVICE_PID, APSConfigAdminServiceProvider.class.getName());
        this.configAdminService = this.context.registerService(APSConfigAdminService.class.getName(), this.configAdminProvider,
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

        // Setup sychronization tracker
        this.groupsServiceTracker = new APSServiceTracker<APSGroupsService>(context, APSGroupsService.class,
                APSServiceTracker.LARGE_TIMEOUT);
        this.groupsServiceTracker.start();

        // We create and start a new Synchronizer when an active APSGroupsService becomes available, and stop it when
        // the active APSGroupsService leaves. This because the Synchronizer need to rejoin the group when there is a
        // new APSGroupsService since membership is automatically removed when the service goes away.

        this.groupsServiceTracker.onActiveServiceAvailable(new OnServiceAvailable<APSGroupsService> () {
            public void onServiceAvailable(APSGroupsService groupsService, ServiceReference serviceReference) throws Exception {
                APSConfigServiceActivator.this.synchronizer =
                        new Synchronizer(configAdminLogger, configAdminProvider, envStore, memoryStore,
                                configStore, groupsService);
                APSConfigServiceActivator.this.synchronizer.start();

                Dictionary props = new Properties();
                props.put(Constants.SERVICE_PID, Synchronizer.class.getName());
                APSConfigServiceActivator.this.syncMgmtService =
                        APSConfigServiceActivator.this.context.registerService(APSConfigSyncMgmtService.class.getName(),
                        APSConfigServiceActivator.this.synchronizer, props);
            }
        });

        this.groupsServiceTracker.onActiveServiceLeaving(new OnServiceLeaving<APSGroupsService>() {
            @Override
            public void onServiceLeaving(ServiceReference service, Class serviceAPI) throws Exception {
                // We have to synchronize here since there will be a potential shutdown conflict if the
                // whole server is taken down. In that case it is possible that this executes at the same
                // time as takedownServcies(context), which also shuts down the synchronizer.
                synchronized (APSConfigServiceActivator.this) {
                    APSConfigServiceActivator.this.syncMgmtService.unregister();
                    if (APSConfigServiceActivator.this.synchronizer != null) {
                        APSConfigServiceActivator.this.synchronizer.stop();
                        APSConfigServiceActivator.this.synchronizer = null;
                    }
                }
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        takedownServices(context);
    }

    private  void takedownServices(BundleContext context) throws Exception {
        if (this.configServiceExtender != null) {
            this.context.removeBundleListener(this.configServiceExtender);
            this.configServiceExtender = null;
        }

        synchronized (this) {
            if (this.synchronizer != null) {
                this.synchronizer.stop();
                this.synchronizer = null;
            }
        }

        if (this.groupsServiceTracker != null) {
            this.groupsServiceTracker.stop(context);
            this.groupsServiceTracker = null;
        }

        if (this.configured) {
            if (this.configAdminService != null) {this.configAdminService.unregister();}
            if (this.configAdminLogger != null) {
                this.configAdminLogger.info("APSConfigAdminServiceProvider have been unregistered!");
                this.configAdminLogger.stop(context);
            }
            if (this.configService != null) {
                this.configService.unregister();
            }
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
