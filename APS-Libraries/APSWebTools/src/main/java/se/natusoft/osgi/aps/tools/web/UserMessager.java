/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         This provides some utility classes for web applications.
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
 *     tommy ()
 *         Changes:
 *         2011-08-27: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web;

/**
 * Handles presenting user with messages.
 *
 * Different GUI choices needs different implementations of this. The basic idea
 * behind this is to make message handling less dependent on GUI. Unit tests can
 * also supply own implementation of this.
 */
public interface UserMessager {

    /**
     * Shows an error message on the window.
     * 
     * @param caption The message caption.
     * @param message The message.
     */
    public void error(String caption, String message);

    /**
     * Shows a warning message on the window.
     * 
     * @param caption The message caption.
     * @param message The message.
     */
    public void warning(String caption, String message);

    /**
     * Shows an info message on the window.
     * 
     * @param caption The message caption.
     * @param message The message.
     */
    public void info(String caption, String message);

    /**
     * Shows a tray message on the window.
     * 
     * @param caption The message caption.
     * @param message The message.
     */
    public void tray(String caption, String message);
}
