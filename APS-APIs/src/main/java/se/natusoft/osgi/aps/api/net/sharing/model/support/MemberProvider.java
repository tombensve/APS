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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2014-08-25: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.sharing.model.support;

import se.natusoft.osgi.aps.api.net.sharing.model.Member;

import java.util.UUID;

/**
 * Provides a default implementation of Member.
 */
public class MemberProvider implements Member {
    //
    // Private Members
    //

    /** The id of the member. */
    private UUID id;

    //
    // Constructors
    //

    /**
     * Creates a new MemberProvider.
     */
    public MemberProvider() {
        this.id = UUID.randomUUID();
    }

    /**
     * Creates a new MemberProvider.
     *
     * @param id The id of the member.
     */
    public MemberProvider(UUID id) {
        this.id = id;
    }

    //
    // Methods
    //

    /**
     * Returns the ID of this member.
     */
    @Override
    public UUID getId() {
        return null;
    }
}
