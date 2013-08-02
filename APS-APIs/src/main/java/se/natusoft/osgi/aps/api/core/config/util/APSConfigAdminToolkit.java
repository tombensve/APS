/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.2
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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-05-01: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.config.util;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;

/**
 * This provides a set of static tool methods related to config.
 */
public class APSConfigAdminToolkit {

    /**
     * Makes this non instantiable.
     */
    private APSConfigAdminToolkit() {}

    /**
     * Returns the size of a _many_ type value. It will return 1 for a non _many_ type value.
     *
     * @param confInstRef A config instance reference to get the size for.
     * @param configAdmin The APSConfigAdmin from which the values in the APSConfigInstReference comes from (or this will fail!).
     */
    public static int getSize(APSConfigInstReference confInstRef, APSConfigAdmin configAdmin) {
        if (!confInstRef.getConfigValueEditModel().isMany()) {
            return 1;
        }
        return configAdmin.getSize(confInstRef.getConfigValueEditModel(), confInstRef.getConfigEnvironment());
    }

    /**
     * Returns the configuration value pointed to by the configuration instance reference.
     *
     * @param confInstRef The config instance reference to get the value for.
     * @param configAdmin The APSConfigAdmin from which the values in the APSConfigInstReference comes from (or this will fail!).
     */
    public static String getValue(APSConfigInstReference confInstRef, APSConfigAdmin configAdmin) {
        if (confInstRef.isIndexNeeded()) {
            return configAdmin.getConfigValue(confInstRef.getConfigValueEditModel(), confInstRef.getIndex(), confInstRef.getConfigEnvironment());
        }
        return configAdmin.getConfigValue(confInstRef.getConfigValueEditModel(), confInstRef.getConfigEnvironment());
    }

    /**
     * Updates the configuration value pointed to by the config instance reference.
     *
     * @param confInstRef The config instance reference to update the value for.
     * @param configAdmin The APSConfigAdmin from which the values in the APSConfigInstReference comes from (or this will fail!).
     * @param value The value to update with.
     */
    public static void updateValue(APSConfigInstReference confInstRef, APSConfigAdmin configAdmin, String value) {
        if (confInstRef.isIndexNeeded()) {
            configAdmin.setConfigValue(confInstRef.getConfigValueEditModel(), confInstRef.getIndex(), value, confInstRef.getConfigEnvironment());
        }
        else {
            configAdmin.setConfigValue(confInstRef.getConfigValueEditModel(), value, confInstRef.getConfigEnvironment());
        }
    }

    /**
     * Removes a value from the configuration.
     *
     * @param confInstRef The config instance reference to remove the value for.
     * @param configAdmin The APSConfigAdmin from which the values in the APSConfigInstReference comes from (or this will fail!).
     */
    public static void removeValue(APSConfigInstReference confInstRef, APSConfigAdmin configAdmin) {
        if (confInstRef.isIndexNeeded()) {
            configAdmin.removeConfigValue(confInstRef.getConfigValueEditModel(), confInstRef.getIndex(), confInstRef.getConfigEnvironment());
        }
        else {
            configAdmin.removeConfigValue(confInstRef.getConfigValueEditModel(), confInstRef.getConfigEnvironment());
        }
    }
}
