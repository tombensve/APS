/*
 *
 * PROJECT
 *     Name
 *         APS Vaadin Web Tools
 *
 *     Code Version
 *         1.0.0
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
 *         2012-02-26: Created!
 *
 */
package se.natusoft.osgi.aps.tools.web.vaadin.components.menutree;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import se.natusoft.osgi.aps.tools.models.IntID;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi.MenuBuilder;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi.MenuItemData;
import se.natusoft.osgi.aps.tools.web.vaadin.models.HierarchicalModel;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a semi advanced menu tree component.
 *
 * You add 'MenuBuilder's to provide the menu contents. The builders also provide actions for when
 * a menu entry is accessed.
 */
public class MenuTree extends Tree implements Action.Handler, ItemDescriptionGenerator, Refreshable {
    //
    // Constants
    //

    /** Indicates no actions. */
    public static final Action[] NO_ACTIONS = new Action[0];

    //
    // Private Members
    //

    /** An extended model also mapping a data object to each item. */
    private HierarchicalModel<MenuItemData> menuModel = null;

    /** handleAction will forward to this. */
    private MenuActionHandler actionHandler = null;

    /** The menu builders responsible for menu content. */
    private List<MenuBuilder> menuBuilders = new LinkedList<>();

    //
    // Constructors
    //

    /**
     * Creates a new MenuTree component.
     */
    public MenuTree() {
        setImmediate(true);
        setSelectable(true);
        setNullSelectionAllowed(false);
        setItemCaptionMode(ItemCaptionMode.PROPERTY);
        setItemCaptionPropertyId(HierarchicalModel.getDefaultCaption());
        setItemDescriptionGenerator(this); // For tooltips.

        // Please note that it is the MenuBuilders that provides the actions to handle!
        addActionHandler(this);
    }

    //
    // Methods
    //

    /**
     * Adds a MenuBuilder to the menu tree.
     *
     * @param menuBuilder The menu builder to add.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void addMenuBuilder(MenuBuilder menuBuilder) {
        this.menuBuilders.add(menuBuilder);
    }

    /**
     * Called by Table when a cell (and row) is painted or a item is painted
     * in Tree.
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
        String tooltip = "";

        MenuItemData itemData = getItemData((IntID)itemId);
        if (itemData != null) {
            if (itemData.getToolTipText() != null) {
                tooltip = itemData.getToolTipText();
            }
        }

        return tooltip;
    }

    /**
     * Reloads the contents of the menu. This is never done automatically!!
     */
    public void refresh() {
        setContainerDataSource(createHierarchicalModel());
        //expandHierarchicalModel();
    }

    /**
     * Returns a data object associated with a menu item by its item id.
     *
     * @param itemId The item id of the item whose associated data to get.
     *
     * @return The associated data or null.
     */
    public MenuItemData getItemData(IntID itemId) {
        return this.menuModel.getData(itemId);
    }

    /**
     * Sets the action handler for forward actions to.
     *
     * @param actionHandler The action handler to set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setActionHandler(MenuActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    /**
     * Creates an hierarchical model of configold environments and configurations.
     *
     * @return A Vaadin HierarchicalContainer that can be used for a Tree or any other hierarchical component.
     */
    @SuppressWarnings("unchecked")
    private HierarchicalContainer createHierarchicalModel() {
        this.menuModel = new HierarchicalModel<>(new IntID());

        for (MenuBuilder menuBuilder : this.menuBuilders) {
            menuBuilder.buildMenuEntries(this.menuModel);
        }

        return this.menuModel.getHierarchicalContainer();
    }

    /**
     * Expands all nodes in the hierarchical model.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void expandHierarchicalModel() {
        Iterator<IntID> it = IntID.rangeIterator(new IntID(), (IntID)this.menuModel.getCurrentItemId());
        while(it.hasNext()) {
            expandItemsRecursively(it.next());
        }
    }

    /**
     * Gets the list of actions applicable to this handler.
     *
     * @param target the target handler to list actions for. For item
     *               containers this is the item id.
     * @param sender the party that would be sending the actions. Most of this
     *               is the action container.
     *
     * @return the list of Action
     */
    @Override
    public Action[] getActions(Object target, Object sender) {
        IntID itemId = null;

        if (target != null) {
            itemId = (IntID)target;
        }

        MenuItemData itemData = getItemData(itemId);

        if (itemData != null) {
            return itemData.getActions();
        }
        return NO_ACTIONS;
    }

    /**
     * Handles an action for the given target. The handler method may just
     * discard the action if it's not suitable.
     *
     * @param action the action to be handled.
     * @param sender the sender of the action. This is most often the action
     *               container.
     * @param target the target of the action. For item containers this is the
     *               item id.
     */
    @Override
    public void handleAction(Action action, Object sender, Object target) {
        this.actionHandler.handleAction(action, sender, target);
    }

    //
    // Inner Classes
    //


    /**
     * This provides half of the Action.Handler API since we provide the first part our self.
     */
    public static interface MenuActionHandler {

        /**
         * Handles an action for the given target. The handler method may just
         * discard the action if it's not suitable.
         *
         * @param action the action to be handled.
         * @param sender the sender of the action. This is most often the action
         *               container.
         * @param target the target of the action. For item containers this is the
         *               item id.
         */
        public void handleAction(Action action, Object sender, Object target);
    }

}
