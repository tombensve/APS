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
package se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.editors;

import com.vaadin.ui.*;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserServiceAdmin;
import se.natusoft.osgi.aps.api.auth.user.model.Role;
import se.natusoft.osgi.aps.api.auth.user.model.RoleAdmin;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.EditorIdentifier;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.EditorPanel;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.css.CSS;

/**
 * Confirms and deletes a role. The role to delete is passed in setRole() by
 * DeleteRoleComponentHandler.
 */
public class RoleDeleteEditor extends EditorPanel implements EditorIdentifier {
    //
    // Private Members
    //

    /** The user service to create and update roles with. */
    private APSSimpleUserServiceAdmin userServiceAdmin = null;

    /** The role being deleted. */
    private Role role = null;

    /** A visual box to put the role id in instead of a TextField since values cannot be changed here. */
    private Panel idPanel = null;

    /** A label with the role id. This is put in the idPanel. */
    private Label idLabel = null;

    /** A visual box to put the role description in instead of a TextField. */
    private Panel descriptionPanel = null;

    /** A label with the role description. This is put in the descriptionPanel. */
    private Label descriptionLabel = null;

    /** A visual box to put the sub role list in. */
    private Panel subRolesPanel = null;

    /** A label containing a list of sub roles of the role. */
    private Label subRolesLabel = null;


    //
    // Constructors
    //

    /**
     * Creates a new RoleDeleteEditor instance.
     *
     * @param userServiceAdmin The user admin service for editing the role.
     */
    public RoleDeleteEditor(APSSimpleUserServiceAdmin userServiceAdmin) {
        this.userServiceAdmin = userServiceAdmin;
        this.setStyleName(CSS.APS_EDITING_TEXT);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);
        verticalLayout.setStyleName(CSS.APS_EDITING_TEXT + " " + CSS.APS_CONTENT_PANEL);

        this.idPanel = new Panel();
        this.idPanel.setCaption("Role id");
        this.idLabel = new Label("");
        this.idPanel.addComponent(this.idLabel);
        verticalLayout.addComponent(idPanel);

        this.descriptionPanel = new Panel();
        this.descriptionPanel.setCaption("Description");
        this.descriptionLabel = new Label("");
        this.descriptionPanel.addComponent(this.descriptionLabel);
        verticalLayout.addComponent(this.descriptionPanel);

        this.subRolesPanel = new Panel();
        this.subRolesPanel.setCaption("Sub roles");
        this.subRolesLabel = new Label("");
        this.subRolesPanel.addComponent(this.subRolesLabel);
        verticalLayout.addComponent(this.subRolesPanel);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        verticalLayout.addComponent(horizontalLayout);
        horizontalLayout.setSpacing(true);

        Button deleteButton = new Button("Delete");
        deleteButton.addListener(new Button.ClickListener() {
            /** Click handling. */
            @Override
            public void buttonClick(Button.ClickEvent event) {
                delete();
            }
        });
        horizontalLayout.addComponent(deleteButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.addListener(new Button.ClickListener() {
            /** Click handling. */
            @Override
            public void buttonClick(Button.ClickEvent event) {
                cancel();
            }
        });
        horizontalLayout.addComponent(cancelButton);

        setContent(verticalLayout);
    }

    //
    // Methods
    //

    /**
     * Sets the role to work on.
     *
     * @param role The role to work on.
     */
    public void setRole(RoleAdmin role) {
        this.role = role;
        this.idLabel.setValue(this.role.getId() + (role.isMasterRole() ? " (master)" : ""));
        this.descriptionLabel.setValue(this.role.getDescription());

        StringBuilder sb = new StringBuilder();
        String comma = "";
        for (Role subRole : role.getRoles()) {
            sb.append(comma);
            sb.append(subRole.getId());
            comma = ", ";
        }
        this.subRolesLabel.setValue(sb.toString());
    }

    /**
     * Deletes The current role.
     */
    private void delete() {
        try {
            this.userServiceAdmin.deleteRole(this.role);
            notifySuccess("Role deleted.", "Role '" + role.getId() + "' was deleted!");
            clearCenter();
            refreshDependentComponents();
        }
        catch (RuntimeException re) {
            notifyError("Failed to delete role!", "Failed to delete role: " + re.getMessage());
            getLogger().error("Failed to delete role!", re);
            throw re;
        }
    }

    /**
     * Cancels the delete.
     */
    private void cancel() {
        notifySuccess("Role delete cancelled.", "Deletion of role '" + this.role.getId() + "' was cancelled!");
        clearCenter();
        refreshDependentComponents();
    }

    /**
     * Implementation of EditorIdentifier.
     *
     * @return The id of this editor.
     */
    @Override
    public String getEditorId() {
        return this.role != null ? this.role.getId() : "";
    }
}

