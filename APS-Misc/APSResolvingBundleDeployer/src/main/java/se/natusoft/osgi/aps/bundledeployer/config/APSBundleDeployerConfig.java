/* 
 * 
 * PROJECT
 *     Name
 *         APS Resolving Bundle Deployer
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         Deploys bundles resolving dependencies as automatically as possible by accepting a few
 *         deploy failures and retrying until it works or a fail threshold has been reached.
 *         
 *         Unless the server deployed on supports WABs using the extender pattern, no war files
 *         will deploy correctly.
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
 *         2013-01-03: Created!
 *         
 */
package se.natusoft.osgi.aps.bundledeployer.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration model.
 */
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.bundle-deployer",
        version = "1.0.0",
        description = "Configuration for APSResolvingBundleDeployer.",
        group = "aps"
)
public class APSBundleDeployerConfig extends APSConfig {

    /** This is auto managed through this instance.  */
    public static ManagedConfig<APSBundleDeployerConfig> managed = new ManagedConfig<>();

    @APSConfigItemDescription(
            description = "The directory to deploy bundles from. All bundles in this directory will be attempted to be deployed."
    )
    public APSConfigValue deployDirectory;

    @APSConfigItemDescription(
            description = "The number of failed deploys before giving upp. The more bundles and the more dependencies among them " +
                          "the higher the value should be.",
            defaultValue = {@APSDefaultValue(value = "8")}
    )
    public APSConfigValue failThreshold;


}
