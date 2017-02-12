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
 *         2015-01-10: Created!
 *
 */
package se.natusoft.osgi.aps.tools.services;

import se.natusoft.osgi.aps.tools.annotation.activator.*;
import se.natusoft.osgi.aps.tools.apis.APSActivatorSearchCriteriaProvider;

@OSGiServiceProvider(threadStart = true)
public class SearchCriteriaProviderTestService implements TestService {

    @OSGiService(searchCriteriaProvider = MySearchCriteriaProvider.class)
    private TestService service;

    @Override
    public String getServiceInstanceInfo() throws Exception {
        return service.getServiceInstanceInfo();
    }

    public static class MySearchCriteriaProvider implements APSActivatorSearchCriteriaProvider {

        /**
         * This should return a String starting with '(' and ending with ')'. The final ServiceListener
         * criteria will be (&(objectClass=MyService)(_providedSearchCriteria()_))
         * <p/>
         * Whatever is returned it will probably  reference a property and a value that the service you
         * are looking for where registered with.
         */
        @Override
        public String provideSearchCriteria() {
            return "(instance=second)";
        }
    }
}
