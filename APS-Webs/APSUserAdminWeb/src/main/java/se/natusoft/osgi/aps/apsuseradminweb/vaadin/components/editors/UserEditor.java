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

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ServerSideCriterion;
import com.vaadin.ui.*;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserService;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserServiceAdmin;
import se.natusoft.osgi.aps.api.auth.user.model.Role;
import se.natusoft.osgi.aps.api.auth.user.model.User;
import se.natusoft.osgi.aps.api.auth.user.model.UserAdmin;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.EditorIdentifier;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.EditorPanel;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.HelpText;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.css.CSS;

import java.util.Properties;

/**
 * This is a component that edits a user. It is reusable within a session. The user to edit is
 * passed in setUser() by EditUserComponentHandler and DeleteUserComponentHandler.
 */
public class UserEditor extends EditorPanel implements EditorIdentifier {
    //
    // Constants
    //

    /** Vaadin data container key for table column 'Property' in 'propertiesEditor' table. */
    private static final String USER_PROPS_KEY = "Property";

    /** Vaadin data container key for table column 'Value' in 'propertiesEditor' table. */
    private static final String USER_PROPS_VALUE = "Value";

    /** Vaadin data container key for table column 'Role id' in 'availableRoles' and 'selectedRoles' tables. */
    private static final String ROLE_ID = "Role id";

    /** Vaadin data container key for table column 'Role Description' in 'availableRoles' and 'selectedRoles' tables. */
    private static final String ROLE_DESC = "Role Description";

    //
    // Private Members
    //

    /** The APS user service to get and persist user changes with. */
    private APSSimpleUserServiceAdmin userServiceAdmin = null;

    /** The currently edited user. */
    private UserAdmin user = null;

    /** Id provider for propertiesEditor data container. */
    private int propId = 0;

    /** This is set to the id of the selected property in propertiesEditor and is used to delete the selected property. */
    private Object selectedPropertyId = null;

    // GUI Components

    /** The text field showing the userid of the edited user. It is only changeable when creating a new user. */
    private TextField userId = null;

    /** The current auth for the user. */
    private PasswordField authCurrent = null;

    /** new auth for the user. */
    private PasswordField authNewOne = null;

    /** new auth for the user. */
    private PasswordField authNewTwo = null;

    /** A Table with editable=true containing the users properties. */
    private Table propertiesEditor = null;

    /** Provides help text for the properties editor. */
    private HelpText propsHelpText = null;

    /** Pressing this button adds an empty property to the propertiesEditor. */
    private Button plusButton = null;

    /** This is only enabled when a specific property have been selected in propertiesEditor and will remove that property. */
    private Button minusButton = null;

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

    /** Help text for the role tables. */
    private HelpText roleHelptext = null;

    /** Saves the current changes one way or another (save changed, create new user, delete user) */
    private Button saveButton = null;

    /**
     * This is set to true by DeleteUserComponentHandler and in this case this editor is used for deleting a user.
     * In the case of this being 'true' all components exception save & cancel buttons are disabled. The caption of
     * the save button is also changed to "Delete".
     * <p/>
     * Please note that when this gets set to 'true' it is automatically restored to false in save() and cancel().
     */
    private boolean forDelete = false;

    //
    // Constructors
    //

    /**
     * Creates a new UserEditor instance.
     *
     * @param userServiceAdmin The user admin service for editing the user.
     */
    public UserEditor(APSSimpleUserServiceAdmin userServiceAdmin) {
        this.userServiceAdmin = userServiceAdmin;

        this.setStyleName(CSS.APS_EDITING_TEXT);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);
        verticalLayout.setStyleName(CSS.APS_EDITING_TEXT + " " + CSS.APS_CONTENT_PANEL);

        // User id & Auth
        {
            HorizontalLayout firstRowLayout = new HorizontalLayout();
            firstRowLayout.setSpacing(true);

            this.userId = new TextField("User id");
            this.userId.setColumns(30);
            this.userId.setImmediate(true);
            this.userId.setEnabled(true);

            firstRowLayout.addComponent(this.userId);

            this.authCurrent = new PasswordField("Current auth");
            this.authCurrent.setColumns(30);
            this.authCurrent.setImmediate(true);
            this.authCurrent.setEnabled(true);

            firstRowLayout.addComponent(this.authCurrent);

            verticalLayout.addComponent(firstRowLayout);

            HorizontalLayout secondRowLayout = new HorizontalLayout();
            secondRowLayout.setSpacing(true);

            this.authNewOne = new PasswordField("New auth");
            this.authNewOne.setColumns(30);
            this.authNewOne.setImmediate(true);
            this.authNewOne.setEnabled(true);

            secondRowLayout.addComponent(this.authNewOne);

            this.authNewTwo = new PasswordField("New auth confirm");
            this.authNewTwo.setColumns(30);
            this.authNewTwo.setImmediate(true);
            this.authNewTwo.setEnabled(true);

            secondRowLayout.addComponent(this.authNewTwo);

            verticalLayout.addComponent(secondRowLayout);
        }

