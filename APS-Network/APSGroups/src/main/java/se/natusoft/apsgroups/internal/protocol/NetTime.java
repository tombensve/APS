/* 
 * 
 * PROJECT
 *     Name
 *         APSGroups
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         Provides network groups where named groups can be joined as members and then send and
 *         receive data messages to the group. This is based on multicast and provides a verified
 *         multicast delivery with acknowledgements of receive to the sender and resends if needed.
 *         The sender will get an exception if not all members receive all data. Member actuality
 *         is handled by members announcing themselves relatively often and will be removed when
 *         an announcement does not come in expected time. So if a member dies unexpectedly
 *         (network goes down, etc) its membership will resolve rather quickly. Members also
 *         tries to inform the group when they are doing a controlled exit. Most network aspects
 *         are configurable.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files and other things.
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
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.apsgroups.internal.protocol;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class is intended to minimize the time difference between different members. It cannot completely
 * remove it though. It works by members sending their version of this and this class then calculates the
 * diff between that time an local time and then adjust local time with that diff.
 *
 * The idea for this is that everybody in the group should most probably be in sync on roughly the same time.
 */
public class NetTime {
    //
    // Private Members
    //

    /** The local timediff to the group time. */
    private long timeDiff = 0;

    //
    // Constructors
    //

    /**
     * Creates a new NetTime instance.
     */
    public NetTime() {}

    //
    // Methods
    //

    /**
     * Updates the group time diff from the group.
     *
     * @param timeValue The group time value.
     */
    public synchronized void updateNetTime(long timeValue) {
        long now = new Date().getTime();
        this.timeDiff = timeValue - now;
    }

    /**
     * Returns the time diff.
     */
    private synchronized long getTimeDiff() {
        return this.timeDiff;
    }

    /**
     * Creates a new Time instance representing now in group time.
     */
    public Time getCurrentNetTime() {
        return createWithLocalTime(new Date().getTime());
    }

    /**
     * Creates a new Time using a local time value.
     *
     * @param localTime The local time value.
     */
    public Time createWithLocalTime(long localTime) {
        Time time = new Time();
        time.setLocalTimeValue(localTime);
        return time;
    }

    /**
     * Creates a new Time using a net time value.
     *
     * @param netTime The net time value.
     */
    public Time createWithNetTime(long netTime) {
        Time time = new Time();
        time.setNetTimeValue(netTime);
        return time;
    }

    //
    // Inner Classes
    //

    /**
     * This holds a time in group time.
     */
    public class Time implements Comparable<Time> {
        //
        // Private Members
        //

        /** The time value of this time object. */
        private long localTimeValue = 0;

        //
        // Constructors
        //

        /**
         * Creates a new Time instance.
         */
        private Time() {}

        //
        // Methods
        //

        /**
         * Sets this time with a net time value.
         *
         * @param netTimeValue The net time value to set.
         */
        public void setNetTimeValue(long netTimeValue) {
            this.localTimeValue = netTimeValue - getTimeDiff();
        }

        /**
         * Sets this time with a local time value.
         *
         * @param localTimeValue The local time value to set.
         */
        public void setLocalTimeValue(long localTimeValue) {
            this.localTimeValue = localTimeValue;
        }

        /**
         * @return a clone of this Time instance.
         */
        public Time clone() {
            Time time = new Time();
            time.setLocalTimeValue(this.localTimeValue);
            return time;
        }

        /**
         * Adds a time value to this time.
         *
         * @param timeValue The time value to add.
         */
        public void addToTime(long timeValue) {
            this.localTimeValue += timeValue;
        }

        /**
         * Returns the time value representing the current time in group time.
         */
        public long getNetTimeValue() {
            return this.localTimeValue + getTimeDiff();
        }

        /**
         * Returns the time value representing the current time in group time.
         */
        public long getTimeValue() {
            return this.localTimeValue;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         * <p/>
         * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
         * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
         * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
         * <tt>y.compareTo(x)</tt> throws an exception.)
         * <p/>
         * <p>The implementor must also ensure that the relation is transitive:
         * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
         * <tt>x.compareTo(z)&gt;0</tt>.
         * <p/>
         * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
         * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
         * all <tt>z</tt>.
         * <p/>
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
         * class that implements the <tt>Comparable</tt> interface and violates
         * this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is
         * inconsistent with equals."
         * <p/>
         * <p>In the foregoing description, the notation
         * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
         * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
         * <tt>0</tt>, or <tt>1</tt> according to whether the value of
         * <i>expression</i> is negative, zero or positive.
         *
         * @param t the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         *         is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(Time t) {
            long t_timeValue = t.getTimeValue();
            long l_timeValue = getTimeValue();
            if (t_timeValue < l_timeValue) {
                return -1;
            }
            else if (t_timeValue > l_timeValue) {
                return 1;
            }
            return 0;
        }

        /**
         * @return The string representation of this time.
         */
        public String toString() {
            return "" + this.localTimeValue;
        }

        /**
         * Returns the net time as a Date.
         */
        public Date getNetTimeDate() {
            return new Date(getNetTimeValue());
        }

        /**
         * Returns the net time as a Calendar.
         */
        public Calendar getNetTimeCalendar() {
            Calendar cal = Calendar.getInstance();
            cal.setTime(getNetTimeDate());
            return cal;
        }

        /**
         * Converts the net time to local time and returns as a Calendar.
         *
         * @param locale The locale to use.
         */
        public Calendar getNetTimeCalendar(Locale locale) {
            Calendar cal = Calendar.getInstance(locale);
            cal.setTime(getNetTimeDate());
            return cal;
        }

        /**
         * Converts the net time to local time and returns as a Date.
         */
        public Date getLocalTimeDate() {
            return new Date(getTimeValue());
        }

        /**
         * Converts the net time to local time and returns as a Calendar.
         */
        public Calendar getLocalTimeCalendar() {
            Calendar cal = Calendar.getInstance();
            cal.setTime(getLocalTimeDate());
            return cal;
        }

        /**
         * Converts the net time to local time and returns as a Calendar.
         *
         * @param locale The locale to use.
         */
        public Calendar getLocalTimeCalendar(Locale locale) {
            Calendar cal = Calendar.getInstance(locale);
            cal.setTime(getLocalTimeDate());
            return cal;
        }
    }
}
