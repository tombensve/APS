/*
 *
 * PROJECT
 *     Name
 *         ServerDeployPlugin
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Basically copies one file from one place to another. This is however used
 *         to deploy build artifacts from target to server pickup directory.
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
 *         2013-02-05: Created!
 *
 */
package se.natusoft.osgi.aps;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.List;

/**
 * Deploys to server by simply copying one file from one place to another place.
 *
 * @goal copy
 * @phase install
 */
@SuppressWarnings( { "JavaDoc", "unused" } )
public class ServerDeployPlugin extends AbstractMojo {
    /**
     * Source file. This is for backwards compatibility. 'deployableList' should be used instead.
     *
     * @parameter
     */
    private String sourceFile;

    /**
     * Destination file. This is for backwards compatibility. 'deployableList' should be used instead.
     *
     * @parameter
     */
    private String destFile;

    /**
     * A list of file to deploy.
     *
     * @parameter
     */
    @SuppressWarnings( { "SpellCheckingInspection", "MismatchedQueryAndUpdateOfCollection" } )
    private List<Deployable> deployables;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    public void execute() throws MojoExecutionException {
        try {
            getLog().info("Executing ServerDeployPlugin!");
            // For backwards compatibility.
            if (this.sourceFile != null && this.destFile != null && this.sourceFile.trim().length() > 0 && this.destFile.trim().length() > 0) {
                deploy(new Deployable().setSourceFile(this.sourceFile).setDestFile(this.destFile));
            }
            // The new way of doing it.
            else if (deployables != null && !deployables.isEmpty()) {
                for (Deployable deployable : deployables) {
                    deploy(deployable);
                }
            } else {
                throw new MojoExecutionException("No sourceFile & destFile nor a deployableList have been provided! Nothing will be deployed!");
            }
        }
        catch (MojoExecutionException mee) {
            throw mee;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Failed due to bug in plugin!", e);
        }
    }

    private void deploy(Deployable deployable) throws MojoExecutionException {
        deployable.sourceFile = expandHome(deployable.sourceFile);
        deployable.destFile = expandHome(deployable.destFile);
        deployable.destPath = expandHome(deployable.destPath);

        File source = new File(deployable.sourceFile);
        File dest;
        if (deployable.destFile != null && !deployable.destFile.trim().isEmpty()) {
            dest = new File(deployable.destFile);
        }
        else {
            String destPath = deployable.destPath;
            if (!destPath.endsWith(File.separator)) {
                destPath += File.separator;
            }
            dest = new File(destPath + source.getName());
        }

        boolean validRun = true;
        if (this.project.getPackaging().equals("pom")) {
            validRun = false;
            getLog().info("Skipping deploy for this project due to it having packaging:pom and thus does not have anything deployable!");
        }
        else if (!source.exists()) {
            validRun = false;
            getLog().warn("Failed to deploy due to that '" + source.getAbsolutePath() + "' does not exist!");
        }
        else if (!dest.getParentFile().exists()) {
            validRun = false;
            getLog().warn("Failed to deploy due to that target path '" + dest.getParentFile().getAbsolutePath() + "' does not exist!");
        }

        if (validRun) {
            try {
                BufferedInputStream srcStream = new BufferedInputStream(new FileInputStream(source));
                BufferedOutputStream destStream = new BufferedOutputStream(new FileOutputStream(dest));
                int b = srcStream.read();
                do {
                    destStream.write(b);
                    b = srcStream.read();
                } while (b != -1);
                destStream.flush();
                destStream.close();
                srcStream.close();
                getLog().info("Deployed '" + source.getAbsolutePath() + "' to '" + dest.getAbsolutePath() + "'!");
            }
            catch (IOException ioe) {
                throw new MojoExecutionException(ioe.getMessage(), ioe);
            }
        }

    }

    private static String expandHome(String path) {
        if (path == null) return null;

        String result = path;

        if (result.startsWith("~")) {
            String home = System.getProperty("user.home");
            result = home + result.substring(1);
        }

        return result;
    }

    //
    // Internal Classes
    //

    /**
     * This represents one deployable.
     */
    @SuppressWarnings( "WeakerAccess" )
    public static final class Deployable {
        /**
         * Source file.
         *
         * @parameter
         * @required
         */
        private String sourceFile;

        /**
         * Destination file.
         *
         * @parameter
         */
        private String destFile;

        /**
         * Destingation path without the filename.
         *
         * @parameter
         */
        private String destPath;

        public Deployable setSourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }

        public Deployable setDestFile(String destFile) {
            this.destFile = destFile;
            return this;
        }

        public Deployable setDestPath(String destPath) {
            this.destPath = destPath;
            return this;
        }

        public String getSourceFile() {
            return this.sourceFile;
        }

        public String getDestFile() {
            return this.destFile;
        }

        public String getDestPath() {
            return this.destPath;
        }
    }

}
