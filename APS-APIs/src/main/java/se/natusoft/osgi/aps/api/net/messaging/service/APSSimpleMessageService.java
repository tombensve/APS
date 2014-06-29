/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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
 *         2013-08-30: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

import java.io.*;

/**
 * This is as the name suggests a simple message service where a named group
 * can be joined to send messages to all members of that group.
 *
 * Yes, this API is very similar to APSGroups, but that API is a bit too explicit
 * for the provided implementation. This is very simple and generic.
 */
public interface APSSimpleMessageService {

    /**
     * Joins a message group.
     *
     * @param name The name of the message group to join.
     *
     * @return A MessageGroup instance used to send and receive messages to/from the group.
     *
     * @throws APSMessageException On nay failure to join.
     */
    MessageGroup joinMessageGroup(String name) throws APSMessageException;

    /**
     * This represents a specific message group.
     */
    public interface MessageGroup {

        /**
         * Returns the name of the group.
         */
        String getName();

        /**
         * Creates a new message.
         */
        Message createMessage();

        /**
         * Creates a new message.
         *
         * @param content The content of the message.
         */
        Message createMessage(byte[] content);

        /**
         * Sends a message.
         *
         * @param message The message to send.
         *
         * @throws APSMessageException depending on implementation!
         */
        void sendMessage(Message message) throws APSMessageException;

        /**
         * Adds a listener to receive messages.
         *
         * @param listener The listener to add.
         */
        void addMessageListener(Message.Listener listener);

        /**
         * Removes a listener from receiving messages.
         *
         * @param listener The listener to remove.
         */
        void removeMessageListener(Message.Listener listener);

        /**
         * Leaves the group. This should always be done in case the implementation needs
         * to do any cleanup.
         */
        void leave();

        /**
         * This represents a message to send/receive.
         */
        public interface Message {

            /**
             * Sets the message content bytes overwriting any previous content.
             *
             * @param content The content to set.
             */
            void setBytes(byte[] content);

            /**
             * Returns the message content bytes.
             */
            byte[] getBytes();

            /**
             * Returns an OutputStream for writing message content. This will replace any previous content.
             */
            OutputStream getOutputStream();

            /**
             * Returns an InputStream for reading the message content.
             */
            InputStream getInputStream();

            /**
             * This needs to be implemented to receive messages.
             */
            public interface Listener {

                /**
                 * Called when a message is received.
                 *
                 * @param groupName The name of the group that delivered the message.
                 * @param message The received message.
                 */
                void receiveMessage(String groupName, Message message);
            }

            /**
             * A simple default implementation of message.
             */
            public class Provider implements Message {

                private byte[] content = new byte[0];

                /**
                 * Creates a new Provider.
                 */
                public Provider() {}

                /**
                 * Sets the message content bytes overwriting any previous content.
                 *
                 * @param content The content to set.
                 */
                @Override
                public void setBytes(byte[] content) {
                    this.content = content;
                }

                /**
                 * Returns the message content bytes.
                 */
                @Override
                public byte[] getBytes() {
                    return this.content;
                }

                /**
                 * Returns an OutputStream for writing message content. This will replace any previous content.
                 */
                @Override
                public OutputStream getOutputStream() {
                    return new ByteArrayOutputStream() {

                        @Override
                        public void close() throws IOException {
                            super.close();
                            Provider.this.content = toByteArray();
                        }
                    };
                }

                /**
                 * Returns an InputStream for reading the message content.
                 */
                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(this.content);
                }
            }
        }
    }

    /**
     * Thrown on sendMessage(). Please note that this is a runtime exception!
     */
    public static class APSMessageException extends APSRuntimeException {

        /**
         * Creates a new _APSMessageException_.
         *
         * @param message The exception message.
         */
        public APSMessageException(String message) {
            super(message);
        }

        /**
         * Creates a new _APSMessageException_.
         *
         * @param message The exception message.
         * @param cause The cause of this exception.
         */
        public APSMessageException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}
