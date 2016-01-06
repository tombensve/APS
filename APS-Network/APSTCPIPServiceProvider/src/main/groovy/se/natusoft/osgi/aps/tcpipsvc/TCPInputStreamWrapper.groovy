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
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * An InputStream wrapper to stop client code from closing the stream.
 */
@CompileStatic
@TypeChecked
class TCPInputStreamWrapper extends InputStream {
    //
    // Properties
    //

    /** The stream to wrap. */
    InputStream wrapee

    /** The service logger. */
    APSLogger logger

    /** If set to false reads will not be allowed! */
    boolean allowRead = true

    //
    // Methods
    //

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * <p> A subclass must provide an implementation of this method.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception IOException  if an I/O error occurs.
     */
    @Override
    int read() throws IOException {
        if (this.allowRead) {
            return this.wrapee.read()
        }
        else {
            throw new IOException("Read on non readable stream! Maybe this is an async call ?")
        }
    }

    /**
     * Removes the possibility for client code close the stream. The service handles that.
     *
     * @throws IOException
     */
    @Override
    void close() throws IOException {
        this.logger.error("Client code of APSTCPIPService have tried to close an InputStream from a TCP Socket. This " +
                "behaviour is cauth by the service who handles such things. Clients of this service should not do " +
                "that!", new IOException("This is just to help pinpoint where in the code the close() call was made!"))
    }
}
