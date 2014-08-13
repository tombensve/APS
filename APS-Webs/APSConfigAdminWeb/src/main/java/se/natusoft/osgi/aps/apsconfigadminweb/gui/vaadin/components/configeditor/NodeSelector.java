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
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
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
@SuppressWarnings("UnusedAssignment")
public class NodeSelector extends Panel implements ItemDescriptionGenerator {
    //
    // Private Members
    //

    /** The config admin for the config handled by the NodeSelector. */
    private APSConfigAdmin configAdmin = null; // TODO: See todo in constructor.

    /** An extended model also mapping a data object to each item. */
    private HierarchicalModel<NodeData> configNodeModel = null;

    /** Our data source. */
    private DataSource dataSource = null;

    /** The listeners on this component. */
    private List<NodeSelectionListener> nodeSelectionListeners = new LinkedList<>();

    /**
     * A NodeData object representing the currently selected tree node.
     *
     * PLEASE NOTE:\
     * This is data representing a node in the tree that was selected. This is set in itemSelected(...) below. \
     * The content of this tree is loaded from a model that is externally modified! That means that this held data \
     * might get stale.
     *
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
     *
     * @param configAdmin The config admin for the config handled by the NodeSelector.
     */
    public NodeSelector(APSConfigAdmin configAdmin) {
        this.configAdmin = configAdmin; // TODO: This is actually not used anymore!

        setStyleName(CSS.APS_CONFIG_NODE_SELECTOR);
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);

        setContent(verticalLayout);

        this.configNodeTree = new Tree();
        this.configNodeTree.setImmediate(true);
        this.configNodeTree.setSelectable(true);
        this.configNodeTree.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
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

        this.configNodeTree.addValueChangeListener(this.itemSelectListener);

