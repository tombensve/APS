package se.natusoft.osgi.aps.core.lib.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.misc.json.model.APSMapJson
import se.natusoft.osgi.aps.api.misc.json.model.APSMapJsonSchema
import se.natusoft.osgi.aps.api.misc.json.service.APSMapJsonValidationService
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider
import se.natusoft.osgi.aps.core.lib.MapJsonDocValidator

/**
 * Yes, this is a library type bundle that exports everything. But sometimes a service is more useful
 * so I decided that libraries can also publish supporting services for library functionality.
 *
 * Note that the service package are not exported!
 */
@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-map-json-validation-service-provider" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Misc ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.JSON )
        ]
)
@CompileStatic
@TypeChecked
class MapJsonDocValidationServiceProvider implements APSMapJsonValidationService {

    /**
     * Validates a Map JSON structure against a schema as defined by APSToolsGroovyLib/MapJsonDocValidator.
     *
     * @param toValidate The map JSON to validate.
     * @param schema The schema to validate against.
     *
     * @throws APSValidationException on validation failure.
     */
    @Override
    void validate(APSMapJson toValidate, APSMapJsonSchema schema) throws APSValidationException {
        new MapJsonDocValidator(validStructure: schema).validate(toValidate)
    }
}
