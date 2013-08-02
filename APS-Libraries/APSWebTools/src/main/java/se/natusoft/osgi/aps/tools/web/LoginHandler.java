/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         This provides some utility classes for web applications.
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
 *         2013-02-03: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web;

/**
 * This is a simple API for doing a login.
 */
public interface LoginHandler {

    /**
     * Returns true if this handler sits on a valid login.
     */
    public boolean hasValidLogin();

    /**
     * Logs in with a userid and a password.
     *
     * @param userId The id of the user to login.
     * @param pw The password of the user to login.
     *
     * @return true if successfully logged in, false otherwise.
     */
    boolean login(String userId, String pw);

    /**
     * If the handler creates service trackers or other things that needs to be shutdown
     * when no longer used this method needs to be called when the handler is no longer
     * needed.
     */
    public void shutdown();
}
