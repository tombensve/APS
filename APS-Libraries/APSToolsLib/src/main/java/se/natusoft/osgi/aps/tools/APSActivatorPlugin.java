/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         1.0.0
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
 *     tommy ()
 *         Changes:
 *         2017-01-05: Created!
 *         
 */
package se.natusoft.osgi.aps.tools;

/**
 * A service API to implement and publish to be able to plugin to APSActivator.
 *
 * APSActivator will look for all published instances of this service and call them.
 */
@SuppressWarnings("unused")
public interface APSActivatorPlugin {

    /**
     * For the plugin to interact with the activator.
     */
    interface ActivatorInteraction {

        /**
         * Adds an instance to manage.
         *
         * @param instance The instance to add.
         * @param forClass The class of the instance to receive this 'instance'.
         */
        void addManagedInstance(Object instance, Class forClass);
    }

    /**
     * When APSActivator analyzes each class of the bundle it will also pass the class to this method.
     *
     * @param bundleClass The analyzed class.
     */
    void analyseBundleClass(ActivatorInteraction activatorInteraction, Class bundleClass);
}
