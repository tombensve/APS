/* 
 * 
 * PROJECT
 *     Name
 *         APS TCP Simple Message Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides a direct TCP based message service that is not persistent. This service makes use of
 *         the TCPIPService.
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
 *         2016-06-19: Created!
 *         
 */
package se.natusoft.osgi.aps.net.messaging.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.ManagedConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList

@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.net.msg.tcp-simple-message-service",
        description = "Configuration for aps-tcp-simple-message-service-provider.",
        version =  "1.0.0",
        group = "network"
)
@CompileStatic
@TypeChecked
class ServiceConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this config when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the config to become managed before we try to access it. Our bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the config
     * by using the extender pattern. Even if the config service is running before us we might access the config
     * before the config service have had a change to manage the config. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<ServiceConfig> managed = new ManagedConfig<ServiceConfig>()

    @APSConfigItemDescription(
            description = "The APSTCPIPService connection point URI to listen to for messages.",
            environmentSpecific = true
    )
    public APSConfigValue listenConnectionPointUrl

    @APSConfigItemDescription(
            description = "If this is checked then the listen connection point URI will be registered with the discovery service.",
            isBoolean = true,
            environmentSpecific = true
    )
    public APSConfigValue registerWithDiscoveryService

    @APSConfigItemDescription(
            description = "A list of APSTCPIPService connection point URIs to try to send messages to.",
            environmentSpecific = true
    )
    public APSConfigValueList sendConnectionPointUrls

    @APSConfigItemDescription(
            description = "If this is checked then send to connection points will be looked for in the discovery service.",
            isBoolean = true,
            environmentSpecific = true
    )
    public APSConfigValue lookInDiscoveryService

}
