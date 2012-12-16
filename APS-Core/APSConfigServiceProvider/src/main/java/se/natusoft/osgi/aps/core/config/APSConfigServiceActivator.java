/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         1.0.0
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
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService;
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem;
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService;
import se.natusoft.osgi.aps.core.config.service.APSConfigAdminServiceProvider;
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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Dictionary;
import java.util.Properties;

public class APSConfigServiceActivator implements BundleActivator, BundleListener {
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

        this.context.addBundleListener(this);
        // Check already started bundles that we might have missed.
        for (Bundle bundle : this.context.getBundles()) {
            handleBundleStart(bundle);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        takedownServices(context);
    }

    private  void takedownServices(BundleContext context) {
        this.context.removeBundleListener(this);

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

    //
    // Extender handling
    //

    /**
     * Receives notification that a bundle has had a lifecycle change.
     *
     * @param event The <code>BundleEvent</code>.
     */
    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.STARTED) {
            handleBundleStart(event.getBundle());
        }
        else if (event.getType() == BundleEvent.STOPPED) {
            handleBundleStop(event.getBundle());
        }
    }

    /**
     * Handles config registration and injection for bundle config classes.
     *
     * @param bundle The bundle to handle.
     */
    private void handleBundleStart(Bundle bundle) {
        Dictionary<String, String> headers = bundle.getHeaders();
        String configClasses = headers.get("APS-Configs");
        if (configClasses != null) {
            for (String configClass : configClasses.split(",")) {
                configClass = configClass.trim();

                try {
                    Class cfgClass = bundle.loadClass(configClass);
                    this.configServiceProvider.registerConfiguration(cfgClass, false);
                    this.configLogger.info("Registered configuration class '" + configClass + "' for bundle '" +
                            bundle.getSymbolicName() + "'.");

                    // Lets find the first public static instance of the configuration in the class.
                    Field confInstField = null;
                    Field managedConfigField = null;
                    for (Field field : cfgClass.getDeclaredFields()) {
                        if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                            if (field.getType().equals(cfgClass)) {
                                confInstField = field;
                            }
                            else if (field.getType().equals(ManagedConfig.class)) {
                                managedConfigField = field;
                            }
                        }
                    }

                    try {
                        if (confInstField != null) {
                            confInstField.set(null, this.configServiceProvider.getConfiguration(cfgClass));
                            this.configLogger.info(
                                    "Injected configuration instance '" + configClass + "' into '" +
                                            confInstField.getName() + "' for bundle '" + bundle.getSymbolicName() + "'."
                            );
                        }
                        if (managedConfigField != null) {
                            ManagedConfig managedConfig = (ManagedConfig)managedConfigField.get(null);
                            if (managedConfig != null) {
                                managedConfig.serviceProviderAPI.setConfigInstance(this.configServiceProvider.getConfiguration(cfgClass));
                                managedConfig.serviceProviderAPI.setManaged();
                            }
                        }

                    } catch (IllegalAccessException iae) {
                        // This should theoretically never happen since we have already determined
                        // that the field is both public and static.
                        this.configLogger.error(
                                "Failed to set configuration instance of type '" + configClass + "' in " +
                                        "bundle '" + bundle.getSymbolicName() + "' of name '" + confInstField.getName() + "'!",
                                iae
                        );
                    }


                } catch (ClassNotFoundException cnfe) {
                    this.configLogger.error(
                            "Bundle '" + bundle.getSymbolicName() + "' has defined '" + configClass + "' " +
                            "as an APS configuration class, but it cannot be loaded from the bundle!",
                            cnfe
                    );
                }

            }
        }
    }

    /**
     * Handles config deregistration for bundle config classes.
     * @param bundle
     */
    private void handleBundleStop(Bundle bundle) {
        Dictionary<String, String> headers = bundle.getHeaders();
        String configClasses = headers.get("APS-Configs");
        if (configClasses != null) {
            for (String configClass : configClasses.split(",")) {
                configClass = configClass.trim();

                try {
                    Class cfgClass = bundle.loadClass(configClass);
                    this.configServiceProvider.unregisterConfiguration(cfgClass);
                    this.configLogger.info(
                            "Unregistered configuration '" + configClass + "' for bundle '" +
                            bundle.getSymbolicName() + "'!"
                    );

                } catch (ClassNotFoundException cnfe) {
                    // This should already have happened at start of bundle and been logged then.
                    // so we keep quiet now.
                }
            }
        }
    }
}
