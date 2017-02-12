/* 
 * 
 * PROJECT
 *     Name
 *         APS Vertx TCP Messaging Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides messaging over TCP/IP using Vert.x Net service.
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

@CompileStatic
@TypeChecked
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.net.aps.vertx.tcp.provider",
        description = "Configuration for aps-vertx-tcp-provider.",
        version =  "1.0.0",
        group = "network"
)
class VertxTCPConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this config when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the config to become managed before we try to access it. Our bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the config
     * by using the extender pattern. Even if the config service is running before us we might access the config
     * before the config service have had a change to manage the config. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<VertxTCPConfig> managed = new ManagedConfig<VertxTCPConfig>();


    @APSConfigItemDescription(
            description = "Maps topic names to URIs",
            environmentSpecific = true
    )
    public APSConfigList<VertxTCPEntry> tcpMappings

    @APSConfigDescription(
            configId = "entry",
            description = "A TCP mapping entry.",
            version = "1.0.0"
    )
    static class VertxTCPEntry extends APSConfig {

        @APSConfigItemDescription(
                description = "The topic to map.",
                environmentSpecific = true
        )
        public APSConfigValue topic

        @APSConfigItemDescription(
                description = "An URI in the following format: tcp(s)://host:port/?inst=n#in or tcp(s)://host:port/?inst=n#out. The first (#in) will setup a service while the second (#out) will setup a client.",
                environmentSpecific = true
        )
        public APSConfigValue uri

    }

}
