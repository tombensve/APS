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
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService.APSConfigEnvAdmin;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.ConfigEnvEditor;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.css.CSS;
import se.natusoft.osgi.aps.tools.models.ID;
import se.natusoft.osgi.aps.tools.web.vaadin.APSTheme;
import se.natusoft.osgi.aps.tools.web.vaadin.components.HTMLFileLabel;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi.MenuBuilder;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi.MenuItemData;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.MenuActionProvider;
import se.natusoft.osgi.aps.tools.web.vaadin.models.HierarchicalModel;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshable;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.RefreshableSupport;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshables;

import java.util.HashMap;
import java.util.Map;

/**
 * This builds menu entries for config environments and also provides support for those.
 */
public class ConfigEnvMenuBuilder implements MenuBuilder<APSConfigAdmin>, RefreshableSupport {
    //
    // Constants
    //

    /** Create a new configuration environment. */
    public static final Action ACTION_NEW_CONFIG_ENV = new Action("New config env");

    /** Set new active configuration environment. */
    public static final Action ACTION_SET_ACTIVE_CONFIG_ENV = new Action("Set as active");

    /** Delete a configuration environment. */
    public static final Action ACTION_DELETE_CONFIG_ENV = new Action("Delete config env");

    /** Actions for the configuration environment root menu. */
    public static final Action[] CONFIG_ENV_ROOT_ACTIONS = new Action[] {ACTION_NEW_CONFIG_ENV};

    /** Actions for configuration environment menu items. */
    public static final Action[] CONFIG_ENV_ITEM_ACTIONS = new Action[] {ACTION_SET_ACTIVE_CONFIG_ENV, ACTION_DELETE_CONFIG_ENV};


    //
    // Private Members
    //

    /** The APS configuration admin. */
    private APSConfigEnvAdmin configEnvAdmin = null;
    
    /** The refreshables to trigger on refresh. */
    private Refreshables refreshables = new Refreshables();

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEnvMenuBuilder.
     *
     * @param configEnvAdmin The APS configuration admin to get information from.
     */
    public ConfigEnvMenuBuilder(APSConfigEnvAdmin configEnvAdmin) {
        this.configEnvAdmin = configEnvAdmin;
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
     * This should add menu entries to the received menu model.
     *
     * @param menuModel The model to add menu entries to.
     */
    @Override
    public void buildMenuEntries(HierarchicalModel<MenuItemData<APSConfigAdmin>> menuModel) {
        // This will be initialized with the root node and then sub nodes will be added to this.
        ID configEnvsId = null;

        // Setup root node for config environments.
        {
            MenuItemData itemData = new MenuItemData();

            itemData.setActions(CONFIG_ENV_ROOT_ACTIONS);
            itemData.setSelectComponentHandler(new ConfigEnvDescriptionHandler());

            Map<Action, MenuActionProvider> actionComponentHandlerMap = new HashMap<Action, MenuActionProvider>();
            actionComponentHandlerMap.put(ACTION_NEW_CONFIG_ENV,
                    new ConfigEnvEditor(null, this.configEnvAdmin, this.refreshables, ConfigEnvEditor.EDIT_ACTION));
            itemData.setActionComponentHandlers(actionComponentHandlerMap);

            configEnvsId = menuModel.addItem(null, itemData, "Config Environments");
        }

        // Setup one sub node per config environment.
        {
            APSConfigEnvironment activeConfigEnv = this.configEnvAdmin.getActiveConfigEnvironment();

            for (APSConfigEnvironment configEnv : this.configEnvAdmin.getAvailableConfigEnvironments()) {
                MenuItemData itemData = new MenuItemData();

                itemData.setToolTipText(configEnv.getDescription());
                itemData.setActions(CONFIG_ENV_ITEM_ACTIONS);
                itemData.setSelectComponentHandler(
                        new ConfigEnvEditor(configEnv, this.configEnvAdmin, this.refreshables, ConfigEnvEditor.EDIT_ACTION)
                );

                Map<Action, MenuActionProvider> actionComponentHandlerMap2 = new HashMap<Action, MenuActionProvider>();
                actionComponentHandlerMap2.put(ACTION_DELETE_CONFIG_ENV,
                        new ConfigEnvEditor(configEnv, this.configEnvAdmin, this.refreshables, ConfigEnvEditor.DELETE_ACTION));
                actionComponentHandlerMap2.put(ACTION_SET_ACTIVE_CONFIG_ENV,
                        new ConfigEnvEditor(configEnv, this.configEnvAdmin, this.refreshables, ConfigEnvEditor.CHANGE_ACTIVE_ACTION));
                itemData.setActionComponentHandlers(actionComponentHandlerMap2);

                String menuName = configEnv.getName();
                if (configEnv.equals(activeConfigEnv)) {
                    menuName += " [Active]";
                }

                menuModel.addItem(configEnvsId, itemData, menuName);
            }
        }
    }


    /**
     * This handles the "Config Environments" menu entry.
     */
    private static class ConfigEnvDescriptionHandler extends VerticalLayout implements ComponentHandler {

        /**
         * Creates a new ConfigEnvDescriptionHandler.
         */
        public ConfigEnvDescriptionHandler() {
            setMargin(true);
            this.setStyleName(CSS.APS_CONTENT_PANEL);

            addComponent(new HTMLFileLabel("/html/config-env-description.html", APSTheme.THEME, getClass().getClassLoader()));
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
