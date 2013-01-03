package se.natusoft.osgi.aps.bundledeployer.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration model.
 */
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.bundle-deployer",
        version = "1.0.0",
        description = "Configuration for APSResolvingBundleDeployer.",
        group = "aps"
)
public class APSBundleDeployerConfig extends APSConfig {

    /** This is auto managed through this instance.  */
    public static ManagedConfig<APSBundleDeployerConfig> managed = new ManagedConfig<>();

    @APSConfigItemDescription(
            description = "The directory to deploy bundles from. All bundles in this directory will be attempted to deploy."
    )
    public APSConfigValue deployDirectory;

    @APSConfigItemDescription(
            description = "The number of failed deploys before giving upp. The more bundles and the more dependencies among them " +
                          "the higher the value should be.",
            defaultValue = {@APSDefaultValue(value = "8")}
    )
    public APSConfigValue failThreshold;


}
