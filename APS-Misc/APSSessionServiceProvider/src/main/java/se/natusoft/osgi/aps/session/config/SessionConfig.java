package se.natusoft.osgi.aps.session.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration for APSSessionServiceProvider.
 */
@APSConfigDescription(
        version = "1.0",
        configId = "se.natusoft.osgi.aps.aps-session-service-provider",
        group = "misc",
        description = "Configuration for aps-session-service-provider providing of APSSessionService."
)
public class SessionConfig extends APSConfig {

    /** This will receive a populated instance of this config class if it is listed in the APS-Config: manifest entry. */
    public static SessionConfig get;

    @APSConfigItemDescription(
            description = "The default timeout of the session in minutes.",
            defaultValue = @APSDefaultValue("15")
    )
    public APSConfigValue timeout;
}
