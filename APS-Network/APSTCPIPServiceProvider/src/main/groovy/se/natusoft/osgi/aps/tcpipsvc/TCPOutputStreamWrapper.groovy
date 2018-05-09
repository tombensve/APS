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
import se.natusoft.osgi.aps.util.APSLogger

/**
 * An OutputStream wrapper to stop client code from closing the stream.
 */
@CompileStatic
@TypeChecked
class TCPOutputStreamWrapper extends OutputStream {
    //
    // Properties
    //

    /** The wrapped OutputStream. */
    OutputStream wrapee

    /** The service logger. */
    APSLogger logger

    /** Setting this to false disables writing. */
    boolean allowWrite = true

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p>
     * Subclasses of <code>OutputStream</code> must provide an
     * implementation for this method.
     *
     * @param b the <code>byte</code>.
     * @exception IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             output stream has been closed.
     */
    @Override
    void write(int b) throws IOException {
        if (!this.allowWrite) {
            throw new IOException("Writing is not allowed on this stream! Maybe an async connection point?")
        }
        wrapee.write(b)
    }

    /**
     * Removes the possibility for client code close the stream. The service handles that.
     *
     * @throws IOException
     */
    @Override
    void close() throws IOException {
        this.logger.error("Client code of APSTCPIPService have tried to close an OutputStream from a TCP Socket. This " +
                "behaviour is cauth by the service who handles such things. Clients of this service should not do " +
                "that!", new IOException("This is just to help pinpoint where in the code the close() call was made!"))
    }

}
