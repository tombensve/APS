/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.1
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
 *         2012-09-08: Created!
 *         
 */
package se.natusoft.osgi.aps.api.misc.session;

/**
 * This represents an active session.
 */
public interface APSSession {

    /**
     * @return The id of this session.
     */
    String getId();

    /**
     * @return true if this session is still valid.
     */
    boolean isValid();

    /**
     * Saves an object in the session. Will do nothing if the session is no longer valid.
     *
     * @param name The name to store the object under.
     * @param object An object to store in the session.
     */
    void saveObject(String name, Object object);

    /**
     * Returns a object stored under the specified name or null if no object is stored under that name.
     * <p/>
     * If isValid() returns false then this will always return null.
     *
     * @param name The name of the object to get.
     */
    Object retrieveObject(String name);
}
