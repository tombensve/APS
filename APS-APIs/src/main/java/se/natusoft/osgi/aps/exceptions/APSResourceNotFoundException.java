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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-08-10: Created!
 *         
 */
package se.natusoft.osgi.aps.exceptions;

/**
 * This exception is thrown if any resource is not found.
 */
public class APSResourceNotFoundException extends APSRuntimeException {

    /**
     * Creates a new _APSResourceNotFoundException_.
     *
     * @param resource The name of the unavailable resource.
     */
    public APSResourceNotFoundException(String resource) {
        super("Resource '" + resource + "' was not found!");
    }

    /**
     * Creates a new _APSResourceNotFoundException_.
     *
     * @param resource The name of the unavailable resource.
     * @param message An additional message.
     */
    public APSResourceNotFoundException(String resource, String message) {
        super("Resource '" + resource + "' was not found! [" + message + "]");
    }

    /**
     * Creates a new _APSResourceNotFoundException_.
     *
     * @param resource The name of the unavailable resource.
     * @param cause The cause of this exception.
     */
    public APSResourceNotFoundException(String resource, Throwable cause) {
        super("Resource '" + resource + "' was not found!", cause);
    }

    /**
     * Creates a new _APSResourceNotFoundException_.
     *
     * @param resource The name of the unavailable resource.
     * @param message An additional message.
     * @param cause The cause of this exception.
     */
    public APSResourceNotFoundException(String resource, String message, Throwable cause) {
        super("Resource '" + resource + "' was not found! [" + message + "]", cause);
    }
}
