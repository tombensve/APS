/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         A more advanced configuration service that uses annotated interfaces to
 *         describe and provide access to configuration. It supports structured
 *         configuration models.
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
 *         2011-08-13: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.service.store;

import org.junit.Before;
import org.junit.Test;
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEnvironmentImpl;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigServiceFileTool;
import se.natusoft.osgi.aps.core.test.support.filesystem.model.APSFilesystemImpl;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class APSConfigEnvStoreTest {

    /** Needed for tests. */
    private APSConfigServiceFileTool fileTool;

    /**
     * Tests the APSConfigEnvStore class.
     */
    public APSConfigEnvStoreTest() {
    }

//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }

    /**
     * Sets up a filesystem under target.
     *
     * @throws Exception on failure.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        File projRoot = new File("target");
        File testFs = new File(projRoot, "testfs");
        testFs.mkdirs();
        APSFilesystem fs = new APSFilesystemImpl(testFs.getAbsolutePath(), "APSConfigEnvStoreTest");
        this.fileTool = new APSConfigServiceFileTool(fs);
    }

//    @After
//    public void tearDown() {
//    }


    /**
     * Performs general tests.
     *
     * @throws Exception on any failure.
     */
    @Test
    public void testAll() throws Exception {
        System.out.print("testAll...");

        fileTool.removeFile(APSConfigEnvStore.ENVS_FILE + ".properties");

        // Adding envs
        APSConfigEnvStore envStore = new APSConfigEnvStore(fileTool);
        envStore.removeEnvironment(envStore.getConfigEnvironments().get(0)); // Remove default

        envStore.addEnvironment(new APSConfigEnvironmentImpl("devel", "The development configuration environment", 0));
        envStore.addEnvironment(new APSConfigEnvironmentImpl("systest", "The system test configuration environment", 0));

        assertEquals(envStore.getConfigEnvironments().size(), 2);
        assertEquals(envStore.getConfigEnvironments().get(0).getName(), "devel");
        assertEquals(envStore.getConfigEnvironments().get(1).getName(), "systest");

        // Creating new env store reading old saved data.
        envStore = new APSConfigEnvStore(fileTool);

        assertEquals(envStore.getConfigEnvironments().size(), 2);
        assertEquals(envStore.getConfigEnvironments().get(0).getName(), "devel");
        assertEquals(envStore.getConfigEnvironments().get(1).getName(), "systest");

        // Setting & getting active env.
        envStore.setActiveConfigEnvironment(envStore.getConfigEnvironments().get(0));

        assertEquals(envStore.getActiveConfigEnvironment().getName(), "devel");

        // Save of active env
        envStore = new APSConfigEnvStore(fileTool);

        assertEquals(envStore.getActiveConfigEnvironment().getName(), "devel");

        // Remove
        envStore.removeEnvironment(envStore.getConfigEnvironments().get(1));

        assertEquals(envStore.getConfigEnvironments().size(), 1);
        assertEquals(envStore.getConfigEnvironments().get(0).getName(), "devel");

        // Remove saved
        envStore = new APSConfigEnvStore(fileTool);

        assertEquals(envStore.getConfigEnvironments().size(), 1);
        assertEquals(envStore.getConfigEnvironments().get(0).getName(), "devel");

        System.out.println("ok");
    }
}
