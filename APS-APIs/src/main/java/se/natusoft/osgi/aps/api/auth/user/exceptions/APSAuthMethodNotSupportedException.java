/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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
 *         2013-02-21: Created!
 *         
 */
package se.natusoft.osgi.aps.api.auth.user.exceptions;

import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

/**
 * This is thrown by APSAuthService when the implementation does not support the selected auth method.
 */
public class APSAuthMethodNotSupportedException extends APSRuntimeException {

    /**
     * Creates a new APSAuthMethodNotSupportedException instance.
     *
     * @param message The exception message.
     */
    public APSAuthMethodNotSupportedException(String message) {
        super(message);
    }

    /**
     * Creates a new APSAuthMethodNotSupportedException instance.
     *
     * @param message The exception message.
     * @param cause The exception that is the cause of this one.
     */
    public APSAuthMethodNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
