/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         Provides a library of utilities, among them APSServiceTracker used by all other APS bundles.
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
 *         2013-08-02: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.exceptions;

/**
 * This is thrown by APSActivator on failure.
 */
public class APSActivatorException extends RuntimeException {
    /**
     * Creates a new _APSRuntimeException_ instance.
     *
     * @param message The exception message.
     */
    public APSActivatorException(String message) {
        super(message);
    }

    /**
     * Creates a new _APSRuntimeException_ instance.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public APSActivatorException(String message, Throwable cause) {
        super(message, cause);
    }
}