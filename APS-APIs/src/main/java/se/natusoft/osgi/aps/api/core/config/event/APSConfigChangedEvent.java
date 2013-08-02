/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.2
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
 *         2012-07-19: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.config.event;

/**
 * This is an event object passed on updated config.
 */
public class APSConfigChangedEvent {
    //
    // Private Members
    //

    /** The id of the configuration that changed. */
    private String configId;

    //private APSConfig

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigChangedEvent instance.
     *
     * @param configId The id of the changed config.
     */
    public APSConfigChangedEvent(String configId) {
        this.configId = configId;
    }

    //
    // Methods
    //

    /**
     * @return The id of the changed config (top level config objects only!).
     */
    public String getConfigId() {
        return this.configId;
    }
}
