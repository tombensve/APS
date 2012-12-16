/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         1.0.0
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
 *         2011-10-22: Created!
 *         
 */
package se.natusoft.osgi.aps.tools;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.tools.tracker.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This will track a set of required services and call an available callback when all services are available,
 * and a nonAvailable callback when one or more is missing.
 */
public class APSRequiredServicesTracker implements OnServiceAvailable, OnServiceLeaving {
    //
    // Private Members
    //

    /** Trackers for each individual service. */
    private Map<Class, APSServiceTracker> trackers = new HashMap<Class, APSServiceTracker>();

    /** The number of required services tracked. When the size of the map equals this then the callback is called. */
    private int numberOfTrackedServices = 0;

    /** Holds the received service instances of the tracked services */
    private RequiredServices requiredServices = new RequiredServices();

    /** The callback for availability. */
    private OnRequiredServicesAvailability onAvailabilityCallback = null;

    /** The callback for non availability. */
    private OnRequiredServicesNonAvailability onNonAvailablilityCallback = null;

    /** Will change to true when all services are available. Used to only call onNonAvailabilityCallback once. */
    private boolean allServicesAvailable = false;

    /** A logger to log to. */
    private APSLogger logger = null;

    /** A logger for debug logging.  */
    private APSLogger debugLogger = null;

    /** The bundle context provided at construction. */
    private BundleContext context = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSRequiredServicesTracker taking a set of service api classes.
     * This variant will internally create a tracker for each service api class.
     *
     * @param context The bundle context.
     * @param timeout The same timeout string as past to APSServiceTracker. (Ex: "10 min" "5 sec")
     * @param services The class objects of the service API interfaces.
     */
    public APSRequiredServicesTracker(BundleContext context, String timeout, Class... services) {
        this.context = context;
        for (Class svc : services) {
            APSServiceTracker tracker = new APSServiceTracker(context, svc, timeout);
            tracker.onActiveServiceAvailable(this);
            tracker.onActiveServiceLeaving(this);
            this.trackers.put(svc, tracker);
            ++this.numberOfTrackedServices;
        }
    }

    /**
     * Creates a new APSRequiredServicesTracker taking a set of externally created trackers.
     * This vairant offers more flexibility in setting upp each tracker.
     * <p/>
     * <b>PLEASE NOTE:</b> Dont start the trackers passed to this constructor. They will be started
     * when this tracker is started and stopped when this trackker is stopped. Also dont attatch
     * any callbacks to the passed trackers. Use the callback on this tracker.
     *
     * @param context The bundle context.
     * @param trackers Externaly created trackers for each required service to track.
     */
    public APSRequiredServicesTracker(BundleContext context, APSServiceTracker... trackers) {
        this.context = context;
        for (APSServiceTracker tracker : trackers) {
            tracker.onActiveServiceAvailable(this);
            tracker.onActiveServiceLeaving(this);
            this.trackers.put(tracker.getServiceClass(), tracker);
            ++this.numberOfTrackedServices;
        }
    }

    //
    // Methods
    //

    /**
     * Starts the tracker.
     */
    public void start() {
        for (Class key : this.trackers.keySet()) {
            APSServiceTracker tracker = this.trackers.get(key);
            tracker.start();
        }
    }

    /**
     * Stops the tracker.
     *
     * @param context The stop context.
     */
    public void stop(BundleContext context) {
        for (Class key : this.trackers.keySet()) {
            APSServiceTracker tracker = this.trackers.get(key);
            tracker.stop(context);
        }
    }

    /**
     * Sets a logger to log to.
     *
     * @param logger The logger to set.
     */
    public void setLogger(APSLogger logger) {
        this.logger = logger;
    }

    /**
     * Sets a logger used for debug logging. The tracker will only try to log to it when it is available.
     *
     * @param debugLogger
     */
    public void setDebugLogger(APSLogger debugLogger) {
        this.debugLogger = debugLogger;
    }

    /**
     * This will wait for all services to become available. This is useful in an activators start() method so that you can break
     * and fail the start.
     *
     * @param timeout The timeout in milliseconds. 0 == forever (though probably a bad idea).
     *
     * @return true if the services did become available false on timeout.
     */
    public boolean waitForAllServicesToBecomeAvailable(int timeout) {
        int msecs = 0;
        boolean resultStatus = true;
        boolean done = false;
        while (!done) {
            if (this.requiredServices.getServiceCount() == this.numberOfTrackedServices) {
                done = true;
            }
            else {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    done = true;
                    if (this.requiredServices.getServiceCount() != this.numberOfTrackedServices) {
                        resultStatus = false;
                    }
                }
                if (timeout > 0) {
                    msecs += 3000;
                    if (msecs >= timeout) {
                        done = true;
                        resultStatus = false;
                    }
                }
            }
        }

