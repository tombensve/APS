/* 
 * 
 * PROJECT
 *     Name
 *         APS User Admin Web
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This is an administration web for aps-simple-user-service that allows editing of roles and users.
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
package se.natusoft.osgi.aps.apsuseradminweb.vaadin.menubuilders;

import com.vaadin.event.Action;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserServiceAdmin;
import se.natusoft.osgi.aps.api.auth.user.model.Role;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.componenthandlers.DeleteRoleComponentHandler;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.componenthandlers.EditRoleComponentHandler;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.Description;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.editors.RoleDeleteEditor;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.editors.RoleEditor;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.models.ID;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi.MenuBuilder;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi.MenuItemData;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.MenuActionProvider;
import se.natusoft.osgi.aps.tools.web.vaadin.models.HierarchicalModel;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshable;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.RefreshableSupport;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshables;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This builds menu entries for roles.
 */
public class RolesMenuBuilder implements MenuBuilder<Role>, RefreshableSupport, Serializable {
    //
    // Constants
    //

    /** Create a new user. */
    public static final Action ACTION_NEW_ROLE = new Action("New role");

    /** Delete a user. */
    public static final Action ACTION_DELETE_ROLE = new Action("Delete role");

    /** Actions for the user root menu. */
    public static final Action[] ROLE_ROOT_ACTIONS = new Action[] {ACTION_NEW_ROLE};

    /** Actions for user menu items. */
    public static final Action[] ROLE_ITEM_ACTIONS = new Action[] {ACTION_DELETE_ROLE};


    //
    // Private Members
    //

    /** The user service to create/modify/delete user & roles with. */
    private APSSimpleUserServiceAdmin userServiceAdmin = null;

    /** The refreshables to trigger on refresh. */
    private Refreshables refreshables = new Refreshables();

    /** A refreshable that clears the center view. */
    private Refreshable clearCenterRefreshable = null;

    /** The editor for old and new roles. */
    private RoleEditor roleEditor = null;

    /** The editor for deleting a role. */
    private RoleDeleteEditor roleDeleteEditor = null;

    //
    // Constructors
    //

    /**
     * Creates a new RolesMenuBuilder
     *
     * @param userServiceAdmin The user service to create/modify/delete users & roles with.
     * @param logger The application logger.
     */
    public RolesMenuBuilder(APSSimpleUserServiceAdmin userServiceAdmin, APSLogger logger) {
        this.userServiceAdmin = userServiceAdmin;
        this.roleEditor = new RoleEditor(this.userServiceAdmin);
        this.roleEditor.setLogger(logger);
        this.roleDeleteEditor = new RoleDeleteEditor(this.userServiceAdmin);
        this.roleDeleteEditor.setLogger(logger);
    }

    //
    // Methods
    //

    /**
     * Adds a refreshable to be passed to editors on menu entry selection.
     *
     * @param refreshable The refreshable to add.
     */
    public void addRefreshable(Refreshable refreshable) {
        this.refreshables.addRefreshable(refreshable);
    }

    /**
     * Sets a refreshable that clears the center view.
     *
     * @param clearCenterRefreshable The refreshable to set.
     */
    public void setClearCenterRefreshable(Refreshable clearCenterRefreshable) {
        this.clearCenterRefreshable = clearCenterRefreshable;
    }

    /**
     * This should add menu entries to the received menu model.
     *
     * @param menuModel The model to add menu entries to.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void buildMenuEntries(HierarchicalModel<MenuItemData<Role>> menuModel) {
        this.roleEditor.setRefreshables(this.refreshables);
        this.roleEditor.setClearCenterRefreshable(this.clearCenterRefreshable);
        this.roleDeleteEditor.setRefreshables(this.refreshables);
        this.roleDeleteEditor.setClearCenterRefreshable(this.clearCenterRefreshable);

        // This will be initialized with the root node and then sub nodes will be added to this.
        ID userNodeId;

        // Setup root node.
        {
            MenuItemData itemData = new MenuItemData();

            itemData.setActions(ROLE_ROOT_ACTIONS);
            itemData.setSelectComponentHandler(Description.DESCRIPTION_VIEW);
            itemData.setToolTipText("Right click to add a new role!");

            Map<Action, MenuActionProvider> actionComponentHandlerMap = new HashMap<Action, MenuActionProvider>();
            actionComponentHandlerMap.put(ACTION_NEW_ROLE,
                    new EditRoleComponentHandler(this.roleEditor, null));
            itemData.setActionComponentHandlers(actionComponentHandlerMap);

            userNodeId = menuModel.addItem(null, itemData, "Roles");
        }

        // setup 'Master Roles' node
        ID masterRolesNode;
        {
            MenuItemData itemData = new MenuItemData();
            itemData.setToolTipText("Master roles (not being referenced by any other role).");

            masterRolesNode = menuModel.addItem(userNodeId, itemData, "Master Roles");
        }

        // Setup 'Sub Roles' node
        ID subRolesNode;
        {
            MenuItemData itemData = new MenuItemData();
            itemData.setToolTipText("Sub roles (referenced by other roles).");

            subRolesNode = menuModel.addItem(userNodeId, itemData, "Sub Roles");
        }

        // Setup user nodes
        {
            for (Role role : this.userServiceAdmin.getRoles()) {

                MenuItemData<Role> roleItemData = new MenuItemData<Role>();
                roleItemData.setItemRepresentative(role);
                roleItemData.setToolTipText(role.getDescription());

                roleItemData.setActions(ROLE_ITEM_ACTIONS);
                roleItemData.setSelectComponentHandler(new EditRoleComponentHandler(this.roleEditor, role));

                Map<Action, MenuActionProvider> actionComponentHandlerMap = new HashMap<Action, MenuActionProvider>();
                actionComponentHandlerMap.put(ACTION_DELETE_ROLE,
                        new DeleteRoleComponentHandler(this.roleDeleteEditor, role));
                roleItemData.setActionComponentHandlers(actionComponentHandlerMap);

                if (!role.isMasterRole()) {
                    menuModel.addItem(subRolesNode, roleItemData, role.getId());
                }
                else {
                    menuModel.addItem(masterRolesNode, roleItemData, role.getId());
                }
//                menuModel.addItem(userNodeId, userItemData, role.getId());
            }
        }
    }
}