        verticalLayout.addComponent(this.configNodeTree);
    }

    //
    // Public API for providing information.
    //

    /**
     * This component loads its data from this data source.
     */
    public static interface DataSource {

        /**
         * Returns the reference to the root node of the config model.
         */
        APSConfigReference getRootReference();

        /**
         * Returns the count of instances for the specified model. The passed model must return true for isMany()!
         */
        int getInstanceCount(APSConfigReference configRef);
    }

    //
    // Public Event Support
    //

    /**
     * Event that gets sent when a node in the tree gets selected.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static class NodeSelectedEvent extends Event {
        //
        // Private Members
        //

        /** The selected model. */
        private APSConfigReference selectedRef = null;

        /** The indexed state of the node. */
        private boolean indexed = false;

        //
        // Constructors
        //

        /**
         * Constructs a new event with the specified source component.
         *
         * @param source the source component of the event.
         * @param selectedRef The reference to the selected config node.
         */
        public NodeSelectedEvent(Component source, APSConfigReference selectedRef, boolean indexed) {
            super(source);
            this.selectedRef = selectedRef;
            this.indexed = indexed;
        }

        //
        // Methods
        //

        /**
         * @return The config reference of the selected node.
         */
        public APSConfigReference getSelectedReference() {
            return this.selectedRef;
        }

        /**
         * @return true if this is a "many" type node also having an index.
         */
        public boolean isMany() {
            return this.selectedRef.getConfigValueEditModel().isMany();
        }

        /**
         * Returns true if this node is indexed.
         */
        public boolean isIndexed() {
            return this.indexed;
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

    //
    // Public Methods
    //

    /**
     * Reloads data from the data source.
     */
    @SuppressWarnings("ConstantConditions")
    public void refreshData() {
        HierarchicalModel<NodeData> hmodel  = new HierarchicalModel<>(new IntID());

        APSConfigReference rootRef = this.dataSource.getRootReference();

        ID selectedID = null;
        selectedID = buildHierarchicalModel(hmodel, null, rootRef, selectedID);

        this.configNodeTree.removeValueChangeListener(this.itemSelectListener);
        this.configNodeTree.setContainerDataSource(hmodel.getHierarchicalContainer());
        this.configNodeTree.addValueChangeListener(this.itemSelectListener);
        this.configNodeTree.select(selectedID);

        this.configNodeModel = hmodel;

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
    // Private Methods
    //

    /**
     * Returns the closest APSConfigEditModel from the specified reference.
     *
     * @param ref The reference to get the edit model from.
     */
    private APSConfigEditModel refToEditModel(APSConfigReference ref) {
        APSConfigValueEditModel editModel = ref.getConfigValueEditModel();
        return (editModel instanceof APSConfigEditModel) ? (APSConfigEditModel)editModel : editModel.getParent();
    }

    /**
     * Recursively builds the model from the config node structure.
     *
     * @param hmodel The HierarchicalModel to build.
     * @param parentID The id of the parent or null if root.
     * @param currentRef A reference to the current node to add to the model.
     * @param selectedID This will be returned possibly modified. The final id returned in refreshData() is the id to select in the tree.
     */
    private ID buildHierarchicalModel(HierarchicalModel<NodeData> hmodel, ID parentID, APSConfigReference currentRef, ID selectedID) {

        APSConfigEditModel currentNode = refToEditModel(currentRef);

        String name = getSimpleConfigId(currentNode.getConfigId());
        NodeData nodeData = new NodeData(currentRef, false);
        nodeData.setToolTipText(currentNode.getConfigId() + ":" + currentNode.getVersion() + "<hr/>" + currentNode.getDescription());

        ID currNodeId = null;

        if (parentID == null) {
            currNodeId = hmodel.addItem(nodeData, name);
        }
        else {
            currNodeId = hmodel.addItem(parentID, nodeData, currentNode.isMany() ? name + " : " +
                    this.dataSource.getInstanceCount(currentRef) : name);
        }

        if (this.selectedNodeData != null &&
                currentNode.equals(this.selectedNodeData.getConfigNodeModel()) && this.selectedNodeData.getIndex() < 0) {
            selectedID = currNodeId;
        }

        if (currentNode.isMany()) {
            ID instanceId = null;

            // In case the selected index is greater than the number of entries, then set it to the last entry.
            int currNodeSize = this.dataSource.getInstanceCount(currentRef);
            if (this.selectedNodeData != null && this.selectedNodeData.getIndex() >= currNodeSize) {
                this.selectedNodeData.setIndex(currNodeSize - 1);
            }

            selectedID = null;
            int size = this.dataSource.getInstanceCount(currentRef);
            for (int i = 0; i < size; i++) {
                NodeData instanceNodeData = new NodeData(currentRef._(i), true);

                instanceId = hmodel.addItem(currNodeId, instanceNodeData, name + " : " + i);
                selectedID = buildChildren(selectedID, hmodel, currentRef._(i), instanceId);

                // If this is the selected node model and this is the selected index of that model then set the selectedID to this id
                // so that the tree node can be selected using this id. If we don't select it again after reloading a new built model
                // nothing will be selected!
                if (this.selectedNodeData != null && currentNode.equals(this.selectedNodeData.getConfigNodeModel()) &&
                        this.selectedNodeData.getIndex() == i) {
                    selectedID = instanceId;
                }
            }

            // If none of the indexed instances were selected then select the parent node if this is the previously selected node.
            if (selectedID == null && this.selectedNodeData != null && currentNode.equals(this.selectedNodeData.getConfigNodeModel())) {
                selectedID = currNodeId;
            }
        }
        else {
            selectedID = buildChildren(selectedID, hmodel, currentRef, currNodeId);
        }

        return selectedID;
    }

    /**
     * Builds children for possibly indexed node by calling buildHierarchicalModel.
     *
     * @param selectedID The current selected id.
     * @param hmodel The model being built.
     * @param currentRef The reference to the node whose children to build.
     * @param currNodeId The current node that will be the parent of child nodes.
     *
     * @return A possibly updated selectedID.
     */
    private ID buildChildren(ID selectedID, HierarchicalModel<NodeData> hmodel, APSConfigReference currentRef, ID currNodeId) {
        APSConfigEditModel currentNode = refToEditModel(currentRef);

        for (APSConfigValueEditModel valueEditModel : currentNode.getValues()) {
            if (valueEditModel instanceof APSConfigEditModel) { // We are only interested in nodes, not values.
                APSConfigReference childRef = currentRef._(valueEditModel);
                selectedID = buildHierarchicalModel(hmodel, currNodeId, childRef, selectedID);
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
     * Called when an item is selected.
     *
     * @param id The id of the selected item.
     */
    private void itemSelected(ID id) {
        NodeData nodeData = this.configNodeModel.getData(id);

        if (nodeData != null) {
            this.selectedNodeData = nodeData;

            fireNodeSelectedEvent(nodeData.getConfigReference(), nodeData.isIndexed());
        }
    }

    /**
     * Fires a NodeSelectedEvent.
     *
     * @param selectedRef The selected config reference.
     * @param indexed True if this node is indexed.
     */
    private void fireNodeSelectedEvent(APSConfigReference selectedRef, boolean indexed) {
        NodeSelectedEvent event = new NodeSelectedEvent(this, selectedRef, indexed);
        for (NodeSelectionListener listener : this.nodeSelectionListeners) {
            listener.nodeSelected(event);
        }
    }

    //
    // Inner Support Classes
    //

    /**
     * This is a model of information that gets passed for each item to the HierarchicalModel.
     */
    @SuppressWarnings("UnusedDeclaration")
    private static class NodeData {
        //
        // Private Members
        //

        /** The tooltip text. */
        private String toolTipText = null;

        /** The reference to the config value represented by this node data. */
        private APSConfigReference configRef = null;

        /**
         * The indexed state of this node. In the GUI there is an extra parent node for each list value.
         * This parent node represents the type and shows the number of entries in the list, but have
         * no index itself. It does have an APSConfigReference but it should be without and index.
         * Each instance (children of parent) have an APSConfigReference based in the parent reference
         * plus an index. Only the children have editable values to the right, the parent only shows
         * information about the node and instructions on how to add or remove values. Thereby GUI wise
         * we need to know the indexed state of each node. For the parent indexed should be false,
         * while configRef.getConfigValueEditModel().isMany() is true. For children both should be true.
         *
         * So in other words, the rendered hierarchy have one node more for lists than the edit model
         * hierarchy does. The first represents presentation while the other represents data structure.
         *
         * I did consider calling this 'parent' instead, but that would not have made things clearer :-).
         */
        private boolean indexed = false;

        //
        // Constructors
        //

        /**
         * Creates a new NodeData.
         *
         * @param configRef The config reference of this node.
         * @param indexed If this node is indexed or not.
         */
        public NodeData(APSConfigReference configRef, boolean indexed) {
            setConfigReference(configRef);
            this.indexed = indexed;
        }

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
         * Returns the reference to the config value represented by this NodeData.
         */
        public APSConfigReference getConfigReference() {
            return this.configRef;
        }

        /**
         * Sets the reference to the config value represented by this NodeData.
         *
         * @param configRef The config reference to set.
         */
        public void setConfigReference(APSConfigReference configRef) {
            this.configRef = configRef;
        }

        /**
         * @return The model representing the config node.
         */
        public APSConfigEditModel getConfigNodeModel() {
            APSConfigEditModel nodeModel = null;

            if (this.configRef != null) {
                APSConfigValueEditModel ced = this.configRef.getConfigValueEditModel();
                nodeModel = (ced instanceof APSConfigEditModel) ? (APSConfigEditModel) ced : ced.getParent();
            }

            return nodeModel;
        }

        /**
         * Returns true if this node is indexed.
         */
        public boolean isIndexed() {
            return this.indexed;
        }

        /**
         * @return The index of a "many" type node.
         */
        public int getIndex() {
            int ix = -1;
            if (this.configRef != null && hasMany()) {
                ix = this.configRef.getIndex();
            }
            return ix;
        }

        /**
         * Sets the index of a "many" type node.
         *
         * @param index The index to set.
         */
        public void setIndex(int index) {
            if (this.configRef != null && hasMany()) {
                this.configRef = this.configRef._(index);
            }
        }

        /**
         * @return true if this represents a node of "many" type that also have an index.
         */
        public boolean hasMany() {
            return this.configRef != null && this.configRef.getConfigValueEditModel().isMany();
        }
    }

}
