/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *         2018-05-26: Created!
 *         
 */
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
