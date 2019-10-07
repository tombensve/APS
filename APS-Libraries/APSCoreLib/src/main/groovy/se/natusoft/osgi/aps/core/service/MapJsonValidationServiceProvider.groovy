/*
 *
 * PROJECT
 *     Name
 *         APS Core Lib
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         This library is made in Groovy and thus depends on Groovy, and contains functionality that
 *         makes sense for Groovy, but not as much for Java.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2018-05-23: Created!
 *
 */
package se.natusoft.osgi.aps.core.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.misc.json.model.APSMapJson
import se.natusoft.osgi.aps.api.misc.json.model.APSMapJsonSchema
import se.natusoft.osgi.aps.api.misc.json.service.APSMapJsonValidationService
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider
import se.natusoft.osgi.aps.core.lib.MapJsonSchemaValidator

/**
 * Yes, this is a library type bundle that exports everything. But sometimes a service is more useful
 * so I decided that libraries can also publish supporting services for library functionality.
 *
 * Note that the service package are not exported!
 */
@SuppressWarnings( "unused" )
@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-map-json-validation-service-provider" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Misc ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.JSON )
        ]
)
@CompileStatic
@TypeChecked
class MapJsonValidationServiceProvider implements APSMapJsonValidationService {

    /**
     * Validates a Map JSON structure against a schema as defined by APSToolsGroovyLib/MapJsonDocSchemaValidator.
     *
     * @param toValidate The map JSON to validate.
     * @param schema The schema to validate against.
     *
     * @throws APSValidationException on validation failure.
     */
    @Override
    void validate(APSMapJson toValidate, APSMapJsonSchema schema) throws APSValidationException {

        new MapJsonSchemaValidator(validStructure: schema).validate(toValidate)
    }
}
