/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         This provides some utility classes for web applications.
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
package se.natusoft.osgi.aps.tools.web.vaadin.models;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import se.natusoft.osgi.aps.tools.models.ID;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a wrapping of Vaadins HierarchicalContainer. 
 *
 * The reason for this is that I want to be able to associate a set of specific data with each item. For a Tree you
 * can use item properties, but that is more of a side-effect than intentional usage. It will work less well with a
 * Table. So this wrapper will build a HierarchicalContainer, generate item ids, which will also be used to associate
 * a data object with each item in a separate map. Once the HierarchicalModel is built the HierarchicalContainer can
 * be gotten from it and the data object for any item can be looked up by its id.
 *
 * This does currently not support default values!
 *
 * @param <Data> The type of the data model associated with Hierarchical model entries.
 */
public class HierarchicalModel<Data> {
    //
    // Private Members
    //
    
    /** The next item id to use. */
    private ID itemId = null;
    
    /** Associates a data object with each item. */
    private Map<ID, Data> itemDataMap = new HashMap<ID, Data>();
    
    /** The HierarchicalContainer we will be building. */
    private HierarchicalContainer container = new HierarchicalContainer();
    
    /** The field captions. */
    private String[] captionProperties = null;
    
    //
    // Constructors
    //

    /**
     * Creates a new HierarchicalModel. 
     *
     * @param idProvider An ID implementation providing IDs.
     * @param captionProperties container captions.
     */
    public HierarchicalModel(ID idProvider, String... captionProperties) {
        this.itemId = idProvider;
        this.captionProperties = captionProperties;
        for (String caption : captionProperties) {
            container.addContainerProperty(caption, String.class, null);
        }
    }

    /**
     * Creates a new HierarchicalModel.
     *
     * @param idProvider An ID implementation providing IDs.
     */
    public HierarchicalModel(ID idProvider) {
        this(idProvider, getDefaultCaption());
    }                
    
    //
    // Methods
    //

    /**
     * If the no arg constructor is used the caption property is made up, and this will return it.
     * 
     * @return The made up caption property.
     */
    public static String getDefaultCaption() {
        return "caption";
    }

    /**
     * Adds an item to the model. 
     * 
     * @param parent If non null this should be the item id of the parent of the item added.
     * @param data The data to associate with the item.
     * @param captions Caption values for the caption properties.
     *                 
     * @return The item id of the added item.
     */
    public ID addItem(ID parent, Data data, String... captions) {
        this.itemId = this.itemId.newID();
        Item item = this.container.addItem(this.itemId);
        this.container.setChildrenAllowed(this.itemId, false);
        for (int i = 0; i < this.captionProperties.length; i++) {
            if (i < captions.length) {
                item.getItemProperty(this.captionProperties[i]).setValue(captions[i]);
            }
        }
        
        this.itemDataMap.put(this.itemId, data);

        if (parent != null) {
            this.container.setChildrenAllowed(parent, true);
            this.container.setParent(this.itemId, parent);
        }

        return itemId;
    }

    /**
     * Adds an item to the model.
     *
     * @param data The data to associate with the item.
     * @param captions Caption values for the caption properties.
     *
     * @return The item id of the added item.
     */
    public ID addItem(Data data, String... captions) {
        return addItem(null, data, captions);
    }

    /**
     * Returns the associated data for an item.
     *
     * @param itemId The id of the item to get associated data for.
     *
     * @return The associated data.
     */
    public Data getData(ID itemId) {
        return this.itemDataMap.get(itemId);
    }

    /**
     * @return The current item id, which is also always the last/highest item id.
     */
    public ID getCurrentItemId() {
        return this.itemId;
    }
    
    /**
     * Returns the HierarchicalContainer that we have built so far.
     * <p/>
     * Please note that it is the internal instance that is returned, not a copy of it! Thereby any changes
     * made after the call to this method will still affect the returned object!
     */
    public HierarchicalContainer getHierarchicalContainer() {
        return this.container;
    }
}
