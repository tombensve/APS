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
package se.natusoft.osgi.aps.api.general;

import java.util.UUID;

/**
 * This is a general client identification for when such are needed.
 */
public final class ClientID {

    //
    // Private Members
    //

    /** An optional name of the client. */
    private String name = "<anonymous>";

    /** A unique id for the client. */
    private UUID id = UUID.randomUUID();

    //
    // Constructors
    //

    /**
     * Creates an anonymous client ID.
     */
    public ClientID() {}

    /**
     * Creates a named client ID.
     *
     * @param name The name of this client.
     */
    public ClientID(String name) {
        this.name = name;
    }

    //
    // Methods
    //

    /**
     * Returns the ID of this client.
     */
    public final UUID getId() {
        return this.id;
    }

    /**
     * Returns the name of this client.
     */
    public final String getName() {
        return this.name;
    }
}
