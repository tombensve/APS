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
 *         2016-07-02: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.tcpip;

import java.net.InetAddress;
import java.util.List;

/**
 * A service that provides streamed TCP tunnels.
 *
 * The service does not provide any way of using the tunnels other than providing information of where each
 * tunnel is located, what hostname/address and port to connect to, to access the tunnel.
 */
public interface APSTunnelService {

    /**
     * Looks upp a configured, named tunnel and returns data about it.
     *
     * @param name The name to lookup.
     *
     * @return The named tunnel if name matches, or null otherwise.
     */
    Tunnel lookup(String name);

    /**
     * @return A List of all defined tunnels.
     */
    List<Tunnel> getAllTunnels();

    /**
     * Represents a specific tunnel.
     */
    interface Tunnel {

        /**
         * @return The configured name of the tunnel.
         */
        String getName();

        /**
         * @return The local address to connect to, to access the tunnel. This would usually be 127.0.0.1.
         */
        InetAddress getLocalConnectionAddress();

        /**
         * @return The port of the tunnel.
         */
        int getPort();
    }
}
