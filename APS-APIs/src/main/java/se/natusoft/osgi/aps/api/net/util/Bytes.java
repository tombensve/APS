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
package se.natusoft.osgi.aps.api.net.util;

import java.io.*;
import java.util.Arrays;

/**
 * This is a generic object holding a byte array and provides different ways of setting and getting the byte data.
 */
public class Bytes {
    //
    // Private Members
    //

    /** The byte data held by this object. */
    private byte[] bytes = new byte[0];

    //
    // Constructors
    //

    /**
     * Creates a new empty object.
     */
    public Bytes() {}

    /**
     * Creates a new Bytes instance with initial bytes.
     *
     * @param bytes The bytes of this Bytes object.
     */
    public Bytes(byte[] bytes) {
        setBytes(bytes);
    }

    //
    // Methods
    //

    /**
     * Sets bytes.
     *
     * @param bytes The bytes to set.
     */
    public void setBytes(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Gets the bytes.
     */
    public byte[] getBytes() {
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    /**
     * Returns an OutputStream that can be used to fill this object with data. This object
     * will be updated when the stream is closed!
     */
    public OutputStream getContentWriteStream() {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                bytes = super.toByteArray();
            }
        };
    }

    /**
     * Returns an InputStream to read the content of this object.
     */
    public InputStream getContentReadStream() {
        return new ByteArrayInputStream(this.bytes);
    }

    /**
     * Returns content as a String.
     */
    public String toString() {
        return new String(this.bytes);
    }

    /**
     * Sets content from a String.
     *
     * @param str The String to set content from.
     */
    public void fromString(String str) {
        this.bytes = str.getBytes();
    }
}
