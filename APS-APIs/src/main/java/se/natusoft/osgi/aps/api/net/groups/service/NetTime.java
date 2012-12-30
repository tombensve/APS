/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.0
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-30: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.groups.service;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This represents a common network time between members for handling date and time data.
 * The net time is synchronized between all members. Each receiver of net time diffs it with
 * local time and stores the diff so that they can convert to/from local/net time.
 */
public interface NetTime extends Serializable {

    /**
     * Returns the number of milliseconds since Januray 1, 1970 in net time.
     */
    public long getNetTime();

    /**
     * Returns the net time as a Date.
     */
    public Date getNetTimeDate();

    /**
     * Returns the net time as a Calendar.
     */
    public Calendar getNetTimeCalendar();

    /**
     * Returns the net time as a Calendar.
     *
     * @param locale The locale to use.
     */
    public Calendar getNetTimeCalendar(Locale locale);

    /**
     * Converts the net time to local time and returns as a Date.
     */
    public Date getLocalTimeDate();

    /**
     * Converts the net time to local time and returns as a Calendar.
     */
    public Calendar getLocalTimeCalendar();

    /**
     * Converts the net time to local time and returns as a Calendar.
     *
     * @param locale The locale to use.
     */
    public Calendar getLocalTimeCalendar(Locale locale);
}
