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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-08-19: Created!
 *         
 */
package se.natusoft.osgi.aps.tools;

import se.natusoft.osgi.aps.tools.annotation.APSRunInBundlesContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps a service provider class and for each @APSRunInBundlesContext annotation on a service method
 * that method will be run with the bundles classloader as thread context classloader, and the previous
 * thread context classloader will be restored on exit.
 */
public class APSContextWrapper {

    /**
     * Creates a new proxied instance. Register this as your service!
     *
     * @param serviceProvider The service implementation to proxy.
     * @param serviceAPI The class of the implemented service API.
     * @param <ServiceProvider> Will act as Object but make code more readable.
     * @param <ServiceAPI> A a proxied implementation of this is returned.
     */
    public static <ServiceProvider, ServiceAPI> ServiceAPI wrap(ServiceProvider serviceProvider, Class<ServiceAPI> serviceAPI) {
        return wrap(serviceProvider, serviceAPI, null);
    }

    /**
     * Creates a new proxied instance. Register this as your service!
     *
     * @param serviceProvider The service implementation to proxy.
     * @param serviceAPI The class of the implemented service API.
     * @param logger The logger to user on errors.
     * @param <ServiceProvider> Will act as Object but make code more readable.
     * @param <ServiceAPI> A a proxied implementation of this is returned.
     */
    public static <ServiceProvider, ServiceAPI> ServiceAPI wrap(ServiceProvider serviceProvider, Class<ServiceAPI> serviceAPI, APSLogger logger) {
        Class[] interfaces = new Class[1];
        interfaces[0] = serviceAPI;

        WrapperHandler handler = new WrapperHandler(serviceProvider, logger);

        return (ServiceAPI) Proxy.newProxyInstance(serviceProvider.getClass().getClassLoader(), interfaces, handler);
    }

    /**
     * The wrapper handler being a go-between of each call.
     */
    private static class WrapperHandler implements InvocationHandler {

        /** The service provider to forward calls to. */
        private Object serviceProvider = null;

        /** When the @APSRunInBundlesContext is found calling method is added to this, and this is looked at first for performance. */
        private Map<Method, Boolean> contextEnabled = new HashMap<Method, Boolean>();

        /** A logger to log to when errors occur. */
        private APSLogger logger = null;

        /**
         * Creates a new WrapperHandler instance.
         *
         * @param serviceProvider The service provider to forward calls to.
         * @param logger A logger to log to on errors.
         */
        public WrapperHandler(Object serviceProvider, APSLogger logger) {
            this.serviceProvider = serviceProvider;
            this.logger = logger;
        }

        /**
         * This is called for each method called. This provides the functionallity.
         *
         * @param o The object the method was invoked on. This is ignored.
         * @param method The method called. The same method in the service provider instance will be called.
         * @param objects The method arguments. These will be forwarded.
         * @return The result of the forwarded call.
         *
         * @throws Throwable When things go bad.
         */
        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {

            Boolean contextEnabled = this.contextEnabled.get(method);
            if (contextEnabled == null) {
                Method targetMethod = this.serviceProvider.getClass().getMethod(method.getName(), method.getParameterTypes());
                APSRunInBundlesContext runInBundlesContextAnnotation = targetMethod.getAnnotation(APSRunInBundlesContext.class);
                if (runInBundlesContextAnnotation != null) {
                    this.contextEnabled.put(method, Boolean.TRUE);
                    contextEnabled = true;
                }
                else {
                    this.contextEnabled.put(method, Boolean.FALSE);
                    contextEnabled = false;
                }
            }
            ClassLoader origContextClassLoader = null;
            if (contextEnabled) {
                origContextClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.serviceProvider.getClass().getClassLoader());
            }

            try {
                return method.invoke(this.serviceProvider, objects);
            }
            catch (InvocationTargetException ite) {
                if (this.logger != null) {
                    this.logger.error("Exception in '" + this.serviceProvider.getClass().getSimpleName() + "." +
                            method.getName() + "' : " + ite.getCause().getMessage(), ite.getCause());
                }
                throw ite.getCause();
            }
            finally {
                if (origContextClassLoader != null) {
                    Thread.currentThread().setContextClassLoader(origContextClassLoader);
                }
            }
        }
    }
}
