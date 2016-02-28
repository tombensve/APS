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
 *         2016-02-27: Created!
 *         
 */
package se.natusoft.osgi.aps.util;

/**
 * This is a utility base class.
 */
public class APSObject {
    //
    // Private Members
    //

    /** Flag to keep track of if delayedInit() has been called or not. */
    private boolean initDone = false;

    //
    // Methods
    //

    /**
     * A call to this will call delayedInit() once and only once.
     */
    protected void init() {
        if (!this.initDone) {
            delayedInit();
            this.initDone = true;
        }
    }

    /**
     * This does nothing, it is intended to be overridden.
     */
    protected void delayedInit() {}
}
