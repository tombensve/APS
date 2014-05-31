/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         0.10.0
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
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
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
     * @param configEditModel The model representing the configuration to edit.
     * @param configAdmin The config admin api.
     * @param configAdminService The config admin service for getting config envs and updating edited configs.
     * @param logger The logger to log to.
     * @param userNotifier For sending notifications to user.
     */
    public ConfigEditor(APSConfigEditModel configEditModel, APSConfigAdmin configAdmin,
                        APSConfigAdminService configAdminService, APSLogger logger, UserNotifier userNotifier) {
        this.logger = logger;
        this.liveConfigAdmin = configAdmin;
        this.editedConfigAdmin = configAdmin.cloneConfig();
        this.configAdminService = configAdminService;
        this.currentConfigNode = new ConfigNode(configEditModel);
        this.userNotifier = userNotifier;

        setupGUI();
    }
    
    //
    // Methods
    //

    /**
     * @return The component that should handle the item.
     */
    @Override
    public AbstractComponent getComponent() {
        return this;
    }

    /**
     * Builds the gui of this component.
     * <p/>
     * The code in this method builds the base gui, the static parts that is only created once. It ends by calling
     * loadCurrentNodeData() to do a first time load of the root node of the ConfigNavigator.
     */
    private void setupGUI() {
        setStyleName(CSS.APS_CONFIGID_LABEL);
        setSizeFull();

        VerticalLayout mainLayout = new VerticalLayout(); {
            mainLayout.setSpacing(true);
            mainLayout.setMargin(true);
            mainLayout.setStyleName(CSS.APS_CONTENT_PANEL);
            mainLayout.setHeight(100, Unit.PERCENTAGE);

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

                ConfigNode configNode = new ConfigNode(this.editedConfigAdmin.getConfigModel());
                if (!configNode.getNodeChildren().isEmpty()) {

                    VerticalLayout nodesAndButtonsLayout = new VerticalLayout(); {
                        nodesAndButtonsLayout.setWidth(null);
                        nodesAndButtonsLayout.setHeight("100%");
                        nodesAndButtonsLayout.setMargin(false);
                        nodesAndButtonsLayout.setSpacing(false);

                        this.nodeSelector = new NodeSelector();
                        this.nodeSelector.setHeight("100%");
                        this.nodeSelector.setWidth(null);
//                        this.nodeSelector.setScrollable(true);
                        this.nodeSelector.setDataSource(this.nodeSelectorDataSource);
                        this.nodeSelector.addListener(new NodeSelectionListener() {
                            @Override
                            public void nodeSelected(NodeSelectedEvent event) {
                                selectCurrentNode(event.getSelectedModel(), event.getIndex());
                            }
                        });
                        nodesAndButtonsLayout.addComponent(this.nodeSelector);

                        HorizontalLayout buttonsLayout = new HorizontalLayout(); {
                            buttonsLayout.setMargin(false);
                            buttonsLayout.setSpacing(false);
                            buttonsLayout.setHeight("100%");

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
                }

                this.configNodeValuesEditor = new ConfigNodeValuesEditor(); {
                    this.configNodeValuesEditor.setWidth("100%");
                    this.configNodeValuesEditor.setHeight("100%");

                    this.configNodeValuesEditor.setDataSource(this.configNodeValueEditorDataSource);
                }
                contentLayout.addComponent(this.configNodeValuesEditor);
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
     * Refreshes all relevant components. This implements Refreshable.
     */
    @Override
    public void refresh() {
        setCaption("&nbsp;<b>Config ID:</b> " + this.currentConfigNode.getNodeConfigId());
        this.editForConfigEnvSelect.refreshData();

        this.configNodeValuesEditor.refreshData();
    }

    //
    // Event Handlers
    //

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
     * @param configNode The config node selected.
     * @param index The possible index of a "many" node or -1.
     */
    private void selectCurrentNode(APSConfigEditModel configNode, int index) {
        this.currentConfigNode.setCurrentNode(configNode, index);
        this.currentConfigNode.setDescription(configNode.getDescription());

        if (index >= 0) {

            // We need to get a correctly indexed node! Please note that the returned APSConfigEditModel
            // will return false for isMany()! This because it is now representing a specific entry.
            APSConfigEditModel configInstanceNode = this.editedConfigAdmin.getConfigListEntry(configNode, index,
                    this.editForConfigEnvSelect.getSelectedConfigEnvironment());

            this.currentConfigNode.setCurrentInstanceNode(configInstanceNode);
            this.currentConfigNode.setDescription(configInstanceNode.getDescription());

        }

        enableDisableAddRemoveButtons(configNode, index);

        refresh();
    }

    /**
     * Enables or disables the node add and remove buttons depending on the specified node and index.
     *
     * @param configNode The config node selected.
     * @param index The possible index of a "many" node or -1.
     */
    private void enableDisableAddRemoveButtons(APSConfigEditModel configNode, int index) {
        this.addNodeButton.setEnabled(false);
        this.removeNodeButton.setEnabled(false);
        if (configNode.isMany()) {
            this.addNodeButton.setEnabled(true);

            if (index >= 0) {
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
            this.editedConfigAdmin.createConfigListEntry(selectedNode,
                    this.editForConfigEnvSelect.getSelectedConfigEnvironment());
            this.nodeSelector.refreshData();
        }
        else {
            this.userNotifier.error("Operation on bad node type!", "This node has only one static instance!");
        }
    }

    /**
     * Removes an instance from a node.
     */
    private void removeNodeInstance() {
        APSConfigEditModel selectedNode = this.currentConfigNode.getCurrentNode();
        APSConfigEnvironment confEnv = this.editForConfigEnvSelect.getSelectedConfigEnvironment();
        int removeIndex = this.currentConfigNode.getIndex();

        if (selectedNode.isMany() && removeIndex >= 0 && this.editedConfigAdmin.getSize(selectedNode, confEnv) > 0) {
            this.editedConfigAdmin.removeConfigListEntry(
                    selectedNode,
                    this.currentConfigNode.getIndex(),
                    confEnv
            );

            // If we removed the last index, then set the new index to the new last index,
            // or no index if empty.
            int size = this.editedConfigAdmin.getSize(selectedNode, confEnv);
            if (removeIndex >= size) {
                if (size > 0) {
                    this.currentConfigNode.setIndex(removeIndex - 1);
                }
                else {
                    this.currentConfigNode.setIndex(ConfigNode.NO_INDEX);
                }
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
    // Data Sources
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
        public List<APSConfigValueEditModel> getNodeValues() {
            // Please note that after the setCurrentInstanceNode(...) in selectCurrentNode(...)
            // the getNodeValues() below will return nodes that are correctly indexed and can
            // be used to set and get values.
            return ConfigEditor.this.currentConfigNode.getNodeValues();
        }

        /**
         * Returns the current index if the current node is a "many" type node or -1 otherwise.
         */
        @Override
        public int getIndex() {
            return ConfigEditor.this.currentConfigNode.getIndex();
        }

        /**
         * Returns the value represented by the specified value edit model.
         *
         * @param valueEditModel The value edit model to get real value for.
         */
        @Override
        public String getValue(APSConfigValueEditModel valueEditModel) {
            return ConfigEditor.this.editedConfigAdmin.getConfigValue(valueEditModel, getCurrentConfigEnvironment());
        }

        /**
         * Updates the value represented by the value edit model to the specified value.
         *
         * @param valueEditModel The value edit model representing the value to update.
         * @param value The new value to update with.
         */
        @Override
        public void updateValue(APSConfigValueEditModel valueEditModel, String value) {
            ConfigEditor.this.editedConfigAdmin.setConfigValue(valueEditModel, value, getCurrentConfigEnvironment());
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
         * @param valueEditModel The model representing the value edited by this component instance.
         */
        @Override
        public int getSize(APSConfigValueEditModel valueEditModel) {
            return ConfigEditor.this.editedConfigAdmin.getSize(valueEditModel, getCurrentConfigEnvironment());
        }

        /**
         * Returns the value at the specified index.
         *
         * @param valueEditModel The model representing the value edited by this component instance.
         * @param index The index to get the value for.
         */
        @Override
        public String getValue(APSConfigValueEditModel valueEditModel, int index) {
            return ConfigEditor.this.editedConfigAdmin.getConfigValue(valueEditModel, index, getCurrentConfigEnvironment());
        }

        /**
         * Adds the specified value to the set of values.
         *
         * @param valueEditModel The model representing the value edited by this component instance.
         * @param value The value to add.
         */
        @Override
        public void addValue(APSConfigValueEditModel valueEditModel, String value) {
            ConfigEditor.this.editedConfigAdmin.addConfigValue(valueEditModel, value, getCurrentConfigEnvironment());
        }

        /**
         * Removes a value from the set of values.
         *
         * @param valueEditModel The model representing the value edited by this component instance.
         * @param index The index of the value to remove.
         */
        @Override
        public void removeValue(APSConfigValueEditModel valueEditModel, int index) {
            ConfigEditor.this.editedConfigAdmin.removeConfigValue(valueEditModel, index, getCurrentConfigEnvironment());
        }

        /**
         * Updates a value.
         *
         * @param valueEditModel The model representing the value edited by this component instance.
         * @param value The value to update with.
         * @param index The index to update.
         */
        @Override
        public void updateValue(APSConfigValueEditModel valueEditModel, String value, int index) {
            ConfigEditor.this.editedConfigAdmin.setConfigValue(valueEditModel, index, value, getCurrentConfigEnvironment());
        }
    };

    /** A data source for the 'nodeSelector' component. */
    private NodeSelector.DataSource nodeSelectorDataSource = new NodeSelector.DataSource() {

        /**
         * Returns the root node of the config model.
         */
        @Override
        public APSConfigEditModel getRootModel() {
            return ConfigEditor.this.editedConfigAdmin.getConfigModel();
        }

        /**
         * Returns the count of instances for the specified model. The passed model must return true for isMany()!
         */
        @Override
        public int getInstanceCount(APSConfigEditModel configEditModel) {
            return ConfigEditor.this.editedConfigAdmin.getSize(
                    configEditModel,
                    ConfigEditor.this.editForConfigEnvSelect.getSelectedConfigEnvironment()
            );
        }
    };
}
