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

/**
 * This is a synchronization event.
 */
public interface APSReSyncEvent extends APSSyncEvent {

    public static final String ALL_KEYS = "";

    /**
     * A default implementation of the APSSyncEvent.
     */
    static class Default extends APSSyncEvent.Default implements APSReSyncEvent {

        /**
         * Creates a new APSSyncEvent.Default.
         */
        public Default() {
        }

        /**
         * Creates a new APSSyncEvent.Default.
         *
         * @param key The key to synchronize.
         */
        public Default(String key) {
            super.setKey(key);
        }

        /**
         * Sets the key of the sync content in this event.
         *
         * @param key The key to set.
         */
        public APSReSyncEvent.Default key(String key) {
            super.setKey(key);
            return this;
        }

        /**
         * Returns true if this is a synchronize all keys event.
         */
        public boolean isResyncAll() {
            return super.getKey().length() == 0;
        }
    }
}
