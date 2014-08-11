/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         0.11.0
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
 *         2012-03-07: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import se.natusoft.osgi.aps.api.core.config.model.admin.*;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.ConfigEnvSelector;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.ConfigEnvSelector.ConfigEnvChangeEvent;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.ConfigEnvSelector.ConfigEnvChangeListener;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.ConfigNodeValuesEditor;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.NodeSelector;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.NodeSelector.NodeSelectedEvent;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor.NodeSelector.NodeSelectionListener;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.css.CSS;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.editor.ConfigNode;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException;
import se.natusoft.osgi.aps.tools.web.UserNotifier;
import se.natusoft.osgi.aps.tools.web.vaadin.components.HorizontalLine;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshable;

import java.util.List;

import static se.natusoft.osgi.aps.api.core.config.util.APSConfigStaticUtils.refToEditModel;

/**
 * This is a component that edits a configuration based on an APSConfigAdmin instance.
 * <p/>
 * This is actually both a component and a kind of controller. This sits on the
 * config service and 3 other relevant objects needed to edit the config. It is also
 * doing the editing. Any code that changes the configuration is done here. The
 * sub-components are pure GUI components some of which holds relevant data. All
 * trigger events that are taken care of here, or using data sources that are also
 * provided by this class.
 */
public class ConfigEditor extends Panel implements ComponentHandler, Refreshable {
    //
    // Private Members
    //

    /** The logger to log to. */
    private APSLogger logger = null;

    /** For getting valid and active config environments and updating edited config. */
    private APSConfigAdminService configAdminService = null;

    /** The real live config admin instance. */
    private APSConfigAdmin liveConfigAdmin = null;

    /** A clone of the real config admin instance for editing. */
    private APSConfigAdmin editedConfigAdmin = null;

    /** The currently edited node. */
    private ConfigNode currentConfigNode = null;

    /** For showing notifications to user. */
    private UserNotifier userNotifier = null;

    //----- GUI Components -----//

    /** Selects the config environment to edit for. Also provides the currently selected config environment. */
    private ConfigEnvSelector editForConfigEnvSelect = null;

    /** Selects config nodes of the selected config. */
    private NodeSelector nodeSelector = null;

    /** The panel containing the content editor. */
    private ConfigNodeValuesEditor configNodeValuesEditor = null;

    /** Button to add a node. */
    private Button addNodeButton = null;

