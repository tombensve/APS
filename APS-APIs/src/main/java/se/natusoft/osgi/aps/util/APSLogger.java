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
 *         2011-08-04: Created!
 *
 */
package se.natusoft.osgi.aps.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import se.natusoft.osgi.aps.tracker.APSNoServiceAvailableException;
import se.natusoft.osgi.aps.tracker.APSServiceTracker;
import se.natusoft.osgi.aps.tuples.Tuple3;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This wraps a LogService instance and simply logs to stdout when no logservice is available.
 *
 * Note that this also implements the LogService API!
 */
public class APSLogger implements LogService {
    //
    // Constants
    //

    /**
     * For property constructor. The value is an OutputStream to log to.
     */
    @SuppressWarnings( "WeakerAccess" )
    public static final String PROP_OUTPUT_STREAM = "out-stream";

    /**
     * For property constructor. Any value will do. Will save any log entries done before the start(bc) call
     * and send them to the log service when started if such is available.
     */
    @SuppressWarnings( "WeakerAccess" )
    public static final String PROP_CACHE_AND_DELAY = "cache-and-delay";

    /**
     * For setting then name of the logger.
     */
    public static final String PROP_LOGGING_FOR = "loggingFor";

    private static final SimpleDateFormat YYMMDD_HHMMSS = new SimpleDateFormat( "yy-MM-dd hh:mm:ss" );

    //
    // Private Members
    //

    /**
     * The service reference of the service this logger is for.
     */
    private ServiceReference svcRef = null;

    /**
     * This can be set for non services using APSLogger.
     */
    private String loggingFor = "";

    /**
     * A LogService tracker.
     */
    private APSServiceTracker<LogService> logServiceTracker = null;

    /**
     * The tracked service instance.
     */
    private LogService logService = null;

    /**
     * The stream to write to when log service is not available.
     */
    private PrintStream outStream = System.out;

    /**
     * List of delayed log entries.
     */
    private List<Tuple3<Integer, String, Exception>> delayedLogEntries = null;

    /**
     * The logging bundle.
     */
    private Bundle bundle;

    //
    // Constructors
    //

    /**
     * Creates a new _APSLogger_ instance. This will log to the specified stream when no LogService is available.
     *
     * @param outStream The stream to log to when no LogService is available. Set to null to not log at all when no LogService is available.
     */
    public APSLogger( OutputStream outStream ) {

        if ( outStream != null ) {

            if ( outStream instanceof PrintStream ) {

                this.outStream = ( PrintStream ) outStream;
            }
            else {

                this.outStream = new PrintStream( outStream );
            }
        }
    }

    /**
     * A new more flexible constructor where named properties can be passed.
     *
     * @param props Configuration properties.
     */
    public APSLogger( Map<String, Object> props ) {

        if ( props.containsKey( PROP_OUTPUT_STREAM ) ) {

            this.outStream = new PrintStream( ( OutputStream ) props.get( "out-stream" ) );
        }
        else if ( props.containsKey( PROP_CACHE_AND_DELAY ) ) {

            this.delayedLogEntries = Collections.synchronizedList( new LinkedList<>() );
        }
        else if ( props.containsKey( PROP_LOGGING_FOR ) ) {

            setLoggingFor( ( String ) props.get( PROP_LOGGING_FOR ) );
        }
    }

    /**
     * A version of the properties Map constructor taking the properties as an array.
     *
     * @param propsArray An array of properties pairs of 2: property, value.
     */
    public APSLogger( Object... propsArray ) {
        this( arrayToProps( propsArray ) );
    }

    /**
     * Creates the logger in its simplest form. This wont log anywhere until start(context) has been called
     * and at least one LogService is available.
     */
    public APSLogger() {
    }

    //
    // Methods
    //

    /**
     * Converts and array of properties to a Map.
     *
     * @param propsArray The array to convert.
     */
    private static Map<String, Object> arrayToProps( Object... propsArray ) {

        Map<String, Object> props = new HashMap<>();
        int propIx = 0;

        while ( propIx < propsArray.length ) {

            if ( !( ( propIx + 1 ) < propsArray.length ) ) {

                throw new IllegalArgumentException( "Bad properties array! Last property is missing a value!" );
            }

            props.put( propsArray[ propIx ].toString(), propsArray[ propIx + 1 ] );
            propIx += 2;
        }

        return props;
    }

