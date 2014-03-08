/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         Edits configurations registered with the APSConfigurationService.
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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-04-08: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.editor;

import java.util.Stack;

/**
 * This navigates a readable config path at instance level. This means that each index of a list is available.
 */
public class ConfigPathNavigator {
    //
    // Private Members
    //
    
    /** Parent paths. */
    private Stack<String> paths = new Stack<String>();
    
    /** Current path. */
    private String path = null;
    
    //
    // Constructors
    //

    /**
     * Creates a new ConfigPath.
     */
    public ConfigPathNavigator() {}
    
    //
    // Methods
    //

    /**
     * Enters the specified sub node path.
     *
     * @param relativePath The name of the relative path to enter.
     */
    public void enterPath(String relativePath) {
        enterPath(relativePath, -1);
    }

    /**
     * Enters the specified sub node path. 
     * 
     * @param relativePath The name of the relative path to enter.
     * @param index The index of an "many" indexed path.
     */
    public void enterPath(String relativePath, int index) {
        if (this.path != null) {
            this.paths.push(this.path);
            this.path = this.path + "/" + relativePath;
            if (index >= 0) {
                this.path = this.path + "[" + index + "]";
            }
        }
        else {
            this.path = relativePath;
        }        
    }

    /**
     * Returns to the previous path.
     */
    public void leavePath() {
        // Please note that we don't allow backing out of the first entered path!
        if (!this.paths.isEmpty()) {
            this.path = this.paths.pop();
        }
    }

    /**
     * @return The current path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @return true if the root path.
     */
    public boolean isRoot() {
        return this.paths.size() <= 1;
    }
}
