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
 *         2012-02-13: Cleaned up and renamed.
 *
 */
package se.natusoft.osgi.aps.api.core.configold.model.admin;

import se.natusoft.osgi.aps.api.core.configold.APSConfig;

import java.util.List;
import java.util.Set;

/**
 * This is a model describing a subclass of APSConfig, modeling its structure. The annotated configold classes
 * extending _APSConfig_ is used for both describing the configuration and to provide configuration values.
 * Since such a class can contain both _APSConfigValue_ members and other subclass of _APSConfig_ members and
 * lists of both the configuration can have a structure. Internally however the configuration is stored
 * with key and value for each value where keys are automatically built and are internal to the implementation.
 *
 * Implementations of this interface describes/models a specific APSConfig subclass. The _getValues()_ method
 * returns a list of the values held by the modelled configold class. The entries in the list is either an
 * _APSConfigValueModel_ or another _APSConfigModel_ depending on the value type.
 *
 * This is intended for configuration editors using APSConfigAdminService to edit configuration. This models
 * the structure of the original and makes it easier to any editor to represent and edit this structure.
 */
public interface APSConfigEditModel extends APSConfigValueEditModel {

    /**
     * Returns the configClass version.
     */
    String getVersion();

    /**
     * Returns the configuration id specified by the configClass.
     */
    String getConfigId();

    /**
     * The group of the configold.
     */
    String getGroup();

    /**
     * @return The Class for the APSConfig subclass parsed by this model.
     */
    Class<? extends APSConfig> getConfigClass();

    /**
     * Returns the values for this configClass.
     */
    List<APSConfigValueEditModel> getValues();

    /**
     * Gets a value by its name (java bean property name but in all lowercase).
     *
     * @param name The name of the value to get.
     *
     * @return The named value.
     */
    APSConfigValueEditModel getValueByName(String name);

    /**
     * Returns the names of all available values of the configuration class represented by this model.
     */
    Set<String> getValueNames();
}
