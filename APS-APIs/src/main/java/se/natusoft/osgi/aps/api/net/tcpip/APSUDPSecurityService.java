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
 *         2016-02-27: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.tcpip;

import java.net.DatagramPacket;
import java.net.URI;
import java.util.Map;

/**
 * Secures/unsecures UDP byte data.
 */
public interface APSUDPSecurityService {

    /**
     * This creates and returns some service client specific context that should always
     * be passed back to to the service calls.
     *
     * It is optional for implementations to make use of this, and if they don't null
     * should be returned.
     *
     * Client should however not make any assumptions about the service implementation
     * and always call this method and pass its value back to the other service methods.
     *
     * @param connectionPoint This can be used to map to specific configuration.
     */
    Object createSecurityContext(URI connectionPoint);

    /**
     * Secures the passed data and returns the secured version of it.
     *
     * @param data The data to secure.
     * @param securityContext The security context for the operation.
     *
     * @return A secured version of the data.
     */
    DatagramPacket secure(DatagramPacket data, Object securityContext);

    /**
     * Unsecures the passed data and returns the unsecure version of it.
     *
     * @param data The data to unsecure.
     * @param securityContext The security context for the operation.
     *
     * @return An unsecured version of the data.
     */
    DatagramPacket unsecure(DatagramPacket data, Object securityContext);
}
