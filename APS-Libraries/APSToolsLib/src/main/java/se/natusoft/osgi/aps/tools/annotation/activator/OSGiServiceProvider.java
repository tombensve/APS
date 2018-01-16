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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-08-19: Created!
 *
 */
package se.natusoft.osgi.aps.tools.annotation.activator;

import se.natusoft.osgi.aps.tools.APSActivator;
import se.natusoft.osgi.aps.tools.apis.APSActivatorServiceSetupProvider;

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
     * WARNING: If you use a managed configuration instance to provide the instances then you will have a chicken-egg
     * situation! == Deadlock.
     *
     * An alternative to providing static information. This class will be instantiated if specified and
     * provideServiceInstancesSetup() will be called to provide implemented service APIs, service
     * properties, and a service instance. In this last, it differs from instanceFactoryClass() since
     * that does not provide an instance. This allows for more easy configuration of each instance.
     */
    Class<? extends APSActivatorServiceSetupProvider> serviceSetupProvider() default APSActivatorServiceSetupProvider.class;

    /**
     * WARNING: If you use a managed configuration instance to provide the instances then you will have a chicken-egg
     * situation! == Deadlock.
     *
     * This can be used as an alternative and will instantiate the specified factory class which will deliver
     * one set of Properties per instance.
     *
     * This provides service APIs by providing a InstanceFactory.SERVICE_API_CLASSES_PROPERTY property containing
     * a colon separated list of fully qualified interfaces. If not provided the first implemented interface will
     * be used.
     *
     * Since this does not provide a service instance, declaring a Properties member of the service will have
     * that member injected with the properties of the instance as a method of providing configuration to the
     * instance.
     *
     * serviceSetupProvider() is newer and more flexible!
     */
    Class<? extends APSActivator.InstanceFactory> instanceFactoryClass() default APSActivator.InstanceFactory.class;

    /**
     * If true this service will be stared in a separate thread. This means the bundle start
     * will continue in parallel and that any failures in startup will be logged, but will
     * not stop the bundle from being started. If this is true it wins over required service
     * dependencies of the service class. Specifying this as true allows you to do things that
     * cannot be done in a bundle activator start method, like calling a service tracked by
     * APSServiceTracker, without causing a deadlock.
     */
    boolean threadStart() default false;

}
