package se.natusoft.osgi.aps.api.core.config.model;

import se.natusoft.osgi.aps.api.misc.json.model.APSMapJson;
import se.natusoft.osgi.aps.api.util.APSMapJsonDelegator;

import java.util.Map;

/**
 * ## How Configuration Works
 *
 * This specific interface represents a specific configuration. Configuration is stored as JSON or rather a
 * JSON like Map structure. Implementations can store it as JSON. Implementations should store configuration
 * in a permanent place that survives OSGi bundle redeployment.
 *
 * The configuration is externally managed and edited. Bundles gets a read only view of it. To receive a configuration
 * a bundle needs the following MANIFEST.MF entries:
 *
 * __APS-ConfigId:__ - A unique id string.
 *
 * __APS-ConfigSchema:__ - A path to a configuration schema .json file within the bundle. This makes use of MapJsonDocValidator
 * in APSGroovyToolsLib, which also makes it available as a APSMapJsonValidationService.
 *
 * __APS-ConfigDefaults:__ - This is a default configuration file that will be used the first time a configuration is
 * seen. After that it will have no effect.
 *
 * To get the current configuration an APSConsumer&lt;APSConfig&gt; must be published using the same _APS-ConfigId_ key
 * in service properties and the id as value. It will then be called with the configuration when it is available.
 *
 * The APS platform will provide a web application where registered configurations can be edited.
 */
public interface APSConfig extends APSMapJson {

    // MANIFEST Keys
    String APS_CONFIG_ID = "APS-ConfigId";
    String APS_CONFIG_SCHEMA = "APS-ConfigSchema";
    String APS_CONFIG_DEFAULTS = "APS-ConfigDefaults";

    class APSConfigDelegator extends APSMapJsonDelegator implements APSConfig {
        public APSConfigDelegator(Map<String, Object> content) {
            super(content);
        }
    }

    static APSConfig delegateTo(Map<String, Object> mapJson) {
        return new APSConfigDelegator(mapJson);
    }
}
