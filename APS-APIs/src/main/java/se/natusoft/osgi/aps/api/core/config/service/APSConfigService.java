package se.natusoft.osgi.aps.api.core.config.service;

import se.natusoft.osgi.aps.api.core.config.model.APSConfig;

import java.util.List;

/**
 * # APSConfigService
 *
 * To use this service a bundle must have 2 manifest entries:
 *
 * __APSConfigId:__ my.unique.config.id
 *
 * __APSConfigSchema:__ The path to configuration schema .json file within the bundle.
 *
 * Implementations of this service need to use the OSGi extender pattern and look at deployed
 * bundles and the above 2 manifest entries.
 *
 * - The config id data should be persisted somewhere, and if no such exists it should be created.
 * - For each config id the configuration should be loaded if available since before and be provided in a
 *   _APSConfig_ structure.
 * - The possibly loaded configuration should be passed to all services implementing APSConsumer\<APSConfig\>
 *   and having a "aps.config.id: _configid_" property in the published service that matches the specific
 *   config id. APSServiceTracker can help with that.
 *
 */
public interface APSConfigService {

    String APS_CONFIG_ID = "APS-ConfigId";
    String APS_CONFIG_SCHEMA = "APS-ConfigSchema";
    String APS_CONFIG_DEFAULTS = "APS-ConfigDefaults";

    /**
     * @return all registered configuration ids.
     */
    List<String> getAllConfigIds();

    /**
     * @return The configuration for the specified config id. This can be passed as is
     * to APSGroovyToolsLib/MapJsonDocValidator toValidate argument.
     *
     * @param configId The configuration id for the config to get.
     */
    APSConfig getConfig(String configId);

    /**
     * Valdiates a config for a configuration id. This is intended for GUI configuration editors.
     *
     * @param configId The id of the configuration to validate.
     * @param config The actual configuration to validate.
     *
     * @return true if OK, false otherwise.
     */
    boolean validateConfig(String configId, APSConfig config);
}
