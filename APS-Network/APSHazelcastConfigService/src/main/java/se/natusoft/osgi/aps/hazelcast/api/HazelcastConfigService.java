package se.natusoft.osgi.aps.hazelcast.api;

import com.hazelcast.config.Config;

/**
 * Provides partly configured Hazelcast configurations.
 *
 * This service provides an Hazelcast Config instance with information configured in APSConfigAdminWeb.
 * It supports multiple named configurations which is why you need to provide a name to get the correct
 * configuration.
 */
public interface HazelcastConfigService {

    /**
     * Returns the named configuration instance or null if the name is not defined.
     *
     * @param name The name of the configuration to get.
     */
    Config getConfigInstance(String name);
}
