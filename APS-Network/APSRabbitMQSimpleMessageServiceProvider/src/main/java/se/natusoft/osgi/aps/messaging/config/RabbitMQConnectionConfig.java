/* 
 * 
 * PROJECT
 *     Name
 *         APS RabbitMQ SimpleMessageService Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides an implementation of APSMessageService using RabbitMQ Java Client.
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
package se.natusoft.osgi.aps.messaging.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration for RabbitMQ connections.
 */
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.messaging.rabbitmq",
        description = "This configures connection data for RabbitMQ server for aps-rabbitmq-simple-message-service-provider.",
        version = "1.0.0",
        // APSConfigAdminWeb GUI tree placement.
        group = "network.messaging"
)
public class RabbitMQConnectionConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this config when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the config to become managed before we try to access it. Out bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the config
     * by using the extender pattern. Even if the config service is running before us we might access the config
     * before the config service have had a change to manage the config. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<RabbitMQConnectionConfig> managed = new ManagedConfig<RabbitMQConnectionConfig>() {

        /**
         * This makes it safe to do:
         * <pre>
         *     RabbitMQConnectionConfig.managed.get().host.toString();
         * </pre>
         * directly.
         */
        @Override
        public RabbitMQConnectionConfig get() {
            if (!super.isManaged()) {
                super.waitUtilManaged();
            }
            return super.get();
        }
    };

    @APSConfigItemDescription(description = "The host where the RabbitMQ server runs.", environmentSpecific = true)
    public APSConfigValue host;

    @APSConfigItemDescription(description = "The port the RabbitMQ service is listening on.",
            environmentSpecific = true, defaultValue = @APSDefaultValue("5672"))
    public APSConfigValue port;

    @APSConfigItemDescription(description = "A user if such is required by the RabbitMQ server.",
            environmentSpecific = true, defaultValue = @APSDefaultValue("guest"))
    public APSConfigValue user;

    @APSConfigItemDescription(description = "A password if such is required by the RabbitMQ server.",
            environmentSpecific = true, defaultValue = @APSDefaultValue("guest"))
    public APSConfigValue password;

    @APSConfigItemDescription(description = "A virtual host name to use if specified. Can be blank.",
            environmentSpecific = true)
    public APSConfigValue virtualHost;

    @APSConfigItemDescription(description = "Changes the default timeout to this. Can be blank.",
            environmentSpecific = true)
    public APSConfigValue timeout;

}
