package se.natusoft.osgi.aps.test.tools;

import java.io.File;

/**
 * This finds the maven root of a multi-module build.
 *
 * Be warned that this assumes that this is called during a maven build from an executing test! Only then
 * is it guaranteed that new File("."); will be at a maven project root.
 */
public class MavenRootFile {

    //
    // Private Members
    //

    /** The found root. */
    private File mavenRoot = null;

    //
    // Constructors
    //

    /**
     * Creates a new MavenRootFile.
     */
    public MavenRootFile() {
        find();
    }

    //
    // Methods
    //

    /**
     * This does the actual root finding.
     */
    private void find() {
        File dir = new File(".").getAbsoluteFile();

        File check = new File(dir, "pom.xml");
        while (dir != null && check.exists()) {
            this.mavenRoot = dir;
            dir = dir.getParentFile();
            check = new File(dir, "pom.xml");
        }
    }

    /**
     * Returns the found root.
     */
    public File getRoot() {
        return this.mavenRoot;
    }
}
