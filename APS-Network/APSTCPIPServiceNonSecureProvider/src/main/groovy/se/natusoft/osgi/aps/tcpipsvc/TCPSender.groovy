/* 
 * 
 * PROJECT
 *     Name
 *         APS TCPIP Service NonSecure Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides a nonsecure implementation of APSTCPIPService.
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
import se.natusoft.osgi.aps.api.net.tcpip.TCPRequest
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.util.ClientConnection
import se.natusoft.osgi.aps.tools.util.ClientConnectionSupport

/**
 * Handles TCP request connections.
 */
@CompileStatic
@TypeChecked
class TCPSender implements ConnectionProvider {
    //
    // Properties
    //

    /** Our config */
    ConfigWrapper config

    /** A logger to log to. */
    APSLogger logger

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
     * This method is called when configuration have been updated.
     */
    @Override
    public void configChanged() {
        // Nothing needs to be done here! The next request will connect to the new values.
    }

    /**
     * Returns the type of the connection.
     */
    @Override
    public ConnectionProvider.Type getType() {
        ConnectionProvider.Type.TCP
    }

    /**
     * Returns the direction of the connection.
     */
    @Override
    public ConnectionProvider.Direction getDirection() {
        ConnectionProvider.Direction.Write
    }

    public void send(TCPRequest request) throws IOException {
        ClientConnectionSupport<Socket> clientConnectionSupport = new ClientConnectionSupport<>(6, 5000, new ClientConnection<Socket>() {
            @Override
            Socket connect() throws IOException {
                return new Socket(config.host, config.port)
            }
        })

        Socket socket = (Socket)clientConnectionSupport.connect()
        TCPOutputStreamWrapper requestStream = new TCPOutputStreamWrapper(wrapee: socket.outputStream, logger: this.logger)
        TCPInputStreamWrapper responseStream = new TCPInputStreamWrapper(wrapee: socket.inputStream, logger:  this.logger)
        try {
            request.tcpRequest(requestStream, responseStream)
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
