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
 *         2012-04-08: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.editor;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;

import java.util.LinkedList;
import java.util.List;

import static se.natusoft.osgi.aps.api.core.config.util.APSConfigStaticUtils.refToEditModel;

/**
 * Manages the current config node.
 */
@SuppressWarnings("UnusedDeclaration")
public class ConfigNode {
    //
    // Private Members
    //

    /** The current node. */
    private APSConfigEditModel currentNode = null;

    /** The current config instance reference. */
    private APSConfigReference currentRef = null;

    /** The editable node values of this node. */
    private List<APSConfigReference> nodeValues = null;

    /** The child nodes of this node. */
    private List<APSConfigReference> nodeChildren = null;

    /** The description of this node. */
    private String description = null;

    /** The indexed state of the node. */
    private boolean indexed = false;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigNavigator.
     *
     * @param rootRef The reference to the root node of the config tree.
     * @param indexed The indexed state of this node.
     */
    public ConfigNode(APSConfigReference rootRef, boolean indexed) {
        setCurrentNode(rootRef, indexed);
    }

    //
    // Public Methods
    //

    /**
     * Changes the current node.
     *
     * @param currentRef The reference to the new node to set.
     * @param indexed The indexed state of this node.
     */
    public void setCurrentNode(APSConfigReference currentRef, boolean indexed) {
        this.currentRef = currentRef;
        this.indexed = indexed;
        this.currentNode = refToEditModel(currentRef);

        this.nodeValues = new LinkedList<>();
        this.nodeChildren = new LinkedList<>();
        if (this.currentNode != null && this.currentNode.getValues() != null) {
            for (APSConfigValueEditModel nodeMember : this.currentNode.getValues()) {
                if (APSConfigEditModel.class.isAssignableFrom(nodeMember.getClass())) {
                    this.nodeChildren.add(this.currentRef._(nodeMember));
                } else {
                    this.nodeValues.add(this.currentRef._(nodeMember));
                }
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
     * Returns the indexed state of this node.
     */
    public boolean isIndexed() {
        return this.indexed;
    }

    /**
     * Changes the index.
     *
     * @param index The new index.
     */
    public void setIndex(int index) {
        this.currentRef = this.currentRef._(index);
    }

    /**
     * @return The editable values of the current node.
     */
    public List<APSConfigReference> getNodeValues() {
        return this.nodeValues;
    }

    /**
     * @return The children of the current node.
     */
    public List<APSConfigReference> getNodeChildren() {
        return this.nodeChildren;
    }

    /**
     * @return The current node.
     */
    public APSConfigEditModel getCurrentNode() {
        return this.currentNode;
    }

    /**
     * Returns the current configuration reference.
     */
    public APSConfigReference getCurrentConfigReference() {
        return this.currentRef;
    }

    /**
     * @return The index for the current node or -1 if the current node is not a "many" type node.
     */
    public int getIndex() {
        return this.currentRef.getIndex();
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
