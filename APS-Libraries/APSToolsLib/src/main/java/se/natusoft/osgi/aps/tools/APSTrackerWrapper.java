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
 *         2011-08-03: Created!
 *
 */
package se.natusoft.osgi.aps.tools;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This is a tool that takes a service interface, creates a tracker for it and
 * provides a proxied implementation looking up and calling real service.
 */
public class APSTrackerWrapper {

    //
    // Methods
    //

    /**
     * Wraps a tracker returning an object implementing the tracked service interface and will automatically get service from
     * the tracker and forward call to service.
     *
     * @param <Service> The service type.
     * @param tracker   An APSServiceTracker instance tracking the service. Please note that the tracker must be started for this to work!
     * @return A facade object implementing the service interface passed to the specified tracker.
     */
    public static <Service> Service wrap(APSServiceTracker<Service> tracker) {
        Class[] interfaces = new Class[1];
        interfaces[0] = tracker.getServiceClass();

        FacadeHandler handler = new FacadeHandler(tracker);

        //noinspection unchecked
        return (Service) Proxy.newProxyInstance(tracker.getServiceClass().getClassLoader(), interfaces, handler);
    }

    /**
     * Wraps a tracker returning an object implementing the tracked service interface and will automatically get service from
     * the tracker and forward call to service.
     *
     * @param <Service>                      The service type.
     * @param tracker                        An APSServiceTracker instance tracking the service. Please note that the tracker must be started for this to work!
     * @param cacheCallsOnNoServiceAvailable If true calls to service will be cached if service is not available and called when service
     *                                       becomes available. This will of course only work for methods that do not return a value!!
     *                                       This will however make the calls non blocking! APSServiceTracker.allocateService() which
     *                                       is default behavior will block until service is available!
     * @return A facade object implementing the service interface passed to the specified tracker.
     */
    public static <Service> Service wrap(APSServiceTracker<Service> tracker, boolean cacheCallsOnNoServiceAvailable) {
        Class[] interfaces = new Class[1];
        interfaces[0] = tracker.getServiceClass();

        FacadeHandler handler = new FacadeHandler(tracker, cacheCallsOnNoServiceAvailable);

        //noinspection unchecked
        return (Service) Proxy.newProxyInstance(tracker.getServiceClass().getClassLoader(), interfaces, handler);
    }

    //
    // Inner Classes
    //

    /**
     * Provides an implementation for each facade.
     */
    private static class FacadeHandler implements InvocationHandler {
        //
        // Private Members
        //

        /**
         * The service tracker tracking our service.
         */
        private APSServiceTracker tracker = null;

        private boolean cacheCalls = false;

        private Queue<Runnable> callCache = new LinkedList<>();

        private APSLogger logger = new APSLogger();

        //
        // Constructors
        //

        /**
         * Creates a new FacadeHandler instance.
         *
         * @param tracker The tracker of the service to facade.
         */
        @SuppressWarnings("WeakerAccess")
        public FacadeHandler(APSServiceTracker tracker) {
            this.tracker = tracker;
            this.tracker.onActiveServiceAvailable((service, serviceRef) -> {
                this.callCache.forEach(Runnable::run);
                this.callCache.clear();
            });
            this.logger.setLoggingFor("APSTrackerWrapper");
        }

        /**
         * Creates a new FacadeHandler instance.
         *
         * @param tracker The tracker of the service to facade.
         */
        @SuppressWarnings("WeakerAccess")
        public FacadeHandler(APSServiceTracker tracker, boolean cacheCallsOnNoServiceAvailable) {
            this(tracker);
            this.cacheCalls = cacheCallsOnNoServiceAvailable;
        }

        //
        // Methods
        //

        /**
         * Main handler method.
         *
         * @param proxy  The object method was invoked on.
         * @param method The method to invoke.
         * @param args   The method arguments.
         * @return Any eventual return value.
         * @throws Throwable on failure.
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if (this.tracker.hasTrackedService() || !cacheCalls) {

                    return method.invoke(this.tracker.allocateService(), args);

                } else {
                    this.callCache.add(() -> {

                        //noinspection TryWithIdenticalCatches
                        try {
                            method.invoke(this.tracker.allocateService(), args);

                        } catch (InvocationTargetException e) {

                            this.logger.error(e.getCause().getMessage(), e.getCause());

                        } catch (IllegalAccessException e) {

                            this.logger.error(e.getMessage(), e);

                        } finally {

                            this.tracker.releaseService();
                        }
                    });

                    // This must be null since only null can be cast to anything! Or rather a null value
                    // is legal for any type of object. Since the actual method.invoke(...) call will be
                    // done later, we cannot return the result of that.
                    return null;
                }
            } catch (InvocationTargetException ite) {

                this.logger.error(ite.getCause().getMessage(), ite.getCause());
                throw ite.getCause();

            } catch (IllegalAccessException iae) {

                this.logger.error(iae.getMessage(), iae);
                throw iae;

            } finally {

                this.tracker.releaseService();
            }
        }
    }
}
