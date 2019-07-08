/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
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
 *         2012-02-17: Created!
 *
 */
package se.natusoft.osgi.aps.api.core.platform.model;

/**
 * This model provides information about a platform installation.
 */
public class NodeInfo {

    //
    // Private Members
    //

    /** The address to send events to this node. */
    private String address;

    /** Returns the node identifier. */
    private String name = "";

    /** Returns the purpose of the node.*/
    private String purpose = "";

    /** Returns the description of the node. */
    private String description = "";

    /** This is true for local node, false for other. */
    private boolean localNode;


    //
    // Constructors
    //

    /**
     * Creates a new PlatformDescription.
     */
    public NodeInfo() {}

    //
    // Methods
    //

    /**
     * @return The node address.
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Sets the node address.
     *
     * @param address The node address.
     */
    public NodeInfo setAddress(String address) {
        this.address = address;
        return this;
    }

    /**
     * Returns the node identifier.
     */
    public String getName() {
        return name;
    }

    public NodeInfo setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the purpose of the node.
     */
    public String getPurpose() {
        return purpose;
    }

    public NodeInfo setPurpose(String purpose) {
        this.purpose = purpose;
        return this;
    }

    /**
     * Returns the description of the node.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the node.
     *
     * @param description The description of the node.
     */
    public NodeInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return true if this is a local node.
     */
    public boolean isLocalNode() {
        return this.localNode;
    }

    /**
     * Sets if this is a local node or not.
     *
     * @param localNode The local node state to set.
     */
    public NodeInfo setLocalNode(boolean localNode) {
        this.localNode = localNode;
        return this;
    }
}
