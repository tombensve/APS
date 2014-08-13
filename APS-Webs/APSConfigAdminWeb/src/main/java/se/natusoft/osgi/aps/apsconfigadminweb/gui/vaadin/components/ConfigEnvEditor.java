/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         1.0.0
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-02-26: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService.APSConfigEnvAdmin;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.css.CSS;
import se.natusoft.osgi.aps.tools.web.UserNotifier;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.MenuActionExecutor;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshables;

/**
 * Edits config environments.
 */
public class ConfigEnvEditor extends Panel implements ComponentHandler, MenuActionExecutor {
    //
    // Constants
    //
    
    /** Indicates editing functionality. */
    public static final int EDIT_ACTION = 1;
    
    /** Indicates delete functionality. */
    public static final int DELETE_ACTION = 2;

    /** Indicates change active environment. No GUI is needed for this. */
    public static final int CHANGE_ACTIVE_ACTION = 3;
    
    //
    // Private Members
    //

    /** The configuration admin (part of APSConfigAdminService) to use for adding/removing configuration environments. */
    private APSConfigEnvAdmin configEnvAdmin = null;

    /** The edited config environment. */
    private APSConfigEnvironment configEnv = null;

    /** The name of the config environment. */
    private TextField nameTextField = null;

    /** The description of the config environment. */
    private TextArea descriptionTextArea = null;
    
    /** The original name of the config env. Needed for when name is changed. */
    private String origName = null;

    /** The components that needs to be refreshed due to changes in this one. */
    private Refreshables refreshables = null;

    /** The action to perform. */
    private int action  = 0;

    /** For showing notifications to the user. */
    private UserNotifier userNotifier = null;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEnvEditor.
     * 
     * @param configEnv The edited config environment.
     * @param configEnvAdmin The configuration admin to use for adding/removing configuration environments.
     * @param refreshables The Refreshable components to refresh after edit.
     * @param action The action to perform.
     * @param userNotifier For showing notifications to the user.
     */
    public ConfigEnvEditor(APSConfigEnvironment configEnv, APSConfigEnvAdmin configEnvAdmin, Refreshables refreshables, int action, UserNotifier userNotifier) {
        this.configEnvAdmin = configEnvAdmin;
        this.configEnv = configEnv;
        this.refreshables = refreshables;
        this.action = action;
        this.userNotifier = userNotifier;
        
        if (action == EDIT_ACTION) {
            initForEdit();
        }
        else if (action == DELETE_ACTION && configEnv != null) {
            initForDelete();
        }
    }

    //
    // Public Methods
    //

    /**
     * @return The component that should handle the item.
     */
    @Override
    public AbstractComponent getComponent() {
        return this;
    }

    /**
     * Executes the menu action.
     */
    @Override
    public void executeMenuAction() {
        if (this.action == CHANGE_ACTIVE_ACTION) {
            this.configEnvAdmin.selectActiveConfigEnvironment(this.configEnv);

            notify("Changed '" + this.configEnv.getName() + "' to active configuration environment!");

            this.refreshables.refresh();
        }

    }

    //
    // Private Methods
    //

    /**
     * Setup for editing.
     */
    private void initForEdit() {
        setStyleName(CSS.APS_CONTENT_PANEL);
        addStyleName(CSS.APS_CONFIGID_LABEL);

        if (this.configEnv == null) {
            setCaption("Creating new config environment");
        }
        else {
            this.origName = this.configEnv.getName();
            setCaption("Editing config environment '" + this.configEnv.getName() + "'");
        }

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addStyleName(CSS.APS_EDITING_TEXT);
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);

        this.nameTextField = new TextField("Config environment name");
        if (configEnv != null) { this.nameTextField.setValue(this.configEnv.getName()); }
        this.nameTextField.setColumns(30);
        this.nameTextField.setImmediate(true);
        this.nameTextField.setEnabled(!(configEnv != null && configEnv.equals(this.configEnvAdmin.getActiveConfigEnvironment())));

