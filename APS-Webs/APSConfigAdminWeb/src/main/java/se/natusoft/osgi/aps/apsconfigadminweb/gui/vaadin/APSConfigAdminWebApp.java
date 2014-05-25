/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         0.10.0
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
 *     tommy ()
 *         Changes:
 *         2011-08-28: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.components.LeftBar;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.menu.ConfigEnvMenuBuilder;
import se.natusoft.osgi.aps.apsconfigadminweb.gui.vaadin.menu.ConfigMenuBuilder;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.tools.models.IntID;
import se.natusoft.osgi.aps.tools.web.APSAdminWebLoginHandler;
import se.natusoft.osgi.aps.tools.web.WebClientContext;
import se.natusoft.osgi.aps.tools.web.vaadin.APSVaadinOSGiApplication;
import se.natusoft.osgi.aps.tools.web.vaadin.components.SidesAndCenterLayout;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.MenuTree;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.MenuTree.MenuActionHandler;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi.MenuItemData;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.MenuActionExecutor;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.MenuActionProvider;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshable;

import javax.servlet.annotation.WebServlet;
import java.util.Map;

/**
 * The main Vaadin app class for the configuration administration application.
 */
@Title("Application Platform Services Configuration App")
@Theme("aps")
public class APSConfigAdminWebApp extends APSVaadinOSGiApplication implements MenuActionHandler {

    @WebServlet(value = "/apsconfigadminweb/*",
            asyncSupported = true)
    @VaadinServletConfiguration(
            productionMode = false,
            ui = APSConfigAdminWebApp.class)
    public static class Servlet extends VaadinServlet {}


    //
    // Private Members
    //

    /** A tracker for the APSConfigAdminService. */
    private APSServiceTracker<APSConfigAdminService> configAdminServiceTracker = null;

    /** Our logger. */
    private APSLogger logger = null;

    /** The main app window layout. */
    private SidesAndCenterLayout layout = null;

    /** The left side menu. */
    private MenuTree menuTree = null;

    /** A login handler. */
    private APSAdminWebLoginHandler loginHandler = null;

    //
    // Vaadin GUI init
    //

    private void handleLogin(WebClientContext clientContext) {

        if (this.loginHandler == null) {
            this.loginHandler = new APSAdminWebLoginHandler(clientContext.getBundleContext()) {

                @Override
                public boolean login(String userId, String pw) {
                    boolean result = super.login(userId, pw);

                    if (!result) {
                        getUserNotifier().warning("Login failed!", "Bad userid or password!");
                    }

                    return result;
                }
            };
        }

        this.loginHandler.setSessionIdFromRequestCookie(VaadinService.getCurrentRequest());

        if (!this.loginHandler.hasValidLogin()) {
            Window notAuthWindow = new Window("Application Platform Services Administration App");
            notAuthWindow.setClosable(false);
            notAuthWindow.setSizeFull();
            VerticalLayout nawvl = new VerticalLayout();
            Label loginMessage = new Label("<font size='+2'>Please login!</font>", ContentMode.HTML);
            nawvl.addComponent(loginMessage);
            notAuthWindow.setContent(nawvl);
            UI.getCurrent().addWindow(notAuthWindow);
        }

    }

    /**
     * Initializes services used by the application.
     *
     * @param clientContext The client context for accessing services.
     */
    @Override
    public void initServices(WebClientContext clientContext) {
        if (VaadinService.getCurrentRequest().getParameter("adminRefresh") != null) {
            close();
        }

        this.logger = new APSLogger(System.out);
        this.logger.start(clientContext.getBundleContext());

        this.configAdminServiceTracker =
                new APSServiceTracker<>(clientContext.getBundleContext(), APSConfigAdminService.class, APSServiceTracker.LARGE_TIMEOUT);
        this.configAdminServiceTracker.setLogger(this.logger);
        this.configAdminServiceTracker.start();

        clientContext.addService(APSConfigAdminService.class, this.configAdminServiceTracker.getWrappedService());

        handleLogin(clientContext);

    }

    /**
     * Called when session is about to die to cleanup anything setup in initServices().
     *
     * @param clientContext The context for the current client.
     */
    @Override
    public void cleanupServices(WebClientContext clientContext) {
        if (this.configAdminServiceTracker != null) {
            this.configAdminServiceTracker.stop(clientContext.getBundleContext());
            this.configAdminServiceTracker = null;
        }

        if (this.logger != null) {
            this.logger.stop(clientContext.getBundleContext());
            this.logger = null;
        }
    }

