/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *         2011-05-15: Created!
 *
 */
package se.natusoft.osgi.aps.api.core.config.service;

import se.natusoft.osgi.aps.api.core.config.APSConfig;


/**
 * This defines the Application-Platform-Services configuration service API.
 *
 * Please note that this will always return the configuration for the currently
 * selected environment (which only _APSConfigAdminService_ can change).
 *
 * Please note that if you let your configuration be auto-managed by specifying
 * the "APS-Configs: fully_qualified_name_to_config_class,..." MANIFEST.MF entry
 * then this service does not need to be used.
 */
public interface APSConfigService {

    /**
     * Registers a configuration Class with the configuration service. The passed
     * class must extend APSConfig and be annotated with _@APSConfigDescription_. Values
     * must be public fields annotated with _@APSConfigItemDescription_ and of one of the
     * following types:
     *
     *  * _APSConfigValue_ - A simple value.
     *  * _APSConfigValueList_ - A list of values.
     *  * _? extends APSConfig_ - Another configuration class following the same rules as this one.
     *  * _APSConfigList\<? extends APSConfig\>_ - A list of another configuration class.
     *
     * The values of the configuration are editable with the _APSConfigAdminService_
     * which will also persist the configuration values.
     *
     * If the version of the config Class is new (first time registered) and the prevVersion has been provided
     * then the configuration values of the previous version will be loaded and then saved with this version.
     * Any new values will ofcourse have the default values.
     *
     * This should be called on bundle start. It will load the configuration from persistent store
     * (when such is available) into memory for fast access. A configuration needs to be edited
     * through _APSConfigAdminService_ before it is persisted. Before that only the default values will
     * be returned.
     *
     * Please also call _unregisterConfiguration(...)_ on bundle stop!
     *
     * @param configClass The config class to register.
     * @param forService If true then this configuration is for a service and will also be registered in the
     *                   standard OSGi configuration service. (Note: This was a bad idea and is quite useless!)
     *
     * @throws APSConfigException on bad configClass interface.
     */
    void registerConfiguration(Class<? extends APSConfig> configClass, boolean forService) throws APSConfigException;

    /**
     * This tells the _APSConfigService_ that the specified configuration is no longer actively used by anyone and will be
     * removed from memory.
     *
     * This should always be done on bundle stop.
     *
     * @param configClass The config Class for the configuration.
     */
    void unregisterConfiguration(Class<? extends APSConfig> configClass);

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
    <Config extends APSConfig> Config getConfiguration(Class<Config> configClass) throws APSConfigException;
}
