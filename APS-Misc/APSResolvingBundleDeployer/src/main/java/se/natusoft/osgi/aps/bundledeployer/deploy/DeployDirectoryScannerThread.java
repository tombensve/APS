package se.natusoft.osgi.aps.bundledeployer.deploy;

import se.natusoft.osgi.aps.bundledeployer.config.APSBundleDeployerConfig;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

/**
 * This thread scans for bundle files to deploy in configured directory.
 */
public class DeployDirectoryScannerThread extends Thread {
    //
    // Private Members
    //

    /** An instance of the bundle deployer. */
    private BundleDeployer bundleDeployer = null;

    /** The bundle logger. */
    private Logger logger = null;

    /** The last scanned set of files. */
    private File[] lastScannedFiles = null;

    /** The running state of this thread. */
    private boolean running = false;

    //
    // Constructors
    //

    /**
     * Creates a new DeployDirectoryScannerThread.
     *
     * @param bundleDeployer The bundle deployed to use for deploying and undeploying.
     */
    public DeployDirectoryScannerThread(BundleDeployer bundleDeployer, Logger logger) {
        this.bundleDeployer = bundleDeployer;
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Terminates the thread.
     */
    public synchronized void terminate() {
        this.running = false;
    }

    /**
     * @return true if the thread should keep running.
     */
    private  synchronized boolean keepRunning() {
        return this.running;
    }

    /**
     * The thread start and end.
     */
    public void run() {
        this.running = true;

        this.logger.info("DeployDirectoryScannerThread started!");

        waitForConfig();

        while(keepRunning()) {
            scanDirectory();

            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException ie) {
                this.logger.error("Thread.sleep(...) got unexpectedly interrupted!", ie);
            }
        }

        this.logger.info("DeployDirectoryScannerThread stopped!");
    }

    /**
     * Scans the configured directory and uses the bundle deployer to deploy found bundles.
     */
    private void scanDirectory() {
        String scanDirConf = APSBundleDeployerConfig.managed.get().deployDirectory.toString();

        // Cancel if there is no configured scan directory!
        if (scanDirConf == null || scanDirConf.trim().length() == 0) {
            this.logger.error("No deploy directory to scan has been configured!");
            return;
        }

        File scanDir = new File(scanDirConf);

        // Also cancel if the configured scan directory does not exist.
        if (!scanDir.exists()) {
            this.logger.error("Configured deploy scan directory '" + scanDirConf + "' does not exist!");
            return;
        }

        File[] scanFiles = scanDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && (pathname.getName().endsWith(".jar") || pathname.getName().endsWith(".war"));
            }
        });

        // Handle deploys
        Map<File, File> currentFiles = new HashMap<>();
        for (File bundleFile : scanFiles) {
            currentFiles.put(bundleFile, bundleFile);

            // The bundle deployer will ignore this if it is already deployed.
            this.bundleDeployer.addDeploy(bundleFile);
        }

        // Handle undeploys
        if (this.lastScannedFiles != null) {
            for (File lastBundleFile : this.lastScannedFiles) {
                if (!currentFiles.containsKey(lastBundleFile)) {
                    this.bundleDeployer.addUndeploy(lastBundleFile);
                }
            }
        }

        this.lastScannedFiles = scanFiles;
        currentFiles.clear();
    }

    /**
     * Waits until config is managed.
     */
    private void waitForConfig() {
        if (!APSBundleDeployerConfig.managed.isManaged()) {
            this.logger.info("DeployDirectoryScanner: Waiting for config to become managed ...");
            APSBundleDeployerConfig.managed.waitUtilManaged();
            this.logger.info("DeployDirectoryScanner: config is now managed!");
        }
    }

}
