/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Service Provider
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         Provides an implementation of aps-apis:se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService
 *         using aps-json-lib as JSON parser/creator.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-01-22: Created!
 *         
 */
package se.natusoft.osgi.aps.json.service;

import se.natusoft.osgi.aps.json.JSONErrorHandler;

/**
 * Proxies the APS API JSONErrorHandler with an implementation of the error handler of the implementation JSON parser,
 * which in this case is the one in aps-json-lib.
 */
public class JSONErrorHandlerProxy implements JSONErrorHandler {
    //
    // Private Members
    //

    /** The API error handler we are proxying. */
    private se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler jsonErrorHandler = null;

    //
    // Constructor
    //

    /**
     * Creates a new JSONErrorHandlerProxy.
     *
     * @param jsonErrorHandler The error handler we are proxying.
     */
    public JSONErrorHandlerProxy(se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler jsonErrorHandler) {
        this.jsonErrorHandler = jsonErrorHandler;
    }

    //
    // Methods
    //
    
    /**
     * Warns about something.
     *
     * @param message The warning message.
     */
    @Override
    public void warning(String message) {
        this.jsonErrorHandler.warning(message);
    }

    /**
     * Indicate failure.
     *
     * @param message The failure message.
     * @param cause   The cause of the failure. Can be null!
     *
     * @throws RuntimeException This method must throw a RuntimeException.
     */
    @Override
    public void fail(String message, Throwable cause) throws RuntimeException {
        this.jsonErrorHandler.fail(message, cause);
    }
}
