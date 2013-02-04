/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         0.9.0
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
 *         2011-10-17: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.tracker;

import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

/**
 * This is thrown by withService() on any exception. The real exception
 * is available with getCause().
 */
public class WithServiceException extends APSRuntimeException {

    /**
     * Creates a new WithException.
     *
     * @param message The exception message.
     * @param cause The cause of this exception.
     */
    public WithServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
