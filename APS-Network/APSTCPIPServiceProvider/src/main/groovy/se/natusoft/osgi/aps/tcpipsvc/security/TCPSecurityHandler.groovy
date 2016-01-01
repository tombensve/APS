/* 
 * 
 * PROJECT
 *     Name
 *         APS TCPIP Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides an implementation of APSTCPIPService. This service does not provide any security of its own,
 *         but makes use of APSTCPSecurityService, and APSUDPSecurityService when available and configured for
 *         security.
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
 *         2016-01-01: Created!
 *         
 */
package se.natusoft.osgi.aps.tcpipsvc.security

import se.natusoft.osgi.aps.api.net.tcpip.APSTCPSecurityService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException

/**
 * Wraps APSTCPSecurityService throwing an IOException if service is not available.
 */
class TCPSecurityHandler {
    //
    // Private Members
    //

    @OSGiService(timeout = "10 seconds")
    private APSTCPSecurityService tcpSecurityService

    //
    // Methods
    //

    /**
     * This will return a secure socket.
     *
     * @param name The configuration name requesting a secure socket.
     * @param host The host to connect to.
     * @param port The port to connect to.
     * @param secure true if security is wanted.
     *
     * @throws IOException on failure to create a secure socket. This is most probably caused by not having an APSTCPSecurityService.
     */
    Socket createSocket(String name, InetAddress host, int port, boolean secure) throws IOException {
        // IDEA bug: https://youtrack.jetbrains.com/issue/IDEA-149960
        //noinspection GroovyUnusedAssignment
        Socket socket = null
        if (secure) {
            //noinspection SpellCheckingInspection
            try {
                socket = tcpSecurityService.getSocketFactory(name).createSocket(host, port)
            }
            catch (APSNoServiceAvailableException nsae) {
                throw new IOException("Security was requested, but no APSTCPSecurityService is available!", nsae)
            }
        }
        else {
            socket = new Socket(host, port)
        }
        return socket
    }

    /**
     * If an APSTCPSecurityService is available it is used to get a secure server socket, otherwise a non
     * secure server socket is returned.
     *
     * @param name The configuration name requesting a secure socket.
     * @param secure true if security is wanted.
     *
     * @throws IOException
     */
    ServerSocket createServerSocket(String name, boolean secure) throws IOException {
        // IDEA bug: https://youtrack.jetbrains.com/issue/IDEA-149960
        //noinspection GroovyUnusedAssignment
        ServerSocket socket = null
        if (secure) {
            //noinspection SpellCheckingInspection
            try {
                socket = tcpSecurityService.getServerSocketFactory(name).createServerSocket()
            }
            catch (APSNoServiceAvailableException nsae) {
                throw new IOException("Security was requested, but no APSTCPSecurityService is available!", nsae)
            }
        }
        else {
            socket = new ServerSocket()
        }
        return socket
    }
}
