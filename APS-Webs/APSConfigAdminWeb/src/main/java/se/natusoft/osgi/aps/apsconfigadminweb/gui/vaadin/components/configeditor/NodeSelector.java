/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         0.9.1
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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-04-17: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.configeditor;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.css.CSS;
import se.natusoft.osgi.aps.tools.models.ID;
import se.natusoft.osgi.aps.tools.models.IntID;
import se.natusoft.osgi.aps.tools.web.vaadin.models.HierarchicalModel;

import java.util.LinkedList;
import java.util.List;

/**
 * This renders a three of config nodes and config node instances that can be selected.
 * It will trigger an event for each selection.
 */
public class NodeSelector extends Panel implements ItemDescriptionGenerator {
    //
    // Private Members
    //

    /** An extended model also mapping a data object to each item. */
    private HierarchicalModel<NodeData> configNodeModel = null;

    /** Our data source. */
    private DataSource dataSource = null;

    /** The listeners on this component. */
    private List<NodeSelectionListener> nodeSelectionListeners = new LinkedList<NodeSelectionListener>();

    /**
     * A NodeData object representing the currently selected tree node.
     * <p/>
     * <b>PLEASE NOTE: </b>
     * This is data representing a node in the tree that was selected. This is set in itemSelected(...) below.
     * The content of this tree is loaded from a model that is externally modified! That means that this held data
     * might get stale.
     * <p/>
     * A new HierarchicalContainer is built and set with setContainerDataSource() in refreshData(). This have
     * the side effect of the previously selected item no longer being selected. Therefore we also use the information
     * in this object to identify the new model entry that represents the same (as far as possible) entry in the
     * old model.
     */
    private NodeData selectedNodeData = null;

    //----- GUI Components -----//

    /** The config node tree. */
    private Tree configNodeTree = null;

    /** Listens to item selections. */
    private ValueChangeListener itemSelectListener = null;

    //
    // Constructors
    //

