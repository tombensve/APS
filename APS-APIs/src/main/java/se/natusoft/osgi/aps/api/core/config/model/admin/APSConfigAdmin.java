/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.1
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
 *     tommy ()
 *         Changes:
 *         2011-08-13: Created!
 *         2012-02-14: Cleaned up.
 *
 */
package se.natusoft.osgi.aps.api.core.config.model.admin;

/**
 * This represents a specific configuration.
 *
 * This API is intended for editing configuration. Start by getting the APSConfigModel that represents the configuration structure. Then
 * each model value (_APSConfigValueModel_) can be used to get or set configuration values using this API. It is the config models
 * that generate the keys for the values in the store, which is why they are used to set and get configuration values.
 *
 * Please note that some configuration values are not dependent on configuration environment while others are. Those configuration values
 * whose definition are annotated with _@APSConfigItemDescription(environmentSpecific=true)_ will have different values depending on the
 * configuration environment, which is why that is also passed to all get and set methods. Easiest is to not care which values are env
 * specific and which are and always pass a configuration environment. If you are making a GUI editor for the configuration it is however
 * a good idea to check if a value is configuration environment specific or not so that the user can choose for which environment to set
 * a value. This information is available in _APSConfigValueModel.isConfigEnvironmentSpecific()_. The _APSConfigAdminService_ also
 * provides all defined configuration environments since it is also used for defining the configuration environments.
 */
public interface APSConfigAdmin {

    /**
     * Returns the configuration id.
     */
    String getConfigId();

    /**
     * Returns the group if any of this config.
     */
    String getGroup();

    /**
     * Returns the version of the configuration.
     */
    String getVersion();

    /**
     * Returns a configuration value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is gotten for the specific config env when this argument
     *                          is non null, and for first that have a value if this is null. It is however strongly recommended to always pass
     *                          a valid config env for this.
     * @return The referenced config value or a default value.
     */
    String getConfigValue(APSConfigValueEditModel valueEditModel, APSConfigEnvironment configEnvironment);

    /**
     * Sets a configuration value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param value The value to set.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is set for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null. It is however strongly recommended to always pass
     *                          a valid config env for this.
     */
    void setConfigValue(APSConfigValueEditModel valueEditModel, String value, APSConfigEnvironment configEnvironment);

    /**
     * Removes a configuration value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is removed for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null, making null a more useful value in this case.
     * @return The removed value.
     */
    String removeConfigValue(APSConfigValueEditModel valueEditModel, APSConfigEnvironment configEnvironment);

    /**
     * Gets a configuration "many" value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param index The index of the "many" value.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is gotten for the specific config env when this argument
     *                          is non null, and for first that have a value if this is null. It is however strongly recommended to always pass
     *                          a valid config env for this.
     * @return The referenced config value or a default value.
     */
    String getConfigValue(APSConfigValueEditModel valueEditModel, int index, APSConfigEnvironment configEnvironment);

    /**
     * Sets a configuration "many" value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param index The index of the "many" value.
     * @param value the many value to set.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is set for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null. It is however strongly recommended to always pass
     *                          a valid config env for this.
     */
    void setConfigValue(APSConfigValueEditModel valueEditModel, int index, String value, APSConfigEnvironment configEnvironment);

    /**
     * Adds a configuration "many" value last.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param value The value to add.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is removed for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null, making null a more useful value in this case.
     */
    void addConfigValue(APSConfigValueEditModel valueEditModel, String value, APSConfigEnvironment configEnvironment);

    /**
     * Removes a configuration "many" value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param index The index to remove.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is removed for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null, making null a more useful value in this case.
     * @return The removed value.
     */
    String removeConfigValue(APSConfigValueEditModel valueEditModel, int index, APSConfigEnvironment configEnvironment);

    /**
     * The _APSConfigEditModel_ that represents an _APSConfigList_ represents just the list itself. This returns a new _APSConfigEditModel_
     * that represents an entry at a specific index in the list and can be used to set and get values for the config entry at that index.
     *
     * The returned model is a temporary model and should **only** be used to access and modify values of the specified index.
     * Never ever pass such a model as input to any of the get/add/removeConfig() methods. It should only be used with
     * the _\*configValue()_ methods. Also note that the returned model will return false for _isMany()_ since it is not
     * representing the whole list, but an entry in the list.
     *
     * @param configModel The original config model representing its structural place.
     * @param index The index the get a version for.
     *
     * @return A config model representing the specified index.
     */
    APSConfigEditModel getConfigListEntry(APSConfigEditModel configModel, int index);

    /**
     * The APSConfigModel that represents an APSConfigList represents just the list itself. This creates a new list entry and
     * returns a model representing that entry and can be used to set and get values in the entry.
     *
     * The returned model is a temporary model and should **only** be used to access and modify values of the specified index.
     * Never ever pass such a model as input to any of the _get/add/removeConfig()_ method. It should only be used with
     * the _\*configValue()_ methods. This model will return false for _isMany()_!
     *
     * @param configModel The config model representing the APSConfigList.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is removed for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null, making null a more useful value in this case.
     * @return A new config model representing the created list entry.
     */
    APSConfigEditModel createConfigListEntry(APSConfigEditModel configModel, APSConfigEnvironment configEnvironment);

    /**
     * This removes a config entry in the _APSConfigList_ represented by the specified _APSConfigModel_.
     *
     * @param configModel The config model representing the config entry to remove.
     * @param index The index of the entry to remove.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is removed for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null, making null a more useful value in this case.
     */
    void removeConfigListEntry(APSConfigEditModel configModel, int index, APSConfigEnvironment configEnvironment);

    /**
     * Gets the number of values of a "many" value.
     * <p/>
     * Please note that APSConfigModel is a subclass of APSConfigValueModel. This method is also valid for
     * APSConfigModels.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param configEnvironment The config environment to get the number of values for.
     *
     * @return The size.
     */
    int getSize(APSConfigValueEditModel valueEditModel, APSConfigEnvironment configEnvironment);

    /**
     * @return the structural root model of the configuration.
     */
    APSConfigEditModel getConfigModel();

    /**
     * This object represents a specific configuration. When editing a configuration you might not want changed values to
     * have immediate effect, but rather have the user do a "save" to change the active configuration. In this case use
     * this method to clone the config, edit that, then on save call _APSConfigAdminService.updateConfiguration(clone)_.
     */
    APSConfigAdmin cloneConfig();

    /**
     * Sends an event to all listeners of modified config.
     *
     * This needs to be called by any code that updates config values. I do not
     * want to trigger this for every changed value!
     */
    void sendConfigModifiedEvent();

}
