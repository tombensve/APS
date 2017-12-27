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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2011-08-30: Created!
 *
 */
package se.natusoft.osgi.aps.tools;

import org.osgi.framework.*;
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException;
import se.natusoft.osgi.aps.tools.tracker.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *  Provides an alternative service tracker.
 *
 *  This makes services available in three ways.
 *
 *  1. With the _OnServiceAvailable_, _OnServiceLeaving_, _OnActiveServiceAvailable_, _OnActiveServiceLeaving_,
 *  and _WithService_ interfaces. These are all callbacks and implementations must be passed to the
 *  appropriate method to register the callback.
 *
 *  2. With _allocateService()_ and _releaseService()_, which always have to match like open and close.
 *  **Do not** call allocateService() more than once without having called releaseService() in between!
 *
 *  3. with getWrappedService() which will provide a proxied implementation of the service interface that
 *  will use the tracker to get a service instance and forward the call to this service. It handles
 *  services coming and going and waits for the specified timeout if no service is available before
 *  throwing an APSNoServiceAvailableException (which is a runtime exception!). This is the easiest
 *  usage of the tracker and you don't need to handle allocateService() and releaseService() since
 *  that is done automatically for you.
 *
 *  Independent on how many services are tracked there is only one considered active and
 *  it is the active service that gets used in all cases where one/any service provider is needed.
 *  The first tracked service is set to the active service. If the active service goes away and there
 *  are other tracked services the first in the list becomes the new active service. If there
 *  are no other services in the list of tracked services then there will be no active
 *  service either.
 *
 *  All ways of getting a service throws APSNoServiceAvailableException if the service is not available.
 *  Thereby you will never get a null service!
 *
 *  When the tracker is created a timeout can optionally be provided. When this is provided this amount
 *  of time will be waited for a service to become available before APSNoServiceAvailableException is
 *  thrown.
 *
 *  **Please note** that when a timeout is provided and the tracker is used to get a service instance
 *  in the activator the startup for that bundle will hang waiting for the service to become available.
 *  Bundle starts taking too much time is in general a bad thing.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"}) // This is a library class with functionality that might not have been used
                            // by any clients yet. That does not mean that those methods should not be
                            // there.
public class APSServiceTracker<Service>  implements ServiceListener {
    //
    // Constants
    //

    /** Can be used for String timeout value. */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static final String SHORT_TIMEOUT = "3 seconds";

    /** Can be used for String timeout value. */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static final String MEDIUM_TIMEOUT = "30 seconds";

    /** Can be used for String timeout value. */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static final String LARGE_TIMEOUT = "2 minutes";

    /** Can be used for String timeout value. */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static final String VERY_LARGE_TIMEOUT = "5 minutes";

    /** Can be used for String timeout value. */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static final String HUGE_LARGE_TIMEOUT = "10 minutes";

    /** Can be used for String timeout value. */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static final String NO_TIMEOUT = "forever";

    @SuppressWarnings({"unused", "WeakerAccess"})
    public static final String DEFAULT_TIMEOUT = MEDIUM_TIMEOUT;

    //
    // Private Members
    //

    /** The timeout when waiting for service to become available. Default value is fail immediately if service is not available! */
    private int timeout = -1;

    /** The service class to track. */
    private Class<Service> serviceClass = null;

    /** A copy of the bundle context. */
    private BundleContext context = null;

    /** Our tracked services. */
    private TrackedServices trackedServices = new TrackedServices();

    /** Our currently active service */
    private ActiveService active = new ActiveService();

    /** An optional logger. */
    private APSLogger logger = null;

    /** An optional debug logger. */
    private APSLogger debugLogger = null;

    /** If true the active service instance is fetched and cached until it goes away. */
    private boolean cacheActiveService = false;

    /** Any additional search criteria supplied by the tracker user. */
    private String additionalSearchCriteria = null;

    /** If this is provided it will be called before APSNoServiceAvailableException is thrown. */
    private OnTimeout onTimeout = null;

    /** Handle start() being called more than once. */
    private boolean started = false;

//    /** A pool of executors for handling event driven callbacks. */
//    private ExecutorService executorService = Executors.newWorkStealingPool();

