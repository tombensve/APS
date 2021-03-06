/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
package se.natusoft.osgi.aps.activator.annotation;

import se.natusoft.osgi.aps.activator.APSActivatorSearchCriteriaProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that the annotated field is a service that the class depends on.
 *
 * This only works when APSActivator is used as bundle activator!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OSGiService {

    /** The timeout for a service to become available. Defaults to 30 seconds. */
    String timeout() default "30 seconds";

    /** Any additional search criteria. Should start with '(' and end with ')'. Defaults to none. */
    String additionalSearchCriteria() default "";

    /**
     * This should specify a Class implementing APSActivatorSearchCriteriaProvider. If specified it will be
     * used instead of additionalSearchCriteria() by instantiating the Class and calling its method to get
     * a search criteria back. This allows for search criteria coming from configuration, which a static
     * annotation String does not.
     */
    Class<? extends APSActivatorSearchCriteriaProvider> searchCriteriaProvider() default APSActivatorSearchCriteriaProvider.class;

    /** If set to true the service using this service will not be registered until the service becomes available. */
    boolean required() default false;

    /** When APSServiceTracker is injected rather than service then this provides the service interface to track. */
    Class serviceAPI() default Object.class;

    /**
     * If this is set to true and a proxied implementation of the service is injected rather than the tracker directly
     * then any call made to the proxy will be cached if the service is not available and then later run when the
     * service becomes available. This of course means that methods returning a value will always return null when
     * service is not currently available since the real call will be made in the future. Returning a Future instead
     * in this case does not work since 'Future's are blocking, and we try to avoid blocking here.
     *
     * __YOU HAVE TO BE VERY CAREFUL WHEN SETTING THIS TO TRUE! NO CALLS RETURNING A VALUE!__
     *
     * The point of this is to be non blocking. By default with a proxied implementation tracker.allocateService() will
     * be called, and this blocks waiting for the service to become available if it is not.
     *
     * @return true or false (default).
     */
    boolean nonBlocking() default false;
}
