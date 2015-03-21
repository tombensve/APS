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

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 * Provides an implementation of ServiceReference for testing.
 */
public class TestServiceReference implements ServiceReference {
    //
    // Private Members
    //

    private TestBundleContext bundleContext = null;
    private List<Bundle> usingBundles = new ArrayList<>();
    private Dictionary props;

    //
    // Constructors
    //

    /**
     * Creates a new ServiceReference.
     *
     * @param bundleContext The context of the bundle the service belongs to.
     */
    public TestServiceReference(TestBundleContext bundleContext, Dictionary props) {
        this.bundleContext = bundleContext;
        this.props = props;
    }

    //
    // Methods
    //

    /*package*/ void setProperties(Dictionary properties) {
        this.props = properties;
    }

    /**
     * Adds "using" bundle that will be returned by getUsingBundles().
     *
     * @param usingBundle The "using" bundle to add.
     */
    public void addUsingBundle(Bundle usingBundle) {
        this.usingBundles.add(usingBundle);
    }

    @Override
    public Object getProperty(String key) {
        return this.props.get(key);
    }

    @Override
    public String[] getPropertyKeys() {
        String[] propKeys = new String[this.bundleContext.getProperties().stringPropertyNames().size()];
        return this.bundleContext.getProperties().stringPropertyNames().toArray(propKeys);
    }

    @Override
    public Bundle getBundle() {
        return this.bundleContext.getBundle();
    }

    @Override
    public Bundle[] getUsingBundles() {
        Bundle[] bundles = new Bundle[this.usingBundles.size()];
        return this.usingBundles.toArray(bundles);
    }


    /**
     * Always returns false.
     */
    @Override
    public boolean isAssignableTo(Bundle bundle, String className) {
        return false;
    }

    /**
     * Always returns 0.
     */
    @Override
    public int compareTo(Object reference) {
        return 0;
    }
}
