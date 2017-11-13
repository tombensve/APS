package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.model.APSConfig
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService
import se.natusoft.osgi.aps.api.misc.json.service.APSMapJsonValidationService
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-config-service-provider" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Misc ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Configuration )
        ]
)
@CompileStatic
@TypeChecked
class APSConfigServiceProvider implements APSConfigService {

    //
    // Private Members
    //

    @OSGiService
    private APSMapJsonValidationService mapJsonValidationService

    @Managed
    private ConfigManager configManager

    //
    // Methods
    //

    /**
     * @return all registered configuration ids.
     */
    @Override
    List<String> getAllConfigIds() {
        return null
    }

    /**
     * @return The configuration for the specified config id. This can be passed as is
     * to APSGroovyToolsLib/MapJsonDocValidator toValidate argument.
     *
     * @param configId The configuration id for the config to get.
     */
    @Override
    APSConfig getConfig(String configId) {
        return null
    }

    /**
     * Valdiates a config for a configuration id. This is intended for GUI configuration editors.
     *
     * @param configId The id of the configuration to validate.
     * @param config The actual configuration to validate.
     *
     * @return true if OK, false otherwise.
     */
    @Override
    boolean validateConfig(String configId, APSConfig config) {
        return false
    }
}
