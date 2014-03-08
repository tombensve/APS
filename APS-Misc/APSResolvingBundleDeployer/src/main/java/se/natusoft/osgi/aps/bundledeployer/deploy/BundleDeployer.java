/* 
 * 
 * PROJECT
 *     Name
 *         APS Resolving Bundle Deployer
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         Deploys bundles resolving dependencies as automatically as possible by accepting a few
 *         deploy failures and retrying until it works or a fail threshold has been reached.
 *         
 *         Unless the server deployed on supports WABs using the extender pattern, no war files
 *         will deploy correctly.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2013-01-03: Created!
 *         
 */
package se.natusoft.osgi.aps.bundledeployer.deploy;

import java.io.File;
import java.util.Set;

/**
 * The bundle deployed API.
 */
public interface BundleDeployer {
    /**
     * @return a set of deployed bundle files.
     */
    Set<File> getDeployedBundleFiles();

    /**
     * Adds a deploy to the end of the deploy queue.
     *
     * @param bundleFile The bundle to add.
     */
    void addDeploy(File bundleFile);

    /**
     * Adds a bundle file to be undeployed.
     *
     * @param bundleFile The bundle file to undeploy.
     */
    void addUndeploy(File bundleFile);
}