        verticalLayout.addComponent(this.nameTextField);

        this.descriptionTextArea = new TextArea("Description of config environment.");
        this.descriptionTextArea.setRows(3);
        this.descriptionTextArea.setColumns(60);
        this.descriptionTextArea.setImmediate(true);
        if (configEnv != null) { this.descriptionTextArea.setValue(this.configEnv.getDescription()); }
        verticalLayout.addComponent(this.descriptionTextArea);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        verticalLayout.addComponent(horizontalLayout);
        horizontalLayout.setSpacing(true);

        Button saveButton = new Button("Save");
        saveButton.addClickListener(new ClickListener() {
            /** Click handling. */
            @Override
            public void buttonClick(ClickEvent event) {
                saveConfigEnv();
            }
        });
        horizontalLayout.addComponent(saveButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(new ClickListener() {
            /** Click handling. */
            @Override
            public void buttonClick(ClickEvent event) {
                cancel();
            }
        });
        horizontalLayout.addComponent(cancelButton);

        setContent(verticalLayout);
        
    }

    /**
     * Setup for deleting.
     */
    private void initForDelete() {
        setCaption("Deleting config environment '" + this.configEnv.getName() + "'");
        setStyleName("aps-editing-text");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);
        verticalLayout.setStyleName(CSS.APS_EDITING_TEXT + " " + CSS.APS_CONTENT_PANEL);
        
        Label nameLabel = new Label("Config environment name:");
        verticalLayout.addComponent(nameLabel);
        Label nameValue = new Label(this.configEnv.getName());
        Panel confNamePanel = new Panel(nameValue);
        verticalLayout.addComponent(confNamePanel);

        Label descLabel = new Label("Description of config environment:");
        verticalLayout.addComponent(descLabel);
        Label descValue = new Label(this.configEnv.getDescription());
        Panel confNameDescPanel = new Panel(descValue);
        verticalLayout.addComponent(confNameDescPanel);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setMargin(false);
        horizontalLayout.setSpacing(true);
        verticalLayout.addComponent(horizontalLayout);
        
        Button deleteButton = new Button("Delete");
        deleteButton.addClickListener(new ClickListener() {
            /** click handling. */
            @Override
            public void buttonClick(ClickEvent event) {
                deleteConfigEnv();
            }
        });
        if (this.configEnv.equals(this.configEnvAdmin.getActiveConfigEnvironment())) {
            deleteButton.setEnabled(false);
        }
        horizontalLayout.addComponent(deleteButton);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(new ClickListener() {
            /** Click handling. */
            @Override
            public void buttonClick(ClickEvent event) {
                cancel();
            }
        });
        horizontalLayout.addComponent(cancelButton);

        setContent(verticalLayout);

    }

    /**
     * Shows notification.
     *
     * @param message The notification message.
     */
    private void notify(String message) {
        this.userNotifier.info("Config environment changed", message);
    }

    /**
     * Saves the current config env.
     */
    private void saveConfigEnv() {
        String name = this.nameTextField.getValue();
        String description = this.descriptionTextArea.getValue();

        if (this.origName != null) {
            APSConfigEnvironment current = this.configEnvAdmin.getConfigEnvironmentByName(this.origName);
            this.configEnvAdmin.removeConfigEnvironment(current);
        }
        this.configEnvAdmin.addConfigEnvironment(name, description);

        notify("Saved '" + name + "'");
        this.refreshables.refresh();
    }

    /**
     * Deletes the current config env.
     */
    private void deleteConfigEnv() {
        this.configEnvAdmin.removeConfigEnvironment(this.configEnv);

        notify("Configuration environment '" + this.configEnv + "' was deleted!");
        this.refreshables.refresh();
    }

    /**
     * Cancel an initiated edit or delete.
     */
    private void cancel() {
        notify("Cancelled!");
        this.refreshables.refresh();
    }

}
