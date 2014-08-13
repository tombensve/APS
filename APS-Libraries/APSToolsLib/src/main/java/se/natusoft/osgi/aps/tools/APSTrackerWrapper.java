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
     * @param <Service>
     * @param tracker An APSServiceTracker instance tracking the service. Please note that the tracker must be started for this to work!
     *
     * @return A facade object implementing the service interface passed to the specified tracker.
     */
    public static <Service> Service wrap(APSServiceTracker<Service> tracker) {
        Class[] interfaces = new Class[1];
        interfaces[0] = tracker.getServiceClass();

        FacadeHandler handler = new FacadeHandler(tracker);
        
        return (Service)Proxy.newProxyInstance(tracker.getServiceClass().getClassLoader(), interfaces, handler);
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
        
        /** The service tracker tracking our service. */
        private APSServiceTracker tracker = null;

        //
        // Constructors
        //
        
        /**
         * Creates a new FacadeHandler instance.
         * 
         * @param tracker  The tracker of the service to facade.
         */
        public FacadeHandler(APSServiceTracker tracker) {
            this.tracker = tracker;
        }
        
        //
        // Methods
        //
        
        /**
         * Main handler method.
         * 
         * @param o
         * @param method
         * @param args
         * @return
         * @throws Throwable 
         */
        @Override
        public Object invoke(Object o, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(this.tracker.allocateService(), args);
            }
            catch (InvocationTargetException ite) {
                System.out.println(">>>>>>" + ite.getCause().getMessage());
                throw ite.getCause();
            }
            finally {
                this.tracker.releaseService();
            }
        }
    }
}
