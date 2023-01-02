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
import se.natusoft.osgi.aps.activator.APSActivator;
import se.natusoft.osgi.aps.runtime.APSBundle;
import se.natusoft.osgi.aps.runtime.APSRuntime;
import se.natusoft.osgi.aps.tools.services.SimpleService;
import se.natusoft.osgi.aps.tools.services.TestService;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("PackageAccessibility")
public class APSActivatorServiceRegTest {

    @Test
    public void simpleTest() throws Exception {
        APSRuntime testTools = new APSRuntime();
        APSBundle apsBundle = testTools.createBundle("test-bundle");
        apsBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/tools/services/SimpleService.class",
                "/se/natusoft/osgi/aps/tools/services/TestService.class"
        );

        APSActivator activator = new APSActivator();
        activator.start( apsBundle.getBundleContext());

        try {
            assertTrue("There should only be one registered service!", apsBundle.getRegisteredServices().length == 1);
            TestService service = (TestService) apsBundle.getBundleContext().getService( apsBundle.getRegisteredServices()[0]);
            assertNotNull("The managed logger field was not injected!", ((SimpleService)service).getLogger());
            assertNotNull("Service should not be null!", service);
            System.out.println("Service: " + service);
            Assert.assertEquals("This service should be of type OSGiService!", service.getClass(),
                    SimpleService.class);
        }
        finally {
            activator.stop( apsBundle.getBundleContext());
        }
    }

    @Test
    public void serviceSetupProviderTest() throws Exception {
        APSRuntime testTools = new APSRuntime();
        APSBundle apsBundle = testTools.createBundle("test-bundle");
        apsBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/tools/services/ServicesSetupProviderTestService.class"
        );

        APSActivator activator = new APSActivator();
        activator.start( apsBundle.getBundleContext());

        try {
            int not3Count = 0;
            while ( apsBundle.getRegisteredServices().length != 3 && not3Count < 3) {
                Thread.sleep(500); // Short delay to give activator threads time to finnish.
                ++not3Count;
            }
            if (not3Count >= 3) fail("Took too long for 3 services to be registered!");

            assertTrue("There should be 3 registered service instances!", apsBundle.getRegisteredServices().length == 3);

            List<String> validValues = new LinkedList<>();
            validValues.add("first");
            validValues.add("second");
            validValues.add("third");

            TestService service = (TestService) apsBundle.getBundleContext().getService( apsBundle.getRegisteredServices()[0]);
            assertNotNull("Service should not be null!", service);
            System.out.println(service.getServiceInstanceInfo());
            assertEquals("'instance' property does not match!", service.getServiceInstanceInfo(),
                    apsBundle.getRegisteredServices()[0].getProperty("instance"));
            validValues.remove(service.getServiceInstanceInfo());
            apsBundle.getBundleContext().ungetService( apsBundle.getRegisteredServices()[0]);

            service = (TestService) apsBundle.getBundleContext().getService( apsBundle.getRegisteredServices()[1]);
            assertNotNull("Service should not be null!", service);
            System.out.println(service.getServiceInstanceInfo());
            assertEquals("'instance' property does not match!", service.getServiceInstanceInfo(),
                    apsBundle.getRegisteredServices()[1].getProperty("instance"));
            validValues.remove(service.getServiceInstanceInfo());
            apsBundle.getBundleContext().ungetService( apsBundle.getRegisteredServices()[1]);

            service = (TestService) apsBundle.getBundleContext().getService( apsBundle.getRegisteredServices()[2]);
            assertNotNull("Service should not be null!", service);
            System.out.println(service.getServiceInstanceInfo());
            assertEquals("'instance' property does not match!", service.getServiceInstanceInfo(),
                    apsBundle.getRegisteredServices()[2].getProperty("instance"));
            validValues.remove(service.getServiceInstanceInfo());
            apsBundle.getBundleContext().ungetService( apsBundle.getRegisteredServices()[2]);

            assertTrue("Not all 'validValues' where checked off!", validValues.size() == 0);
        }
        finally {
            activator.stop( apsBundle.getBundleContext());
        }
    }
}
