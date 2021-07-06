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
 *         2015-01-10: Created!
 *
 */
package se.natusoft.aps.activator;

import java.util.List;

/**
 * Provides dynamic setup of each service instance to create.
 */
public interface APSActivatorServiceSetupProvider {

    /**
     * Provides setup for each instance to create and register.
     *
     * Each returned Setup instance will result in one registered service instance.
     *
     * Each Properties instance should contain a common property with different values
     * that can be searched for during service lookup/tracking.
     */
    List<ServiceSetup> provideServiceInstancesSetup();

}
