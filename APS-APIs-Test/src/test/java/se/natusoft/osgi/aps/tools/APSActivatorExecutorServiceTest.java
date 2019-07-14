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
 *         2017-01-05: Created!
 *
 */
package se.natusoft.osgi.aps.tools;

import org.junit.Assert;
import org.junit.Test;
import se.natusoft.osgi.aps.activator.APSActivator;
import se.natusoft.osgi.aps.test.tools.APSOSGIServiceTestTools;
import se.natusoft.osgi.aps.tools.services.TestService;
import se.natusoft.osgi.aps.tracker.APSServiceTracker;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class APSActivatorExecutorServiceTest extends APSOSGIServiceTestTools {


    @Test
    public void testExecutorService() throws Throwable {

        deploy("exec-svc-bundle").with(new APSActivator()).using(new String[]
                {"/se/natusoft/osgi/aps/tools/services/APSActivatorExecutorServiceTestSvc.class"});

        try {
            with_new_bundle("test-verify-bundle", bundleContext -> {

                APSServiceTracker<TestService> tsTracker =
                        new APSServiceTracker<>(bundleContext, TestService.class, "20 seconds");
                tsTracker.start();

                TestService svc = tsTracker.allocateService();

                    Assert.assertEquals("OK", svc.getServiceInstanceInfo());

                tsTracker.releaseService();

                tsTracker.stop(bundleContext);
            });
        }
        finally {
            shutdown();
        }

    }

}
