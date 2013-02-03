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

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ServerSideCriterion;
import com.vaadin.ui.*;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserServiceAdmin;
import se.natusoft.osgi.aps.api.auth.user.model.Role;
import se.natusoft.osgi.aps.api.auth.user.model.RoleAdmin;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.EditorIdentifier;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.EditorPanel;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.HelpText;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.css.CSS;

import java.util.List;

/**
 * Edits a role. This component is reusable within a session. The role to edit is passed in setRole() by
 * EditRoleComponentHandler.
 */
public class RoleEditor extends EditorPanel implements EditorIdentifier {
    //
    // Constants
    //

    /** Vaadin data container key for table column 'Role id' in 'availableRoles' and 'selectedRoles' tables. */
    private static final String ROLE_ID = "Role id";

    /** Vaadin data container key for table column 'Role Description' in 'availableRoles' and 'selectedRoles' tables. */
    private static final String ROLE_DESC = "Role Description";

    //
    // Private Members
    //

    /** The user service for getting and changing roles with. */
    private APSSimpleUserServiceAdmin userServiceAdmin = null;

    /** The role being edited. */
    private RoleAdmin role = null;

    /** The role id */
    private TextField idTextField = null;

    /** Indicates a master role. */
    private CheckBox masterRole = null;

    /** The description of the role. */
    private TextArea descriptionTextArea = null;

    /**
     * This is a Table containing all available roles minus the roles of the current user. Roles are dragged and
     * dropped from this table to the 'selectedRoles' table to add a role to the user.
     */
    private Table availableRoles = null;

    /**
     * This is a table containing the current users roles. Roles are dragged and dropped from this table to the
     * 'availableRoles' table to remove a role from the user.
     */
    private Table selectedRoles = null;


    //
    // Constructors
    //

    /**
     * Creates a new RoleEditor instance.
     *
     * @param userServiceAdmin The user admin service for editing the role.
     */
    public RoleEditor(APSSimpleUserServiceAdmin userServiceAdmin) {
        this.userServiceAdmin = userServiceAdmin;

        this.setStyleName(CSS.APS_EDITING_TEXT);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);
        verticalLayout.setStyleName(CSS.APS_EDITING_TEXT + " " + CSS.APS_CONTENT_PANEL);

        // Role id, master and description.
        {
            HorizontalLayout horizLayout = new HorizontalLayout();
            horizLayout.setSpacing(true);

            this.idTextField = new TextField("Role id");
            this.idTextField.setColumns(30);
            this.idTextField.setImmediate(false);
            this.idTextField.setEnabled(true);
            horizLayout.addComponent(this.idTextField);

            this.masterRole = new CheckBox("Master Role");
            this.masterRole.setImmediate(false);
            this.masterRole.setEnabled(true);
            horizLayout.addComponent(this.masterRole);

            verticalLayout.addComponent(horizLayout);

            this.descriptionTextArea = new TextArea("Description of role");
            this.descriptionTextArea.setRows(3);
            this.descriptionTextArea.setColumns(60);
            this.descriptionTextArea.setImmediate(false);
            this.descriptionTextArea.setEnabled(true);
            verticalLayout.addComponent(this.descriptionTextArea);
        }

