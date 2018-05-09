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

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPSecurityService
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.tracker.APSNoServiceAvailableException

/**
 * Wraps APSTCPSecurityService throwing an IOException if service is not available.
 */
@CompileStatic
@TypeChecked
class TCPSecurityHandler {
    //
    // Private Members
    //

    @OSGiService(timeout = "10 seconds")
    private APSServiceTracker<APSTCPSecurityService> tcpSecurityServiceTracker

    //
    // Methods
    //

    boolean hasSecurityService() {
        return this.tcpSecurityServiceTracker.hasTrackedService()
    }

    /**
     * This will return a secure socket.
     *
     * @param connectionPoint The connection point to connect the socket to.
     *
     * @throws IOException on failure to create a secure socket. This is most probably caused by not having an APSTCPSecurityService.
     */
    Socket createSocket(URI connectionPoint) throws IOException {
        // IDEA bug: https://youtrack.jetbrains.com/issue/IDEA-149960
        //noinspection GroovyUnusedAssignment
        Socket socket = null

        //noinspection SpellCheckingInspection
        try {

            socket = this.tcpSecurityServiceTracker.allocateService().
                    getSocketFactory(connectionPoint).createSocket(connectionPoint.host, connectionPoint.port)
            this.tcpSecurityServiceTracker.releaseService()
        }
        catch (APSNoServiceAvailableException nsae) {
            throw new IOException("Security was requested, but no APSTCPSecurityService is available!", nsae)
        }
        return socket
    }

    /**
     * If an APSTCPSecurityService is available it is used to get a secure server socket, otherwise a non
     * secure server socket is returned.
     *
     * @param connectionPoint The connection point to connect the socket to.
     *
     * @throws IOException
     */
    ServerSocket createServerSocket(URI connectionPoint) throws IOException {
        // IDEA bug: https://youtrack.jetbrains.com/issue/IDEA-149960
        //noinspection GroovyUnusedAssignment
        ServerSocket socket = null

        //noinspection SpellCheckingInspection
        try {
            socket = this.tcpSecurityServiceTracker.allocateService().
                    getServerSocketFactory(connectionPoint).createServerSocket(connectionPoint.port)
            this.tcpSecurityServiceTracker.releaseService()
        }
        catch (APSNoServiceAvailableException nsae) {
            throw new IOException("Security was requested, but no APSTCPSecurityService is available!", nsae)
        }
        return socket
    }
}
