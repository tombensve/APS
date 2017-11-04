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
 * API for publishing data to consumers.
 *
 * @param <Published> The type published.
 */
public interface APSPublisher<Published> {

    /* The following are useful metadata suggestions. */

    /** Names the data. */
    String META_NAME = "apsName";

    /** When this is non null then state is valid and the property contains one of the above states. */
    String META_ENABLE_STATE_PROPERTY = "STATE_PROPERTY";

    /** The default state property. */
    String META_DEFAULT_STATE_PROPERTY = "apsState";

    /** This indicates that the named data delivered is new. */
    String META_STATE_NEW = "STATE_NEW";

    /** This indicates that the named data delivered have been seen before, but is updated. */
    String META_STATE_UPDATE = "STATE_UPDATED";

    /** This indicates that the named data have been revoked and is thus invalid. */
    String META_STATE_REVOKE = "STATE_REVOKED";

    /**
     * Publishes data.
     *
     * @param published The published data.
     * @param meta Meta data about the published data.
     */
    void apsPublish(Published published, Map<String, String> meta);
}
