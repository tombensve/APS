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
package se.natusoft.osgi.aps.bundledeployer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.bundledeployer.deploy.BundleDeployerThread;
import se.natusoft.osgi.aps.bundledeployer.deploy.DeployDirectoryScannerThread;
import se.natusoft.osgi.aps.bundledeployer.deploy.Logger;

public class APSResolvingBundleDeployerActivator implements BundleActivator {
    //
    // Private Members
    //

    /** Handles deploy and undeploy. */
    private BundleDeployerThread bundleDeployerThread = null;

    /** Handles deploy directory scanning. */
    private DeployDirectoryScannerThread deployDirectoryScannerThread = null;

    /** Our logger. */
    private Logger logger = null;
    
    //
    // Bundle Start.
    //
    
    @Override
    public void start(BundleContext context) throws Exception {
        this.logger = new Logger(context);

        this.bundleDeployerThread = new BundleDeployerThread(context, this.logger);
        this.bundleDeployerThread.start();

        this.deployDirectoryScannerThread = new DeployDirectoryScannerThread(this.bundleDeployerThread, this.logger);
        this.deployDirectoryScannerThread.start();
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {

        if (this.deployDirectoryScannerThread != null) {
            this.deployDirectoryScannerThread.terminate();
            try {this.deployDirectoryScannerThread.join(10000);} catch (InterruptedException ie) {}
        }

        if (this.bundleDeployerThread != null) {
            this.bundleDeployerThread.terminate();
            try {this.bundleDeployerThread.join(10000);} catch (InterruptedException ie) {}
        }

    }

}
