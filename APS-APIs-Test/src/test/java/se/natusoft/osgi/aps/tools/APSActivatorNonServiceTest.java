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
 *         2015-01-18: Created!
 *
 */
package se.natusoft.osgi.aps.tools;

import org.junit.Test;
import se.natusoft.osgi.aps.activator.APSActivator;
import se.natusoft.osgi.aps.activator.annotation.BundleStart;
import se.natusoft.osgi.aps.activator.annotation.BundleStop;
import se.natusoft.osgi.aps.activator.annotation.Initializer;
import se.natusoft.osgi.aps.runtime.APSBundle;
import se.natusoft.osgi.aps.runtime.APSRuntime;
import se.natusoft.osgi.aps.runtime.BundleEntryPath;

import static org.junit.Assert.assertEquals;

public class APSActivatorNonServiceTest {

    @Test
    public void nonServiceInstanceTest() throws Exception {
        APSRuntime testTools = new APSRuntime();
        APSBundle apsBundle = testTools.createBundle("test-bundle");
        apsBundle.addEntryPaths(
                new BundleEntryPath("/se/natusoft/osgi/aps/tools/APSActivatorNonServiceTest.class")
        );

        APSActivator activator = new APSActivator();
        activator.start( apsBundle.getBundleContext());

        try {
            assertEquals("bundleStart() was never called!", "Jeehaa!", System.getProperty("test.bundle.start"));
            assertEquals("init() was never called!", "init", System.getProperty("test.bundle.init"));
        }
        finally {
            activator.stop( apsBundle.getBundleContext());
        }
    }

    // APSActivator should identify this class as a "managed" class, instantiate it, and call all of the
    // annotated methods below.

    @BundleStart
    public void bundleStart() {
        System.out.println("In bundleStart()!");
        System.setProperty("test.bundle.start", "Jeehaa!");
    }

    @BundleStop
    public void bundleStop() {
        System.out.println("In bundleStop()!");
    }

    @Initializer
    public void init() {
        System.out.println("In init()!");
        if (!System.getProperty("test.bundle.start").equals("Jeehaa!")) throw new RuntimeException("bundleStart() should have been called before the init() method!");
        System.setProperty("test.bundle.init", "init");
    }
}
