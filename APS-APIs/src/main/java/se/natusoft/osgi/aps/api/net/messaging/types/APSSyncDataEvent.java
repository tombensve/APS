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

import se.natusoft.osgi.aps.codedoc.Implements;

import java.util.Date;

/**
 * This is a synchronization event.
 */
public interface APSSyncDataEvent extends APSSyncEvent {

    /**
     * Indicates that the timestamp is not provided.
     */
    public static final long NO_TIMESTAMP = 0;

    /**
     * Returns the content of this sync event.
     */
    APSDataPacket getContent();

    /**
     * Returns the timestamp of the content in this sync event.
     */
    long getTimestamp();


    /**
     * A default implementation of the APSSyncEvent.
     */
    static class Default extends APSSyncEvent.Default implements APSSyncDataEvent {

        /** The sync content. */
        private APSDataPacket content;

        /** A possible timestamp of the sync content. */
        private long _timestamp = NO_TIMESTAMP;

        /**
         * Creates a new APSSyncEvent.Default.
         */
        public Default() {
        }

        /**
         * Sets the key of the sync content in this event.
         *
         * @param key The key to set.
         */
        public APSSyncDataEvent.Default key(String key) {
            super.key(key);
            return this;
        }

        /**
         * Sets the sync content of this event.
         *
         * @param content The content to set.
         */
        public APSSyncDataEvent.Default content(APSDataPacket content) {
            this.content = content;
            return this;
        }

        /**
         * Sets the sync content of this event.
         *
         * @param content The content to set.
         */
        public APSSyncDataEvent.Default content(byte[] content) {
            this.content = new APSDataPacket.Default(content);
            return this;
        }

        /**
         * Sets the sync content of this event.
         *
         * @param content The content to set.
         */
        public APSSyncDataEvent.Default content(String content) {
            return content(content.getBytes());
        }

        /**
         * Sets the timestamp of the sync content in this event.
         *
         * @param timestamp The timestamp to set.
         */
        public APSSyncDataEvent.Default timestamp(long timestamp) {
            this._timestamp = timestamp;
            return this;
        }

        /**
         * Set timestamp to now.
         */
        public APSSyncDataEvent.Default now() {
            this._timestamp = new Date().getTime();
            return this;
        }

        /**
         * Sets the sync content of this event.
         *
         * @param content The content to set.
         */
        public void setContent(APSDataPacket content) {
            this.content = content;
        }

        /**
         * Sets the sync content of this event.
         *
         * @param content The content to set.
         */
        public void setContent(byte[] content) {
            this.content = new APSDataPacket.Default(content);
        }

        /**
         * Sets the sync content of this event.
         *
         * @param content The content to set.
         */
        public void setContent(String content) {
            setContent(content.getBytes());
        }

        /**
         * Sets the timestamp of the sync content in this event.
         *
         * @param timestamp The timestamp to set.
         */
        public void setTimestamp(long timestamp) {
            this._timestamp = timestamp;
        }

        /**
         * Returns the content of this sync event.
         */
        @Override
        @Implements(APSSyncDataEvent.class)
        public APSDataPacket getContent() {
            return this.content;
        }

        /**
         * Returns the timestamp of the content in this sync event.
         */
        @Override
        @Implements(APSSyncDataEvent.class)
        public long getTimestamp() {
            return this._timestamp;
        }
    }
}
