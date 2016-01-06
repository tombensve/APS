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
 *         2016-01-01: Created!
 *
 */
package se.natusoft.osgi.aps.tcpipsvc.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue

@APSConfigDescription(
        configId = "expert",
        description = "Special expert settings..",
        version = "1.0.0"
)
@CompileStatic
@TypeChecked
public class ExpertConfig extends APSConfig {

    @APSConfigItemDescription(description = "Sets the size of the thread pool for executing callbacks of TCP service listeners.",
            environmentSpecific = true, defaultValue = @APSDefaultValue(value = "50"))
    public APSConfigValue tcpCallbackThreadPoolSize

    @APSConfigItemDescription(description = "If time in milliseconds between exceptions are less than this, exception guard will trigger when the below number of exceptions are reached.", environmentSpecific = true, defaultValue = @APSDefaultValue(value = "500"))
    public APSConfigValue exceptionGuardReactLimit

    @APSConfigItemDescription(description = "If the number of consecutive exceptions reaches this and they are all within the above reoccur time limit then the exception guard will report a problem and terminate whatever loop this occurs in.", environmentSpecific = true, defaultValue = @APSDefaultValue(value = "10"))
    public APSConfigValue exceptionGuardMaxExceptions
}
