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
 *         2016-06-19: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;

import java.io.*;

/**
 * This represents a message to be sent or received.
 */
public interface APSMessage {

    /**
     * Provide the message content as an array of bytes.
     *
     * @param messsage The message bytes to set.
     */
    void setBytes(byte[] messsage);

    /**
     * Returns the message as an array of bytes.
     */
    byte[] getBytes();

    /**
     * Sets the message content as a string.
     *
     * @param text The message text to set.
     */
    void setText(String text);

    /**
     * Return the message as a text string. If it wasn't sent as one this can have unexpected results!
     */
    String getText();

    /**
     * Fill the message content using an OutputStream.
     */
    OutputStream getOutputStream();

    /**
     * Read the message content using an InputStream.
     */
    InputStream getInputStream();

    /**
     * Provides an APSMessage implementation.
     */
    class Provider implements APSMessage {
        //
        // Private Members
        //

        private byte[] message;

        //
        // Methods
        //

        /**
         * Provide the message content as an array of bytes.
         *
         * @param message The message bytes to set.
         */
        @Override
        public void setBytes(@NotNull byte[] message) {
            this.message = message;
        }

        /**
         * Returns the message as an array of bytes.
         */
        @Override
        @Nullable
        public byte[] getBytes() {
            return this.message;
        }

        /**
         * Sets the message content as a string.
         *
         * @param text The message text to set.
         */
        @Override
        public void setText(String text) {
            try {
                this.message = text.getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException ignore) {
                this.message = text.getBytes();
            }
        }

        /**
         * Return the message as a text string. If it wasn't sent as one this can have unexpected results!
         */
        @Override
        public String getText() {
            try {
                return new String(this.message, "UTF-8");
            }
            catch (UnsupportedEncodingException ignore) {
                return new String(this.message);
            }
        }

        /**
         * Fill the message content using an OutputStream. What is written will not appear in message until close()!
         */
        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream() {

                @Override
                public void flush() throws IOException {
                    super.flush();
                    Provider.this.message = getBytes();
                }

                @Override
                public void close() throws IOException {
                    this.flush();
                    super.close();
                }
            };
        }

        /**
         * Read the message content using an InputStream.
         */
        @Override
        public InputStream getInputStream() {
            if (this.message == null) {
                this.message = new byte[0];
            }
            return new ByteArrayInputStream(this.message);
        }
    }
}