        // Roles
        {
            HorizontalLayout rolesLayout = new HorizontalLayout();
            rolesLayout.setSizeFull();

            // Available
            this.availableRoles = new Table("Available roles");
            this.availableRoles.setImmediate(true);
            this.availableRoles.setPageLength(10);
            this.availableRoles.setSortAscending(true);
            this.availableRoles.setSizeFull();
            this.availableRoles.setDragMode(Table.TableDragMode.ROW);
            this.availableRoles.setDropHandler(new DropHandler() {
                @Override
                public void drop(DragAndDropEvent event) {
                    DataBoundTransferable t = (DataBoundTransferable) event.getTransferable();
                    Object itemId = t.getItemId();
                    removeSubRole(itemId);
                }

                @Override
                public AcceptCriterion getAcceptCriterion() {
                    return new RoleAcceptCriterion(RoleEditor.this.availableRoles);
                }
            });
            VerticalLayout availableRolesFrame = new VerticalLayout();
            availableRolesFrame.setMargin(false, true, false, false);
            availableRolesFrame.addComponent(this.availableRoles);
            rolesLayout.addComponent(availableRolesFrame);

            // Selected
            this.selectedRoles = new Table("Selected sub roles of the role");
            this.selectedRoles.setImmediate(true);
            this.selectedRoles.setPageLength(10);
            this.selectedRoles.setSortAscending(true);
            this.selectedRoles.setSizeFull();
            this.selectedRoles.setDragMode(Table.TableDragMode.ROW);
            this.selectedRoles.setDropHandler(new DropHandler() {
                @Override
                public void drop(DragAndDropEvent event) {
                    DataBoundTransferable t = (DataBoundTransferable) event.getTransferable();
                    Object itemId = t.getItemId();
                    addSubRole(itemId);
                }

                @Override
                public AcceptCriterion getAcceptCriterion() {
                    return new RoleAcceptCriterion(RoleEditor.this.selectedRoles);
                }
            });
            VerticalLayout selectedRolesFrame = new VerticalLayout();
            selectedRolesFrame.setMargin(false, false, false, true);
            selectedRolesFrame.addComponent(this.selectedRoles);
            rolesLayout.addComponent(selectedRolesFrame);

            rolesLayout.setExpandRatio(availableRolesFrame, 0.5f);
            rolesLayout.setExpandRatio(selectedRolesFrame, 0.5f);

            verticalLayout.addComponent(rolesLayout);

            /* Help text for the role tables. */
            HelpText roleHelptext = new HelpText(
                    "Drag and drop roles back and forth to set or remove a role. Also note that it is fully possible to " +
                            "create circular role dependencies. Don't!"
            );
            verticalLayout.addComponent(roleHelptext);
        }

        // Save / Cancel
        {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            verticalLayout.addComponent(horizontalLayout);
            horizontalLayout.setSpacing(true);

            Button saveButton = new Button("Save");
            saveButton.addListener(new Button.ClickListener() {
                /** Click handling. */
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    save();
                }
            });
            horizontalLayout.addComponent(saveButton);

