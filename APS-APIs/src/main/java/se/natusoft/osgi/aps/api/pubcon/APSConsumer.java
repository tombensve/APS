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
 *         2017-10-29: Created!
 *
 */
package se.natusoft.osgi.aps.api.pubcon;

import java.util.Map;

/**
 * API for consuming APS PubCon data.
 *
 * @param <Consumed> The type of data consumed.
 */
public interface APSConsumer<Consumed> {

    /**
     * Consumes data.
     *
     * @param consumed The data consumed.
     * @param meta Meta data about the data consumed.
     */
    void apsConsume(Consumed consumed, Map<String, String> meta);
}