    /**
     * This will start tracking a LogService to use for logging. When available logs will be sent to
     * the LogService instead of the backup stream.
     *
     * @param context The bundle context.
     */
    @Deprecated
    public void start( BundleContext context ) {
        connectToLogService( context );
    }

    /**
     * This will start tracking a LogService to use for logging. When available logs will be sent to
     * the LogService instead of the backup stream.
     *
     * @param context The bundle context.
     */
    public void connectToLogService( BundleContext context ) {

        this.bundle = context.getBundle();

        // This has no timeout and will thus fail immediately if no service is available to avoid longer blocking when logger
        // is used from bundle activator start() method.
        this.logServiceTracker = new APSServiceTracker<>( context, LogService.class );
        this.logServiceTracker.start();
        // This is what is used from now on to log to LogService.
        this.logService = this.logServiceTracker.getWrappedService();

        if ( this.delayedLogEntries != null ) {

            this.delayedLogEntries.forEach( entry -> {

                try {

                    logToService( this.logService, entry.t1, entry.t2, entry.t3 );
                } catch ( Exception ignore ) {
                }
            } );

            this.delayedLogEntries = null;
        }

    }

    /**
     * Stops tracking a LogService to log to.
     *
     * @param context The bundle context.
     */
    @Deprecated
    public void stop( BundleContext context ) {
        disconnectFromLogService( context );
    }

    /**
     * Stops tracking a LogService to log to.
     *
     * @param context The bundle context.
     */
    public void disconnectFromLogService( BundleContext context ) {
        this.logService = null;

        if ( this.logServiceTracker != null ) {

            this.logServiceTracker.stop( context );
        }
        this.bundle = null;
    }

