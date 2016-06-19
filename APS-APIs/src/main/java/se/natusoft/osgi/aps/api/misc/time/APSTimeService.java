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
 *         2016-06-12: Created!
 *         
 */
package se.natusoft.osgi.aps.api.misc.time;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;

import java.time.Instant;

/**
 * Intended for providing a common time across nodes. This is most simply implemented by using a common ntp server
 * as source. Note that the time returned by this service might not be the same as the local time!
 */
public interface APSTimeService {

    /**
     * Returns the time provided by the service.
     */
    @NotNull Instant getTime();

    /**
     * Returns the last time the time was updated or null if there have been no successful updates.
     */
    @Nullable Instant getLastTimeUpdate();
}
