/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
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
 *         2011-10-17: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.tracker;

import org.osgi.framework.ServiceReference;

/**
 * This interface is used in conjunction with APSServiceTracker and provides callback code to run when a service becomes
 * available.
 */
public interface OnServiceAvailable<Service> {

    /**
     * Receives a new service.
     *
     * @param service The received service.
     * @param serviceReference The reference to the received service.
     *
     * @throws Exception Implementation can throw any exception. How it is handled depends on the APSServiceTracker method this
     *                   gets passed to.
     */
    public void onServiceAvailable(Service service, ServiceReference serviceReference) throws Exception;
}
