/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         0.9.1
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
 *         2012-03-17: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi;

import com.vaadin.event.Action;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.MenuActionProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds item related information that is associated with a model item by its item id.
 */
public class MenuItemData<ItemRepresentative> {

    //
    // Private Members
    //

    /** This represents one specific configuration. */
    private ItemRepresentative itemRepresentative = null;

    /** The tooltip text for the item. */
    private String toolTipText = null;

    /** The actions for the item. */
    private Action[] actions = null;

    /** The component hander for when this item is selected. */
    private ComponentHandler selectComponentHandler = null;

    /** The menu action handlers per action. */
    private Map<Action, MenuActionProvider> actionComponentHandlers = new HashMap<Action, MenuActionProvider>();

    //
    // Constructors
    //

    /**
     * Creates a new MenuTreeItemData instance.
     */
    public MenuItemData() {}

    //
    // Methods
    //

    /** This represents one specific configuration. */
    public ItemRepresentative getItemRepresentative() {
        return itemRepresentative;
    }

    public void setItemRepresentative(ItemRepresentative itemRepresentative) {
        this.itemRepresentative = itemRepresentative;
    }

    /** The tooltip text for the item. */
    public String getToolTipText() {
        return toolTipText;
    }

    public void setToolTipText(String toolTipText) {
        this.toolTipText = toolTipText;
    }

    /** The actions for the item. */
    public Action[] getActions() {
        return actions;
    }

    public void setActions(Action[] actions) {
        this.actions = actions;
    }

    /** The component hander for when this item is selected. */
    public ComponentHandler getSelectComponentHandler() {
        return selectComponentHandler;
    }

    public void setSelectComponentHandler(ComponentHandler selectComponentHandler) {
        this.selectComponentHandler = selectComponentHandler;
    }

    /** The menu action handlers per action. */
    public Map<Action, MenuActionProvider> getActionComponentHandlers() {
        return actionComponentHandlers;
    }

    public void setActionComponentHandlers(Map<Action, MenuActionProvider> actionComponentHandlers) {
        this.actionComponentHandlers = actionComponentHandlers;
    }
}
