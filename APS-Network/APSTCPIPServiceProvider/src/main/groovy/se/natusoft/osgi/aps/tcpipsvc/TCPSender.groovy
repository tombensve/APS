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
 *         2015-04-11: Created!
 *
 */
package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.Issue
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequest
import se.natusoft.osgi.aps.tcpipsvc.security.TCPSecurityHandler
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.util.ClientConnection
import se.natusoft.osgi.aps.tools.util.ClientMultiTryConnection

/**
 * Handles TCP request connections.
 */
@CompileStatic
@TypeChecked
class TCPSender implements ConnectionProvider {
    //
    // Properties
    //

    /** The connection point for the sender. */
    URI connectionPoint

    /** A logger to log to. */
    APSLogger logger

    /** The security handler */
    TCPSecurityHandler securityHandler

    //
    // Methods
    //

    /**
     * Starts the provider.
     *
     * @throws IOException
     */
    @Override
    public void start() throws IOException {
        // Nothing needs to be done here!
    }

    /**
     * Stops the provider.
     *
     * @throws IOException
     */
    @Override
    public void stop() throws IOException {
        // Nothing needs to be done here!
    }

    /**
     * Returns the direction of the connection.
     */
    @Override
    public ConnectionProvider.Direction getDirection() {
        ConnectionProvider.Direction.Write
    }

    /**
     * Lets the client send of the request and read the response.
     *
     * @param request
     * @throws IOException
     */
    public void send(StreamedRequest request) throws IOException {
        ClientConnection<Socket> clientConnectionSupport = new ClientMultiTryConnection<>(6, 5000, new ClientConnection<Socket>() {
            @Override
            Socket connect() throws IOException {
                //noinspection GroovyUnusedAssignment
                @Issue(id = "IDEA-149960", url = "https://youtrack.jetbrains.com/issue/IDEA-149960",
                        description = "Problem: 'Assignment is not used.' warning.")
                Socket socket = null
                if (TCPSender.this.connectionPoint.fragment?.contains("secure")) {
                    if (!TCPSender.this.securityHandler.hasSecurityService()) {
                        throw new IOException("Security requested and no APSTCPSecurityService is available!")
                    }

                    socket = securityHandler.createSocket(connectionPoint)
                }
                else {
                    socket = new Socket(connectionPoint.host, connectionPoint.port)
                }

                return socket
            }
        })

        Socket socket = (Socket)clientConnectionSupport.connect()
        TCPOutputStreamWrapper requestStream = new TCPOutputStreamWrapper(wrapee: socket.outputStream, logger: this.logger)
        TCPInputStreamWrapper responseStream = new TCPInputStreamWrapper(wrapee: socket.inputStream, logger:  this.logger)

        if (this.connectionPoint.fragment?.contains("async")) {
            responseStream.allowRead = false
        }

        try {
            request.sendRequest(this.connectionPoint, requestStream, responseStream)
        }
        finally {
            try {
                socket.close()
            }
            catch (IOException ioe) {
                this.logger.error("Failed to close client socket!", ioe)
            }
        }
    }
}
