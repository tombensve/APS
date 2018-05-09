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
 *         2012-08-26: Created!
 *
 */
package se.natusoft.osgi.aps.apsuseradminweb.vaadin;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.VerticalLayout;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserServiceAdmin;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.Description;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.components.LeftBar;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.menubuilders.RolesMenuBuilder;
import se.natusoft.osgi.aps.apsuseradminweb.vaadin.menubuilders.UsersMenuBuilder;
import se.natusoft.osgi.aps.util.APSLogger;
import se.natusoft.osgi.aps.tracker.APSServiceTracker;
import se.natusoft.osgi.aps.model.IntID;
import se.natusoft.osgi.aps.tools.web.WebClientContext;
import se.natusoft.osgi.aps.tools.web.vaadin.APSVaadinOSGiApplication;
import se.natusoft.osgi.aps.tools.web.vaadin.components.SidesAndCenterLayout;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.MenuTree;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.builderapi.MenuItemData;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.ComponentHandler;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.MenuActionExecutor;
import se.natusoft.osgi.aps.tools.web.vaadin.components.menutree.handlerapi.MenuActionProvider;
import se.natusoft.osgi.aps.tools.web.vaadin.tools.Refreshable;

import javax.servlet.annotation.WebServlet;
import java.util.Map;

/**
 * This is the main app for editing users and roles. As Vaadin works, this also represents the session.
 * That is, each new session gets its own instance of this class.
 */
@Title("Application Platform Services SimpleUserService Administration App")
@Theme("aps")
@PreserveOnRefresh
public class APSUSerAdminWebApp extends APSVaadinOSGiApplication implements MenuTree.MenuActionHandler {

    @WebServlet(value = "/*",
            asyncSupported = true)
    @VaadinServletConfiguration(
            productionMode = false,
            ui = APSUSerAdminWebApp.class)
    public static class Servlet extends VaadinServlet {}

    //
    // Private Members
    //

    /** Our logger. */
    private APSLogger logger = null;

    /** A tracker for the APSSimpleUserServiceAdmin. */
    private APSServiceTracker<APSSimpleUserServiceAdmin> userServiceTracker = null;

    /** A proxied wrapper around the tracker that makes the service more transparent in use. */
    private APSSimpleUserServiceAdmin userService = null;

    /** The main app window layout. */
    private SidesAndCenterLayout layout = null;

    /** The left side menu. */
    private MenuTree menuTree = null;

    //
    // Vaadin GUI init
    //

    /**
     * Let our base class handle the login.
     *
     * @param clientContext The clients context.
     */
    @Override
    protected void handleLogin(WebClientContext clientContext) {
        useDefaultLoginHandler();
    }

    /**
     * Initializes services used by the application.
     *
     * @param clientContext The client context for accessing services.
     */
    @Override
    public void initServices(WebClientContext clientContext) {
        this.logger = new APSLogger(System.out);
        this.logger.start(clientContext.getBundleContext());

        this.userServiceTracker = new APSServiceTracker<>(
                clientContext.getBundleContext(),
                APSSimpleUserServiceAdmin.class,
                APSServiceTracker.LARGE_TIMEOUT
        );
        this.userServiceTracker.setLogger(this.logger);
        this.userServiceTracker.start();

        this.userService = this.userServiceTracker.getWrappedService();
    }

    /**
     * Called when session is about to die to cleanup anything setup in initServices().
     *
     * @param clientContext The context for the current client.
     */
    @Override
    public void cleanupServices(WebClientContext clientContext) {
        if (this.userServiceTracker != null) {
            this.userServiceTracker.stop(clientContext.getBundleContext());
            this.userServiceTracker = null;
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

        this.menuTree.addValueChangeListener(new Property.ValueChangeListener() {
            /** Handles input changes in the menu tree. */
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                menuTreeItemHandler(event);
            }
        });

        UsersMenuBuilder usersMenuBuilder = new UsersMenuBuilder(this.userService, this.logger);
        usersMenuBuilder.addRefreshable(this.menuTree);
        usersMenuBuilder.addRefreshable(new MenuRefreshable());
        usersMenuBuilder.setClearCenterRefreshable(new ClearCenterRefreshable());
        this.menuTree.addMenuBuilder(usersMenuBuilder);

        RolesMenuBuilder rolesMenuBuilder = new RolesMenuBuilder(this.userService, this.logger);
        rolesMenuBuilder.addRefreshable(this.menuTree);
        rolesMenuBuilder.addRefreshable(new MenuRefreshable());
        rolesMenuBuilder.setClearCenterRefreshable(new ClearCenterRefreshable());
        this.menuTree.addMenuBuilder(rolesMenuBuilder);

        this.menuTree.refresh();

        this.menuTree.setActionHandler(this);
        leftBar.addComponent(this.menuTree);

        this.layout.setLeft(leftBar);
        this.layout.setCenter(Description.DESCRIPTION_VIEW);
        this.layout.doLayout(); // This is required after contents have been set.

//        /* The window to show when an authorized user is not available. */
//        Window notAuthWindow = new Window("Application Platform Services Administration App");
//        notAuthWindow.setSizeFull();
//        VerticalLayout nawvl = new VerticalLayout();
//        Label loginMessage = new Label("<font size='+2'>Please login!</font>", ContentMode.HTML);
//        nawvl.addComponent(loginMessage);
//        notAuthWindow.setContent(nawvl);

    }

    /**
     * Private handler of ValueChangedListener for MenuTree.
     *
     * @param event The received event.
     */
    private void menuTreeItemHandler(Property.ValueChangeEvent event) {
        IntID itemId = (IntID)event.getProperty().getValue();

        if (itemId != null) {
            MenuItemData itemData = this.menuTree.getItemData(itemId);
            ComponentHandler guiHandler = itemData.getSelectComponentHandler();
            if (guiHandler != null) {
                this.layout.setCenter(guiHandler.getComponent());
            }
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

//    /**
//     * This method is called before {@link com.vaadin.terminal.Terminal} applies the request to
//     * Application.
//     *
//     * @param request
//     * @param response
//     */
//    @Override
//    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
//        if (this.loginHandler != null) {
//            this.loginHandler.setSessionIdFromRequestCookie(request);
//            if (this.loginHandler.hasValidLogin()) {
//                setMainWindow(this.main);
//            }
//            else {
//                try {
//                    response.sendError(401, "Authorization required!");
//                }
//                catch (IOException ioe) {
//                    this.logger.error("Failed to send error response!", ioe);
//                }
//            }
//        }
//
//        if (request.getParameter("adminRefresh") != null) {
//            close();
//        }
//    }
//
//    /**
//     * This method is called at the end of each request.
//     *
//     * @param request
//     * @param response
//     */
//    @Override
//    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
//    }

    //
    // Inner Classes
    //

    /**
     * Refreshes the menu when called.
     */
    private class MenuRefreshable implements Refreshable {

        /**
         * Refreshes its content.
         */
        @Override
        public void refresh() {
            APSUSerAdminWebApp.this.layout.doLayout();
        }
    }

    /**
     * Clears the center view when called.
     */
    private class ClearCenterRefreshable implements Refreshable {

        /**
         * Refreshes its content.
         */
        @Override
        public void refresh() {
            APSUSerAdminWebApp.this.layout.setCenter(Description.DESCRIPTION_VIEW);
        }
    }
}
