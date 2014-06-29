/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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
 *         2013-08-02: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.groups.service;

import java.util.List;

/**
 * This service provides information about current groups and members.
 */
public interface APSGroupsInfoService {
    /**
     * Returns the names of all available groups.
     */
    List<String> getGroupNames();

    /**
     * Returns a list of member ids for the specified group.
     *
     * @param groupName The name of the group to get member ids for.
     */
    List<String> getGroupMembers(String groupName);

    /**
     * Returns a list of "groupName : groupMember" for all groups and members.
     */
    List<String> getGroupsAndMembers();
}
