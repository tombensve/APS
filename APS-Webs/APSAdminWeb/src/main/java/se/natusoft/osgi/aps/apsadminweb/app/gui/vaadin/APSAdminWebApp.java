/* 
 * 
 * PROJECT
 *     Name
 *         APS Administration Web
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         This is a web application providing and administration web frame.
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
 *         2011-08-27: Created!
 *         
 */
package se.natusoft.osgi.aps.apsadminweb.app.gui.vaadin;

import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import se.natusoft.osgi.aps.apsadminweb.service.APSAdminWebService;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.tools.web.APSAdminWebLoginHandler;
import se.natusoft.osgi.aps.tools.web.ClientContext;
import se.natusoft.osgi.aps.tools.web.vaadin.APSTheme;
import se.natusoft.osgi.aps.tools.web.vaadin.APSVaadinOSGiApplication;
import se.natusoft.osgi.aps.tools.web.vaadin.VaadinLoginDialogHandler;
import se.natusoft.osgi.aps.tools.web.vaadin.components.SidesAndCenterLayout;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the main administration app for APS. It uses APSAdminWebService to get registered admin web apps and
 * makes a tab for each found. In other words, this is just a front for different admin web apps. All an admin
 * web app needs to do to be available here is to register itself with APSAdminWebService (which includes the
 * url to the app). Each tabs content is a Vaadin Embedded component running the registered app.
 */
public class APSAdminWebApp extends APSVaadinOSGiApplication implements ClickListener, HttpServletRequestListener {
    //
    // Private Members
    //

    /** The main window. */
    private Window main = null;

    /** The application tabs. */
    private TabPanel tabPanel = null;

    /** The main window layout. */
    private VerticalLayout mainLayout = null;

    /** A tracker for the APSAdminWebService. */
    private APSServiceTracker<APSAdminWebService> adminWebServiceTracker = null;

    /** A login handler. */
    private APSAdminWebLoginHandler loginHandler = null;

    /** A gui login dialog handler. */
    private VaadinLoginDialogHandler loginDialogHandler = null;

    /** The session id if the admin web APSSession. */
//    private String sessionId = null;

    //
    // Vaadin GUI init
    //

    /**
     * Initializes services used by the application.
     *
     * @param clientContext The client context for accessing services.
     */
    @Override
    public void initServices(ClientContext clientContext) {
        this.adminWebServiceTracker = new APSServiceTracker<APSAdminWebService>(clientContext.getBundleContext(), APSAdminWebService.class, "2 minutes");
        this.adminWebServiceTracker.start();

        // This might seem unflexible, but it isn't! The getWrappedService() call returns a proxied implementation of the service
        // that uses the service tracker to get the service and forwards calls to it. It handles services coming and going. If a
        // service is not available it will wait for the specified timeout before failing with an APSNoServiceAvailableException
        // which is a runtime exception. This is a convenient use of the APSServiceTracker if you don't need more control over
        // service management yourself.
        clientContext.addService(APSAdminWebService.class, this.adminWebServiceTracker.getWrappedService());

        this.loginHandler = new APSAdminWebLoginHandler(clientContext.getBundleContext()) {

            @Override
            public boolean login(String userId, String pw) {
                boolean result = super.login(userId, pw);

                if (!result) {
                    if (APSAdminWebApp.this.main != null) {
                        APSAdminWebApp.this.main.showNotification("Login failed!", "Bad userid or password!", Window.Notification.TYPE_WARNING_MESSAGE);
                    }
                }

                return result;
            }
        };
    }

    /**
     * Called when session is about to die to cleanup anything setup in initServices().
     *
     * @param clientContex The context for the current client.
     */
    @Override
    public void cleanupServices(ClientContext clientContex) {
        this.adminWebServiceTracker.stop(clientContex.getBundleContext());
        if (this.loginHandler != null) {
            this.loginHandler.shutdown();
        }
    }

    /**
     * Creates the application GUI.
     */
    @Override
    public void initGUI() {

        this.setTheme(APSTheme.THEME);

        this.main = new Window("Application Platform Services Administration App");
        this.main.setSizeFull();

        this.mainLayout = new VerticalLayout();
        this.mainLayout.setSizeFull();

        SidesAndCenterLayout layout = new SidesAndCenterLayout();
        layout.setTop(new LogoPanel(this));
        this.tabPanel = new TabPanel(getClientContext());
        layout.setCenter(this.tabPanel);

        layout.doLayout();
        this.mainLayout.addComponent(layout);
        this.main.setContent(this.mainLayout);

        // And finally set the main window in the application to make it visible.
        setMainWindow(this.main);

        this.loginDialogHandler = new VaadinLoginDialogHandler(this.main, this.loginHandler);
        this.loginDialogHandler.setLoginDialogTitle("APS Admin Login");
    }

    //
    // Event Handler Methods
    //
    
    /**
     * Called when refresh button is clicked.
     * 
     * @param event The click event.
     */
    @Override
    public void buttonClick(ClickEvent event) {
        this.getMainWindow().showNotification("Refreshing ...");
        this.tabPanel.refreshTabs();
    }

    /**
     * This method is called before {@link com.vaadin.terminal.Terminal} applies the request to
     * Application.
     *
     * @param request
     * @param response
     */
    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        if (this.loginDialogHandler != null && this.loginHandler != null) {
            this.loginHandler.setSessionIdFromRequestCookie(request);
            if (!this.loginHandler.hasValidLogin()) {
                this.loginDialogHandler.doLoginDialog();
            }
            this.loginHandler.saveSessionIdOnResponse(response);
        }
    }

    /**
     * This method is called at the end of each request.
     *
     * @param request
     * @param response
     */
    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        // For some odd reason the cookie must be set *both* in onRequestStart() and here for it to work!
        if (this.loginHandler != null) {
            this.loginHandler.saveSessionIdOnResponse(response);
        }
    }
}