    /** A button to remove a node. */
    private Button removeNodeButton = null;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEditor.
     *
     * @param configRef A reference representing an instance of the configuration to edit.
     * @param configAdmin The config admin api.
     * @param configAdminService The config admin service for getting config envs and updating edited configs.
     * @param logger The logger to log to.
     * @param userNotifier For sending notifications to user.
     */
    public ConfigEditor(APSConfigReference configRef, APSConfigAdmin configAdmin,
                        APSConfigAdminService configAdminService, APSLogger logger, UserNotifier userNotifier) {
        this.logger = logger;
        this.liveConfigAdmin = configAdmin;
        this.editedConfigAdmin = configAdmin.cloneConfig();
        this.configAdminService = configAdminService;
        this.currentConfigNode = new ConfigNode(configRef, false);
        this.userNotifier = userNotifier;

        setupGUI();
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
     * Refreshes all relevant components. This implements Refreshable.
     */
    @Override
    public void refresh() {
        setCaption("&nbsp;<b>Config ID:</b> " + this.currentConfigNode.getNodeConfigId());
        this.editForConfigEnvSelect.refreshData();

        this.configNodeValuesEditor.refreshData();
    }

    //
    // Private Methods
    //

    /**
     * Builds the gui of this component.
     *
     * The code in this method builds the base gui, the static parts that is only created once. It ends by calling
     * loadCurrentNodeData() to do a first time load of the root node of the ConfigNavigator.
     */
    private void setupGUI() {
        setSizeFull(); // <-- Important to limit size to browser window size.
        setStyleName(CSS.APS_CONTENT_PANEL);
        addStyleName(CSS.APS_CONFIGID_LABEL);

        VerticalLayout mainLayout = new VerticalLayout(); {
            mainLayout.setSpacing(true);
            mainLayout.setMargin(true);
            mainLayout.setHeight("100%"); // We want the layout to stretch the full height of the window.

            this.editForConfigEnvSelect = new ConfigEnvSelector();
            this.editForConfigEnvSelect.setDataSource(this.configAdminService.getConfigEnvAdmin());
            this.editForConfigEnvSelect.addListener(new ConfigEnvChangeListener() {
                @Override
                public void configEnvironmentChanged(ConfigEnvChangeEvent event) {
                    handleChangedConfigEnv(event.getSelectedConfigEnvironment());
                }
            });
            this.editForConfigEnvSelect.setSizeUndefined();
            mainLayout.addComponent(this.editForConfigEnvSelect);

            HorizontalLine hr = new HorizontalLine();
            mainLayout.addComponent(hr);

            HorizontalLayout contentLayout = new HorizontalLayout(); {
                contentLayout.setSpacing(true);
                contentLayout.setSizeFull();

                ConfigNode configNode = this.currentConfigNode;
                if (!configNode.getNodeChildren().isEmpty()) {

                    VerticalLayout nodesAndButtonsLayout = new VerticalLayout(); {
                        // Force width to be unspecified. Default is 100%. This makes the width
                        // adapt to the content.
                        nodesAndButtonsLayout.setWidth(null);
                        nodesAndButtonsLayout.setHeight("100%");
                        nodesAndButtonsLayout.setMargin(false);
                        nodesAndButtonsLayout.setSpacing(false);

                        this.nodeSelector = new NodeSelector(this.editedConfigAdmin);
                        // Make the NodeSelector whatever size the layout is.
                        this.nodeSelector.setHeight("100%");
                        this.nodeSelector.setWidth("100%");
                        this.nodeSelector.setDataSource(this.nodeSelectorDataSource);
                        this.nodeSelector.addListener(new NodeSelectionListener() {
                            @Override
                            public void nodeSelected(NodeSelectedEvent event) {
                                selectCurrentNode(event.getSelectedReference(), event.isIndexed());
                            }
                        });
                        nodesAndButtonsLayout.addComponent(this.nodeSelector);
                        // This makes the NodeSelector occupy whatever space is left
                        // after the buttons below it are rendered. The buttons must
                        // not have an ExpandRatio!
                        nodesAndButtonsLayout.setExpandRatio(this.nodeSelector, 1.0f);

                        HorizontalLayout buttonsLayout = new HorizontalLayout(); {
                            buttonsLayout.setMargin(false);
                            buttonsLayout.setSpacing(false);
                            // This will make this layout use whatever space is needed to fit
                            // the buttons.
                            buttonsLayout.setSizeUndefined();

                            this.addNodeButton = new Button(" + ");
                            this.addNodeButton.setEnabled(false);
                            this.addNodeButton.addClickListener(new ClickListener() {
                                @Override
                                public void buttonClick(ClickEvent event) {
                                    addNodeInstance();
                                }
                            });
                            buttonsLayout.addComponent(this.addNodeButton);

                            this.removeNodeButton = new Button(" - ");
                            this.removeNodeButton.setEnabled(false);
                            this.removeNodeButton.addClickListener(new ClickListener() {
                                @Override
                                public void buttonClick(ClickEvent event) {
                                    removeNodeInstance();
                                }
                            });
                            buttonsLayout.addComponent(this.removeNodeButton);
                        }
                        nodesAndButtonsLayout.addComponent(buttonsLayout);

                    }
                    contentLayout.addComponent(nodesAndButtonsLayout);
                    // Note: no expandRatio for this!
                }

                this.configNodeValuesEditor = new ConfigNodeValuesEditor(); {
                    this.configNodeValuesEditor.setWidth("100%");
                    this.configNodeValuesEditor.setHeight("100%");

                    this.configNodeValuesEditor.setDataSource(this.configNodeValueEditorDataSource);
                }
                contentLayout.addComponent(this.configNodeValuesEditor);
                // This makes the ConfigValuesEditor use whatever space is left to the right of the window.
                contentLayout.setExpandRatio(this.configNodeValuesEditor, 1.0f);
            }
            mainLayout.addComponent(contentLayout);
            // This will make all but contentLayout occupy as much space as they need and
            // contentLayout to take the rest. This works because we don't set any expandRatio
            // on any other component in mainLayout!
            mainLayout.setExpandRatio(contentLayout, 1.0f);

            HorizontalLine hr2 = new HorizontalLine();
            mainLayout.addComponent(hr2);

            HorizontalLayout saveCancelButtonsLayout = new HorizontalLayout(); {
                saveCancelButtonsLayout.setSpacing(true);

                Button saveButton = new Button("Save");
                saveButton.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        saveEdit();
                    }
                });
                saveCancelButtonsLayout.addComponent(saveButton);

                Button cancelButton = new Button("Cancel");
                cancelButton.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        cancelEdit();
                    }
                });
                saveCancelButtonsLayout.addComponent(cancelButton);
            }
            mainLayout.addComponent(saveCancelButtonsLayout);
        }

        setContent(mainLayout);

        refresh();
        if (this.nodeSelector != null) {
            this.nodeSelector.refreshData();
        }
    }

    /**
     * Handles configuration environment selection for config editing.
     *
     * @param selectedConfigEnv The config environment that has been selected.
     */
    private void handleChangedConfigEnv(APSConfigEnvironment selectedConfigEnv) {
        refresh();
        if (this.nodeSelector != null) {
            this.nodeSelector.refreshData();
        }

        this.userNotifier.info("Now editing for configuration environment", selectedConfigEnv.getName());
    }

    /**
     * Handles selection of a config node.
     *
     * @param configRef The reference to the config node selected.
     * @param indexed True if the node is indexed.
     */
    private void selectCurrentNode(APSConfigReference configRef, boolean indexed) {
        if (configRef == null) return;
        this.currentConfigNode.setCurrentNode(configRef, indexed);
        this.currentConfigNode.setDescription(configRef.getConfigValueEditModel().getDescription());

        enableDisableAddRemoveButtons(refToEditModel(configRef).isMany(), this.editedConfigAdmin.getListSize(configRef) > 0);

        refresh();
    }

    /**
     * Enables or disables the node add and remove buttons.
     *
     * @param isMany If true add button is enabled.
     * @param haveEntries If true the remove button is enabled.
     */
    private void enableDisableAddRemoveButtons(boolean isMany, boolean haveEntries) {
        this.addNodeButton.setEnabled(false);
        this.removeNodeButton.setEnabled(false);
        if (isMany) {
            this.addNodeButton.setEnabled(true);

            if (haveEntries) {
                this.removeNodeButton.setEnabled(true);
            }
        }
    }

    /**
     * Adds an instance to a node.
     */
    private void addNodeInstance() {
        APSConfigEditModel selectedNode = this.currentConfigNode.getCurrentNode();
        if (selectedNode.isMany()) {
            this.editedConfigAdmin.addConfigList(
                    this.currentConfigNode.getCurrentConfigReference()._(this.editForConfigEnvSelect.getSelectedConfigEnvironment())
            );
            this.nodeSelector.refreshData();
        }
        else {
            // This will only happen if add button is incorrectly shown when not a "many" node.
            this.userNotifier.error("BUG: Operation on bad node type!", "This node has only one static instance! Please report this!");
        }
    }

    /**
     * Removes an instance from a node.
     */
    private void removeNodeInstance() {
        APSConfigEditModel selectedNode = this.currentConfigNode.getCurrentNode();
        APSConfigReference selectedRef = this.currentConfigNode.getCurrentConfigReference();
        APSConfigEnvironment confEnv = this.editForConfigEnvSelect.getSelectedConfigEnvironment();
        int removeIndex = selectedRef.getIndex();

        if (selectedNode.isMany() && removeIndex >= 0 && this.editedConfigAdmin.getListSize(selectedRef) > 0) {
            this.editedConfigAdmin.removeConfigList(selectedRef._(confEnv));

            // If we removed the last index, then set the new index to the new last index,
            // or no index if empty.
            int size = this.editedConfigAdmin.getListSize(selectedRef._(confEnv));
            if (removeIndex >= size) {
                // If removeIndex is 0 and size is 0 (which is the requirement for getting here) then (removeIndex - 1) will be -1,
                // which is OK since an empty list have no indexes.
                this.currentConfigNode.setIndex(removeIndex - 1);
            }

            this.nodeSelector.refreshData();
        }
        else {
            this.userNotifier.error("Operation on bad node type!", "This node has only one static instance!");
        }
    }

    /**
     * Saves the edited configuration.
     */
    private void saveEdit() {
        try {
            this.configAdminService.updateConfiguration(this.editedConfigAdmin);
            this.editedConfigAdmin.sendConfigModifiedEvent();
            this.userNotifier.info("Saved!", "Saved config for '" + this.editedConfigAdmin.getConfigId() + "'!");
        }
        catch (APSNoServiceAvailableException nsae) {
            this.logger.error("Faled to save config for '" + this.editedConfigAdmin.getConfigId() + "'!", nsae);

            this.userNotifier.error("Save Faield!",
                    "The configuration service is currently not available on the server! Please try again later.");
        }
    }

    /**
     * Cancels the edit.
     */
    private void cancelEdit() {
        this.editedConfigAdmin = this.liveConfigAdmin.cloneConfig();

        refresh();
        if (this.nodeSelector != null) {
            this.nodeSelector.refreshData();
        }

        this.userNotifier.info("Cancelled!", "Cancelled changes for '" + this.editedConfigAdmin.getConfigId() + "'!");
    }

    //
    // Data Source Providers
    //

    /** The data source for the 'configNodeValuesEditor' instance. */
    private ConfigNodeValuesEditor.DataSource configNodeValueEditorDataSource = new ConfigNodeValuesEditor.DataSource() {

        /**
         * Returns the description of the node.
         */
        @Override
        public String getNodeDescription() {
            return ConfigEditor.this.currentConfigNode.getDescription();
        }

        /**
         * Returns true if this is a "many" node, in which case the index is valid.
         */
        @Override
        public boolean isManyNode() {
            return ConfigEditor.this.currentConfigNode.getCurrentNode().isMany();
        }

        /**
         * Returns the values of the current node.
         */
        @Override
        public List<APSConfigReference> getNodeValues() {
            return ConfigEditor.this.currentConfigNode.getNodeValues();
        }

        /**
         * Returns the indexed state of the node.
         */
        @Override
        public boolean isIndexed() {
            return ConfigEditor.this.currentConfigNode.isIndexed();
        }

        /**
         * Returns the current index if the current node is a "many" type node or -1 otherwise.
         */
        @Override
        public int getIndex() {
            return ConfigEditor.this.currentConfigNode.getIndex();
        }

        /**
         * Returns the current config environment.
         */
        @Override
        public APSConfigEnvironment getCurrentConfigEnvironment() {
            return ConfigEditor.this.editForConfigEnvSelect.getSelectedConfigEnvironment();
        }

        //--- ValueComponentListEditor.DataSource ---//

        /**
         * Returns the number of values.
         *
         * @param valueRef The config reference representing the value edited by this component instance.
         */
        @Override
        public int getSize(APSConfigReference valueRef) {
            return ConfigEditor.this.editedConfigAdmin.getListSize(valueRef._(getCurrentConfigEnvironment()));
        }

        /**
         * Returns the value at the specified index.
         *
         * @param valueRef The config reference representing the value edited by this component instance.
         */
        @Override
        public String getValue(APSConfigReference valueRef) {
            return ConfigEditor.this.editedConfigAdmin.getConfigValue(valueRef._(getCurrentConfigEnvironment()));
        }

        /**
         * Adds the specified value to the set of values.
         *
         * @param valueRef The config reference representing the value edited by this component instance.
         * @param value The value to add.
         */
        @Override
        public void addValue(APSConfigReference valueRef, String value) {
            ConfigEditor.this.editedConfigAdmin.addConfigValue(valueRef._(getCurrentConfigEnvironment()), value);
        }

        /**
         * Removes a value from the set of values.
         *
         * @param valueRef The config reference representing the value edited by this component instance.
         */
        @Override
        public void removeValue(APSConfigReference valueRef) {
            ConfigEditor.this.editedConfigAdmin.removeConfigValue(valueRef._(getCurrentConfigEnvironment()));
        }

        /**
         * Updates a value.
         *
         * @param valueRef The config reference representing the value edited by this component instance.
         * @param value The value to update with.
         */
        @Override
        public void updateValue(APSConfigReference valueRef, String value) {
            ConfigEditor.this.editedConfigAdmin.setConfigValue(valueRef._(getCurrentConfigEnvironment()), value);
        }
    };

    /** A data source for the 'nodeSelector' component. */
    private NodeSelector.DataSource nodeSelectorDataSource = new NodeSelector.DataSource() {

        /**
         * Returns the root node of the config model.
         */
        @Override
        public APSConfigReference getRootReference() {
            return ConfigEditor.this.editedConfigAdmin.createRootRef();
        }

        /**
         * Returns the count of instances for the specified model. The passed model must return true for isMany()!
         */
        @Override
        public int getInstanceCount(APSConfigReference configRef) {
            return ConfigEditor.this.editedConfigAdmin.getListSize(
                    configRef._(ConfigEditor.this.editForConfigEnvSelect.getSelectedConfigEnvironment())
            );
        }
    };
}
