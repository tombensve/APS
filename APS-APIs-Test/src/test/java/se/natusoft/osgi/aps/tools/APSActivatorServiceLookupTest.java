/*
 *
 * PROJECT
 *     Name
 *         APS APIs Tests
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         APS (Application Platform Services) - A smörgåsbord of OSGi application/platform type services intended for
 *         web applications. Currently based on Vert.x for backend and React for frontend (its own web admin apps).
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
package se.natusoft.osgi.aps.tools;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import se.natusoft.aps.activator.APSActivator;
import se.natusoft.osgi.aps.runtime.APSBundle;
import se.natusoft.osgi.aps.runtime.APSRuntime;
import se.natusoft.osgi.aps.tools.services.SearchCriteriaProviderTestService;
import se.natusoft.osgi.aps.tools.services.TestService;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("PackageAccessibility")
public class APSActivatorServiceLookupTest {

    @Test
    public void searchCriteriaProviderTest() throws Exception {
        APSRuntime testTools = new APSRuntime();
        APSBundle apsBundle = testTools.createBundle("test-bundle");
        apsBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/tools/services/ServicesSetupProviderTestService.class",
                "/se/natusoft/osgi/aps/tools/services/SearchCriteriaProviderTestService.class"
        );

        APSActivator activator = new APSActivator();
        activator.start( apsBundle.getBundleContext());

        try {
            // There should now be 3 instances of ServicesSetupProviderTestService, and one of SearchCriteriaProviderTestService.
            // All provides TestService, which is why we need to loop through all services until we find the service with the
            // implementation class we want. That service should have one of the the other class instances injected into it
            // as TestService and will forward getServiceInstanceInfo() to it. Since we filtered on "second" in the search
            // criteria we expect the string "second" to be returned.
            for (ServiceReference serviceReference : apsBundle.getRegisteredServices()) {
                TestService ts = (TestService) apsBundle.getBundleContext().getService(serviceReference);
                if (ts instanceof SearchCriteriaProviderTestService) {
                    Assert.assertEquals("Expected to get 'second' back from getServiceInstanceInfo()!", ts.getServiceInstanceInfo(), "second");
                }
                apsBundle.getBundleContext().ungetService(serviceReference);
            }
        }
        finally {
            activator.stop( apsBundle.getBundleContext());
        }

    }
}
