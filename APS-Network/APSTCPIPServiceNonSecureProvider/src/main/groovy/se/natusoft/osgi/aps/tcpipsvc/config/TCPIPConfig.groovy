/* 
 * 
 * PROJECT
 *     Name
 *         APS TCPIP Service NonSecure Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides a nonsecure implementation of APSTCPIPService.
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
    public APSConfigList<NamedConfig> namedConfigs

    @APSConfigItemDescription(description = "The size of UDP receive byte buffer when received via listeners.", environmentSpecific = true,
            defaultValue = [@APSDefaultValue(value = "10000")])
    public APSConfigValue byteBufferSize

    @APSConfigItemDescription(description = "When selected reconnects will be done on config updates.", environmentSpecific = true,
    isBoolean = true, defaultValue = @APSDefaultValue(value = "true")) // Not entirely sure which of true or false is best default!
    public APSConfigValue respondToConfigUpdates

    @APSConfigItemDescription(description = "Expert configuration. This means, hands off if you do not know what you are doing!")
    public ExpertConfig expert

    @APSConfigDescription(
            configId = "named",
            description = "This is a named config entry.",
            version = "1.0.0"
    )
    public static class NamedConfig extends APSConfig {

        @APSConfigItemDescription(
                description = "The name of this config. This is the name passed in the service to use this configuration.",
                environmentSpecific = true
        )
        public APSConfigValue name

        @APSConfigItemDescription(
                description = "A comment / description of the entry.",
                environmentSpecific = true
        )
        public APSConfigValue comment

        @APSConfigItemDescription(
                description = "Is this a TCP, UDP, or Multicast configuration ?",
                // https://jira.codehaus.org/browse/GROOVY-3278
                validValues = ["TCP", "UDP", "Multicast"],
                environmentSpecific = true
        )
        public APSConfigValue type

        @APSConfigItemDescription(
                description =
                        "An ip address or hostname. For receiving this address is bound to, for sending this address is connected to. For multicast you can leave this blank to default to 224.0.0.1.",
                environmentSpecific = true
        )
        public APSConfigValue address

        @APSConfigItemDescription(
                description = "The port to listen or connect to.",
                environmentSpecific = true
        )
        public APSConfigValue port
    }

    @APSConfigDescription(
            configId = "expert",
            description = "Special expert settings..",
            version = "1.0.0"
    )
    public static class ExpertConfig extends APSConfig {

        @APSConfigItemDescription(description = "Sets the size of the thread pool for executing callbacks of TCP service listeners.",
                environmentSpecific = true, defaultValue = @APSDefaultValue(value = "50"))
        public APSConfigValue tcpCallbackThreadPoolSize

        @APSConfigItemDescription(description = "If time in milliseconds between exceptions are less than this, exception guard will trigger when the below number of exceptions are reached.", environmentSpecific = true, defaultValue = @APSDefaultValue(value = "500"))
        public APSConfigValue exceptionGuardReactLimit

        @APSConfigItemDescription(description = "If the number of consecutive exceptions reaches this and they are all within the above reoccur time limit then the exception guard will report a problem and terminate whatever loop this occurs in.", environmentSpecific = true, defaultValue = @APSDefaultValue(value = "10"))
        public APSConfigValue exceptionGuardMaxExceptions
    }
}
