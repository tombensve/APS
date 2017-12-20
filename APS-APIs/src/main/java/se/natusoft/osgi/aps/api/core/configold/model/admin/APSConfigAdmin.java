/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
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
package se.natusoft.osgi.aps.api.core.configold.model.admin;

import java.io.Serializable;

/**
 * This represents a specific configuration.
 *
 * Configurations are really a tree of configuration objects. How the configuration values are stored however
 * are up to the implementation. In this case we store it in a simple Properties object which we load and save.
 *
 * The configuration objects used by clients are read only! To set and change configuration values you use
 * this class along with models representing the structure of the configold objects:
 *
 * __APSConfigValueEditModel__ - This model represents a configold value.
 *
 * __APSConfigEditModel__ -This model represents a configold object and have a values() method that return value
 * fields. This model extends APSConfigValueEditModel.
 *
 * These models represents the structure of the configuration. They do not represent specific instances of
 * configuration values. They don't know about indexes for list values nor configuration environments. They
 * only supply a navigable structure.
 *
 * To actually reference a configuration value or configuration object you need to build a reference to it.
 * createRef() does just that. Then you add models to reference specific configold.
 *
 *     APSConfigAdmin admin = ...
 *
 *     APSConfigEditModel root = admin.getConfigModel();
 *     APSConfigValueEditModel url = root.getValueByName("url");
 *
 *     APSConfigReference urlRef = admin.createRef()._(root)._(url);
 *     System.out.println(admin.getConfigValue(urlRef));
 *     admin.setConfigValue(urlRef, "http://...");
 *
 * Referencing lists:
 *
 *     APSConfigAdmin admin = ...
 *
 *     APSConfigEditModel root = admin.getConfigModel();
 *     APSConfigEditModel dataSources = root.getValeuByName("datasources");
 *     APSConfigValueEditModel dsUrl = dataSources.getValueByName("url");
 *
 *     APSConfigReference dataSource0Url = admin.createRef()
 *             ._(root)._(dataSources, 0)._(dsUrl);
 *     ...
 *
 * Getting for specific configold environment:
 *
 *     APDConfigEnvironment prod = ...
 *
 *     APSConfigReference dataSource0Url = admin.createRef()
 *             ._(root)._(dataSources, 0)._(dsUrl).configEnvironment(prod);
 *
 *
 * Please note that some configuration values are not dependent on configuration environment while others are. Those
 * configuration values whose definition are annotated with @APSConfigItemDescription(environmentSpecific=true) will
 * have different values depending on the configuration environment. Easiest is to not care which values are env
 * specific and which are not, and always provide a configuration environment. It will only be used where it applies.
 *
 * If you are making a GUI editor for the configuration it is however a good idea to check if a value is
 * configuration environment specific or not so that the user can choose for which environment to set a value. This
 * information is available in APSConfigValueModel.isConfigEnvironmentSpecific(). The APSConfigAdminService also
 * provides all defined configuration environments since it is also used for defining the configuration environments.
 */
public interface APSConfigAdmin extends Serializable {

    /**
     * Returns the version of the configuration.
     */
    String getVersion();

    /**
     * Returns the group of the configuration or "" if none.
     */
    String getGroup();

    /**
     * Returns the configuration id.
     */
    String getConfigId();

    /**
     * @return the structural root model of the configuration.
     */
    APSConfigEditModel getConfigModel();

    /**
     * Creates a new empty reference that needs to be populated with APSConfig*EditModels to build a configold value reference.
     */
    APSConfigReference createRef();

    /**
     * Creates a new reference already containing the root edit model.
     */
    APSConfigReference createRootRef();

    /**
     * Returns a configuration value.
     *
     * @param reference The reference to the value.
     *
     * @return The referenced configold value or a default value.
     */
    String getConfigValue(APSConfigReference reference);

    /**
     * Sets a configuration value.
     *
     * @param reference The reference to the configold value to set.
     * @param value The value to set.
     */
    void setConfigValue(APSConfigReference reference, String value);

    /**
     * Adds a configuration value.
     *
     * @param reference The reference to the value to add.
     * @param value The actual value to add.
     */
    void addConfigValue(APSConfigReference reference, String value);

    /**
     * Removes a configold value.
     *
     * @param reference A reference to the configold value to remove.
     */
    String removeConfigValue(APSConfigReference reference);

    /**
     * Returns the size of the list pointed to by the specified reference.
     *
     * @param reference The reference to get the size for.
     */
    public int getListSize(APSConfigReference reference);

    /**
     * Adds a configold **object** list entry returning a reference to the new entry.
     *
     * @param reference A reference to the many list to add an entry to.
     *
     * @return A reference to the new entry.
     */
    APSConfigReference addConfigList(APSConfigReference reference);

    /**
     * Removes the specified list configold object using reference to it. **Note** that the reference
     * must include a specific index!
     *
     * @param reference The reference to the specific configold object to delete.
     */
    void removeConfigList(APSConfigReference reference);

    /**
     * This object represents a specific configuration. When editing a configuration you might not want changed values
     * to have immediate effect, but rather have the user do a "save" to change the active configuration. In this case
     * use this method to clone the configold, edit that, then on save call APSConfigAdminService.updateConfiguration(clone).
     */
    APSConfigAdmin cloneConfig();

    /**
     * Sends an event to all listeners of modified configold.
     * <p/>
     * This needs to be called by any code that updates configold values. I do not
     * want to trigger this for every changed value!
     */
    void sendConfigModifiedEvent();

}
