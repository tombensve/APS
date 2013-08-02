/* 
 * 
 * PROJECT
 *     Name
 *         APS Resolving Bundle Deployer
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         Deploys bundles resolving dependencies as automatically as possible by accepting a few
 *         deploy failures and retrying until it works or a fail threshold has been reached.
 *         
 *         Unless the server deployed on supports WABs using the extender pattern, no war files
 *         will deploy correctly.
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
 *         2013-01-03: Created!
 *         
 */
package se.natusoft.osgi.aps.bundledeployer.deploy;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * A simple logger.
 */
public class Logger {
    //
    // Private Members
    //

    /** The context of our bundle. */
    private BundleContext context = null;

    /** A reference to the log service. */
    private ServiceReference logServiceRef = null;

    //
    // Constructors
    //

    /**
     * Creates a new Logger.
     *
     * @param bundleContext The context of our bundle.
     */
    public Logger(BundleContext bundleContext) {
        this.context = bundleContext;
    }

    //
    // Methods
    //

    /**
     * @return A LogService instance if such can be found.
     */
    private LogService getLogService() {
        if (this.logServiceRef == null) {
            this.logServiceRef = this.context.getServiceReference(LogService.class.getName());
        }
        if (this.logServiceRef == null) {
            return null;
        }

        return (LogService)this.context.getService(this.logServiceRef);
    }

    /**
     * Releases a successfully gotten LogService.
     */
    private void releaseLogService() {
        if (this.logServiceRef != null) {
            this.context.ungetService(this.logServiceRef);
        }
    }

    /**
     * Logs info message.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        LogService logService = getLogService();
        if (logService != null) {
            logService.log(LogService.LOG_INFO, message);
            releaseLogService();
        }
        else {
            System.out.println("INFO: " + message);
        }
    }

    /**
     * Logs info message.
     *
     * @param message The message to log.
     * @param t A throwable that is the cause of the log.
     */
    public void info(String message, Throwable t) {
        LogService logService = getLogService();
        if (logService != null) {
            logService.log(LogService.LOG_INFO, message, t);
            releaseLogService();
        }
        else {
            System.out.println("INFO: " + message);
            t.printStackTrace();
        }
    }

    /**
     * Logs error message.
     *
     * @param message The message to log.
     */
    public void error(String message) {
        LogService logService = getLogService();
        if (logService != null) {
            logService.log(LogService.LOG_ERROR, message);
            releaseLogService();
        }
        else {
            System.out.println("ERROR: " + message);
        }
    }

    /**
     * Logs error message.
     *
     * @param message The message to log.
     * @param t The throwable that is the cause of the log.
     */
    public void error(String message, Throwable t) {
        LogService logService = getLogService();
        if (logService != null) {
            logService.log(LogService.LOG_ERROR, message, t);
            releaseLogService();
        }
        else {
            System.out.println("ERROR: " + message);
            t.printStackTrace();
        }
    }
}
