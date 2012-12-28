/* 
 * 
 * PROJECT
 *     Name
 *         APS APSNetworkGroups
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
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.osgi.aps.groups.logging;

import se.natusoft.apsgroups.logging.APSGroupsLogger;
import se.natusoft.osgi.aps.tools.APSLogger;

/**
 * Relays all logs to the wrapped APSLogger.
 */
public class APSGroupsLoggerRelay implements APSGroupsLogger {
    //
    // Private Members
    //

    /** The APS OSGi service logger. */
    private APSLogger logger = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSGroupsLoggerRelay.
     *
     * @param logger The APS OSGi service logger to relay logs to.
     */
    public APSGroupsLoggerRelay(APSLogger logger) {
        this.logger = logger;
    }

    //
    // Methods
    //

    public APSLogger getLogger() {
        return this.logger;
    }

    @Override
    public void debug(String message) {
        this.logger.debug(message);
    }

    @Override
    public void debug(String message, Throwable exception) {
        this.logger.debug(message, exception);
    }

    @Override
    public void info(String message) {
        this.logger.info(message);
    }

    @Override
    public void info(String message, Throwable exception) {
        this.logger.info(message, exception);
    }

    @Override
    public void warn(String message) {
        this.logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable exception) {
        this.logger.warn(message, exception);
    }

    @Override
    public void error(String message) {
        this.logger.error(message);
    }

    @Override
    public void error(String message, Throwable exception) {
        this.logger.error(message, exception);
    }
}
