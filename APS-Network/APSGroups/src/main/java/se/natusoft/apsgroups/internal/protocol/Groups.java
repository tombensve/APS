/* 
 * 
 * PROJECT
 *     Name
 *         APS APSNetworkGroups
 *     
 *     Code Version
 *         0.9.0
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

import java.util.HashMap;
import java.util.Map;

/**
 * This represents all locally known groups.
 */
public class Groups {
    //
    // Members
    //

    /** The known groups. */
    private static Map<String, Group> groups = new HashMap<>();

    //
    // Methods
    //

    /**
     * Returns the named group.
     *
     * @param name The name of the group to get.
     */
    public synchronized static Group getGroup(String name) {
        Group group = groups.get(name);
        if (group == null) {
            group = new Group(name);
            groups.put(name, group);
        }

        return group;
    }
}
