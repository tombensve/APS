/* 
 * 
 * PROJECT
 *     Name
 *         APS TCP Simple Message Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides a direct TCP based message service that is not persistent. This service makes use of
 *         the TCPIPService.
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
 *         2016-06-19: Created!
 *         
 */
package se.natusoft.osgi.aps.net.messaging.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents a message to be send and received.
 */
@CompileStatic
@TypeChecked
class ProtocolMessage {

    //
    // Properties
    //

    /** The target of the message. */
    String target

    /** The message content and content type. */
    byte[] data

    //
    // Methods
    //

    /**
     * Reads the message from an InputStream.
     *
     * @param inputStream The stream to read from.
     *
     * @throws IOException on any IO failure.
     */
    public void read(InputStream inputStream) throws IOException {
        DataInputStream dataStream = new DataInputStream(inputStream)
        this.target = dataStream.readUTF()
        int msgSize = dataStream.readInt()
        this.data = new byte[msgSize]
        dataStream.read(this.data, 0, msgSize)
    }

    /**
     * Left shift operator to read from input stream.
     *
     * @param inputStream The stream to read from.
     *
     * @return itself for chaining.
     *
     * @throws IOException
     */
    ProtocolMessage leftShift(InputStream inputStream) throws IOException {
        read(inputStream)
        return this
    }

    /**
     * Writes the message to an OutputStream.
     *
     * @param outputStream The stream to write to.
     *
     * @throws IOException on any IO failure.
     */
    public void write(OutputStream outputStream) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(outputStream)
        dataStream.writeUTF(this.target)
        dataStream.writeInt(data.length)
        dataStream.write(data)
        dataStream.flush()
    }

    /**
     * Right shift operator to write to an output stream.
     *
     * @param outputStream The stream to write to.
     *
     * @throws IOException
     */
    void rightShift(OutputStream outputStream) throws IOException {
        write(outputStream)
    }
}
