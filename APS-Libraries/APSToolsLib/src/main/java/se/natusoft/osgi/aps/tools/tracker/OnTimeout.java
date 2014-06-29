/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides a library of utilities, among them APSServiceTracker used by all other APS bundles.
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
 *         2011-10-17: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.tracker;

/**
 * This interface is used in conjunction with _APSServiceTracker_ and provides callback code to run when a service becomes
 * unavailable.
 */
public interface OnTimeout<Service> {

    /**
     * This gets called on timeout if registered with tracker.onTimeout(...). The call is done before throwing
     * APSNoServiceAvailableException.
     *
     * @throws RuntimeException If an Exception is thrown it will replace the APSNoServiceAvailableException!
     */
    public void onTimeout() throws RuntimeException;
}
