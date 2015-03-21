/*
 *
 * PROJECT
 *     Name
 *         APSOSGiTestTools
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
 *         2015-01-23: Created!
 *
 */
package se.natusoft.osgi.aps.test.tools;

import se.natusoft.osgi.aps.test.tools.internal.ServiceRegistry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is the entry point to using the OSGi service test tools.
 */
public class OSGIServiceTestTools {
    //
    // Private Members
    //

    private long idGen = 0;

    private ServiceRegistry serviceRegistry = new ServiceRegistry();

    private List<TestBundle> bundles = new LinkedList<>();

    private Map<String, TestBundle> bundleByName = new HashMap<>();

    private Map<Long, TestBundle> bundleById = new HashMap<>();

    //
    // Methods
    //

    /**
     * Creates a new TestBundle.
     *
     * @param symbolicName The symbolic name of the bundle to create.
     */
    public TestBundle createBundle(String symbolicName) {
        TestBundle bundle = new TestBundle(++idGen, symbolicName, this.serviceRegistry);
        this.bundles.add(bundle);
        this.bundleByName.put(symbolicName, bundle);
        this.bundleById.put(bundle.getBundleId(), bundle);
        return bundle;
    }

    /**
     * Removes a created bundle.
     *
     * @param bundle The bundle to remove.
     */
    public void removeBundle(TestBundle bundle) {
        this.bundles.remove(bundle);
        this.bundleByName.remove(bundle.getSymbolicName());
        this.bundleById.remove(bundle.getBundleId());
    }

    /**
     * Returns all created bundles.
     */
    public List<TestBundle> getBundles() {
        return this.bundles;
    }

    /**
     * Returns a specific bundle by its symbolic name.
     *
     * @param name The name of the bundle to get.
     */
    public TestBundle getBundleBySymbolicName(String name) {
        return this.bundleByName.get(name);
    }

    /**
     * Returns a specific bundle by its id.
     *
     * @param id The id of the bundle to get.
     */
    public TestBundle getBundleById(long id) {
        return this.bundleById.get(id);
    }
}
