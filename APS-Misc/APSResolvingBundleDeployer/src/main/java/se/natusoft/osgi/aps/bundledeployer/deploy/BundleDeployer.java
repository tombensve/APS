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
