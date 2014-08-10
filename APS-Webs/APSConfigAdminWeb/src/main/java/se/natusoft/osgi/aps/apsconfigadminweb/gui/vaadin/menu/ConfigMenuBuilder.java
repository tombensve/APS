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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-03-17: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.menu;

import com.vaadin.event.Action;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.VerticalLayout;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.ConfigEditor;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.css.CSS;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.models.ID;
import se.natusoft.osgi.aps.tools.web.UserNotifier;
import se.natusoft.osgi.aps.tools.web.vaadin.APSTheme;
import se.natusoft.osgi.aps.tools.web.vaadin.components.HTMLFileLabel;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi.MenuBuilder;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi.MenuItemData;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.MenuActionProvider;
import se.natusoft.osgi.aps.tools.web.vaadin.models.HierarchicalModel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds menu entries for configuration editing.
 */
public class ConfigMenuBuilder implements MenuBuilder<APSConfigAdmin> {
    //
    // Constants
    //

    /** Create a new configuration. */
    public static final Action ACTION_RESET_CONFIG = new Action("Reset config");

    /** Actions for the configuration root menu. */
    public static final Action[] CONFIG_ROOT_ACTIONS = new Action[] {};

    /** Actions for configuration menu items. */
    public static final Action[] CONFIG_ITEM_ACTIONS = new Action[] {ACTION_RESET_CONFIG};

    //
    // Private Members
    //

    /** The logger to log to. */
    private APSLogger logger = null;

    /** The APS configuration admin service providing the menu data. */
    private APSConfigAdminService configAdminService = null;

    /** For notifying users. */
    private UserNotifier userNotifier = null;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigMenuBuilder.
     *
     * @param configAdminService The APS configuration admin service providing menu data.
     * @param logger The logger to log to.
     * @param userNotifier For notifying the user.
     */
    public ConfigMenuBuilder(APSConfigAdminService configAdminService, APSLogger logger, UserNotifier userNotifier) {
        this.configAdminService = configAdminService;
        this.logger = logger;
        this.userNotifier = userNotifier;
    }

    //
    // Methods
    //

    /**
     * This should add menu entries to the received menu model.
     *
     * @param menuModel The model to add menu entries to.
     */
    @Override
    public void buildMenuEntries(HierarchicalModel<MenuItemData<APSConfigAdmin>> menuModel) {
        MenuItemData<APSConfigAdmin> itemData = new MenuItemData<>();
        itemData.setActions(CONFIG_ROOT_ACTIONS);
        itemData.setSelectComponentHandler(new ConfigurationsDescriptionHandler());

        // Sort according to group
        Map<String, List<APSConfigAdmin>> groups = new HashMap<>();
        for (APSConfigAdmin configAdmin : configAdminService.getAllConfigurations()) {
            String groupName = configAdmin.getGroup();
            if (groupName.equals("")) {
                groupName = "__none__";
            }
            List<APSConfigAdmin> groupItems = groups.get(groupName);
            if (groupItems == null) {
                groupItems = new LinkedList<>();
                groups.put(groupName, groupItems);
            }

            groupItems.add(configAdmin);
        }

        // Build menu

        ID configsId = menuModel.addItem(null, itemData, "Configurations");

        Map<String, ID> groupPathIds = new HashMap<>();

        for (String groupName : groups.keySet()) {
            List<APSConfigAdmin> configAdmins = groups.get(groupName);

            ID parent = configsId;
            if (!groupName.equals("__none__")) {
                // In this case we need to build a tree for the group.
                String groupPath = "";
                for (String pathPart : groupName.split("\\.")) {
                    groupPath = groupPath + pathPart;

                    ID pathId = groupPathIds.get(groupPath);
                    if (pathId == null) {
                        itemData = new MenuItemData<>();
                        Map<Action, MenuActionProvider> actionComponentHandlerMap = new HashMap<>();
                        itemData.setActionComponentHandlers(actionComponentHandlerMap);
                        pathId = menuModel.addItem(parent, itemData, pathPart);
                        groupPathIds.put(groupPath, pathId);
                    }
                    parent = pathId;
                }
            }

            for (APSConfigAdmin configAdmin : configAdmins) {

                APSConfigEditModel rootModel = configAdmin.getConfigModel();
                APSConfigReference rootRef = configAdmin.createRootRef();

                itemData = new MenuItemData<>();
                itemData.setItemRepresentative(configAdmin);
                itemData.setToolTipText(rootModel.getConfigId() + ":" + rootModel.getVersion() + "<hr/>" + rootModel.getDescription());
                itemData.setActions(CONFIG_ITEM_ACTIONS);
                itemData.setSelectComponentHandler(new ConfigEditor(rootRef, configAdmin, this.configAdminService, this.logger, this.userNotifier));

                String configId = configAdmin.getConfigId();
                int ix = configId.lastIndexOf('.');
                configId = configId.substring(ix+1);
                menuModel.addItem(parent, itemData, configId);
            }

//            // Sub config branches.
//            buildConfigBranch(menuModel, configModel, itemID, configAdmin);
        }
    }

// Undecided on what to do here, I'm leaving this code commented out for now.

//    /**
//     * Recursively builds menu entries for sub config branches.
//     *
//     * @param menuModel The menu model to add menu entries to.
//     * @param configEditModel The config model the menu entry represents.
//     * @param parentID The id of our parent.
//     * @param configAdmin This represents the whole configuration and are passed along as menu item data since it is needed to edit data later.
//     */
//    private void buildConfigBranch(
//            HierarchicalModel<MenuItemData<APSConfigAdmin>> menuModel,
//            APSConfigEditModel configEditModel,
//            ID parentID,
//            APSConfigAdmin configAdmin
//    ) {
//
//        for (APSConfigValueEditModel valueModel : configEditModel.getValues()) {
//            if (valueModel instanceof APSConfigEditModel) {
//                APSConfigEditModel branchModel = (APSConfigEditModel)valueModel;
//
//                MenuItemData itemData = new MenuItemData();
//                itemData.setItemRepresentative(configAdmin);
//                itemData.setToolTipText(branchModel.getConfigId() + ":" + branchModel.getVersion() + "<hr/>" + branchModel.getDescription());
//                String name = branchModel.getName();
//                if (branchModel.isMany()) {
//                    name = name + "*";
//                    int size = configAdmin.getSize(branchModel, this.activeConfigEnv);
//                }
//                ID itemID = menuModel.addItem(parentID, itemData, name);
//                buildConfigBranch(menuModel, branchModel, itemID, configAdmin);
//            }
//        }
//    }

    /**
     * This handles the "Config Environments" menu entry.
     */
    private static class ConfigurationsDescriptionHandler extends VerticalLayout implements ComponentHandler {

        /**
         * Creates a new ConfigEnvDescriptionHandler.
         */
        public ConfigurationsDescriptionHandler() {
            setMargin(true);
            this.setStyleName(CSS.APS_CONTENT_PANEL);

            addComponent(new HTMLFileLabel("/html/configs-description.html", APSTheme.THEME, getClass().getClassLoader()));
        }

        /**
         * @return The component that should handle the item.
         */
        @Override
        public AbstractComponent getComponent() {
            return this;
        }
    }

}
