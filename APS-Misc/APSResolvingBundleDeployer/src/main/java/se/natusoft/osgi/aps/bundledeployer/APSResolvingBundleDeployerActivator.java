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
