package se.natusoft.osgi.aps.core.config.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * This is a configuration class for this APSConfigService provider.
 */
@APSConfigDescription( version="1.0",
        configId="se.natusoft.osgi.aps.config-service", group="aps",
        description="Configuration for the aps-configuration-service-provider."
)
public class APSConfigServiceConfig extends APSConfig {

    @APSConfigItemDescription(
            description = "Enables configuration synchronization between server installations.",
            isBoolean = true,
            environmentSpecific = true
    )
    public APSConfigValue synchronize;

    @APSConfigItemDescription(
            description = "Specifies a named group for synchronization. All members of the same group " +
                          "will have their configuration synchronized between them. For synchronization " +
                          "to work a configured APSSyncService service must also be available.",
            defaultValue = {@APSDefaultValue("aps-config-sync-default")},
            environmentSpecific = true
    )
    public APSConfigValue synchronizationGroup;
}