    /**
     * Sets the service reference of the service to identify as logger.
     *
     * @param svcRef The service reference to set.
     */
    public void setServiceReference( ServiceReference svcRef ) {
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
    public void setLoggingFor( String loggingFor ) {
        this.loggingFor = "[" + loggingFor + "]";
    }

    /**
     * This method does the main job of this class. It forwards to the log service and
     * if that fails it logs to the stream.
     *
     * @param level   The loglevel.
     * @param message The log message.
     * @param cause   An optional Throwable that is the cause of the log.
     */
    public void log( int level, String message, Throwable cause ) {

        if ( this.logService != null ) {

            try {

                logToService( this.logService, level, message, cause );

            } catch ( APSNoServiceAvailableException nsae ) {

                logToOutStream( level, message, cause );

            } catch ( Exception e ) {

                logToOutStream( level, message, cause );
                logToOutStream( LogService.LOG_ERROR, "APSLogger had an unexpected problem when trying to use LogService!", e );
            }
        }
        else {

            if ( this.delayedLogEntries == null ) {

                logToOutStream( level, message, cause );
            }
        }
    }

    /**
     * Provides LogService API!
     *
     * <hr>
     * <p>
     * Logs a message.
     * <p/>
     * <p/>
     * The <code>ServiceReference</code> field and the <code>Throwable</code> field
     * of the <code>LogEntry</code> object will be set to <code>null</code>.
     *
     * @param level   The severity of the message. This should be one of the
     *                defined log levels but may be any integer that is interpreted in a
     *                user defined way.
     * @param message Human readable string describing the condition or
     *                <code>null</code>.
     * @see #LOG_ERROR
     * @see #LOG_WARNING
     * @see #LOG_INFO
     * @see #LOG_DEBUG
     */
    @Override
    public void log( int level, String message ) {
        log( level, message, null );
    }

    /**
     * Provides LogService API!
     *
     * <hr>
     * <p>
     * Logs a message associated with a specific <code>ServiceReference</code>
     * object.
     * <p/>
     * <p/>
     * The <code>Throwable</code> field of the <code>LogEntry</code> will be set to
     * <code>null</code>.
     *
     * @param sr      THIS IS CURRENTLY IGNORED!
     * @param level   The severity of the message. This should be one of the
     *                defined log levels but may be any integer that is interpreted in a
     *                user defined way.
     * @param message Human readable string describing the condition or
     *                <code>null</code>.
     * @see #LOG_ERROR
     * @see #LOG_WARNING
     * @see #LOG_INFO
     * @see #LOG_DEBUG
     */
    @Override()
    public void log( ServiceReference sr, int level, String message ) {
        log( level, message );
    }

    /**
     * Provides LogService API!
     *
     * <hr>
     * <p>
     * Logs a message with an exception associated and a
     * <code>ServiceReference</code> object.
     *
     * @param sr        THIS IS CURRENTLY IGNORED!
     * @param level     The severity of the message. This should be one of the
     *                  defined log levels but may be any integer that is interpreted in a
     *                  user defined way.
     * @param message   Human readable string describing the condition or
     *                  <code>null</code>.
     * @param exception The exception that reflects the condition or
     *                  <code>null</code>.
     * @see #LOG_ERROR
     * @see #LOG_WARNING
     * @see #LOG_INFO
     * @see #LOG_DEBUG
     */
    @Override
    public void log( ServiceReference sr, int level, String message, Throwable exception ) {
        log( level, message, exception );
    }

    /**
     * This logs to the log service.
     *
     * @param logService the LogService to log to.
     * @param level      The loglevel.
     * @param message    The log message.
     * @param cause      An optional Throwable that is the cause of the log.
     */
    @SuppressWarnings( "WeakerAccess" )
    protected void logToService( LogService logService, int level, String message, Throwable cause ) {

        if ( svcRef != null ) {

            if ( cause != null ) {

                logService.log( this.svcRef, level, this.loggingFor + message, cause );
            }
            else {

                logService.log( this.svcRef, level, this.loggingFor + message );
            }
        }
        else {
            if ( cause != null ) {

                logService.log( level, this.loggingFor + message, cause );
            }
            else {

                logService.log( level, this.loggingFor + message );
            }
        }
    }

    /**
     * This logs to the output stream.
     *
     * @param level   The loglevel.
     * @param message The log message.
     * @param cause   An optional Throwable that is the cause of the log.
     */
    @SuppressWarnings( "WeakerAccess" )
    protected void logToOutStream( int level, String message, Throwable cause ) {

        if ( this.outStream != null ) {

            StringBuilder log = new StringBuilder();
            switch ( level ) {

                case LogService.LOG_DEBUG:
                    log.append( "DEBUG: " );
                    break;

                case LogService.LOG_ERROR:
                    log.append( "ERROR: " );
                    break;

                case LogService.LOG_INFO:
                    log.append( "INFO: " );
                    break;

                case LogService.LOG_WARNING:
                    log.append( "WARNING: " );
            }

            log.append( YYMMDD_HHMMSS.format( new Date() ) );
            log.append( " " );

            if ( svcRef != null ) {

                Object bundleNameObj = svcRef.getProperty( Constants.BUNDLE_NAME );
                if ( bundleNameObj != null ) {

                    log.append( "[" ).append( bundleNameObj ).append( "] " );
                }
            }

            log.append( "[" );
            log.append( Thread.currentThread().getName() );
            log.append( "] " );

            log.append( this.loggingFor );
            log.append( " " );

            log.append( message );
            if ( cause != null ) {

                log.append( '\n' );
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter( sw );
                cause.printStackTrace( pw );
                log.append( sw.toString() );
                pw.close();
            }

            this.outStream.println( log.toString() );
        }
    }

    /**
     * Does a debug log.
     *
     * @param message The log message.
     */
    public void debug( String message ) {
        log( LogService.LOG_DEBUG, message, null );
    }

    /**
     * Does a debug log.
     *
     * @param message The log message.
     * @param cause   The cause of the log entry.
     */
    public void debug( String message, Throwable cause ) {
        log( LogService.LOG_DEBUG, message, cause );
    }

    /**
     * Does an error log.
     *
     * @param message The log message.
     */
    public void error( String message ) {
        log( LogService.LOG_ERROR, message, null );
    }

    /**
     * Does an error log.
     *
     * @param message The log message.
     * @param cause   The cause of the log entry.
     */
    public void error( String message, Throwable cause ) {
        log( LogService.LOG_ERROR, message, cause );
    }

    /**
     * Does an info log.
     *
     * @param message The log message.
     */
    public void info( String message ) {
        log( LogService.LOG_INFO, message, null );
    }

    /**
     * Does an info log.
     *
     * @param message The log message.
     * @param cause   The cause of the log entry.
     */
    public void info( String message, Throwable cause ) {
        log( LogService.LOG_INFO, message, cause );
    }

    /**
     * Does a warning log.
     *
     * @param message The log message.
     */
    public void warn( String message ) {
        log( LogService.LOG_WARNING, message, null );
    }

    /**
     * Does a warning log.
     *
     * @param message The log message.
     * @param cause   The cause of the log entry.
     */
    public void warn( String message, Throwable cause ) {
        log( LogService.LOG_WARNING, message, cause );
    }

}
