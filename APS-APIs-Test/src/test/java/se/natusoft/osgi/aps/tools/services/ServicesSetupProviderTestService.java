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
package se.natusoft.osgi.aps.tools.services;

import se.natusoft.osgi.aps.activator.ServiceSetup;
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider;
import se.natusoft.osgi.aps.activator.APSActivatorServiceSetupProvider;

import java.util.LinkedList;
import java.util.List;

@OSGiServiceProvider(serviceSetupProvider = ServicesSetupProviderTestService.SetupProvider.class)
public class ServicesSetupProviderTestService implements TestService {

    private String instName;

    public ServicesSetupProviderTestService() {}

    public ServicesSetupProviderTestService(String instName) {
        this.instName = instName;
    }

    @Override
    public String getServiceInstanceInfo() {
        return this.instName;
    }

    public static class SetupProvider implements APSActivatorServiceSetupProvider {

        /**
         * Provides setup for each instance to create and register.
         * <p/>
         * Each returned Setup instance will result in one registered service instance.
         * <p/>
         * Each Properties instance should contain a common property with different values
         * that can be searched for during service lookup/tracking.
         */
        @Override
        public List<ServiceSetup> provideServiceInstancesSetup() {
            List<ServiceSetup> setups = new LinkedList<>();

            ServiceSetup setup = new ServiceSetup();
            setup.getServiceAPIs().add("se.natusoft.osgi.aps.tools.services.TestService");
            setup.getProps().setProperty("instance", "first");
            setup.setServiceInstance(new ServicesSetupProviderTestService("first"));
            setups.add(setup);

            setup = new ServiceSetup();
            setup.getServiceAPIs().add("se.natusoft.osgi.aps.tools.services.TestService");
            setup.getProps().setProperty("instance", "second");
            setup.setServiceInstance(new ServicesSetupProviderTestService("second"));
            setups.add(setup);

            setup = new ServiceSetup();
            setup.getServiceAPIs().add("se.natusoft.osgi.aps.tools.services.TestService");
            setup.getProps().setProperty("instance", "third");
            setup.setServiceInstance(new ServicesSetupProviderTestService("third"));
            setups.add(setup);

            return setups;
        }
    }
}
