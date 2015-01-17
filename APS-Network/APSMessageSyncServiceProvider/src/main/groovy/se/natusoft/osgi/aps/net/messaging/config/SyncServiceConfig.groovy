/*
 *
 * PROJECT
 *     Name
 *         APS Message Sync Service Provider
 *
 *     Code Version
 *         1.0.0
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
 *         2013-09-01: Created!
 *
 */
package se.natusoft.osgi.aps.net.messaging.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.ManagedConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue

/**
 * Configures instances of this service.
 */
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.messaging.syncsvc",
        description = "This provides configuration for aps-message-svc-sync-service-provider.",
        version = "1.0.0",
        // APSConfigAdminWeb GUI tree placement.
        group = "network.messaging"
)
@CompileStatic
@TypeChecked
public final class SyncServiceConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this config when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the config to become managed before we try to access it. Our bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the config
     * by using the extender pattern. Even if the config service is running before us we might access the config
     * before the config service have had a change to manage the config. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<SyncServiceConfig> managed = new ManagedConfig<SyncServiceConfig>();

    @APSConfigItemDescription(description = "If selected then the UUID of received messages will be validated that they come from the same sender software.",
            isBoolean = true, environmentSpecific = true)
    APSConfigValue validateSenderUUID

    @APSConfigItemDescription(description = "Defines 'sync' instances provided by the service.",
            environmentSpecific = true)
    public APSConfigList<SyncInstance> instances


    @APSConfigDescription(
            configId = "sync-instance",
            description = "Sync instance config. An APSSyncService instance will be registered as an OSGi service for each of this.",
            version = "1.0.0"
    )
    public static final class SyncInstance extends APSConfig {

        @APSConfigItemDescription(description = "This defines a name that can be used in code to lookup this specific instance.", environmentSpecific = true)
        public APSConfigValue name

        @APSConfigItemDescription(description = "Which APSMessageService instance name to use.", environmentSpecific = true)
        public APSConfigValue messageInstanceName
    }
}