        return resultStatus;
    }

    /**
     * Sets the on availability callback.
     * <p/>
     * <b>PLEASE NOTE</b> that the service returned by RequiredServices.getService(Class) can
     * only be used within the callback! Do not save this service instance not pass it to a
     * thread. After the callback has returned it will no longer be valid!
     *
     * @param onAvailabilityCallback The callback to set.
     */
    public void onAvailability(OnRequiredServicesAvailability onAvailabilityCallback) {
        this.onAvailabilityCallback = onAvailabilityCallback;
        debugLog("@@@@@ Have " + this.requiredServices.getServiceCount() + " out of " + this.numberOfTrackedServices + " services!");
        if (this.requiredServices.getServiceCount() == this.numberOfTrackedServices) {
            this.allServicesAvailable = true;
        }

        if (this.allServicesAvailable && this.onAvailabilityCallback != null) {
            try {
                this.requiredServices.loadServices(this.context);
                this.onAvailabilityCallback.onRequiredServicesAvailability(this.requiredServices);
                this.requiredServices.releaseServices(this.context);
            }
            catch (Exception e) {
                if (this.logger != null) {
                    this.logger.error("Failed to execute onRequiredServicesAvailability callback!", e);
                }
            }
        }
    }

    /**
     * Sets the on non availability callback.
     *
     * @param onNonAvailabilityCallback
     */
    public void onNonAvailability(OnRequiredServicesNonAvailability onNonAvailabilityCallback) {
        this.onNonAvailablilityCallback = onNonAvailabilityCallback;
    }

    /**
     * Does a debug log if a debug logger is available.
     *
     * @param message The debug log message.
     */
    private void debugLog(String message) {
        if (this.debugLogger != null) {
            this.debugLogger.debug(message);
        }
    }

    /**
     * Receives a new service.
     *
     * @param service The received service.
     *
     * @throws Exception Implementation can throw any exception. How it is handled depends on the APSServiceTracker method this
     *                   gets passed to.
     */
    @Override
    public synchronized void onServiceAvailable(Object service, ServiceReference serviceReference) throws Exception {
        for (Class key : this.trackers.keySet()) {
            if (key.isAssignableFrom(service.getClass())) {
                this.requiredServices.putService(key, serviceReference);
                debugLog("@@@@@ Received service: " + key.getName());
            }
        }
        debugLog("@@@@@ Have " + this.requiredServices.getServiceCount() + " out of " + this.numberOfTrackedServices + " services!");
        if (this.requiredServices.getServiceCount() == this.numberOfTrackedServices) {
            this.allServicesAvailable = true;
        }

        if (this.allServicesAvailable && this.onAvailabilityCallback != null) {
            this.requiredServices.loadServices(this.context);
            this.onAvailabilityCallback.onRequiredServicesAvailability(this.requiredServices);
            this.requiredServices.releaseServices(this.context);
        }
    }

    /**
     * A service is leaving.
     *
     * @param service The leaving service. Please note that this can only be used for information! Dont try to get a service with it!
     * @param serviceAPI The service API (interface) class for this service for more easy identification.
     *
     * @throws Exception Implementation can throw any exception. How it is handled depends on the APSServiceTracker method this
     *                   gets passed to.
     */
    @Override
    public synchronized void onServiceLeaving(ServiceReference service, Class serviceAPI) throws Exception {
        this.requiredServices.removeService(serviceAPI);

        debugLog("@@@@@ Service left: " + serviceAPI.getName());
        debugLog("@@@@@ Have " + this.requiredServices.getServiceCount() + " out of " + this.numberOfTrackedServices + " services!");
        if(this.allServicesAvailable && this.onNonAvailablilityCallback != null) {
            this.requiredServices.loadServices(this.context);
            this.onNonAvailablilityCallback.onRequiredServicesNonAvailability(this.requiredServices);
            this.requiredServices.releaseServices(this.context);
        }

        this.allServicesAvailable = false;
    }
}
