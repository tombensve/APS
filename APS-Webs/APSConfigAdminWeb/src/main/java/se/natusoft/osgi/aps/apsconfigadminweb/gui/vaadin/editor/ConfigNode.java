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
 *         2012-04-08: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.editor;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;

import java.util.LinkedList;
import java.util.List;

/**
 * Manages the current config node.
 */
public class ConfigNode {
    //
    // Constants
    //

    /** Represents a non index. */
    public static final int NO_INDEX = -1;

    //
    // Private Members
    //

    /** The current node. */
    private APSConfigEditModel currentNode = null;

    /** The current instance node. This represents the node at a specific index. */
    private APSConfigEditModel currentInstanceNode = null;

    /** The index of the currently selected node or -1 for a non "many" type node. */
    private int index = -1;

    /** The editable node values of this node. */
    private List<APSConfigValueEditModel> nodeValues = null;

    /** The child nodes of this node. */
    private List<APSConfigEditModel> nodeChildren = null;

    /** The description of this node. */
    private String description = null;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigNavigator.
     *
     * @param rootNode The root node of the config tree.
     */
    public ConfigNode(APSConfigEditModel rootNode) {
        setCurrentNode(rootNode, -1);
    }

    //
    // Methods
    //

    /**
     * Changes the current node.
     *
     * @param currentNode The new node to set.
     * @param index The index of the node or -1 for none.
     */
    public void setCurrentNode(APSConfigEditModel currentNode, int index) {
        this.currentNode = currentNode;
        this.index = index;

        this.nodeValues = new LinkedList<APSConfigValueEditModel>();
        this.nodeChildren = new LinkedList<APSConfigEditModel>();
        for (APSConfigValueEditModel nodeMember : this.currentNode.getValues()) {
            if (APSConfigEditModel.class.isAssignableFrom(nodeMember.getClass())) {
                this.nodeChildren.add((APSConfigEditModel)nodeMember);
            }
            else {
                this.nodeValues.add(nodeMember);
            }
        }
    }

    /**
     * Sets a node model that represents a specific index and note the whole list of values.
     * This node returns false on isMany() and should not be used for anything else than
     * referencing values for the instance.
     *
     * @param currentInstanceNode The instance node to set.
     */
    public void setCurrentInstanceNode(APSConfigEditModel currentInstanceNode) {
        this.currentInstanceNode = currentInstanceNode;

        // In this case we need to replace the node child and value lists with new ones
        // based on this model.
        this.nodeValues = new LinkedList<APSConfigValueEditModel>();
        this.nodeChildren = new LinkedList<APSConfigEditModel>();
        for (APSConfigValueEditModel nodeMember : this.currentInstanceNode.getValues()) {
            if (APSConfigEditModel.class.isAssignableFrom(nodeMember.getClass())) {
                this.nodeChildren.add((APSConfigEditModel)nodeMember);
            }
            else {
                this.nodeValues.add(nodeMember);
            }
        }
    }

    /**
     * Sets the description of this node.
     *
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The description of this node.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Changes the index.
     *
     * @param index The new index.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return The editable values of the current node.
     */
    public List<APSConfigValueEditModel> getNodeValues() {
        return this.nodeValues;
    }

    /**
     * @return The children of the current node.
     */
    public List<APSConfigEditModel> getNodeChildren() {
        return this.nodeChildren;
    }

    /**
     * @return The current node.
     */
    public APSConfigEditModel getCurrentNode() {
        return this.currentNode;
    }

    /**
     * This returned node represents a specific instance within a 'many' list value and should only
     * be used to reference values for that specific instance. If you do isMany() on this model it
     * will return false!
     * <p/>
     * Please note that this will return null until setCurrentInstanceNode() have been done!
     *
     * @return The current instance node.
     */
    public APSConfigEditModel getCurrentInstanceNode() {
        return this.currentInstanceNode;
    }

    /**
     * @return The index for the current node or -1 if the current node is not a "many" type node.
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * @return The config id of the current node.
     */
    public String getNodeConfigId() {
        return this.currentNode.getConfigId();
    }

    /**
     * @return The last part of the node config id.
     */
    public String getSimpleNodeConfigId() {
        String configId = getNodeConfigId();
        int ix = configId.lastIndexOf('.');
        return configId.substring(ix+1);
    }

    /**
     * @return true if this is at the root node.
     */
    public boolean isRoot() {
        return this.currentNode.getParent() == null;
    }
}
