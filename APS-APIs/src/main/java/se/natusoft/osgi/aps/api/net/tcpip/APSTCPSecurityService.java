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

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.net.URI;

/**
 * This service provides socket factories. Unfortunately SocketFactory and ServerSocketFactory are not interfaces
 * and can't thereby be published as services directly.
 */
@SuppressWarnings("PackageAccessibility")
public interface APSTCPSecurityService {

    /**
     * Returns a SocketFactory providing some security implementation like SSL for example.
     *
     * @param connectionPoint Can be used to map to a configuration for the SocketFactory.
     */
    SocketFactory getSocketFactory(URI connectionPoint);

    /**
     * Returns a ServerSocketFactory providing some security implementation like SSL for example.
     *
     * @param connectionPoint Can be used to map to a configuration for the ServerSocketFactory.
     */
    ServerSocketFactory getServerSocketFactory(URI connectionPoint);
}
