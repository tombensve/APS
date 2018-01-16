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
 *         2011-08-14: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.model.admin;

import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigEnvironment;

/**
 * This represents a default value for a specific configuration environment.
 */
public class APSConfigDefaultValue {
    //
    // Private Members
    //

    /** The configuration environment the value applies to. */
    private APSConfigEnvironment configEnv;

    /** The actual default value. */
    private String value;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigDefaultValue instance.
     *
     * @param configEnv The configuration enviornment this default value applies to.
     * @param value The default value.
     */
    public APSConfigDefaultValue(APSConfigEnvironment configEnv, String value) {
        this.configEnv = configEnv;
        this.value = value;
    }

    //
    // Methods
    //

    /**
     * @return The configuration environment this default value applies to.
     */
    public APSConfigEnvironment getConfigEnv() {
        return this.configEnv;
    }

    /**
     * @return The default value.
     */
    public String getValue() {
        return this.value;
    }
}
