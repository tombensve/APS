/* 
 * 
 * PROJECT
 *     Name
 *         APS User Admin Web
 *     
 *     Code Version
 *         0.9.0
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
import se.natusoft.osgi.aps.api.auth.user.model.RoleAdmin;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.editors.RoleDeleteEditor;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;

/**
 * Component handler for deleting a role.
 */
public class DeleteRoleComponentHandler implements ComponentHandler {
    //
    // Private Members
    //

    /** The role editor component. */
    private RoleDeleteEditor roleDeleteEditor = null;

    /** The role to edit for this instance. */
    private RoleAdmin role = null;

    //
    // Constructors
    //

    /**
     * Creates a new DeleteRoleComponentHandler.
     *
     * @param roleDeleteEditor The component to use for deleting a role.
     * @param role The role managed by this component handler.
     */
    public DeleteRoleComponentHandler(RoleDeleteEditor roleDeleteEditor, RoleAdmin role) {
        this.roleDeleteEditor = roleDeleteEditor;
        this.role = role;
    }

    //
    // Methods
    //

    /**
     * @return The component that should handle the item.
     */
    @Override
    public AbstractComponent getComponent() {
        this.roleDeleteEditor.setRole(this.role);
        return this.roleDeleteEditor;
    }
}