            Button cancelButton = new Button("Cancel");
            cancelButton.addListener(new Button.ClickListener() {
                /** Click handling. */
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    cancel();
                }
            });
            horizontalLayout.addComponent(cancelButton);
        }

        setContent(verticalLayout);
    }

    //
    // Methods
    //

    /**
     * Saves the role changes.
     */
    private void save() {
        try {
            String successMessage1;
            String successMessage2;
            if (this.role == null) {
                this.role = this.userServiceAdmin.createRole(this.idTextField.getValue().toString(),
                        this.descriptionTextArea.getValue().toString());
                this.role.setMasterRole((Boolean) this.masterRole.getValue());
                successMessage1 = "New role created.";
                successMessage2 = "Role '" + this.role.getId() + "' was created!";
            }
            else {
                this.role.setDescription(this.descriptionTextArea.getValue().toString());
                successMessage1 = "Role updated.";
                successMessage2 = "Role '" + role.getId() + "' was updated!";
            }

            // Roles
            IndexedContainer rolesContainer = (IndexedContainer)this.selectedRoles.getContainerDataSource();
            for (Object itemId : rolesContainer.getItemIds()) {
                Item item = rolesContainer.getItem(itemId);
                String roleId = item.getItemProperty(ROLE_ID).getValue().toString();

                if (!this.role.hasRole(roleId)) {
                    Role role = this.userServiceAdmin.getRole(roleId);
                    this.role.addRole(role);
                }
            }
            List<Role> roles = this.role.getRoles();
            if (roles != null) {
                // TODO: We get a ConcurrentModificationException here when there are no selected roles!
                for (Role role : roles) {
                    boolean roleStillValid = false;
                    for (Object itemId : rolesContainer.getItemIds()) {
                        Item item = rolesContainer.getItem(itemId);
                        String roleId = item.getItemProperty(ROLE_ID).getValue().toString();
                        if (roleId.equals(role.getId())) {
                            roleStillValid = true;
                            break;
                        }
                    }

                    if (!roleStillValid) {
                        this.role.removeRole(role);
                    }
                }
            }

            this.userServiceAdmin.updateRole(this.role);

            notifySuccess(successMessage1, successMessage2);
            refreshDependentComponents();
        }
        catch (RuntimeException re) {
            notifyError("Failed saving role!", "Falied to save role:" + re.getMessage());
            getLogger().error("Failed to save role!", re);
            throw re;
        }
    }

    /**
     * Cancels all changes.
     */
    private void cancel() {
        clearCenter();
        refreshDependentComponents();
        notifySuccess("Role change cancelled", "Role change " + (this.role != null ? "for '" + this.role.getId() + "' " : "") + "cancelled!");
    }

    /**
     * Sets the role to work on.
     *
     * @param role The role to edit.
     */
    public void setRole(RoleAdmin role) {
        this.role = role;
        if (role != null) {
            setCaption("Editing role '" + role.getId() + "'");
            this.idTextField.setValue(this.role.getId());
            this.idTextField.setEnabled(false);
            this.masterRole.setValue(role.isMasterRole());
            this.masterRole.setEnabled(false);
            this.descriptionTextArea.setValue(this.role.getDescription());
            this.availableRoles.setContainerDataSource(createAvailableRolesContainer(role));
            this.selectedRoles.setContainerDataSource(createSubRolesContainer(role));
        }
        else {
            setCaption("Editing new role");
            this.idTextField.setValue("");
            this.idTextField.setEnabled(true);
            this.masterRole.setValue(false);
            this.masterRole.setEnabled(true);
            this.descriptionTextArea.setValue("");
            this.availableRoles.setContainerDataSource(createAvailableRolesContainer(null));
            this.selectedRoles.setContainerDataSource(createSubRolesContainer(null));
        }
    }

    /**
     * Implementation of EditorIdentifier.
     *
     * @return The id for this editor.
     */
    @Override
    public String getEditorId() {
        return this.role != null ? this.role.getId() : "";
    }

    /**
     * Adds a role to the user. This is called by the DropHandler for 'selectedRoles' table when a new role
     * is dropped on it.
     *
     * @param itemId This identifies the item of the 'availableRoles' table that was dropped.
     */
    private void addSubRole(Object itemId) {
        if (itemId != null) {
            Item item = this.availableRoles.getItem(itemId);
            String roleId = (String)item.getItemProperty(ROLE_ID).getValue();
            String roleDesc = (String)item.getItemProperty(ROLE_DESC).getValue();
            this.availableRoles.removeItem(itemId);

            item = this.selectedRoles.addItem(roleId);
            item.getItemProperty(ROLE_ID).setValue(roleId);
            item.getItemProperty(ROLE_DESC).setValue(roleDesc);

            IndexedContainer selectedRolesContainer = (IndexedContainer)this.selectedRoles.getContainerDataSource();
            selectedRolesContainer.sort(new Object[] { ROLE_ID }, new boolean[] { true });
        }
    }

    /**
     * Removes a role from the user. This is called by the DropHandler for 'availableRoles' table when a non contained
     * role is dropped on it from the 'selectedRoles' table. Please note that a role only exists in one or the other
     * of the two role tables.
     *
     * @param itemId This identifies the item of the 'selectedRoles' table that was dropped.
     */
    private void removeSubRole(Object itemId) {
        if (itemId == null) {
            itemId = this.selectedRoles.getValue();
        }

        if (itemId != null) {
            Item item = this.selectedRoles.getItem(itemId);
            String roleId = (String)item.getItemProperty(ROLE_ID).getValue();
            String roleDesc = (String)item.getItemProperty(ROLE_DESC).getValue();
            this.selectedRoles.removeItem(itemId);

            item = this.availableRoles.addItem(roleId);
            item.getItemProperty(ROLE_ID).setValue(roleId);
            item.getItemProperty(ROLE_DESC).setValue(roleDesc);

            IndexedContainer availRolesContainer = (IndexedContainer)this.availableRoles.getContainerDataSource();
            availRolesContainer.sort(new Object[] { ROLE_ID }, new boolean[] { true });

        }
    }

    /**
     * Factory method to create a Vaadin IndexedContainer containing all available roles minus the specified
     * role:s all sub roles. This is used by the 'availableRoles' Table.
     *
     * @param role The user whose roles should be subtracted for all roles.
     *
     * @return The created container.
     */
    private IndexedContainer createAvailableRolesContainer(RoleAdmin role) {
        IndexedContainer availRolesContainer = new IndexedContainer();
        availRolesContainer.addContainerProperty(ROLE_ID, String.class, "");
        availRolesContainer.addContainerProperty(ROLE_DESC, String.class, "");
        for (Role subrole : this.userServiceAdmin.getRoles()) {
            if (!subrole.isMasterRole()) { // We can only add sub roles to other roles.
                Item item = availRolesContainer.addItem(subrole.getId());
                item.getItemProperty(ROLE_ID).setValue(subrole.getId());
                item.getItemProperty(ROLE_DESC).setValue(subrole.getDescription());
            }
        }

        // Remove the sub roles already set on the role.
        if (role != null) {
            for (Role subrole : role.getRoles()) {
                availRolesContainer.removeItem(subrole.getId());
            }
        }

        availRolesContainer.sort(new Object[] { ROLE_ID }, new boolean[] { true });

        return availRolesContainer;
    }

    /**
     * Factory method to create a Vaadin IndexedContainer containing the specified roles all sub roles.
     * This is used by the 'selectedRoles' Table.
     *
     * @param role The user to get the roles from.
     *
     * @return The created container.
     */
    private IndexedContainer createSubRolesContainer(RoleAdmin role) {
        IndexedContainer userRolesContainer = new IndexedContainer();
        userRolesContainer.addContainerProperty(ROLE_ID, String.class, "");
        userRolesContainer.addContainerProperty(ROLE_DESC, String.class, "");

        if (role != null) {
            for (Role subrole : role.getRoles()) {
                Item item = userRolesContainer.addItem(subrole.getId());
                item.getItemProperty(ROLE_ID).setValue(subrole.getId());
                item.getItemProperty(ROLE_DESC).setValue(subrole.getDescription());
            }
        }

        userRolesContainer.sort(new Object[] { ROLE_ID }, new boolean[] { true });

        return userRolesContainer;
    }

    /**
     * This is the "accept criterion" used by both DropHandler for 'availableRoles' and 'selectedRoles'.
     * It accepts any drop of a value that does not already exists in the target. This is kind of redundant
     * since the contents of the two above mentioned Tables are (should be) unique in conjunction. A role
     * can only be in one or the other never both at the same time.
     * <p/>
     * Vaadin however refused to do any dropping at all without this (I tried to keep it on the client
     * side until dropped, but failed). I'm hoping to resolve this and remove this inner class.
     */
    private static class RoleAcceptCriterion extends ServerSideCriterion {
        //
        // Private Members
        //

        /** The Table this criterion is for. */
        private Table sourceTable = null;

        //
        // Constructors
        //

        /**
         * Creates a new RoleAcceptCriterion.
         *
         * @param sourceTable The Table using this criterion.
         */
        public RoleAcceptCriterion(Table sourceTable) {
            this.sourceTable = sourceTable;
        }

        //
        // Methods
        //

        /**
         * Validates the data in event to be appropriate for the
         * {@link com.vaadin.event.dd.DropHandler#drop(com.vaadin.event.dd.DragAndDropEvent)} method.
         * <p/>
         * Note that even if your criterion is validated on client side, you should
         * always validate the data on server side too.
         *
         * @param dragEvent
         * @return
         */
        @Override
        public boolean accept(DragAndDropEvent dragEvent) {
            DataBoundTransferable t = (DataBoundTransferable)dragEvent.getTransferable();
            Object itemId = t.getItemId();
            IndexedContainer container = (IndexedContainer)this.sourceTable.getContainerDataSource();
            return !container.containsId(itemId);
        }
    }

}

