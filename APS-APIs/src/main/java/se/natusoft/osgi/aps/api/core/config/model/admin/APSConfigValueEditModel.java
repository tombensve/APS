/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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
package se.natusoft.osgi.aps.api.core.config.model.admin;

import java.io.Serializable;

/**
 * This is a model describing/modelling a configuration value (_APSConfigValue_).
 *
 * This is intended for configuration editors using APSConfigAdminService to edit configuration. This models
 * the structure of the original and makes it easier to any editor to represent and edit this structure.
 *
 * @see APSConfigEditModel
 */
public interface APSConfigValueEditModel extends Serializable {

    /**
     * The description of this value.
     *
     * @return the description
     */
    String getDescription();

    /**
     * If true this represents an array of values.
     *
     * @return the isMany
     */
    boolean isMany();

    /**
     * The date pattern to use for parsing date values.
     *
     * @return The date pattern.
     */
    String getDatePattern();

    /**
     * true if the value is a boolean type value (based on config class value annotation).
     *
     * @return true or false.
     */
    boolean isBoolean();

    /**
     * If the size of this array > 0 then these are the valid values to set for this value.
     *
     * @return An emtpy array or set of valid values.
     */
    String[] getValidValues();

    /**
     * The name of the value. This is basically the last part of the localKey.
     *
     * @return the name
     */
    String getName();

    /**
     * true if the value represented by this model is environment specific, false otherwise.
     *
     * @return true or false.
     */
    boolean isConfigEnvironmentSpecific();

    /**
     * The parent of this or null if top parent.
     *
     * @return the parent
     */
    APSConfigEditModel getParent();

    /**
     * The default for this value.
     *
     * @param configEnv The configuration environment to get the default value for.
     *
     * @return the defaultValue
     */
    String getDefaultValue(APSConfigEnvironment configEnv);
}
