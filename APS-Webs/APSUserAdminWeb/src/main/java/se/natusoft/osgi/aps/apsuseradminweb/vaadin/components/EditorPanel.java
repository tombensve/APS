/* 
 * 
 * PROJECT
 *     Name
 *         APS User Admin Web
 *     
 *     Code Version
 *         0.9.1
 *     
 *     Description
 *         This is an administration web for aps-simple-user-service that allows editing of roles and users.
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
 *         2012-09-02: Created!
 *         
 */
package se.natusoft.osgi.aps.apsuseradminweb.vaadin.components;

import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshable;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshables;

/**
 * This is a panel that also handles 'Refreshables'.
 */
public class EditorPanel extends Panel {
    //
    // Private Members
    //

    /** A set of components that needs refresh when contents have changes. */
    private Refreshables refreshables = null;

    /** A refreshable that clears the center view. */
    private Refreshable clearCenterRefreshable = null;

    /** The app logger. */
    private APSLogger logger;

    //
    // Methods
    //

    /**
     * Sets the refreshable components container.
     *
     * @param refreshables The components needing refresh on content change.
     */
    public void setRefreshables(Refreshables refreshables) {
        this.refreshables = refreshables;
    }

    /**
     * Sets a refreshable that clears the center view.
     *
     * @param clearCenterRefreshable The clear center refreshable to set.
     */
    public void setClearCenterRefreshable(Refreshable clearCenterRefreshable) {
        this.clearCenterRefreshable = clearCenterRefreshable;
    }

    /**
     * Refreshes dependent components.
     */
    protected void refreshDependentComponents() {
        this.refreshables.refresh();
    }

    /**
     * Clears the center if a clear center refreshable have been provided.
     */
    protected void clearCenter() {
        if (this.clearCenterRefreshable != null) {
            this.clearCenterRefreshable.refresh();
        }
    }

    /**
     * Helper method to display a notification to the user.
     *
     * @param message The notification message.
     */
    protected void notifySuccess(String heading, String message) {
        if (getWindow() != null) {
            getWindow().showNotification(heading, message, Window.Notification.TYPE_TRAY_NOTIFICATION);
        }
    }

    /**
     * Helper method to display a notification to the user.
     *
     * @param message The notification message.
     */
    protected void notifyError(String heading, String message) {
        if (getWindow() != null) {
            getWindow().showNotification(heading, message, Window.Notification.TYPE_ERROR_MESSAGE);
        }
    }

    /**
     * Sets the application logger.
     *
     * @param logger The logger to set.
     */
    public void setLogger(APSLogger logger) {
        this.logger = logger;
    }

    /**
     * Returns the app logger.
     */
    protected APSLogger getLogger() {
        return this.logger;
    }
}
