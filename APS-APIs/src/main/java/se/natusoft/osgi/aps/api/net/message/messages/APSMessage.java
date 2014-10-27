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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2014-10-27: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.message.messages;

import se.natusoft.osgi.aps.annotations.CodeNote;

import java.io.*;
import java.util.Date;

/**
 * This is a base message to build other messages on.
 */
public class APSMessage {

    //
    // Private Members
    //

    /** The timestamp of when the message was received. */
    private long receivedAt;

    //
    // Methods
    //

    /**
     * This should be overridden by specific messages and super called as first thing done.
     *
     * @param dataStream The stream to read from.
     *
     * @throws IOException On failure to read.
     */
    @CodeNote("Intentionally not abstract! Don't want subclass of subclass behavior to be different from direct subclass behavior.")
    protected void readData(DataInputStream dataStream) throws IOException {
        this.receivedAt = new Date().getTime();
    }

    /**
     * This should be overridden by specific messages and super called as first thing done.
     *
     * @param dataStream The stream to write to.
     *
     * @throws IOException On failure to write.
     */
    @CodeNote("Intentionally not abstract! Don't want subclass of subclass behavior to be different from direct subclass behavior.")
    protected void writeData(DataOutputStream dataStream) throws  IOException {}

    /**
     * Sets message bytes from received message.
     *
     * @param data The message bytes to set.
     *
     * @throws IOException on failure to read byte content.
     */
    public void setData(byte[] data) throws IOException {
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        DataInputStream dataStream = new DataInputStream(inStream);
        readData(dataStream);
        dataStream.close();
    }

    /**
     * Gets message bytes to send.
     *
     * @return The bytes.
     *
     * @throws IOException on failure to produce bytes.
     */
    public byte[] getData() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(outStream);
        writeData(dataStream);
        dataStream.close();

        return outStream.toByteArray();
    }

    /**
     * Returns the timestamp of when the message was received.
     */
    public long getReceivedAt() {
        return this.receivedAt;
    }
}