    /**
     * Creates a new NodeSelector.
     */
    public NodeSelector() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);
        verticalLayout.setStyleName(CSS.APS_CONFIG_NODE_SELECTOR);

        setContent(verticalLayout);

        this.configNodeTree = new Tree();
        this.configNodeTree.setImmediate(true);
        this.configNodeTree.setSelectable(true);
        this.configNodeTree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
        this.configNodeTree.setItemCaptionPropertyId(HierarchicalModel.getDefaultCaption());
        this.configNodeTree.setItemDescriptionGenerator(this); // For tooltips.
        this.configNodeTree.setHeight("100%");
        this.configNodeTree.setWidth(null);

        this.itemSelectListener = new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                itemSelected((ID)event.getProperty().getValue());
            }
        };

        this.configNodeTree.addListener(this.itemSelectListener);

        verticalLayout.addComponent(this.configNodeTree);
    }

    //
    // Methods
    //

    /**
     * Reloads data from the data source.
     */
    public void refreshData() {
        HierarchicalModel<NodeData> hmodel  = new HierarchicalModel<NodeData>(new IntID());

        APSConfigEditModel root = this.dataSource.getRootModel();

        ID selectedID = null;
        selectedID = buildHierarchicalModel(hmodel, null, root, selectedID);

        this.configNodeTree.removeListener(this.itemSelectListener);
        this.configNodeTree.setContainerDataSource(hmodel.getHierarchicalContainer());
        this.configNodeTree.addListener(this.itemSelectListener);
        this.configNodeTree.select(selectedID);

        this.configNodeModel = hmodel;

    }

    /**
     * Recursively builds the model from the config node structure.
     *
     * @param hmodel The HierarchicalModel to build.
     * @param parentID The id of the parent or null if root.
     * @param currentNode The current node to add to the model.
     * @param selectedID This will be returned possibly modified. The final id returned in refreshData() is the id to select in the tree.
     */
    private ID buildHierarchicalModel(HierarchicalModel<NodeData> hmodel, ID parentID, APSConfigEditModel currentNode, ID selectedID) {

        String name = getSimpleConfigId(currentNode.getConfigId());
        NodeData nodeData = new NodeData();
        nodeData.setConfigNodeModel(currentNode);
        nodeData.setToolTipText(currentNode.getConfigId() + ":" + currentNode.getVersion() + "<hr/>" + currentNode.getDescription());

        ID currNodeId = null;

        if (parentID == null) {
            currNodeId = hmodel.addItem(nodeData, name);
        }
        else {
            currNodeId = hmodel.addItem(parentID, nodeData, currentNode.isMany() ? name + " : " + this.dataSource.getInstanceCount(currentNode) : name);
        }

        if (this.selectedNodeData != null && currentNode.equals(this.selectedNodeData.getConfigNodeModel()) && this.selectedNodeData.getIndex() < 0) {
            selectedID = currNodeId;
        }

        if (currentNode.isMany()) {
            ID instanceId = null;

            // In case the selected index is greater than the number of entries, then set it to the last entry.
            int currNodeSize = this.dataSource.getInstanceCount(currentNode);
            if (this.selectedNodeData != null && this.selectedNodeData.getIndex() >= currNodeSize) {
                this.selectedNodeData.setIndex(currNodeSize - 1);
            }

            selectedID = null;
            int size = this.dataSource.getInstanceCount(currentNode);
            for (int i = 0; i < size; i++) {
                NodeData instanceNodeData = new NodeData();
                instanceNodeData.setConfigNodeModel(currentNode);
                instanceNodeData.setIndex(i);

                instanceId = hmodel.addItem(currNodeId, instanceNodeData, name + " : " + i);

                // If this is the selected node model and this is the selected index of that model then set the selectedID to this id
                // so that the tree node can be selected using this id. If we don't select it again after reloading a new built model
                // nothing will be selected!
                if (this.selectedNodeData != null && currentNode.equals(this.selectedNodeData.getConfigNodeModel()) && this.selectedNodeData.getIndex() == i) {
                    selectedID = instanceId;
                }
            }

            // If none of the indexed instances were selected then select the parent node if this is the previously selected node.
            if (selectedID == null && this.selectedNodeData != null && currentNode.equals(this.selectedNodeData.getConfigNodeModel())) {
                selectedID = currNodeId;
            }
        }

        // Call ourself to build our children. APSConfigValueEditModel can be both a value and a node. All nodes however
        // extend APSConfigValueEditModel with APSConfigEditModel.
        for (APSConfigValueEditModel valueEditModel : currentNode.getValues()) {
            if (valueEditModel instanceof APSConfigEditModel) { // We are only interested in nodes, not values.
                selectedID = buildHierarchicalModel(hmodel, currNodeId, (APSConfigEditModel)valueEditModel, selectedID);
            }
        }

        return selectedID;
    }

    /**
     * Returns the last part of the config id.
     *
     * @param configId The full config id.
     */
    private String getSimpleConfigId(String configId) {
        int ix = configId.lastIndexOf('.');
        return configId.substring(ix+1);
    }

    /**
     * Sets the data source for this component.
     *
     * @param dataSource The data source to set.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Adds a listener to this component.
     *
     * @param nodeSelectionListener The listener to add.
     */
    public void addListener(NodeSelectionListener nodeSelectionListener) {
        this.nodeSelectionListeners.add(nodeSelectionListener);
    }

    /**
     * Called when an item is selected.
     *
     * @param id The id of the selected item.
     */
    private void itemSelected(ID id) {
        NodeData nodeData = this.configNodeModel.getData(id);

        if (nodeData != null) {
            this.selectedNodeData = nodeData;

            fireNodeSelectedEvent(nodeData.getConfigNodeModel(), nodeData.getIndex());
        }
    }

    /**
     * Fires a NodeSelectedEvent.
     *
     * @param selectedModel The selected config model.
     * @param index The index of a "many" type node.
     */
    private void fireNodeSelectedEvent(APSConfigEditModel selectedModel, int index) {
        NodeSelectedEvent event = new NodeSelectedEvent(this, selectedModel, index);
        for (NodeSelectionListener listener : this.nodeSelectionListeners) {
            listener.nodeSelected(event);
        }
    }

    /**
     * Called by Table when a cell (and row) is painted or a item is painted
     * in Tree. This method provides the implementation of the Vaadin
     * ItemDescriptionGenerator interface.
     *
     * @param source     The source of the generator, the Tree or Table the
     *                   generator is attached to
     * @param itemId     The itemId of the painted cell
     * @param propertyId The propertyId of the cell, null when getting row
     *                   description
     *
     * @return The description or "tooltip" of the item.
     */
    @Override
    public String generateDescription(Component source, Object itemId, Object propertyId) {
        NodeData nodeData = this.configNodeModel.getData((ID)itemId);

        return nodeData != null ? nodeData.getToolTipText() : "";
    }

    //
    // Inner Classes and Interfaces
    //

    /**
     * This is a model of information that gets passed for each item to the HierarchicalModel.
     */
    private static class NodeData {
        //
        // Private Members
        //

        /** The tooltip text. */
        private String toolTipText = null;

        /** The model representing the config node. */
        private APSConfigEditModel configNodeModel = null;

        /** The index of a "many" type node. */
        private int index = -1;

        //
        // Constructors
        //

        /**
         * Creates a new NodeData.
         */
        public NodeData() {}

        //
        // Methods
        //


        /**
         * @return The tooltip text.
         */
        public String getToolTipText() {
            return toolTipText;
        }

        /**
         * Sets the tooltip text.
         *
         * @param toolTipText The text to set.
         */
        public void setToolTipText(String toolTipText) {
            this.toolTipText = toolTipText;
        }

        /**
         * @return The model representing the config node.
         */
        public APSConfigEditModel getConfigNodeModel() {
            return configNodeModel;
        }

        /**
         * Sets the model representing the config node.
         *
         * @param configNodeModel The model to set.
         */
        public void setConfigNodeModel(APSConfigEditModel configNodeModel) {
            this.configNodeModel = configNodeModel;
        }

        /**
         * @return The index of a "many" type node.
         */
        public int getIndex() {
            return index;
        }

        /**
         * Sets the index of a "many" type node.
         *
         * @param index The index to set.
         */
        public void setIndex(int index) {
            this.index = index;
        }

        /**
         * @return true if this represents a node of "many" type that also have an index.
         */
        public boolean hasMany() {
            return this.index >= 0;
        }
    }

    /**
     * This component loads its data from this data source.
     */
    public static interface DataSource {

        /**
         * Returns the root node of the config model.
         */
        APSConfigEditModel getRootModel();

        /**
         * Returns the count of instances for the specified model. The passed model must return true for isMany()!
         */
        int getInstanceCount(APSConfigEditModel configEditModel);
    }

    /**
     * Event that gets sent when a node in the tree gets selected.
     */
    public static class NodeSelectedEvent extends Event {
        //
        // Private Members
        //

        /** The selected model. */
        private APSConfigEditModel selectedModel = null;

        /**
         * The instance index. This is only relevant if selectedModel.isMany() is true. There is a special case
         * when selectedModel.isMany() is true, but the index is less than 0. Models with isMany() == true
         * represents nodes that have zero or more values, and the node model itself is not enough to get to
         * a value, and index is also required. But in the case where index < 0 the event represents a selection
         * of the actual node (which is a placeholder in the tree) and not an instance of it. This is a non value.
         * In this case, display a message with some info to the user that they should select an instance node.
         **/
        private int index = -1;

        //
        // Constructors
        //

        /**
         * Constructs a new event with the specified source component.
         *
         * @param source the source component of the event.
         * @param selectedModel The model of the config node that was selected.
         * @param index The index of a "many" type node.
         */
        public NodeSelectedEvent(Component source, APSConfigEditModel selectedModel, int index) {
            super(source);
            this.selectedModel = selectedModel;
            this.index = index;
        }

        //
        // Methods
        //

        /**
         * @return The config model of the selected node.
         */
        public APSConfigEditModel getSelectedModel() {
            return this.selectedModel;
        }

        /**
         * @return The index of the selected "many" instance node.
         */
        public int getIndex() {
            return this.index;
        }

        /**
         * @return true if this is a "many" type node also having an index.
         */
        public boolean isMany() {
            return this.index >= 0;
        }
    }

    /**
     * A listener for NodeSelectedEvent events.
     */
    public static interface NodeSelectionListener {

        /**
         * Called when a new config node gets selected.
         *
         * @param event The event received.
         */
        public void nodeSelected(NodeSelectedEvent event);
    }
}
