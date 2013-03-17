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
 *         2012-08-26: Created!
 *         
 */
package se.natusoft.osgi.aps.apsuseradminweb.vaadin.componenthandlers;

import com.vaadin.ui.AbstractComponent;
import se.natusoft.osgi.aps.api.auth.user.model.UserAdmin;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.editors.UserEditor;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;

/**
 * Component handler for editing a user.
 */
public class EditUserComponentHandler implements ComponentHandler {
    //
    // Private Members
    //

    /** The user editor component. */
    private UserEditor userEditor = null;

    /** The user to edit for this instance. */
    private UserAdmin user = null;

    //
    // Constructors
    //

    /**
     * Creates a new EditUserComponentHandler.
     *
     * @param userEditor The editor component to use for editing a user.
     * @param user The user managed by this component handler.
     */
    public EditUserComponentHandler(UserEditor userEditor, UserAdmin user) {
        this.userEditor = userEditor;
        this.user = user;
    }

    //
    // Methods
    //

    /**
     * @return The component that should handle the item.
     */
    @Override
    public AbstractComponent getComponent() {
        this.userEditor.setUser(this.user);
        return this.userEditor;
    }
}