        // User Properties
        {
            this.propertiesEditor = new Table();
            this.propertiesEditor.setSelectable(true);
            this.propertiesEditor.setCaption("User properties");
            this.propertiesEditor.setPageLength(8);
            this.propertiesEditor.setEditable(true);
            this.propertiesEditor.setSizeFull();
            this.propertiesEditor.setColumnExpandRatio(USER_PROPS_KEY, 0.3f);
            this.propertiesEditor.setColumnExpandRatio(USER_PROPS_VALUE, 0.7f);
            this.propertiesEditor.setTableFieldFactory(new EditFieldFactory());
            this.propertiesEditor.addListener(new ItemClickEvent.ItemClickListener() {
                @Override
                public void itemClick(ItemClickEvent event) {
                    selectDeselectProperty(event.getItemId());
                }
            });
            verticalLayout.addComponent(this.propertiesEditor);

            // Buttons + info row
            {
                HorizontalLayout plusMinusRow = new HorizontalLayout();
                plusMinusRow.setSpacing(true);

                this.plusButton = new Button("+");
                this.plusButton.addListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        addProperty();
                    }
                });
                plusMinusRow.addComponent(this.plusButton);

                this.minusButton = new Button("-");
                this.minusButton.addListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        deleteProperty();
                    }
                });
                this.minusButton.setEnabled(false);
                plusMinusRow.addComponent(this.minusButton);

                this.propsHelpText = new HelpText("Press + to add new property, select property and press - to delete.");
                plusMinusRow.addComponent(this.propsHelpText);

                verticalLayout.addComponent(plusMinusRow);
            }
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
                    removeRole(itemId);
                }

                @Override
                public AcceptCriterion getAcceptCriterion() {
                    return new RoleAcceptCriterion(UserEditor.this.availableRoles);
                }
            });
            VerticalLayout availableRolesFrame = new VerticalLayout();
            availableRolesFrame.setMargin(false, true, false, false);
            availableRolesFrame.addComponent(this.availableRoles);
            rolesLayout.addComponent(availableRolesFrame);

            // Selected
            this.selectedRoles = new Table("Selected roles");
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
                    addRole(itemId);
                }

                @Override
                public AcceptCriterion getAcceptCriterion() {
                    return new RoleAcceptCriterion(UserEditor.this.selectedRoles);
                }
            });
            VerticalLayout selectedRolesFrame = new VerticalLayout();
            selectedRolesFrame.setMargin(false, false, false, true);
            selectedRolesFrame.addComponent(this.selectedRoles);
            rolesLayout.addComponent(selectedRolesFrame);

            rolesLayout.setExpandRatio(availableRolesFrame, 0.5f);
            rolesLayout.setExpandRatio(selectedRolesFrame, 0.5f);

            verticalLayout.addComponent(rolesLayout);

            this.roleHelptext = new HelpText("Drag and drop roles back and forth to set or remove a role.");
            verticalLayout.addComponent(this.roleHelptext);
        }

        // Save & Cancel
        {
            HorizontalLayout saveCancelLayout = new HorizontalLayout();
            saveCancelLayout.setSpacing(true);

            this.saveButton = new Button("Save");
            this.saveButton.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    save();
                }
            });
            saveCancelLayout.addComponent(saveButton);

            Button cancelButton = new Button("Cancel");
            cancelButton.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    cancel();
                }
            });
            saveCancelLayout.addComponent(cancelButton);

            verticalLayout.addComponent(saveCancelLayout);
        }

        setContent(verticalLayout);
    }

    //
    // Methods
    //

    /**
     * This gets called when the user selects a specific property in the propertiesEditor.
     * <p/>
     * Please note that this gets called on both 'select' and 'deselect'!
     *
     * @param propertyId The id of the selected property.
     */
    private void selectDeselectProperty(Object propertyId) {
        // I doubt that you will ever get here with a null propertyId, but to be 100% safe ...
        if (propertyId != null) {
            if (this.selectedPropertyId != null && propertyId.equals(this.selectedPropertyId)) {
                this.minusButton.setEnabled(false);
                this.selectedPropertyId = null;
            }
            else {
                this.selectedPropertyId = propertyId;
                this.minusButton.setEnabled(true);
            }
        }
        else {
            this.selectedPropertyId = null;
            this.minusButton.setEnabled(false);
        }
    }

    /**
     * This adds one new empty property to the propertiesEditor. This gets called by the 'plusButton' click event handler.
     */
    private void addProperty() {
        IndexedContainer ic = (IndexedContainer)this.propertiesEditor.getContainerDataSource();
        ic.addItem(this.propId++);
        this.propertiesEditor.refreshRowCache();
    }

    /**
     * This deletes the currently selected property in the 'propertiesEditor' if one is selected.
     * This gets called by the 'minusButton' click event handler.
     */
    private void deleteProperty() {
        // This should never be null if we get here, but what the heck lets be sure anyhow.
        if (this.selectedPropertyId != null) {
            IndexedContainer container = (IndexedContainer)this.propertiesEditor.getContainerDataSource();
            container.removeItem(this.selectedPropertyId);
            this.minusButton.setEnabled(false);
            this.selectedPropertyId = null;
        }
    }

    /**
     * Adds a role to the user. This is called by the DropHandler for 'selectedRoles' table when a new role
     * is dropped on it.
     *
     * @param itemId This identifies the item of the 'availableRoles' table that was dropped.
     */
    private void addRole(Object itemId) {
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
    private void removeRole(Object itemId) {
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
     * Saves the current changes. This only dispatches to doSave() or doDelete().
     */
    private void save() {
        try {
            if (this.forDelete) {
                doDelete();
            }
            else {
                doSave();
            }
        }
        catch (RuntimeException re) {
            notifyError("Failed to save user!", "Failed to save user: " + re.getMessage());
            getLogger().error("Failed to save user!", re);
            throw re;
        }
        finally {
            this.forDelete = false;
        }
    }

    /**
     * This does the actual saving of changed information.
     */
    private void doSave() {
        boolean newUser = false;
        if (this.user == null) {
            User checkUser = this.userServiceAdmin.getUser(this.userId.getValue().toString());
            if (checkUser != null) {
                notifyError("User already exists!", "The user with id '" + this.userId.getValue().toString() + "' already exists!");
                // In this case we want the admin user to be able to correct his/her mistake and try saving again.
                // We should therefore not call 'refreshDependentComponents()'!
                return;
            }
            else {
                if (
                        this.authNewOne.getValue() != null &&
                        this.authNewTwo.getValue() != null &&
                        this.authNewOne.getValue().equals(this.authNewTwo.getValue())
                ) {
                    this.user = this.userServiceAdmin.createUser(this.userId.getValue().toString());
                    this.userServiceAdmin.setUserAuthentication(this.user, this.authNewOne.getValue().toString());
                    newUser = true;
                }
                else {
                    notifyError("User not created!", "Password was not specified for user or the two entered passwords " +
                        "did not match!");
                    return;
                }

            }
        }

        // Properties
        Properties props = new Properties();
        IndexedContainer propsContainer = (IndexedContainer)this.propertiesEditor.getContainerDataSource();
        for (Object itemId : propsContainer.getItemIds()) {
            Item item = propsContainer.getItem(itemId);

            String propKey = item.getItemProperty(USER_PROPS_KEY).getValue().toString();
            String propValue = item.getItemProperty(USER_PROPS_VALUE).getValue().toString();

            if (propKey.trim().length() > 0 && propValue.trim().length() > 0) {
                props.put(propKey, propValue);
            }
        }
        this.user.setUserProperties(props);
        // Refresh the properties editor since empty lines can have been filtered.
        this.propertiesEditor.setContainerDataSource(createUserPropertiesContainer(this.user));

        // Roles
        IndexedContainer rolesContainer = (IndexedContainer)this.selectedRoles.getContainerDataSource();
        for (Object itemId : rolesContainer.getItemIds()) {
            Item item = rolesContainer.getItem(itemId);
            String roleId = item.getItemProperty(ROLE_ID).getValue().toString();

            if (!this.user.hasRole(roleId)) {
                Role role = this.userServiceAdmin.getRole(roleId);
                this.user.addRole(role);
            }
        }
        if (this.user.getRoles() != null) {
            for (Role role : this.user.getRoles()) {
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
                    this.user.removeRole(role);
                }
            }
        }

        this.userServiceAdmin.updateUser(this.user);

        if (newUser) {
            notifySuccess("User created!", "Created new user '" + this.user.getId() + "'!");
        }
        else {
            if (this.authNewOne.getValue().toString().length() > 0) {
                if (this.userServiceAdmin.authenticateUser(this.user, this.authCurrent.getValue(), APSSimpleUserService.AUTH_METHOD_PASSWORD)) {
                    if (this.authNewOne.getValue().equals(this.authNewTwo.getValue())) {
                        this.userServiceAdmin.setUserAuthentication(this.user, this.authNewOne.getValue().toString());
                    }
                    else {
                        notifyError("User partly saved!", "User data saved, but failed to change auth due to the two entered new values " +
                                "did not match!");
                    }
                }
                else {
                    notifyError("User partly saved!", "User data saved, but failed to change auth due to entered current auth was " +
                            "incorrect!");
                }
            }
            else {
                notifySuccess("User change saved!", "User change " + (this.user != null ? "for '" + this.user.getId() + "' " : "") + "saved!");
            }
        }

        // Note that we only do refreshDependendComponents() here, not clearCenter()! This however does not matter.
        // The notify above pulls focus from the selected menu entry which triggers the menu selection action with a null
        // selection (unselect) which restores the description page. I currently see no workaround to this problem!
        refreshDependentComponents();

    }

    /**
     * This deletes the currently edited user.
     */
    private void doDelete() {
        if (this.user != null) {
            this.userServiceAdmin.deleteUser(this.user);

            notifySuccess("Deleted user!", "Deleted user '" + this.user.getId() + "'!");
            clearCenter();
            refreshDependentComponents();
        }
    }

    /**
     * This cancels the edit operation ignoring all changes made.
     */
    private void cancel() {
        this.forDelete = false;
        notifySuccess("User edit cancelled", "User change " + (this.user != null ? "for '" + this.user.getId() + "' " : "") + "cancelled!");
        clearCenter();
        refreshDependentComponents();
    }

    /**
     * Sets the user to work on. This is called by EditUserComponentHandler and DeleteUserComponentHandler.
     *
     * @param user The user to work on.
     */
    public void setUser(UserAdmin user) {
        this.user = user;
        enableComponents(!this.forDelete);
        configureComponents();

        this.propertiesEditor.setContainerDataSource(createUserPropertiesContainer(user));
        this.availableRoles.setContainerDataSource(createAvailableRolesContainer(user));
        this.selectedRoles.setContainerDataSource(createUserRolesContainer(user));
    }

    /**
     * This sets up this editor for deleting a user. This is called by DeleteUserComponentHandler. This will disable
     * all components other than the buttons at the bottom. This so that the user can still see all informatoin about
     * the user he/she is about to delete.
     *
     * @param forDelete true or false, but in reality always true when this is called.
     */
    public void setForDelete(boolean forDelete) {
        this.forDelete = forDelete;
        configureComponents();
        enableComponents(!forDelete);
    }

    /**
     * This configures some of this user editors components depending on the state of the editor (new user, existing user,
     * deleting user).
     */
    private void configureComponents() {
        if (this.user != null) {
            setCaption((this.forDelete ? "Deleting " : "Editing ") + "user '" + user.getId() + "'");
            this.userId.setValue(user.getId());
            this.userId.setEnabled(false);
            this.authCurrent.setValue("");
            this.authCurrent.setEnabled(true);
            this.authNewOne.setValue("");
            this.authNewTwo.setValue("");
            this.saveButton.setCaption(this.forDelete ? "Delete" : "Save");
        }
        else {
            setCaption("Editing new user");
            this.userId.setValue("");
            this.userId.setEnabled(true);
            this.authCurrent.setValue("");
            this.authCurrent.setEnabled(false);
            this.authNewOne.setValue("");
            this.authNewTwo.setValue("");
            this.saveButton.setCaption("Create");
        }
    }

    /**
     * Enables or disables certain components of this editor. This is called by setUser() and setForDelete(). It is physically
     * possible to call these 2 methods in any order and both affects the state of the components managed by this method, which
     * is why both call this to make sure it is in the correct state.
     *
     * @param enable true or false.
     */
    private void enableComponents(boolean enable) {
        this.propertiesEditor.setEnabled(enable);
        this.plusButton.setEnabled(enable);
        this.availableRoles.setEnabled(enable);
        this.selectedRoles.setEnabled(enable);
        this.propsHelpText.setEnabled(enable);
        this.roleHelptext.setEnabled(enable);
        this.authCurrent.setEnabled(enable);
        this.authNewOne.setEnabled(enable);
        this.authNewTwo.setEnabled(enable);
    }

    /**
     * Factory method to create a Vaadin IndexedContainer containing the users all properties. This is
     * used by the 'propertiesEditor' Table.
     *
     * @param user The user to get the properties from.
     *
     * @return The created container.
     */
    private IndexedContainer createUserPropertiesContainer(User user) {
        IndexedContainer userPropsContainer = new IndexedContainer();
        userPropsContainer.addContainerProperty(USER_PROPS_KEY, String.class, "");
        userPropsContainer.addContainerProperty(USER_PROPS_VALUE, String.class, "");

        this.propId = 0;
        if (user != null) {
            Properties props = user.getUserProperties();
            for (String propName : props.stringPropertyNames()) {
                String propValue = props.getProperty(propName);

                Item item = userPropsContainer.addItem(this.propId++);
                item.getItemProperty(USER_PROPS_KEY).setValue(propName);
                item.getItemProperty(USER_PROPS_VALUE).setValue(propValue);
            }
        }

        return userPropsContainer;
    }

    /**
     * Factory method to create a Vaadin IndexedContainer containing all available roles minus the specified
     * users all roles. This is used by the 'availableRoles' Table.
     *
     * @param user The user whose roles should be subtracted for all roles.
     *
     * @return The created container.
     */
    private IndexedContainer createAvailableRolesContainer(User user) {
        IndexedContainer availRolesContainer = new IndexedContainer();
        availRolesContainer.addContainerProperty(ROLE_ID, String.class, "");
        availRolesContainer.addContainerProperty(ROLE_DESC, String.class, "");
        for (Role role : this.userServiceAdmin.getRoles()) {
            if (role.isMasterRole()) {
                Item item = availRolesContainer.addItem(role.getId());
                item.getItemProperty(ROLE_ID).setValue(role.getId());
                item.getItemProperty(ROLE_DESC).setValue(role.getDescription());
            }
        }

        // Remove all roles already set on the user.
        if (user != null) {
            for (Role role : ((UserAdmin)user).getRoles()) {
                availRolesContainer.removeItem(role.getId());
            }
        }

        availRolesContainer.sort(new Object[] { ROLE_ID }, new boolean[] { true });

        return availRolesContainer;
    }

    /**
     * Factory method to create a Vaadin IndexedContainer containing the specified users all roles.
     * This is used by the 'selectedRoles' Table.
     *
     * @param user The user to get the roles from.
     *
     * @return The created container.
     */
    private IndexedContainer createUserRolesContainer(User user) {
        IndexedContainer userRolesContainer = new IndexedContainer();
        userRolesContainer.addContainerProperty(ROLE_ID, String.class, "");
        userRolesContainer.addContainerProperty(ROLE_DESC, String.class, "");

        if (user != null) {
            for (Role role : ((UserAdmin)user).getRoles()) {
                Item item = userRolesContainer.addItem(role.getId());
                item.getItemProperty(ROLE_ID).setValue(role.getId());
                item.getItemProperty(ROLE_DESC).setValue(role.getDescription());
            }
        }

        userRolesContainer.sort(new Object[] { ROLE_ID }, new boolean[] { true });

        return userRolesContainer;
    }

    /**
     * Implementation of EditorIdentifier.
     *
     * @return An id for this editor.
     */
    @Override
    public String getEditorId() {
        return this.user != null ? this.user.getId() : "";
    }

    //
    // Inner Classes
    //

    /**
     * This is a TableFieldFactory used on 'propertiesEditor' to produce TextFields that fill the whole
     * table column. The TextFields created by the default factory does not!
     */
    private static class EditFieldFactory implements TableFieldFactory  {

        /**
         * Creates a field based on the Container, item id, property id and the
         * component responsible for displaying the field (most commonly
         * {@link com.vaadin.ui.Table}).
         *
         * @param container  the Container where the property belongs to.
         * @param itemId     the item Id.
         * @param propertyId the Id of the property.
         * @param uiContext  the component where the field is presented.
         * @return A field suitable for editing the specified data or null if the
         *         property should not be editable.
         */
        @Override
        public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
            TextField tf = new TextField();
            tf.setWidth("100%");
            return tf;
        }
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
    private static class RoleAcceptCriterion extends ServerSideCriterion  {
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

