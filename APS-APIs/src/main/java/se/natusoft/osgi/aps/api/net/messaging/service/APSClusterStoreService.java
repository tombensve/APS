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
 *         2015-01-09: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.messaging.service;

import java.util.List;
import java.util.Map;

/**
 * Network storage available to all members of a cluster.
 */
public interface APSClusterStoreService {

    /**
     * Returns the name of this instance.
     */
    String getName();

    /**
     * Returns a named map into which objects can be stored with a name.
     */
    Map<String, Object> getNamedMap(String name);

    /**
     * Returns the available names.
     */
    List<String> getAvailableNames();

    /**
     * Returns true if the implementation supports persistence for stored objects. If false is returned
     * objects are in memory only.
     */
    boolean supportPersistence();
}
