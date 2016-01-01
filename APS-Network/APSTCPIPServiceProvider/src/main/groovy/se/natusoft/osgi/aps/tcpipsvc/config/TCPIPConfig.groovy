/*
 *
 * PROJECT
 *     Name
 *         APS TCPIP Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides an implementation of APSTCPIPService. This service does not provide any security of its own,
 *         but makes use of APSTCPSecurityService, and APSUDPSecurityService when available and configured for
 *         security.
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
 *         2015-04-11: Created!
 *
 */
package se.natusoft.osgi.aps.tcpipsvc.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.ManagedConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue

/**
 * Configuration for TCP/IP connections to provide.
 */
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.net.tcpip",
        description = "This provides named configurations for communicating over TCP/IP using APSTCPIPService.",
        version = "1.0.0",
        group = "network"
)
@CompileStatic
@TypeChecked
class TCPIPConfig extends APSConfig {

    public static final ManagedConfig<TCPIPConfig> managed = new ManagedConfig<>()

    @APSConfigItemDescription(description = "This is one specific named configuration.", environmentSpecific = true)
    public APSConfigList<GroupConfig> groupConfigs

    @APSConfigItemDescription(description = "The size of UDP receive byte buffer when received via listeners.", environmentSpecific = true,
            defaultValue = [@APSDefaultValue(value = "10000")])
    public APSConfigValue byteBufferSize

    @APSConfigItemDescription(description = "When selected reconnects will be done on config updates.", environmentSpecific = true,
    isBoolean = true, defaultValue = @APSDefaultValue(value = "true")) // Not entirely sure which of true or false is best default!
    public APSConfigValue respondToConfigUpdates

    @APSConfigItemDescription(description = "Expert configuration. This means, hands off if you do not know what you are doing!")
    public ExpertConfig expert

}
