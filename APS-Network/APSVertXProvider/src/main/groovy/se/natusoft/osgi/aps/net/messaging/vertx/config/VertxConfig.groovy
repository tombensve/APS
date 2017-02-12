/* 
 * 
 * PROJECT
 *     Name
 *         APS VertX Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This service provides configured Vertx instances allowing multiple services to use the same Vertx instance.
 *         
 *         This service also provides for multiple instances of VertX by associating an instance with a name. Everyone
 *         asking for the same name will get the same instance.
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
 *         2017-01-02: Created!
 *         
 */
package se.natusoft.osgi.aps.net.messaging.vertx.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.ManagedConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList

@CompileStatic
@TypeChecked
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.net.aps.vertx.provider",
        description = "Configuration for aps-vertx-provider.",
        version =  "1.0.0",
        group = "network"
)
class VertxConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this config when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the config to become managed before we try to access it. Our bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the config
     * by using the extender pattern. Even if the config service is running before us we might access the config
     * before the config service have had a change to manage the config. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<VertxConfig> managed = new ManagedConfig<VertxConfig>();


    @APSConfigItemDescription(
            description = "General config entries for Vert.x at top level",
            environmentSpecific = true
    )
    public APSConfigList<VertxConfigValue> optionsValues


    @APSConfigDescription(
            configId = "entry",
            description = "A Vert.x options entry.",
            version = "1.0.0"
    )
    static class VertxConfigValue extends APSConfig {

        @APSConfigItemDescription(
                description = "Name",
                environmentSpecific = true
        )
        public APSConfigValue name

        @APSConfigItemDescription(
                description = "Value",
                environmentSpecific = true
        )
        public APSConfigValue value

        @APSConfigItemDescription(
                description = "Type",
                environmentSpecific = true,
                validValues = ["String", "Int", "Float", "Boolean"]
        )
        public APSConfigValue type

    }
}
