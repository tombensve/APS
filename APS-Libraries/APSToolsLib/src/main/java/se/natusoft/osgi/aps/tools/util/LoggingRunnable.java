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
package se.natusoft.osgi.aps.tools.util;

import se.natusoft.osgi.aps.tools.APSLogger;

/**
 * This implements Runnable and wraps the call with a try+catch.
 *
 * This class is abstract and must be extended to implement doRun().
 */
public abstract class LoggingRunnable implements Runnable {
    //
    // Private Members
    //

    private APSLogger logger;

    //
    // Constructor
    //

    /**
     * Creates a new LoggingRunnable.
     *
     * @param logger The logger to log to.
     */
    public LoggingRunnable(APSLogger logger) {
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Subclasses must provide this. It will be called on run().
     *
     * @throws Exception Any such will be cauth and logged.
     */
    public abstract void doRun() throws Exception;

    /**
     * Calls doRun() wrapped with a try+catch.
     */
    public void run() {
        try {
            doRun();
        }
        catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}
