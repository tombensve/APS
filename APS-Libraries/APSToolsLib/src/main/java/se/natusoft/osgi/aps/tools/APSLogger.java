/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         0.9.2
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
 *         2011-08-04: Created!
 *         
 */
package se.natusoft.osgi.aps.tools;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This wraps a LogService instance and simply logs to stdout when no logservice is available. 
 */
public class APSLogger {
    
    //
    // Private Members
    //

    /** The service reference of the service this logger is for. */
    private ServiceReference svcRef = null;

    /** This can be set for non services using APSLogger. */
    private String loggingFor = "";

    /** A LogService tracker. */
    private APSServiceTracker<LogService> logServiceTracker = null;

    /** The tracked service instance. */
    private LogService logService = null;
    
    /** The stream to write to when log service is not available. */
    private PrintStream outStream = System.out;

    //
    // Constructors
    //

    /**
     * Creates a new _APSLogger_ instance. This will log to the specified stream when no LogService is available.
     *
     * @param outStream The stream to log to when no LogService is available. Set to null to not log at all when no LogService is available.
     */
    public APSLogger(OutputStream outStream) {
        if (outStream != null) {
            if (outStream instanceof PrintStream) {
                this.outStream = (PrintStream)outStream;
            }
            else {
                this.outStream = new PrintStream(outStream);
            }
        }
    }

    /**
     * Creates the logger in its simplest form. This wont log anywhere until start(context) has been called
     * and at least one LogService is available.
     */
    public APSLogger() {}
    
    //
    // Methods
    //

    /**
     * This will start tracking a LogService to use for logging. When available logs will be sent to
     * the LogService instead of the backup stream.
     *
     * @param context The bundle context.
     */
    public void start(BundleContext context) {
        this.logServiceTracker = new APSServiceTracker<LogService>(context, LogService.class);
        this.logServiceTracker.start();
        this.logService = this.logServiceTracker.getWrappedService();
    }

    /**
     * Stops tracking a LogService to log to.
     *
     * @param context The bundle context.
     */
    public void stop(BundleContext context) {
        this.logService = null;
        if (this.logServiceTracker != null) {
            this.logServiceTracker.stop(context);
        }
    }

    /**
     * Sets the service reference of the service to identify as logger.
     * 
     * @param svcRef The service reference to set.
     */
    public void setServiceReference(ServiceReference svcRef) {
        this.svcRef = svcRef;
    }
    
    /**
     * @return The service reference.
     */
    protected ServiceReference getServiceReference() {
        return this.svcRef;
    }

    /**
     * This can be set if this logger is not used by a service. This will be prepended to each logmessage surrounded by [].
     *
     * @param loggingFor The name of the object this logger is logging for.
     */
    public void setLoggingFor(String loggingFor) {
        this.loggingFor = "[" + loggingFor + "]";
    }

    /**
     * This method does the main job of this class. It forwards to the log service and
     * if that fails it logs to the stream.
     * 
     * @param level The loglevel.
     * @param message The log message.
     * @param cause An optional Throwable that is the cause of the log.
     */
    protected void log(int level, String message, Throwable cause) {
        if (this.logService != null) {
            try {
                    logToService(this.logService, level, message, cause);
            }
            catch (Exception e) {
                logToOutStream(level, message, cause);
            }
        }
        else {
            logToOutStream(level, message, cause);
        }
    }
    
    /**
     * This logs to the log service.
     * 
     * @param logService the LogService to log to.
     * @param level The loglevel.
     * @param message The log message.
     * @param cause An optional Throwable that is the cause of the log.
     */
    protected void logToService(LogService logService, int level, String message, Throwable cause) throws Exception {
        if (svcRef != null) {
            if (cause != null) {
                logService.log(this.svcRef, level, this.loggingFor + message, cause);
            }
            else {
                logService.log(this.svcRef, level, this.loggingFor + message);
            }
        }
        else {
            if (cause != null) {
                logService.log(level, this.loggingFor + message, cause);
            }
            else {
                logService.log(level, this.loggingFor + message);
            }
        }        
    }
    
    /**
     * This logs to the output stream.
     * 
     * @param level The loglevel.
     * @param message The log message.
     * @param cause An optional Throwable that is the cause of the log.
     */
    protected void logToOutStream(int level, String message, Throwable cause) {
        if (this.outStream != null) {
            StringBuilder log = new StringBuilder();
            switch (level) {
                case LogService.LOG_DEBUG:
                    log.append("DEBUG: ");
                    break;
                case LogService.LOG_ERROR:
                    log.append("ERROR: ");
                    break;
                case LogService.LOG_INFO:
                    log.append("INFO: ");
                    break;
                case LogService.LOG_WARNING:
                    log.append("WARNING: ");
            }
            if (svcRef != null) {
                Object bundleNameObj = svcRef.getProperty(Constants.BUNDLE_NAME);
                if (bundleNameObj != null) {
                    log.append("[" + bundleNameObj + "] ");
                }
            }
            log.append(this.loggingFor);
            log.append(message);
            if (cause != null) {
                log.append('\n');
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                cause.printStackTrace(pw);
                log.append(sw.toString());
                pw.close();
            }
            this.outStream.println(log.toString());
        }
    }
    
    /**
     * Does a debug log.
     * 
     * @param message The log message.
     */
    public void debug(String message) {
        log(LogService.LOG_DEBUG, message, null);
    }
    
    /**
     * Does a debug log.
     * 
     * @param message The log message.
     * @param cause The cause of the log entry.
     */
    public void debug(String message, Throwable cause) {
        log(LogService.LOG_DEBUG, message, cause);
    }
    
    /**
     * Does an error log.
     * 
     * @param message The log message.
     */
    public void error(String message) {
        log(LogService.LOG_ERROR, message, null);
    }
    
    /**
     * Does an error log.
     * 
     * @param message The log message.
     * @param cause The cause of the log entry.
     */
    public void error(String message, Throwable cause) {
        log(LogService.LOG_ERROR, message, cause);
    }
    
    /**
     * Does an info log.
     * 
     * @param message The log message.
     */
    public void info(String message) {
        log(LogService.LOG_INFO, message, null);
    }
    
    /**
     * Does an info log.
     * 
     * @param message The log message.
     * @param cause The cause of the log entry.
     */
    public void info(String message, Throwable cause) {
        log(LogService.LOG_INFO, message, cause);
    }
    
    /**
     * Does a warning log.
     * 
     * @param message The log message.
     */
    public void warn(String message) {
        log(LogService.LOG_WARNING, message, null);
    }
    
    /**
     * Does a warning log.
     * 
     * @param message The log message.
     * @param cause The cause of the log entry.
     */
    public void warn(String message, Throwable cause) {
        log(LogService.LOG_WARNING, message, cause);
    }
}
