/* 
 * 
 * PROJECT
 *     Name
 *         ServerDeployPlugin
 *     
 *     Code Version
 *         0.11.0
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

/**
 * Deploys to server by simply copying one file from one place to another place.
 *
 * @goal copy
 * @phase install
 */
public class ServerDeployPlugin extends AbstractMojo {
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
     * @required
     */
    private String destFile;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    public void execute() throws MojoExecutionException {
        File source = new File(this.sourceFile);
        File dest = new File(this.destFile);

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
                BufferedInputStream srcStream = new BufferedInputStream(new FileInputStream(this.sourceFile));
                BufferedOutputStream destStream = new BufferedOutputStream(new FileOutputStream(this.destFile));
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
}
