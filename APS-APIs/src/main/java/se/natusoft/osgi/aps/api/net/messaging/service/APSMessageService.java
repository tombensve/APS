/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.3
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
 * Defines a message service.
 */
public interface APSMessageService {

    /**
     * Checks if the named queue exists.
     *
     * @param name The name of the queue to check.
     *
     * @return true if queue exists, false otherwise.
     */
    boolean queueExists(String name);

    /**
     * Defines a queue that lives as long as the queue providing service lives.
     *
     * If the queue already exist nothing happens. If the queue is of another type an
     * _APSMessageException_ could possibly be throws depending on implementation and
     * underlying service used.
     *
     * @param name The name of the queue to define.
     *
     * @throws APSMessageException (possibly) on trying to redefine type.
     */
    Queue defineQueue(String name) throws APSMessageException;

    /**
     * Defines a queue that lives for a long time.
     *
     * If the queue already exist nothing happens. If the queue is of another type an
     * _APSMessageException_ could possibly be thrown depending on implementation and
     * underlying service used.
     *
     * @param name The name of the queue to define.
     *
     * @throws APSMessageException (possibly) on trying to redefine type.
     * @throws UnsupportedOperationException If this type of queue is not supported by the implementation.
     */
    Queue defineDurableQueue(String name) throws APSMessageException, UnsupportedOperationException;

    /**
     * Defines a queue that is temporary and gets deleted when no longer used.
     *
     * If the queue already exist nothing happens. If the queue is of another type an
     * _APSMessageException_ could possibly be thrown depending on implementation and
     * underlying service used.
     *
     * @param name The name of the queue to define.
     *
     * @throws APSMessageException (possibly) on trying to redefine type.
     * @throws UnsupportedOperationException If this type of queue is not supported by the implementation.
     */
    Queue defineTemporaryQueue(String name) throws APSMessageException, UnsupportedOperationException;

    /**
     * Returns the named queue or null if no such queue exists.
     *
     * @param name The name of the queue to get.
     */
    Queue getQueue(String name);

    /**
     * This represents a specific named queue.
     */
    public interface Queue {

        /**
         * Returns the name of the queue.
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
         * @throws APSMessageException
         */
        void sendMessage(Message message) throws APSMessageException;

        /**
         * Adds a listener to received messages.
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
         * Deletes this queue.
         *
         * @throws APSMessageException on failure.
         * @throws UnsupportedOperationException if this is not allowed by the implementation.
         */
        void delete() throws APSMessageException, UnsupportedOperationException;

        /**
         * Clears all the messages from the queue.
         *
         * @throws APSMessageException on failure.
         * @throws UnsupportedOperationException If this is not allowed by the implementation.
         */
        void clear() throws APSMessageException, UnsupportedOperationException;

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
                 * @param queueName The name of the queue that delivered the message.
                 * @param message The received message.
                 */
                void receiveMessage(String queueName, Message message);
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
