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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-31: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.rpc.errors;

import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

/**
 * This is a special exception that services can throw if they are intended to be available as REST services
 * through the aps-external-protocol-extender + aps-ext-protocol-http-transport-provider. This allows for
 * better control over status codes returned by the service call.
 */
public class APSRESTException extends APSRuntimeException {
    //
    // Private Members
    //

    /** A http status code to return */
    private int httpStatusCode;

    //
    // constructors
    //

    /**
     * Creates a new _APSRESTException_.
     *
     * @param httpStatusCode The http status code to return.
     */
    public APSRESTException(int httpStatusCode) {
        super("");
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Creates a new _APSRESTException_.
     *
     * @param httpStatusCode The http status code to return.
     * @param message An error messaging.
     */
    public APSRESTException(int httpStatusCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    //
    // Methods
    //

    /**
     * Returns the http status code.
     */
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }
}
