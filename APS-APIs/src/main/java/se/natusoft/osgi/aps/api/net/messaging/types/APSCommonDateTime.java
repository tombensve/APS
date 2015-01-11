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

import java.util.Date;

/**
 * This is here to handle the fact that no 2 machines on a network do have exactly the same time :-).
 *
 * Note that the "common" time should always be in GMT when this time is synchronized! This means
 * that the diff stored locally includes the local timezone offset from GMT to make it easy.
 */
public interface APSCommonDateTime {

    /**
     * Returns "now" as common date & time.
     */
    public long getCurrentCommonDateTime();

    /**
     * Returns "now" as local time.
     */
    public long getCurrentLocalDateTime();

    /**
     * Converts a common time to a local time.
     *
     * @param commonDateTime The common time to convert.
     *
     * @return The equivalent local time.
     */
    public long toLocalDateTime(long commonDateTime);

    /**
     * Converts local time to common time.
     *
     * @param localDateTime The local time to convert.
     *
     * @return The equivalent common time.
     */
    public long toCommonDateTime(long localDateTime);

    //
    // Default Provider
    //

    /**
     * A default implementation of APSCommonDateTime.
     */
    static class Default implements  APSCommonDateTime {
        //
        // Private Members
        //

        /** The diff in time between local and common time. */
        private long timeDiff = 0;

        //
        // Constructors
        //

        /**
         * Creates a new Default instance.
         */
        public Default() {}

        /**
         * Creates a new Default instance.
         *
         * @param commonDateTime The common date & time.
         */
        public Default(long commonDateTime) {
            update(commonDateTime);
        }

        //
        // Methods
        //

        /**
         * Updates the time diff using a fresh common date & time.
         *
         * @param commonDateTime The common date & time to update with.
         */
        public final void update(long commonDateTime) {
            this.timeDiff = commonDateTime - getCurrentLocalDateTime();
        }

        /**
         * Returns the current time diff.
         */
        public long getTimeDiff() {
            return this.timeDiff;
        }

        /**
         * Returns "now" as common date & time.
         */
        @Override
        public long getCurrentCommonDateTime() {
            return new Date().getTime() + this.timeDiff;
        }

        /**
         * Returns "now" as local time.
         */
        public long getCurrentLocalDateTime() {
            return new Date().getTime();
        }

        /**
         * Converts a common time to a local time.
         *
         * @param commonDateTime The common time to convert.
         * @return The equivalent local time.
         */
        @Override
        public long toLocalDateTime(long commonDateTime) {
            return commonDateTime - this.timeDiff;
        }

        /**
         * Converts local time to common time.
         *
         * @param localDateTime The local time to convert.
         * @return The equivalent common time.
         */
        @Override
        public long toCommonDateTime(long localDateTime) {
            return localDateTime + this.timeDiff;
        }
    }
}
