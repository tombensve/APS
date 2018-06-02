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
 *     tommy ()
 *         Changes:
 *         2015-01-10: Created!
 *
 */
package se.natusoft.osgi.aps.activator;

/**
 * Classes implementing this interface can be specified in @OSGiService annotation. In that case
 * the class will be instantiated and the method below will be called, and the resulting String
 * will be used as additional search criteria (in addition to service class name).
 */
public interface APSActivatorSearchCriteriaProvider {

    /**
     * This should return a String starting with '(' and ending with ')'. The final ServiceListener
     * criteria will be (&(objectClass=MyService)(_providedSearchCriteria()_))
     *
     * Whatever is returned it will probably  reference a property and a value that the service you
     * are looking for where registered with.
     */
    String provideSearchCriteria();

    /**
     * Since the APSActivatorSearchCriteriaProvider is specified in an annotation
     * which is very static, non static implementation classes cannot be instantiated,
     * which means that internal instance information cannot be provided.
     *
     * However if the managed class injecting a service that uses a search criteria provider
     * also implements this interface it will be used to create the instance instead of
     * doing newInstance() on the specified class.
     */
    interface SearchCriteriaProviderFactory {

        /**
         * Returns an instance of an APSActivatorSearchCriteriaProvider.
         */
        APSActivatorSearchCriteriaProvider createSearchCriteriaProvider();
    }
}
