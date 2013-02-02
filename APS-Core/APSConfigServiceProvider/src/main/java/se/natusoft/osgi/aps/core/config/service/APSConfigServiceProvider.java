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
 *         2011-08-14: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.service;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigException;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigAdminImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEditModelImpl;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore.ConfigUpdateListener;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.exceptions.APSNoServiceAvailableException;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is an implementation of the APSConfigService.
 */
public class APSConfigServiceProvider implements APSConfigService, ConfigUpdateListener {
    //
    // Private Members
    //

    /** Our logger. */
    APSLogger logger = null;

    /**
     * The standard OSGi configuration admin service. All configuration is
     * stored here during runtime. All changes are doubled on disk and
     * reloaded here on start.
     * <p>
     * Please note that only configuration for the active environment will
     * be stored here!
     */
    private ConfigurationAdmin osgiConfigAdmin = null;

    /** Holds the active configurations in memory. */
    private APSConfigMemoryStore memoryStore;

    /** The available configuration environments. */
    private APSConfigEnvStore envStore = null;

    /** The configuration data store. */
    private APSConfigPersistentStore configStore = null;

    /** Holds configurations that are synced with the ConfigurationAdmin. */
    private Map<APSConfigAdmin, Configuration> configAdminSynced = new HashMap<APSConfigAdmin, Configuration>();

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigServiceProvider instance.
     *
     * @param logger our logger.
     * @param osgiConfigAdmin The standard OSGi admin service.
     * @param memoryStore The store for storing in memory configurations.
     * @param envStore The store for persistent configuration environment definitions.
     * @param configStore The store for persistent configuration data.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException on any failure.
     */
    public APSConfigServiceProvider(
            APSLogger logger,
            ConfigurationAdmin osgiConfigAdmin,
            APSConfigMemoryStore memoryStore,
            APSConfigEnvStore envStore,
            APSConfigPersistentStore configStore
        ) throws APSConfigException {

        this.logger = logger;
        this.osgiConfigAdmin = osgiConfigAdmin;
        this.memoryStore = memoryStore;
        this.envStore = envStore;
        this.configStore = configStore;

        this.memoryStore.addUpdateListener(this);
    }


    //
    // Service implementation methods.
    //

    /**
     * Registers a configuration Class with the configuration service. The passed
     * class must extend APSConfig and be annotated with @APSConfigDescription. Values
     * must be public fields annotated with @APSConfigItemDescription and of one of the
     * following types:
     * <ul>
     *     <li>APSConfigValue - A simple value.</li>
     *     <li>APSConfigValueList - A list of values.</li>
     *     <li>? extends APSConfig - Another configuration class following the same rules as this one.</li>
     *     <li>APSConfigList&lt;? extends APSConfig&gt; - A list of another configuration class.</li>
     * </ul>
     * <p>
     * The values of the configuration are editable with the APSConfigAdminService
     * which will also persist the configuration values.
     * <p>
     * If the version of the config Class is new (first time registered) and the prevVersion has been provided
     * then the configuration values of the previous version will be loaded and then saved with this version.
     * Any new values will ofcourse have the default values.
     * <p>
     * This should be called on bundle start. It will load the configuration from persistent store
     * (when such is available) into memory for fast access. A configuration needs to be edited
     * through APSConfigAdminService before it is persisted. Before that only the default values will
     * be returned.
     * <p/>
     * Please also call unregisterConfiguration(...) on bundle stop!
     *
     * @param configClass The config class to register.
     * @param forService If true then this configuration is for a service and will also be registered in the
     *                   standard OSGi configuration service.
     *
     * @throws APSConfigException on bad configClass interface.
     */
    @Override
    public void registerConfiguration(Class<? extends APSConfig> configClass, boolean forService) throws APSConfigException {
        APSConfigAdminImpl config = this.configStore.loadConfiguration(configClass);
        this.memoryStore.addConfiguration(config);

        if (this.osgiConfigAdmin != null && forService) {
            try {
                Configuration configuration = this.osgiConfigAdmin.getConfiguration(config.getConfigId(), null);
                configuration.update(config.getConfigInstanceMemoryStore().getProperties());
                this.configAdminSynced.put(config, configuration);
            }
            catch (APSNoServiceAvailableException nsae) {
                throw new APSConfigException("Failed to register configuration", nsae);
            }
            catch (IOException ioe) {
                throw new APSConfigException("Failed to register configuration!", ioe);
            }
        }
    }

    /**
     * This tells the APSConfigService that the specified configuration is no longer actively used by anyone and will be
     * removed from memory.
     * <p>
     * This should always be done on bundle stop.
     *
     * @param configClass The config Class for the configuration.
     */
    @Override
    public void unregisterConfiguration(Class<? extends APSConfig> configClass) {
        APSConfigDescription configAnn = configClass.getAnnotation(APSConfigDescription.class);
        String configId= configAnn.configId();
        String version = configAnn.version();

        APSConfigAdmin config = this.memoryStore.getConfiguration(configId, version);
        this.memoryStore.removeConfiguration(config);

        if (this.osgiConfigAdmin != null) {
            Configuration configuration = this.configAdminSynced.get(config);
            if (configuration != null) {
                try {
                    configuration.delete();
                }
                catch (IOException ioe) {
                    this.logger.error("Failed to delete ConfigurationAdmin created config!", ioe);
                }
                this.configAdminSynced.remove(config);
            }
        }
    }

    /**
     * Returns the configuration for the specified configuration Class.
     *
     * @param <Config> The configuration type which must be a subclass of APSConfig.
     * @param configClass The configuration Class to get the configuration for.
     *
     * @return An populated config Class instance.
     *
     * @throws APSConfigException on failure to get configuration.
     */
    @Override
    public <Config extends APSConfig> Config getConfiguration(Class<Config> configClass) throws APSConfigException {
        APSConfigDescription configAnn = configClass.getAnnotation(APSConfigDescription.class);
        String configId= configAnn.configId();
        String version = configAnn.version();

        APSConfigAdminImpl config = this.memoryStore.getConfiguration(configId, version);
        if (config == null) {
            throw new APSConfigException("The configuraition specified by  '" + configClass.getName() + "' does not exist! " +
                    "A configuration must be registered before it can be fetched!");
        }

        return ((APSConfigEditModelImpl<Config>)config.getConfigModel()).getInstance();
    }

    //
    // Internal support methods
    //

    /**
     * Called when the a config has been updated in the memory store.
     *
     * @param config The updated config.
     */
    @Override
    public void updated(APSConfigAdminImpl config) {
        Configuration configuration = this.configAdminSynced.get(config);
        if (configuration != null) {
            try {
                configuration.update(config.getConfigInstanceMemoryStore().getProperties());
            }
            catch (IOException ioe) {
                this.logger.error("Failed to update ConfigurationAdmin config!", ioe);
            }
        }
    }

    /**
     * Called by the activator on stop.
     */
    public void cleanup() {
        this.memoryStore.removeUpdateListener(this);
    }

}
