/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol HTTP Transport Provider
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         This uses aps-external-protocol-extender to provide remote calls over HTTP. It makes
 *         any published service implementing se.natusoft.osgi.aps.net.rpc.streamed.service.StreamedRPCProtocolService
 *         available for calling services over HTTP.
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
 *         2012-12-31: Created!
 *         
 */
package se.natusoft.osgi.aps.rpchttpextender.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration for aps-rpc-http-transport-provider.
 */
@APSConfigDescription(
        version = "1.0",
        configId = "se.natusoft.osgi.aps.rpc-http-transport",
        group = "network",
        description = "This adds HTTP transport for aps-external-protocol-extender. Actual protocols are also needed for use!"
)
public class RPCServletConfig extends APSConfig {

    /** This will receive a populated instance on deploy by aps-config-service. This is triggered by the APS-Configs: manifest entry! */
    public static ManagedConfig<RPCServletConfig> mc = new ManagedConfig<RPCServletConfig>();

    @APSConfigItemDescription(
            description = "If this is selected the APSAuthService will be used to require authentication for service calls. " +
                          "Authentication is either provided by specifying \"http://.../apsrpc/auth:user:pw/...\" or " +
                          "using basic http authentication as described on 'http://en.wikipedia.org/wiki/Basic_access_authentication'.",
            isBoolean = true,
            environmentSpecific = true,
            defaultValue = {@APSDefaultValue(configEnv="default", value="false"), @APSDefaultValue(configEnv="production", value="true")}
    )
    public APSConfigValue requireAuthentication;

    @APSConfigItemDescription(
            description = "When this is selected then the http://.../apsrpc/_help page will be available, where all remote " +
                          "protocols and callable services are listed. In addition to that those services can be called for " +
                          "testing directly from the web page. Thereby this should be disabled in production environments.",
            isBoolean = true,
            environmentSpecific = true,
            defaultValue = {@APSDefaultValue(configEnv="default", value="true"), @APSDefaultValue(configEnv="production", value="false")}
    )
    public APSConfigValue enableHelpWeb;

}
