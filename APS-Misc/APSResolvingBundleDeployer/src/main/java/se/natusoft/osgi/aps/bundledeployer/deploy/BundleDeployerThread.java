package se.natusoft.osgi.aps.bundledeployer.deploy;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import se.natusoft.osgi.aps.bundledeployer.config.APSBundleDeployerConfig;

import java.io.File;
import java.util.*;

/**
 * This class provides the bundle deployer functionality.
 */
public class BundleDeployerThread extends Thread implements BundleDeployer {
    //
    // Private Members
    //

    /** Our bundle context. */
    private BundleContext context = null;

    /** Bundle logger. */
    private Logger logger = null;

    /** This keeps track of deploy attempts.  */
    private Map<File, Integer> deployAttempts = new HashMap<>();

    /** Keeps track of last modified for a bundle. */
    private Map<File, Long> lastModified = new HashMap<>();

    /** The queue to deploy. */
    private Queue<File> deployQueue = new LinkedList<>();

    /** The que for deployed bundles to be undeployed. */
    private Queue<File> unDeployQueue = new LinkedList<>();

    /** This keeps track of all deployed bundles. */
    private Map<File, Bundle> deployed = new HashMap<>();

    /** The running state of the thread. */
    private boolean running = false;

    //
    // Constructors
    //

    /**
     * Creates a new BundleDeployerThread.
     *
     * @param context The Bundle context.
     * @param logger The bundle logger.
     */
    public BundleDeployerThread(BundleContext context, Logger logger) {
        this.context = context;
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
     * @return true if thread should keep running.
     */
    private synchronized boolean keepRunning() {
        return this.running;
    }

    /**
     * Thread beginning and end.
     */
    public void run() {
        this.running = true;

        this.logger.info("BundleDeployedThread started!");

        waitForConfig();

        while (keepRunning()) {
            if (hasDeploy() || hasUndeploy()) {

                if (hasDeploy()) deployNext();

                if (hasUndeploy()) undeployNext();
            }
            else {
                try {Thread.sleep(5000);} catch (InterruptedException ie) {/* Will happen whe a deploy is added!*/}
            }
        }

        this.logger.info("BundleDeployerThread stopped!");
    }

    /**
     * Deploys the next bundle in the queue.
     */
    private void deployNext() {
        File deployFile = nextDeploy();
        Bundle bundle = null;
        try {
            this.logger.info("About to deploy: " + deployFile);
            bundle = this.context.installBundle("file:" + deployFile.getAbsolutePath());
            bundle.start();
            saveAsDeployed(deployFile, bundle);
            this.logger.info("Deploy successful!");

            this.logger.info("Successfully deployed bundle '" + bundle.getSymbolicName() + ":" + bundle.getVersion() + "'!");

            if (this.lastModified.containsKey(deployFile)) {
                this.lastModified.remove(deployFile);
            }
        }
        catch (BundleException be) {
            if (bundle != null) {
                try {bundle.stop();} catch (BundleException be1) {}
                try {bundle.uninstall();} catch (BundleException be2) {}
            }
            Integer deployCount = this.deployAttempts.get(deployFile);
            if (deployCount == null) {
                deployCount = 1;
                this.deployAttempts.put(deployFile, deployCount);
                addDeploy(deployFile);
            }
            else {
                ++deployCount;
                this.deployAttempts.put(deployFile, deployCount);
                if (deployCount >= APSBundleDeployerConfig.managed.get().failThreshold.toInt()) {
                    // We save the last modified time so that we can ignore this if we see it again with the same modified time.
                    saveLastModified(deployFile);

                    this.logger.error("Failed to deploy bundle '" + deployFile.getAbsolutePath() + "'!", be);
                }
                else {
                    addDeploy(deployFile);
                }
            }
        }
    }

    /**
     * Undeploys the next bundle in the undeploy queue.
     */
    private void undeployNext() {
        File undeployFile = nextUndeploy();
        Bundle undeployBundle = removeDeployment(undeployFile);
        try {
            undeployBundle.uninstall();
        }
        catch (BundleException be) {
            this.logger.error("Failed to undeploy bundle '" + undeployBundle.getSymbolicName() + "'!", be);
        }
    }

    /**
     * @return a set of deployed bundle files.
     */
    @Override
    public synchronized Set<File> getDeployedBundleFiles() {
        return this.deployed.keySet();
    }

    /**
     * Saves information about deployed bundle.
     *
     * @param deployFile The source of the deployed bundle.
     * @param bundle The bundle.
     */
    private synchronized void saveAsDeployed(File deployFile, Bundle bundle) {
        this.deployed.put(deployFile, bundle);
    }

    /**
     * Removes the specified file from map of deployed bundles.
     *
     * @param deployFile The bundle file to remove.
     *
     * @return The files bundle.
     */
    private synchronized Bundle removeDeployment(File deployFile) {
        return this.deployed.remove(deployFile);
    }

    /**
     * Saves the last modified time of a bundle.
     *
     * @param deployFile The bundle file.
     */
    private synchronized void saveLastModified(File deployFile) {
        this.lastModified.put(deployFile, deployFile.lastModified());
    }

    /**
     * Adds a deploy to the end of the deploy queue.
     *
     * @param bundleFile The bundle to add.
     */
    @Override
    public synchronized void addDeploy(File bundleFile) {
        // Skip if already deployed.
        if (this.deployed.containsKey(bundleFile)) {
            return;
        }

        // Skip if already in queue.
        if (this.deployQueue.contains(bundleFile)) {
            return;
        }

        // Skip if deploy have been attempted before and failed and last modified time has not changed.
        Long lastBundleModifiedTime = this.lastModified.get(bundleFile);
        if (lastBundleModifiedTime != null && !(bundleFile.lastModified() > lastBundleModifiedTime)) {
            return;
        }

        this.deployQueue.add(bundleFile);
        notify();

        this.logger.info("Queueing '" + bundleFile + "' for deployemnt!");
    }

    /**
     * @return true if there are deploys available.
     */
    private synchronized boolean hasDeploy() {
        return !this.deployQueue.isEmpty();
    }

    /**
     * @return The next bundle to deploy or null.
     */
    private synchronized File nextDeploy() {
        return this.deployQueue.poll();
    }

    /**
     * Adds a bundle file to be undeployed.
     *
     * @param bundleFile The bundle file to undeploy.
     */
    @Override
    public synchronized void addUndeploy(File bundleFile) {
        this.unDeployQueue.add(bundleFile);
        this.logger.info("Undeploying '" + bundleFile + "'");
    }

    /**
     * @return true if there are bundles to undeploy.
     */
    private synchronized boolean hasUndeploy() {
        return !this.unDeployQueue.isEmpty();
    }

    /**
     * @return The next bundle file to undeploy.
     */
    private synchronized File nextUndeploy() {
        return this.unDeployQueue.poll();
    }

    /**
     * Waits until config is managed.
     */
    private void waitForConfig() {
        if (!APSBundleDeployerConfig.managed.isManaged()) {
            this.logger.info("BundleDeployerThread: Waiting for config to become managed ...");
            APSBundleDeployerConfig.managed.waitUtilManaged();
            this.logger.info("BundleDeployerThread: Config is now managed!");
        }
    }
}
