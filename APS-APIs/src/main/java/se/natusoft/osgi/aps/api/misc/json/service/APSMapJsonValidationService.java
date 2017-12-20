package se.natusoft.osgi.aps.api.misc.json.service;

import se.natusoft.osgi.aps.api.misc.json.model.APSMapJson;
import se.natusoft.osgi.aps.api.misc.json.model.APSMapJsonSchema;
import se.natusoft.osgi.aps.exceptions.APSValidationException;

public interface APSMapJsonValidationService {

    /**
     * Validates a Map JSON structure against a schema as defined by APSToolsGroovyLib/MapJsonDocValidator.
     *
     * @param toValidate The map JSON to validate.
     * @param schema The schema to validate against.
     *
     * @throws APSValidationException on validation failure.
     */
    void validate(APSMapJson toValidate, APSMapJsonSchema schema);
}
