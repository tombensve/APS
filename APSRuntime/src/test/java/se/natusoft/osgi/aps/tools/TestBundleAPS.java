/*
 *
 * PROJECT
 *     Name
 *         APS OSGi Test Tools
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides tools for testing OSGi services.
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
 *         2017-02-19: Created!
 *
 */
package se.natusoft.osgi.aps.tools;

import org.junit.Test;
import se.natusoft.osgi.aps.runtime.APSBundle;
import se.natusoft.osgi.aps.runtime.APSRuntime;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * Tests of TestBundle.
 */
public class TestBundleAPS {

    @Test
    public void testLoadFromMaven() throws IOException {

        APSRuntime testTools = new APSRuntime();
        APSBundle apsBundle = testTools.createBundle("test-bundle");

        apsBundle.loadEntryPathsFromMaven("se.natusoft.osgi.aps", "aps-apis", "1.0.0");

        assertNotNull( apsBundle.getEntry( "/se/natusoft/aps/annotations/APSServiceAPI.class" ));
    }

    @Test
    public void testLoadEntryPathsFromDirScan() throws IOException {
        APSRuntime testTools = new APSRuntime();
        APSBundle apsBundle = testTools.createBundle("test-bundle");

        apsBundle.loadEntryPathsFromDirScan("APSRuntime/target/classes");

        assertNotNull( apsBundle.getEntry("/se/natusoft/osgi/aps/runtime/APSRuntime.class"));

    }
}
