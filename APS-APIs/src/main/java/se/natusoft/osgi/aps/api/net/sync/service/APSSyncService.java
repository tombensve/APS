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
 *         2013-08-31: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.sync.service;

import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

import java.io.*;

/**
 * Defines a data synchronization service.
 *
 * **Please note** that this API is very similar to the APSSimpleMessageService! There are
 * differences in implementations between synchronization and sync, reusing the
 * same API would be confusing and also require services to register extra properties
 * to identify type of service provided.
 */
public interface APSSyncService {
    /**
     * Joins a synchronization group.
     *
     * @param name The name of the group to join.
     *
     * @return joined group.
     */
    SyncGroup joinSyncGroup(String name);

    public interface SyncGroup {

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
         * @throws se.natusoft.osgi.aps.api.net.sync.service.APSSyncService.APSSyncException
         */
        void sendMessage(Message message) throws APSSyncException;

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
         * Adds a resynchronization listener.
         *
         * @param reSyncListener The listener to add.
         */
        void addReSyncListener(ReSyncListener reSyncListener);

        /**
         * Removes a Resynchronization listener.
         *
         * @param reSyncListener The listener to remove.
         */
        void removeReSyncListener(ReSyncListener reSyncListener);

        /**
         * Triggers a re-synchronization between all data and all members.
         */
        void reSyncAll();

        /**
         * Leaves the synchronization group.
         */
        void leaveSyncGroup();

        /**
         * This is called when a total re-synchronization is requested.
         */
        public interface ReSyncListener {

            /**
             * Request that all data be sent again.
             *
             * @param group The group making the request.
             */
            void reSyncAll(SyncGroup group);
        }

        /**
         * This represents a message to send/receive.
         */
        public interface Message {

            /**
             * Returns the sync group the message belongs to.
             */
            SyncGroup getSyncGroup();

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
                 * @param message The received message.
                 */
                void receiveMessage(Message message);
            }

            /**
             * A simple default implementation of message.
             */
            public class Provider implements Message {

                private SyncGroup group;
                private byte[] content = new byte[0];

                /**
                 * Creates a new Provider.
                 *
                 * @param group The group the message belongs to.
                 */
                public Provider(SyncGroup group) {
                    this.group = group;
                }

                /**
                 * Returns the group the message belongs to.
                 */
                @Override
                public SyncGroup getSyncGroup() {
                    return this.group;
                }

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
    public static class APSSyncException extends APSRuntimeException {

        /**
         * Creates a new _APSSyncException_.
         *
         * @param message The exception message.
         */
        public APSSyncException(String message) {
            super(message);
        }

        /**
         * Creates a new _APSSyncException_.
         *
         * @param message The exception message.
         * @param cause The cause of this exception.
         */
        public APSSyncException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
