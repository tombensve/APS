/* 
 * 
 * PROJECT
 *     Name
 *         APS Vaadin Web Tools
 *     
 *     Code Version
 *         0.11.0
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
package se.natusoft.osgi.aps.tools.web.vaadin;

import com.vaadin.ui.Notification;
import se.natusoft.osgi.aps.tools.web.UserNotifier;

/**
 * Implementation of _UserMessager_ for Vaadin applications using a Vaadin Window to do
 * _showNotification(...)_ on.
 */
public class VaadinUserNotifier implements UserNotifier {
    //
    // Constructors
    //

    /**
     * Creates a new VaadinUserMessager instance.
     */
    public VaadinUserNotifier() {
    }
    
    //
    // Methods
    //


    /**
     * Shows an error message on the window.
     * 
     * @param caption The message caption.
     * @param message The message.
     */
    public void error(String caption, String message) {
        Notification.show(
                caption,
                message,
                Notification.Type.ERROR_MESSAGE);
    }
    
    /**
     * Shows a warning message on the window.
     * 
     * @param caption The message caption.
     * @param message The message.
     */
    public void warning(String caption, String message) {
        Notification.show(
                caption,
                message,
                Notification.Type.WARNING_MESSAGE);
    }
    
    /**
     * Shows an info message on the window.
     * 
     * @param caption The message caption.
     * @param message The message.
     */
    public void info(String caption, String message) {
        Notification.show(
                caption,
                message,
                Notification.Type.HUMANIZED_MESSAGE);
    }
    
    /**
     * Shows a tray message on the window.
     * 
     * @param caption The message caption.
     * @param message The message.
     */
    public void tray(String caption, String message) {
        Notification.show(
                caption,
                message,
                Notification.Type.TRAY_NOTIFICATION);
    }
}
