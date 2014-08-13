package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;

/**
 * Base config for hazelcast.
 */
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.hazelcast",
        group = "network",
        description = "Provides Hazelcast configuration. Unless you are very familiar with Hazelcast you " +
                      "should read the documentation at hazelcast.org to get a better understanding of the configuration. " +
                      "DO NOTE that the config documentation sucks. That is because the Hazelcast documentation sucks " +
                      "in this area!",
        version = "1.0.0"
)
public class APSHazelCastConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this config when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the config to become managed before we try to access it. Our bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the config
     * by using the extender pattern. Even if the config service is running before us we might access the config
     * before the config service have had a change to manage the config. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<APSHazelCastConfig> managed = new ManagedConfig<APSHazelCastConfig>() {

        /**
         * This makes it safe to do:
         * <pre>
         *     HazelcastConfig.managed.get().configFile.toString();
         * </pre>
         * directly.
         */
        @Override
        public APSHazelCastConfig get() {
            if (!isManaged()) {
                waitUntilManaged();
            }
            return super.get();
        }
    };

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Configures HazelCast instances."
    )
    public APSConfigList<APSHazelCastInstance> instances;

}
