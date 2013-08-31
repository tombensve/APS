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
package se.natusoft.osgi.aps.api.net.time.service;

import java.util.Date;

/**
 * This service provides network neutral time. Even with NTP it is difficult to keep the same time on different
 * servers. This service creates a network timezone and broadcasts the network time. It supports converting
 * local time to network time and converting network time to local time.
 *
 * Please note that the network time will not be accurate down to milliseconds, but will be reasonable correct
 * for most usages.
 */
public interface APSNetTimeService {

    /**
     * Converts from net time to local time.
     *
     * @param netTime The net time to convert.
     *
     * @return local time.
     */
    public long netToLocalTime(long netTime);

    /**
     * Converts from net time to local time.
     *
     * @param netTime The net time to convert.
     *
     * @return local time.
     */
    public Date netToLocalTime(Date netTime);

    /**
     * Converts from local time to net time.
     *
     * @param localTime The local time to convert.
     *
     * @return net time.
     */
    public long localToNetTime(long localTime);

    /**
     * Converts from local time to net time.
     *
     * @param localTime The local time to convert.
     *
     * @return net time.
     */
    public Date localToNetTime(Date localTime);
}
