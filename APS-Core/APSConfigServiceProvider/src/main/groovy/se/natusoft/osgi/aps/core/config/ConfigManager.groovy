package se.natusoft.osgi.aps.core.config

import se.natusoft.osgi.aps.api.core.config.model.APSConfig

/**
 * This class manages current configurations.
 */
class ConfigManager {

    //
    // Private Members
    //

    /** Holds our current configurations. */
    private Map<String, APSConfig> configurations = Collections.synchronizedMap(new LinkedHashMap<>())

    //
    // Methods
    //

    /**
     * Provides a configuration to manage. Can either be a new or an updated.
     *
     * @param configId The id of the configuration to provide.
     * @param configuration The configuration.
     */
    void provideConfig(String configId, APSConfig configuration) {
        this.configurations.put(configId, configuration)
    }

    /**
     * Removes a configuration.
     *
     * @param configId The id of the configuration to remove.
     */
    void removeConfig(String configId) {
        this.configurations.remove(configId)
    }

    /**
     * Returns true if the specified configuration exists.
     *
     * @param configId The id of the configuration to check.
     *
     * @return true of configuration exists, false otherwise.
     */
    boolean hasConfig(String configId) {
        return this.configurations.containsKey(configId)
    }

    /**
     * Looksup and returns a configuration.
     *
     * @param configId The id of the configuration to lookup.
     *
     * @return The actual configuration or null if not available.
     */
    APSConfig lookupConfig(String configId) {
        return this.configurations.get(configId)
    }

}
