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
 *         2012-12-28: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.groups.service;

import java.io.IOException;
import java.util.Properties;

/**
 * A service that lets clients send data reliable to all members of a group on any host. There is
 * no limit on the size of the data sent, but that said I wouldn't send MB:s of data!
 */
@Deprecated
public interface APSGroupsService {
    /**
     * Joins a group.
     *
     * @param name The name of the group to join.
     *
     * @return A GroupMember that provides the API for sending and receiving data in the group.
     *
     * @throws java.io.IOException The unavoidable one!
     */
    GroupMember joinGroup(String name) throws IOException;

    /**
     * Joins a group.
     *
     * @param name The name of the group to join.
     * @param memberUserData Data provided by users of the service.
     *
     * @return A GroupMember that provides the API for sending and receiving data in the group.
     *
     * @throws java.io.IOException The unavoidable one!
     */
    GroupMember joinGroup(String name, Properties memberUserData) throws IOException;

    /**
     * Leaves as member of group.
     *
     * @param groupMember The GroupMember returned when joined.
     *
     * @throws java.io.IOException The unavoidable one!
     */
    void leaveGroup(GroupMember groupMember) throws IOException;
}
