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
            description = "If this is selected the APSUserService will be used to require authentication for service calls. " +
                          "Authentication is either provided by specifying \"http://.../apsrpc/auth:user:pw/...\" or " +
                          "using basic http authentication as described on 'http://en.wikipedia.org/wiki/Basic_access_authentication'.",
            isBoolean = true,
            environmentSpecific = true,
            defaultValue = @APSDefaultValue(configEnv = "default", value="false")
    )
    public APSConfigValue requireAuthentication;

}
