package se.natusoft.osgi.aps.api.web.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration for the APS admin web.
 */
@APSConfigDescription(
        version="1.0",
        configId="se.natusoft.aps.adminweb",
        group="aps",
        description="Configuration for the APS admin web."
)
public class APSAdminConfig extends APSConfig {

    @APSConfigItemDescription(
            description = "Enable to require a login for accessing '/apsadminweb'. However make sure you have " +
                          "setup the APSSimpleUserServiceDS in persistence/datasources first if you are using " +
                          "the APSSimpleUserService for authentication.",
            isBoolean = true,
            defaultValue = @APSDefaultValue("false")
    )
    public APSConfigValue requireAuthentication;
}
