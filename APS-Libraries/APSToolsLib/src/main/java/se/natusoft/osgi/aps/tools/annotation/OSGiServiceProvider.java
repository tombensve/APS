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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-08-19: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.annotation;

import se.natusoft.osgi.aps.tools.APSActivator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that the annotated class is an OSGi service to be made available by the
 * APSActivator.
 *
 * This only works when APSActivator is used as bundle activator!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OSGiServiceProvider {

    /** Extra properties to register the service with. */
    OSGiProperty[] properties() default {};

    /** The service API to register instance with. If not specified the first implemented interface will be used. */
    Class[] serviceAPIs() default {};

    /** This can be used as an alternative to properties() and also supports several instances. */
    OSGiServiceInstance[] instances() default {};

    /**
     * This can be used as an alternative and will instantiate the specified factory class which will deliver
     * one set of Properties per instance.
     */
    Class<? extends APSActivator.InstanceFactory> instanceFactoryClass() default APSActivator.InstanceFactory.class;

    /**
     * If true this service will be stared in a separate thread. This means the bundle start
     * will continue in parallel and that any failures in startup will be logged, but will
     * not stop the bundle from being started. If this is true it wins over required service
     * dependencies of the service class. Specifying this as true allows you to do things that
     * cannot be done in a bunde activator start method, like calling a service tracked by
     * APSServiceTracker, without causing a deadlock.
     */
    boolean threadStart() default false;

}