    //
    // Constructors
    //


    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * __Note:__ This instance will fail immediately if tracked service is not available!
     *
     * @param context The bundles context.
     * @param serviceClass The class of the service to track.
     */
    public APSServiceTracker(BundleContext context, Class<Service> serviceClass) {
        this.context = context;
        this.serviceClass = serviceClass;
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * __Note:__ This instance will fail immediately if tracked service is not available!
     *
     * @param context The bundles context.
     * @param serviceClass The class of the service to track.
     * @param cacheActive If true then the active service instance will be fetched and kept until the service goes away instead
     *                    of fetching it for every call. This is not a recommended thing to do since I'm not sure of the side effects
     *                    of sitting on service instances for as long as the client is running or the service goes away. There is a
     *                    reason for the ServiceReference! But if you insist in ignoring my warning you can set this to true. It does
     *                    have the side effect of a missed releaseService() not being as bad as when this is false.
     */
    @Deprecated
    public APSServiceTracker(BundleContext context, Class<Service> serviceClass, boolean cacheActive) {
        this(context, serviceClass);
        this.cacheActiveService = cacheActive;
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param serviceClass The class of the service to track.
     * @param timeout The time in seconds to wait for a service to become available. Setting timeout to 0 (or lower) will have
     *                the effect of not waiting at all, and will fail immediately if tracked service is not available. But you
     *                should consider using a constructor that does not supply a timeout instead in that case.
     */
    public APSServiceTracker(BundleContext context, Class<Service> serviceClass, int timeout) {
        this(context, serviceClass);
        this.timeout = (int)TimeUnit.SECONDS.toMillis(timeout);
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param serviceClass The class of the service to track.
     * @param timeout The time to wait for a service to become available. Setting timeout to 0 (or lower) will have
     *                the effect of not waiting at all, and will fail immediately if tracked service is not available.
     *                But you should consider using a constructor that does not supply a timeout instead in that case.
     * @param timeUnit The unit of time in timeout.
     */
    public APSServiceTracker(BundleContext context, Class<Service> serviceClass, long timeout, TimeUnit timeUnit) {
        this(context, serviceClass, (int)timeUnit.toSeconds(timeout));
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param serviceClass The class of the service to track.
     * @param timeout The time in seconds to wait for a service to become available.
     * @param cacheActive If true then the active service instance will be fetched and kept until the service goes away instead
     *                    of fetching it for every call. This is not a recommended thing to do since I'm not sure of the side effects
     *                    of sitting on service instances for as long as the client is running or the service goes away. There is a
     *                    reason for the ServiceReference! But if you insist in ignoring my warning you can set this to true. It does
     *                    have the side effect of a missed releaseService() not being as bad as when this is false.
     */
    @Deprecated
    public APSServiceTracker(BundleContext context, Class<Service> serviceClass, int timeout, boolean cacheActive) {
        this(context, serviceClass, timeout);
        this.cacheActiveService = cacheActive;
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param serviceClass The class of the service to track.
     * @param timeout The time to wait for a service to become available. Formats: "5 min[utes]" / "300 sec[onds]" /
     *                "300000 mili[seconds]" / "forever". Setting timeout value to 0 (or lower) will have the effect
     *                of not waiting at all, and will fail immediately if tracked service is not available. But you
     *                should consider using a constructor that does not supply a timeout instead in that case.
     */
    public APSServiceTracker(BundleContext context, Class<Service> serviceClass, String timeout) {
        this(context, serviceClass);
        setTimeout(timeout);
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param serviceClass The class of the service to track.
     * @param additionalSearchCriteria An LDAP search string not including the service! The final search string will
     *                                 be "(&(objectClass=service)additionalSearchCriteria)". This parameter should
     *                                 thereby always start with an '(' and end with an ')'!
     * @param timeout The time to wait for a service to become available. Formats: "5 min[utes]" / "300 sec[onds]" /
     *                "300000 mili[seconds]" / "forever". Setting timeout value to 0 (or lower) will have the effect
     *                of not waiting at all, and will fail immediately if tracked service is not available. But you
     *                should consider using a constructor that does not supply a timeout instead in that case.
     */
    public APSServiceTracker(BundleContext context, Class<Service> serviceClass, String additionalSearchCriteria, String timeout) {
        this(context, serviceClass);
        this.additionalSearchCriteria = additionalSearchCriteria;
        setTimeout(timeout);
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param serviceClass The class of the service to track.
     * @param additionalSearchCriteria An LDAP search string not including the service! The final search string will
     *                                 be "(&(objectClass=service)additionalSearchCriteria)". This parameter should
     *                                 thereby always start with an '(' and end with an ')'!
     * @param timeout The time to wait for a service to become available. Setting timeout to 0 (or lower) will have
     *                the effect of not waiting at all, and will fail immediately if tracked service is not available.
     *                But you should consider using a constructor that does not supply a timeout instead in that case.
     * @param timeUnit The unit of time in timeout.
     */
    public APSServiceTracker(BundleContext context, Class<Service> serviceClass, String additionalSearchCriteria, long timeout,
                             TimeUnit timeUnit) {
        this(context, serviceClass);
        this.additionalSearchCriteria = additionalSearchCriteria;
        this.timeout = (int)timeUnit.toMillis(timeout);
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param serviceClass The class of the service to track.
     * @param timeout The time to wait for a service to become available. Formats: "5 min[utes]" / "300 sec[onds]" / "300000 mili[seconds]" / "forever".
     * @param cacheActive If true then the active service instance will be fetched and kept until the service goes away instead
     *                    of fetching it for every call. This is not a recommended thing to do since I'm not sure of the side effects
     *                    of sitting on service instances for as long as the client is running or the service goes away. There is a
     *                    reason for the ServiceReference! But if you insist in ignoring my warning you can set this to true. It does
     *                    have the side effect of a missed releaseService() not being as bad as when this is false.
     */
    @Deprecated
    public APSServiceTracker(BundleContext context, Class<Service> serviceClass, String timeout, boolean cacheActive) {
        this(context, serviceClass, timeout);
        this.cacheActiveService = cacheActive;
    }

    //
    // Methods
    //

    /**
     * Sets the timeout as an int.
     *
     * @param timeout The timeout to set.
     */
    public APSServiceTracker setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the timeout from a string spec.
     *
     * @param timeout The time to wait for a service to become available. Formats: "5 min[utes]" / "300 sec[onds]" / "300000 mili[seconds]" / "forever".
     */
    public final APSServiceTracker setTimeout(String timeout) {
        String[] toParts = timeout.split(" ");
        if (toParts[0].toLowerCase().equals("forever")) {
            this.timeout = 0;
        }
        else {
            this.timeout = Integer.valueOf(toParts[0]);
        }
        if (toParts.length == 2) {
            String timeSpec = toParts[1].toLowerCase();
            if (timeSpec.startsWith("min")) {
                this.timeout = this.timeout * 1000 * 60;
            }
            else if (timeSpec.startsWith("sec")) {
                this.timeout = this.timeout * 1000;
            }
            // we treat anything else as "mili" and leave the timeout value as is.
        }
        else {
            this.timeout = this.timeout * 1000;
        }

        return this;
    }

    /**
     * Sets an on timeout handler.
     *
     * @param onTimeout The timeout handler to set.
     */
    public APSServiceTracker setOnTimeout(OnTimeout onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }

    /**
     * Starts tracking services.
     */
    public synchronized APSServiceTracker start() {
        if (!this.started) {
            // A note to yourself: The reason we don't specify versions here is that they are already specified
            // in bundle manifest import.
            String filter = "(" + Constants.OBJECTCLASS + "=" + this.serviceClass.getName() + ")";
            if (this.additionalSearchCriteria != null) {
                filter = "(&" + filter + this.additionalSearchCriteria + ")";
            }
            try {
                ServiceReference[] svcRefs = this.context.getServiceReferences(this.serviceClass.getName(), filter);
                if (svcRefs != null) {
                    for (ServiceReference sref : svcRefs) {
                        this.trackedServices.addService(sref);
                        if (!this.active.hasActiveService()) {
                            this.active.setActive(sref);
                        }
                    }
                }
            } catch (InvalidSyntaxException e) {
                throw new RuntimeException("Failed to start APSServiceTracker!", e);
            }
            try {
                this.context.addServiceListener(this, filter);
            } catch (InvalidSyntaxException e) {
                throw new RuntimeException("Failed to start APSServiceTracker!", e);
            }

            this.started = true;
        }

        return this;
    }

    /**
     * Stops tracking services and clears all tracked services.
     */
    public APSServiceTracker stop() {
        stop(null);
        return this;
    }

    /**
     * Stops tracking services and clears all tracked services.
     *
     * @param context The stop context.
     */
    public synchronized APSServiceTracker stop(BundleContext context) {
        if (context == null) context = this.context;
        context.removeServiceListener(this);
        this.trackedServices.clear();
        this.active.wakeAllWaiting();
        this.active.closeActiveService();
        this.started = false;
        return this;
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The `ServiceEvent object.
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        switch (event.getType()) {
            case ServiceEvent.REGISTERED:
                this.trackedServices.addService(event.getServiceReference());
                if (!this.active.hasActiveService()) {
                    this.active.setActive(event.getServiceReference());
                }
                break;
            case ServiceEvent.MODIFIED:
                if (this.trackedServices.hasService(event.getServiceReference())) {
                    this.trackedServices.removeService(event.getServiceReference());
                    // If it is the active service that is modified we want to force it to be updated.
                    if (!this.trackedServices.hasServices() || (this.active.hasActiveService() && this.active.getActive().equals(event.getServiceReference()))) {
                        this.active.setActive(null); // Will trigger "leaving" event!
                    }
                }
                this.trackedServices.addService(event.getServiceReference());
                if (!this.active.hasActiveService()) {
                    this.active.setActive(event.getServiceReference()); // Will trigger "available" event.
                }
                break;
            case ServiceEvent.MODIFIED_ENDMATCH:
            case ServiceEvent.UNREGISTERING:
                this.trackedServices.removeService(event.getServiceReference());
                if (this.active.hasActiveService() && event.getServiceReference().equals(this.active.getActive())) {
                    if (!this.trackedServices.hasServices()) {
                        this.active.setActive(null);
                    }
                    else {
                        this.active.setActive(this.trackedServices.getFirstService());
                    }
                }
        }
    }

    /**
     * @return The service class of the tracked service.
     */
    public Class<Service> getServiceClass() {
        return this.serviceClass;
    }

    /**
     * @return The timeout for waiting for a service, 0 for waiting forever, -1 for not waiting at all.
     */
    @SuppressWarnings("unused")
    public int getServiceAvailabilityTimeout() {
        return this.timeout;
    }

    /**
     * @return true if a service available timeout have been set on this tracker.
     */
    private boolean hasServiceAvailabilityTimeout() {
        return this.timeout >= 0;
    }

    /**
     * @return true if there is at least one tracked service.
     */
    public boolean hasTrackedService() {
        return this.active.hasActiveService();
    }

    /**
     * @return The bundle context passed to this class on construction.
     */
    public BundleContext getContext() {
        return this.context;
    }

    /**
     * @return The number of tracked services.
     */
    @SuppressWarnings("unused")
    public synchronized int getTrackedServiceCount() {
        return this.trackedServices.size();
    }

    /**
     * Provides this tracker with a logger.
     *
     * @param logger The logger to provide.
     */
    public void setLogger(APSLogger logger) {
        this.logger = logger;
    }

    /**
     * @return The current logger or null if none.
     */
    public APSLogger getLogger() {
        return this.logger;
    }

    /**
     * Sets an optional debug logger.
     *
     * @param debugLogger The debug logger to set.
     */
    @SuppressWarnings("unused")
    public APSServiceTracker setDebugLogger(APSLogger debugLogger) {
        this.debugLogger = debugLogger;
        return this;
    }

    /**
     * Sets the callback to call when a service becomes available. Please note that this callback gets
     * called for all instances of the tracked service, not just the active! If you only want the active
     * one use _onActiveServiceAvailable()_!
     *
     * @param onServiceAvailable The callback to set.
     */
    public APSServiceTracker onServiceAvailable(OnServiceAvailable onServiceAvailable) {
        this.trackedServices.setOnServiceAvailable(onServiceAvailable);
        return this;
    }

    /**
     * Property API for Groovy use.
     *
     * @param onServiceAvailable The on service available callback.
     */
    public APSServiceTracker setOnServiceAvailable(OnServiceAvailable onServiceAvailable) {
        this.trackedServices.setOnServiceAvailable(onServiceAvailable);
        return this;
    }

    /**
     * Sets the callback to call when a service is leaving. Please note that this callback gets
     * called for all instances of the tracked service, not just the active! If you only want the active
     * one use _OnActiveServiceLeaving()_!
     *
     * @param onServiceLeaving The callback to set.
     */
    public APSServiceTracker onServiceLeaving(OnServiceLeaving onServiceLeaving) {
        this.trackedServices.setOnServiceLeaving(onServiceLeaving);
        return this;
    }

    /**
     * Property API for Groovy use.
     *
     * @param onServiceLeaving The on service leaving callback.
     */
    public APSServiceTracker setOnServiceLeaving(OnServiceLeaving onServiceLeaving) {
        this.trackedServices.setOnServiceLeaving(onServiceLeaving);
        return this;
    }

    /**
     * Sets the callback to call when there is a new active service available. This is
     * the most failsafe way of getting hold of a service.
     *
     * Please note that this will also be called when the active service changes, that
     * is the previous active service has gone away, and a new has replaced it.
     *
     * Also note that this callback can be called at any time possibly by another
     * thread! It differs considerably from the withService*() methods who all execute
     * immediately and can thereby also throw exceptions. If this callback throws an
     * exception it will be logged as an error, nothing more!
     *
     * @param onActiveServiceAvailable The callback to set.
     *
     * @see #onActiveServiceLeaving(se.natusoft.osgi.aps.tools.tracker.OnServiceLeaving)
     */
    public APSServiceTracker onActiveServiceAvailable(OnServiceAvailable onActiveServiceAvailable) {
        this.active.setOnActiveServiceAvailable(onActiveServiceAvailable);
        return this;
    }

    /**
     * Property API for Groovy use.
     *
     * @param onActiveServiceAvailable The on active service available callback.
     */
    public APSServiceTracker setOnActiveServiceAvailable(OnServiceAvailable onActiveServiceAvailable) {
        this.active.setOnActiveServiceAvailable(onActiveServiceAvailable);
        return this;
    }

    /**
     * Sets the callback to call when the active service is leaving.
     *
     * @param onActiveServiceLeaving The callback to call when active service is leaving.
     *
     * @see #onActiveServiceAvailable(se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable)
     */
    public APSServiceTracker onActiveServiceLeaving(OnServiceLeaving onActiveServiceLeaving) {
        this.active.setOnActiveServiceLeaving(onActiveServiceLeaving);
        return this;
    }

    /**
     * Property API for Groovy use.
     *
     * @param onActiveServiceLeaving The on active service leaving callback.
     */
    public APSServiceTracker setOnActiveServiceLeaving(OnServiceLeaving onActiveServiceLeaving) {
        this.active.setOnActiveServiceLeaving(onActiveServiceLeaving);
        return this;
    }

    /**
     * Runs the specified callback providing it with a service to use.
     *
     * This will wait for a service to become available if a timeout has been provided for
     * the tracker.
     *
     * Don't use this in an activator start() method! onActiveServiceAvailable() and onActiveServiceLeaving()
     * are safe in a start() method, this is not!
     *
     * @param withService The callback to run and provide service to.
     * @param args Optional arguments to pass to the callback.
     *
     * @throws se.natusoft.osgi.aps.tools.tracker.WithServiceException Wraps any exception thrown by the callback.
     * @throws se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException thrown if there are no services available.
     */
    public APSServiceTracker withService(WithService<Service> withService, Object... args) throws WithServiceException, APSNoServiceAvailableException {

        if (!this.active.hasActiveService()) {
            if (hasServiceAvailabilityTimeout()) {
                waitForService(this.timeout);
            }
            if (!hasTrackedService()) {
                throw new APSNoServiceAvailableException("Service '" + this.serviceClass.getName() + "' is not available!");
            }
        }

        Service service = this.active.allocateActiveService();
        try {
            //noinspection unchecked
            withService.withService(service, args);
        }
        catch (Exception e) {
            throw new WithServiceException(e.getMessage() , e);
        }
        finally {
            this.active.releaseActiveService();
        }

        return this;
    }

    /**
     * Runs the specified callback providing it with a service to use if and only if the service is available.
     * If the service is not available nothing happens and you will not be notified of the failure! This is
     * for "if it works, it works and if it doesn't it doesn't" cases. This is basically a convenience for
     * _withService()_ and ignoring _APSNoServiceAvailableException_.
     *
     * Don't use this in an activator _start()_ method! _onActiveServiceAvailable()_ and _onActiveServiceLeaving()_
     * are safe in a _start()_ method, this is not!
     *
     * @param withService The callback to run and provide service to.
     * @param args Optional arguments to pass to the callback.
     *
     * @throws se.natusoft.osgi.aps.tools.tracker.WithServiceException Wraps any exception thrown by the callback.
     */
    @SuppressWarnings({"unchecked", "unused"})
    public APSServiceTracker withServiceIfAvailable(WithService<Service> withService, Object... args) throws WithServiceException {
        Service service = this.active.allocateActiveService();
        if (service != null) {
            try {
                withService.withService(service, args);
            }
            catch (Exception e) {
                throw new WithServiceException(e.getMessage() , e);
            }
            finally {
                this.active.releaseActiveService();
            }
        }

        return this;
    }

    /**
     * Runs the specified callback for all **currently** available services.
     *
     * Don't use this in an activator _start()_ method! _onActiveServiceAvailable()_ and _onActiveServiceLeaving()_
     * are safe in a _start()_ method, this is not!
     *
     * @param withService The callback to run and provide service to.
     * @param args Optional arguments to pass to the callback.
     *
     * @throws se.natusoft.osgi.aps.tools.tracker.WithServiceException Wraps any exception thrown by the callback.
     */
    @SuppressWarnings("unchecked")
    public APSServiceTracker withAllAvailableServices(WithService<Service> withService, Object... args) throws WithServiceException {
        for (ServiceReference svc : this.trackedServices.getServices()) {
            Service service = (Service)this.context.getService(svc);
            try {
                withService.withService(service, args);
            }
            catch (Exception e) {
                throw new WithServiceException("withService() threw exception. [" + e.getMessage() + "]", e);
            }
            finally {
                this.context.ungetService(svc);
            }
        }

        return this;
    }

    /**
     * Runs the specified callback for all **currently** available services.
     *
     * Don't use this in an activator _start()_ method! _onActiveServiceAvailable()_ and _onActiveServiceLeaving()_
     * are safe in a _start()_ method, this is not!
     *
     * @param withService The callback to run and provide service to.
     * @param args Optional arguments to pass to the callback.
     *
     * @throws se.natusoft.osgi.aps.tools.tracker.WithServiceException Wraps any exception thrown by the callback.
     */
    @SuppressWarnings({"unchecked", "unused"})
    public APSServiceTracker withAllAvailableServicesIncRef(WithServiceIncRef<Service> withService, Object... args) throws WithServiceException {
        for (ServiceReference svc : this.trackedServices.getServices()) {
            Service service = (Service)this.context.getService(svc);
            try {
                withService.withService(service, svc, args);
            }
            catch (Exception e) {
                throw new WithServiceException("withService() threw exception. Get original exception with getCause()!", e);
            }
            finally {
                this.context.ungetService(svc);
            }
        }

        return this;
    }

    /**
     * Waits for an active service to become available.
     *
     * @param timeout The timeout in milliseconds. 0 == forever.
     */
    public APSServiceTracker waitForService(long timeout) {
        if (this.active.hasActiveService()) {
            return this;
        }

        this.active.waitForActiveService(timeout);
        return this;
    }

    /**
     * Returns the active service possibly waiting for one to become available if a timeout has been specified when
     * tracker were created.
     *
     * Please always call _releaseTrackedService()_ when you are done with the service!
     *
     * @return The active service.
     *
     * @throws se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException if no service is available.
     */
    public Service allocateService() throws APSNoServiceAvailableException {
        if (!this.active.hasActiveService()) {
            waitForService(this.timeout);
            if (!this.active.hasActiveService()) {
                if (this.onTimeout != null) {
                    onTimeout.onTimeout();
                }
                throw new APSNoServiceAvailableException("Service '" + this.serviceClass.getName() + "' is not available!");
            }
        }
        return this.active.allocateActiveService();
    }

    /**
     * Releases the previously allocated service.
     */
    public APSServiceTracker releaseService() {
        this.active.releaseActiveService();
        return this;
    }

    /**
     * Returns a service implementation wrapping this tracker and using it to get the service to forward calls to.
     */
    public Service getWrappedService() {
        return APSTrackerWrapper.wrap(this);
    }

    //
    // Inner Classes
    //

    /**
     * Manages the tracked services.
     */
    private class TrackedServices {
        //
        // Private Members
        //

        /** Our tracked services. */
        private List<ServiceReference> serviceRefs = new ArrayList<>();

        /** When set it gets called when a new service becomes available. */
        private OnServiceAvailable onServiceAvailable = null;

        /** When set it gets called when a service becomes unavailable. */
        private OnServiceLeaving onServiceLeaving = null;

        //
        // Constructors
        //

        /**
         * Creates a new TrackedServices.
         */
        TrackedServices() {}

        //
        // Methods
        //

        /**
         * Sets the on new service available callback.
         *
         * @param onServiceAvailable The callback to set.
         */
        public synchronized void setOnServiceAvailable(OnServiceAvailable onServiceAvailable) {
            this.onServiceAvailable = onServiceAvailable;
            for (ServiceReference serviceRef : this.serviceRefs) {
                // Please note that the active service is set after this call, so if this happens to be the
                // same as the active service we cannot reuse its service instance.
                OnServiceRunner osrt = new OnServiceRunner(serviceRef, this.onServiceAvailable);
                //APSServiceTracker.this.executorService.submit(osrt);
                osrt.run();
            }
        }

        /**
         * Sets the on old service leaving callback.
         *
         * @param onServiceLeaving The callback to set.
         */
        void setOnServiceLeaving(OnServiceLeaving onServiceLeaving) {
            this.onServiceLeaving = onServiceLeaving;
        }

        /**
         * Adds a service to the tracked services.
         *
         * @param reference The service to add.
         */
        synchronized void addService(ServiceReference reference) {
            if (!this.serviceRefs.contains(reference)) {
                this.serviceRefs.add(reference);
                if (this.onServiceAvailable != null) {
                    // Please note that the active service is set after this call, so if this happens to be the
                    // same as the active service we cannot reuse its service instance.
                    OnServiceRunner osrt = new OnServiceRunner(reference, this.onServiceAvailable);
                    //APSServiceTracker.this.executorService.submit(osrt);
                    osrt.run();
                }
                if (APSServiceTracker.this.debugLogger != null) {
                    APSServiceTracker.this.debugLogger.debug("Added service: " + reference.toString());
                }
            }
        }

        /**
         * @return The first tracked service in the list.
         */
        synchronized ServiceReference getFirstService() {
            return this.serviceRefs.get(0);
        }

        /**
         * @return true if there are tracked services available.
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        synchronized boolean hasServices() {
            return !this.serviceRefs.isEmpty();
        }

        /**
         * @return A copy of the tracked services.
         */
        public synchronized List<ServiceReference> getServices() {
            return new ArrayList<>(this.serviceRefs);
        }

        /**
         * Returns true if the specified service reference is tracked.
         *
         * @param ref The service refrerence to check.
         * @return true or false.
         */
        synchronized boolean hasService(ServiceReference ref) {
            boolean result = false;

            for (ServiceReference sref : this.serviceRefs) {
                if (sref.compareTo(ref) == 0) {
                    result = true;
                    break;
                }
            }

            return result;
        }

        /**
         * Removes the specified service reference from the list of tracked services.
         *
         * @param ref The service reference to remove.
         */
        synchronized void removeService(ServiceReference ref) {
            ServiceReference found = null;
            for (ServiceReference sref : this.serviceRefs) {
                if (sref.compareTo(ref) == 0) {
                    found = sref;
                    break;
                }
            }
            if (found != null) {
                this.serviceRefs.remove(found);
                if (this.onServiceLeaving != null) {
                    OnServiceRunner osrt = new OnServiceRunner(found, this.onServiceLeaving);
//                    APSServiceTracker.this.executorService.submit(osrt);
                    osrt.run();
                }
                if (APSServiceTracker.this.debugLogger != null) {
                    APSServiceTracker.this.debugLogger.debug("Removed service: " + found.toString());
                }
            }
        }

        /**
         * Clears all tracked services.
         */
        public synchronized void clear() {
            List<ServiceReference> toRemove = new ArrayList<>(this.serviceRefs);
            for (ServiceReference sref : toRemove) {
                removeService(sref);
            }
        }

        /**
         * @return The number of tracked services.
         */
        public synchronized int size() {
            return this.serviceRefs.size();
        }
    }

    /**
     * Manages the active service.
     */
    private class ActiveService {
        //
        // Private Members
        //

        /** The active service reference. */
        private ServiceReference active = null;

        /** The active service. */
        private Service activeService = null;

        /** Lock for synchronizing active. */
        private final Object activeLock = new Object();

        /**
         * This gets called when the first service becomes available and when the activly used
         * service by withSerive*() calls changes.
         */
        private OnServiceAvailable onActiveServiceAvailable = null;

        /** This gets called when the activly used service by withService*() calls leaves. */
        private OnServiceLeaving onActiveServiceLeaving = null;

        //
        // Constructors
        //

        /**
         * Creates a new ActiveService.
         */
        ActiveService() {}

        //
        // Methods
        //

        /**
         * Sets on active service availability callback.
         *
         * @param onActiveServiceAvailable The callback to set.
         */
        void setOnActiveServiceAvailable(OnServiceAvailable onActiveServiceAvailable) {
            this.onActiveServiceAvailable = onActiveServiceAvailable;
            if (this.active != null) {
                OnServiceRunner osrt = (APSServiceTracker.this.cacheActiveService && this.activeService != null) ?
                        new OnServiceRunner(this.activeService, this.onActiveServiceAvailable) :
                        new OnServiceRunner(this.active, this.onActiveServiceAvailable);
//                APSServiceTracker.this.executorService.submit(osrt);
                osrt.run();
            }
        }

        /**
         * Sets on active service availability leaving.
         *
         * @param onActiveServiceLeaving callback to set.
         */
        void setOnActiveServiceLeaving(OnServiceLeaving onActiveServiceLeaving) {
            this.onActiveServiceLeaving = onActiveServiceLeaving;
        }

        /**
         * Sets the active service reference.
         *
         * @param active The service reference to set as active.
         */
        @SuppressWarnings("unchecked")
        public void setActive(ServiceReference active) {
            ServiceReference oldActive = this.active;

            synchronized (this.activeLock) {
                this.active = active;
                if (this.active != null && APSServiceTracker.this.cacheActiveService) {
                    this.activeService = (Service)APSServiceTracker.this.context.getService(this.active);
                    if (this.activeService == null) {
                        throw new RuntimeException("Failed to get service from service reference: " + this.active);
                    }
                }
                else {
                    this.activeService = null;
                }
            }

            if (oldActive != null) {
                if (APSServiceTracker.this.cacheActiveService) {
                    try {
                        APSServiceTracker.this.context.ungetService(oldActive); // It might be too late for this, but what the heck.
                    }
                    catch (IllegalArgumentException | IllegalStateException ignore) {}
                }
                if (this.onActiveServiceLeaving != null) {
                    OnServiceRunner osrt = new OnServiceRunner(oldActive, this.onActiveServiceLeaving);
//                    APSServiceTracker.this.executorService.submit(osrt);
                    osrt.run();
                }
                if (APSServiceTracker.this.debugLogger != null) {
                    APSServiceTracker.this.debugLogger.debug("Removed active!");
                }
            }

            if (this.active != null) {
                wakeAllWaiting();

                if (this.onActiveServiceAvailable != null) {
                    OnServiceRunner osrt = (APSServiceTracker.this.cacheActiveService && this.activeService != null) ?
                            new OnServiceRunner(this.activeService, this.onActiveServiceAvailable) :
                            new OnServiceRunner(this.active, this.onActiveServiceAvailable);
//                    APSServiceTracker.this.executorService.submit(osrt);
                    osrt.run();
                }
                if (APSServiceTracker.this.debugLogger != null) {
                    APSServiceTracker.this.debugLogger.debug("Set active: " + this.active.toString());
                }
            }
        }

        /**
         * @return The active service reference.
         */
        public ServiceReference getActive() {
            synchronized (this.activeLock) {
                return this.active;
            }
        }

        /**
         * Allocates the active service and returns an instance of it. _releaseActiveService()_ should be called when done with it.
         *
         * @return The allocated active service instance.
         */
        @SuppressWarnings("unchecked")
        Service allocateActiveService() {
            return APSServiceTracker.this.cacheActiveService ? this.activeService : (Service)APSServiceTracker.this.context.getService(this.active);
        }

        /**
         * Releases the active service.
         */
        void releaseActiveService() {
            if (!APSServiceTracker.this.cacheActiveService && this.active != null) {
                APSServiceTracker.this.context.ungetService(this.active);
            }
        }

        /**
         * @return true if there is an active service reference set.
         */
        boolean hasActiveService() {
            synchronized (this.activeLock) {
                return this.active != null;
            }
        }

        /**
         * Waits for an active service reference to become available.
         *
         * @param timeout Wait at most this long (in milliseconds). 0 == wait forever.
         */
        synchronized void waitForActiveService(long timeout) {
            if (hasServiceAvailabilityTimeout()) {
                try {
                    this.wait(timeout);
                } catch (InterruptedException e) {
                    // A new service becoming available will trigger a notify which will interrupt this wait.
                }
            }
        }

        /**
         * Wakes all waitForActiveService() calls.
         */
        synchronized void wakeAllWaiting() {
            try {
                this.notifyAll();
            }
            catch (IllegalMonitorStateException imse) {
                // This is not an error! If there are none waiting this will happen!
            }
        }

        /**
         * Closes the active service.
         */
        /*package*/ void closeActiveService() {
            synchronized (this.activeLock) {
                if (this.active != null) {
                    try {
                        APSServiceTracker.this.context.ungetService(this.active);
                    }
                    catch (IllegalStateException | IllegalArgumentException ignore) {}
                }
                this.active = null;
                this.activeService = null;
            }
        }
    }

    /**
     * Runs an _OnServiceAvailable_ or an _OnServiceLeaving_ in another thread.
     */
    private class OnServiceRunner implements Runnable {
        //
        // Private Members
        //

        /** Callback for when service becomes available. */
        private OnServiceAvailable onServiceAvailable = null;

        /** Callback for when service is leaving. */
        private OnServiceLeaving onServiceLeaving = null;

        /** The servcie reference to act on. */
        private ServiceReference reference;

        /** The service to act on. */
        private Service service;

        //
        // Constructors
        //

        /**
         * Creates a new OnServiceRunnerThread.
         *
         * @param service The service to act on.
         * @param onServiceAvailable The callback to run.
         */
        private OnServiceRunner(Service service, OnServiceAvailable onServiceAvailable) {
            if (service == null) {
                throw new IllegalArgumentException("service argument cannot be null!");
            }
            this.service = service;
            this.onServiceAvailable = onServiceAvailable;
        }

        /**
         * Creates a new OnServiceRunnerThread.
         *
         * @param reference The service reference to act on.
         * @param onServiceAvailable The callback to run.
         */
        private OnServiceRunner(ServiceReference reference, OnServiceAvailable onServiceAvailable) {
            if (reference == null) {
                throw new IllegalArgumentException("reference argument cannot be null!");
            }
            this.reference = reference;
            this.onServiceAvailable = onServiceAvailable;
        }

        /**
         * Creates a new OnServiceRunnerThread.
         *
         * @param reference The service reference to act on.
         * @param onServiceLeaving The callback to run.
         */
        private OnServiceRunner(ServiceReference reference, OnServiceLeaving onServiceLeaving) {
            if (reference == null) {
                throw new IllegalArgumentException("reference argument cannot be null!");
            }
            this.reference = reference;
            this.onServiceLeaving = onServiceLeaving;
        }

        //
        // Methods
        //

        /**
         * Runs this thread.
         */
        @Override
        public void run() {
            if (this.onServiceAvailable != null) {
                Service svc;
                if (this.service != null) {
                    svc = this.service;
                }
                else {
                    //noinspection unchecked
                    svc = (Service)APSServiceTracker.this.context.getService(this.reference);
                }

                try {
                    //noinspection unchecked
                    this.onServiceAvailable.onServiceAvailable(svc, this.reference);
                } catch (Exception e) {
                    if (APSServiceTracker.this.logger != null) {
                        APSServiceTracker.this.logger.error("Failed to run an OnServiceAvailable callback for active service!", e);
                    }
                    else {
                        System.out.println("___________________________________________________________________________________");
                        System.out.println("APSServiceTracker: Failed to run an OnServiceAvailable callback for active service!");
                        e.printStackTrace();
                        System.out.println("___________________________________________________________________________________");
                    }
                }
                finally {
                    if (this.reference != null) {
                        APSServiceTracker.this.context.ungetService(this.reference);
                    }
                }
            }

            if (this.onServiceLeaving != null) {
                try {
                    this.onServiceLeaving.onServiceLeaving(this.reference, APSServiceTracker.this.serviceClass);
                }
                catch (Exception e) {
                    if (APSServiceTracker.this.logger != null) {
                        APSServiceTracker.this.logger.error("Failed to run an OnServiceLeaving callback for active service!", e);
                    }
                    else {
                        System.out.println("___________________________________________________________________________________");
                        System.out.println("APSServiceTracker: Failed to run an OnServiceLeaving callback for active service!");
                        e.printStackTrace();
                        System.out.println("___________________________________________________________________________________");
                    }
                }
            }
        }
    }
}
