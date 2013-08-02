/* 
 * 
 * PROJECT
 *     Name
 *         APS User Admin Web
 *     
 *     Code Version
 *         0.9.2
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
import se.natusoft.osgi.aps.api.auth.user.model.User;
import se.natusoft.osgi.aps.api.auth.user.model.UserAdmin;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.componenthandlers.DeleteUserComponentHandler;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.componenthandlers.EditUserComponentHandler;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.Description;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.editors.UserEditor;
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
import java.util.Set;
import java.util.TreeSet;

/**
 * This builds menu entries for users.
 */
public class UsersMenuBuilder implements MenuBuilder<User>, RefreshableSupport, Serializable {
    //
    // Constants
    //

    /** Create a new user. */
    public static final Action ACTION_NEW_USER = new Action("New user");

    /** Delete a user. */
    public static final Action ACTION_DELETE_USER = new Action("Delete user");

    /** Actions for the user root menu. */
    public static final Action[] USER_ROOT_ACTIONS = new Action[] {ACTION_NEW_USER};

    /** Actions for user menu items. */
    public static final Action[] USER_ITEM_ACTIONS = new Action[] {ACTION_DELETE_USER};


    //
    // Private Members
    //

    /** The user service to create/modify/delete users with. */
    private APSSimpleUserServiceAdmin userServiceAdmin = null;

    /** The refreshables to trigger on refresh. */
    private Refreshables refreshables = new Refreshables();

    /** A refreshable that clears the center view. */
    private Refreshable clearCenterRefreshable = null;

    /** The editor for old and new users. */
    private UserEditor userEditor = null;

    //
    // Constructors
    //

    /**
     * Creates a new UserMenuBuilder
     *
     * @param userServiceAdmin The user service to create/modify/delete users with.
     * @param logger The app logger.
     */
    public UsersMenuBuilder(APSSimpleUserServiceAdmin userServiceAdmin, APSLogger logger) {
        this.userServiceAdmin = userServiceAdmin;
        this.userEditor = new UserEditor(this.userServiceAdmin);
        this.userEditor.setLogger(logger);
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
     * Sets a Refreshable that clears the center view.
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
    public void buildMenuEntries(HierarchicalModel<MenuItemData<User>> menuModel) {
        this.userEditor.setRefreshables(this.refreshables);
        this.userEditor.setClearCenterRefreshable(this.clearCenterRefreshable);

        // This will be initialized with the root node and then sub nodes will be added to this.
        ID userNodeId;

        // Setup root node.
        {
            MenuItemData<User> itemData = new MenuItemData<>();

            itemData.setActions(USER_ROOT_ACTIONS);
            itemData.setSelectComponentHandler(Description.DESCRIPTION_VIEW);
            itemData.setToolTipText("Right click to add a new user!");

            Map<Action, MenuActionProvider> actionComponentHandlerMap = new HashMap<>();
            actionComponentHandlerMap.put(ACTION_NEW_USER,
                    new EditUserComponentHandler(this.userEditor, null));
            itemData.setActionComponentHandlers(actionComponentHandlerMap);

            userNodeId = menuModel.addItem(null, itemData, "Users");
        }

        // Setup user nodes
        {
            Map<String, Set<UserAdmin>> usersByStartLetter = new HashMap<>();
            Set<String> startLetters = new TreeSet<>();

            for (UserAdmin user : this.userServiceAdmin.getUsers()) {
                String startLetter = user.getId().substring(0,1).toUpperCase();
                Set<UserAdmin> userSet = usersByStartLetter.get(startLetter);
                if (userSet == null) {
                    userSet = new TreeSet<>();
                    usersByStartLetter.put(startLetter, userSet);
                }
                userSet.add(user);
                startLetters.add(startLetter);
            }

            ID letterNodeId;

            for (String startLetter : startLetters) {
                Map<Action, MenuActionProvider> actionComponentHandlerMap = new HashMap<>();

                MenuItemData<User> letterItemData = new MenuItemData<>();
                letterItemData.setActionComponentHandlers(actionComponentHandlerMap);
                letterItemData.setSelectComponentHandler(Description.DESCRIPTION_VIEW);

                letterNodeId = menuModel.addItem(userNodeId, letterItemData, startLetter);

                for (UserAdmin user : usersByStartLetter.get(startLetter)) {



                    MenuItemData<User> userItemData = new MenuItemData<>();
                    userItemData.setItemRepresentative(user);
                    userItemData.setToolTipText(user.getUserProperties().getProperty(User.USER_NAME));

                    userItemData.setActions(USER_ITEM_ACTIONS);
                    userItemData.setSelectComponentHandler(new EditUserComponentHandler(this.userEditor, user));

                    actionComponentHandlerMap = new HashMap<>();
                    actionComponentHandlerMap.put(ACTION_DELETE_USER,
                            new DeleteUserComponentHandler(this.userEditor, user));
                    userItemData.setActionComponentHandlers(actionComponentHandlerMap);

                    menuModel.addItem(letterNodeId, userItemData, user.getId());
                }
            }
        }
    }
}