    /**
     * Creates the application GUI.
     */
    @Override
    public void initGUI() {

        VerticalLayout mainLayout = new VerticalLayout();
        setContent(mainLayout);
        mainLayout.setMargin(false);
        mainLayout.setSizeFull();

        this.layout = new SidesAndCenterLayout();
        mainLayout.addComponent(this.layout);

        LeftBar leftBar = new LeftBar();

        this.menuTree = new MenuTree();

        APSConfigAdminService configAdminService = getClientContext().getService(APSConfigAdminService.class);

        // The menu builder for editing configuration environments.
        ConfigEnvMenuBuilder configEnvMenuBuilder = new ConfigEnvMenuBuilder(configAdminService.getConfigEnvAdmin(), getUserNotifier());
        configEnvMenuBuilder.addRefreshable(this.menuTree);
        configEnvMenuBuilder.addRefreshable(new RemoveCenterRefreshable());
        this.menuTree.addMenuBuilder(configEnvMenuBuilder);

        // The menu builder for editing configurations.
        ConfigMenuBuilder configMenuBuilder = new ConfigMenuBuilder(configAdminService, this.logger, getUserNotifier());
        this.menuTree.addMenuBuilder(configMenuBuilder);

        this.menuTree.refresh();

        this.menuTree.addValueChangeListener(new ValueChangeListener() {
            /** Handles input changes in the menu tree. */
            @Override
            public void valueChange(ValueChangeEvent event) {
                menuTreeItemHandler((IntID)event.getProperty().getValue());
            }
        });


        this.menuTree.setActionHandler(this);
        leftBar.addComponent(this.menuTree);

        this.layout.setLeft(leftBar);
        this.layout.doLayout(); // This is required after contents have been set.

    }

    //
    // Event Handler Methods
    //

    /**
     * Private handler of ValueChangedListener for MenuTree.
     *
     * @param itemId The item to handle.
     */
    private void menuTreeItemHandler(IntID itemId) {
        if (itemId != null) {
            MenuItemData itemData = this.menuTree.getItemData(itemId);
            ComponentHandler guiHandler = itemData.getSelectComponentHandler();
            if (guiHandler != null) {
                if (this.layout.getCenter() == null) {
                    this.layout.setCenter(guiHandler.getComponent());
                }
                else {
                    if (this.layout.getCenter().equals(guiHandler.getComponent())) {
                        this.layout.setCenter(null);
                    }
                    else {
                        this.layout.setCenter(guiHandler.getComponent());
                    }
                }
            }
            else {
                this.layout.setCenter(null);
            }
        }
        else {
            this.layout.setCenter(null);
        }

        this.layout.doLayout();
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
    @SuppressWarnings("unchecked")
    public void handleAction(Action action, Object sender, Object target) {
        if (target != null) {
            IntID itemId = (IntID)target;
            Item item = this.menuTree.getItem(itemId);
            MenuItemData itemData = this.menuTree.getItemData(itemId);
            
            if (item != null) {
                Map<Action, MenuActionProvider> actionComponentHandlerMap = itemData.getActionComponentHandlers();

                if (actionComponentHandlerMap != null) {
                    MenuActionProvider menuActionProvider = actionComponentHandlerMap.get(action);

                    if (menuActionProvider != null) {

                        if (menuActionProvider instanceof ComponentHandler) {
                            this.layout.setCenter(((ComponentHandler)menuActionProvider).getComponent());
                            this.layout.doLayout();
                        }

                        if (menuActionProvider instanceof MenuActionExecutor) {
                            ((MenuActionExecutor)menuActionProvider).executeMenuAction();
                        }
                    }
                }
            }
        }
    }

    //
    // Inner Classes
    //

    /**
     * Clears the center when done.
     */
    private class RemoveCenterRefreshable implements Refreshable {

        /**
         * Refreshes its content.
         */
        @Override
        public void refresh() {
            APSConfigAdminWebApp.this.layout.setCenter(null);
            APSConfigAdminWebApp.this.layout.doLayout();
        }
    }
}
