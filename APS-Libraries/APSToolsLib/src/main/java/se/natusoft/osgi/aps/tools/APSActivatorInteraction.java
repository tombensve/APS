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
 * This should be injected with @Managed. When this is available APSActivator will not by default register the service with
 * the OSGi platform. The service itself have to set state READY for that to happen.
 */
public interface APSActivatorInteraction {

    /**
     * Holds the signal that can be send.
     */
    enum State {
        /** Indicates that the service is in a startup state. */
        IN_STARTUP,

        /** Indicates the the service is ready to be used. APSActivator will register service with OSGi platform when it sees this. */
        READY,

        /** Indicates that startup failed, and that APSActivator should shutdown */
        STARTUP_FAILED
    }

    /** The default value to use for state. */
    State DEFAULT_VALUE = State.IN_STARTUP;

    /**
     * Sends signals to the APSActivator.
     *
     * @param state The state to set.
     */
    void setState(State state);

    /**
     * Returns the current state.
     */
    State getState();
}
