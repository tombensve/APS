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
 *         2012-12-30: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.discovery.exception;

import se.natusoft.osgi.aps.exceptions.APSIOException;

/**
 * Thrown on service publish problems or bad configuration.
 */
public class APSDiscoveryException extends APSIOException {

    /**
     * Creates a new _APSDiscoveryPublishException_ instance.
     *
     * @param message The exception messaging.
     */
    public APSDiscoveryException(String message) {
        super(message);
    }

    /**
     * Creates a new _APSDiscoveryPublishException_ instance.
     *
     * @param message The exception messaging.
     * @param cause The cause of this exception.
     */
    public APSDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }

}
