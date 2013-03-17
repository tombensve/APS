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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-05-01: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.config.util;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;

/**
 * This references a unique configuration instance.
 * <ul>
 *     <li><b>APSConfigValueEditModel</b> - This represents the basic value in the configuration store.</li>
 *     <li><b>index (int)</b> - For values of 'many' type that needs an index to make the reference unique.</li>
 *     <li><b>APSConfigEnvironment</b> - A configuration environment which is needed for values that are specified as configuration environment specific.</li>
 * </ul>
 * Some values only need the model to make the reference unique, some need the model and an index and some need all 3.
 * <p/>
 * One point of this little model is to be able to pass a complete reference to a value as one object.
 */
public class APSConfigInstReference {
    //
    // Private Members
    //

    /** This model identifies a specific value. */
    private APSConfigValueEditModel valueEditModel = null;

    /** For 'many' type values an index is also needed to make the reference unique. */
    private int index = -1;

    /** The config environment for config environment specific values. */
    private APSConfigEnvironment configEnvironment;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigInstReference.
     *
     * @param valueEditModel The model that identifies a specific configuration value.
     * @param configEnvironment The config environment that applies for this config value reference.
     */
    public APSConfigInstReference(APSConfigValueEditModel valueEditModel, APSConfigEnvironment configEnvironment) {
        this.valueEditModel = valueEditModel;
        this.configEnvironment = configEnvironment;
        if (this.valueEditModel.isMany()) {
            this.index = 0;
        }
    }

    /**
     * Creates a new APSConfigInstReference referencing a 'many' type value.
     *
     * @param valueEditModel The model that identifies a specific configuration value.
     * @param index The index of the value to make the reference unique.
     * @param configEnvironment The config environment that applies for this config value reference.
     */
    public APSConfigInstReference(APSConfigValueEditModel valueEditModel, int index, APSConfigEnvironment configEnvironment) {
        this(valueEditModel, configEnvironment);
        this.index = index;
        if (!valueEditModel.isMany()) {
            this.index = -1;
        }
    }

    //
    // Methods
    //

    /**
     * Returns the model identifying a specific configuration value.
     */
    public APSConfigValueEditModel getConfigValueEditModel() {
        return this.valueEditModel;
    }

    /**
     * Returns the index of a 'many' type config value.
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Returns true if the index is needed to make a unique reference to the value.
     */
    public boolean isIndexNeeded() {
        return this.valueEditModel.isMany();
    }

    /**
     * Returns the config environment that applies for this config value reference.
     */
    public APSConfigEnvironment getConfigEnvironment() {
        return this.configEnvironment;
    }

    /**
     * Returns true if the config environment is needed to make a unique reference to the value.
     */
    public boolean isConfigurationNeeded() {
        return this.valueEditModel.isConfigEnvironmentSpecific();
    }
}
