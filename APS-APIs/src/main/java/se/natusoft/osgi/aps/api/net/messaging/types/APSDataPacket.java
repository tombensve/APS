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
 *         2015-01-09: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.messaging.types;

import java.io.*;

/**
 * A Packet of binary data.
 */
public interface APSDataPacket {

    /**
     * Sets the data content.
     *
     * @param content The content to set.
     */
    public void setContent(byte[] content);

    /**
     * Gets the data content.
     */
    public byte[] getContent();

    /**
     * Returns the data content as an InputStream.
     */
    public DataInputStream getContentInputStream();

    /**
     * Returns an OutputStream for writing data content.
     * <p/>
     * The content will be set on close() of stream.
     */
    public DataOutputStream getContentOutputStream();

    //
    // Default provider
    //

    /**
     * Provides a default implementation of APSData.
     */
    static class Default implements APSDataPacket {
        //
        // Private Members
        //

        /**
         * The packet data.
         */
        private byte[] content;

        //
        // Constructors
        //

        /**
         * Creates a new Provider.
         */
        public Default() {}

        /**
         * Creates a new Provider.
         *
         * @param content Content for the packet.
         */
        public Default(byte[] content) {
            this.content = content;
        }

        //
        // Methods
        //

        /**
         * Sets the packet content.
         *
         * @param content The content to set.
         */
        @Override
        public void setContent(byte[] content) {
            this.content = content;
        }

        /**
         * Gets the packet data.
         */
        @Override
        public byte[] getContent() {
            return this.content;
        }

        /**
         * Returns the packet content as an InputStream.
         */
        @Override
        public DataInputStream getContentInputStream() {
            return new DataInputStream(new ByteArrayInputStream(this.content));
        }

        /**
         * Returns an OutputStream for writing packet content.
         * <p/>
         * The content will be set on close() of stream.
         */
        @Override
        public DataOutputStream getContentOutputStream() {
            return new DataOutputStream(new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    super.close();
                    Default.this.content = toByteArray();
                }
            });
        }
    }
}
