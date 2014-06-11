/*
 *
 * PROJECT
 *     Name
 *         APS Web Tools
 *
 *     Code Version
 *         0.10.0
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
 *     tommy ()
 *         Changes:
 *         2011-08-27: Created!
 *
 */
package se.natusoft.osgi.aps.tools.web.vaadin;

import com.vaadin.server.*;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.tools.APSActivator;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.web.APSAdminWebLoginHandler;
import se.natusoft.osgi.aps.tools.web.OSGiBundleContextProvider;
import se.natusoft.osgi.aps.tools.web.UserNotifier;
import se.natusoft.osgi.aps.tools.web.WebClientContext;

import javax.servlet.ServletContext;

/**
 * APS base class for Vaadin application providing OSGi support.
 *
 * This does the following:
 *
 * * Looks up the bundle context in the servlet context and informs the user that it must be
 *   deployed in an OSGi server to function if not found.
 *
 * * Creates a ClientContext containing the BundleContext, but can also be used to store
 *   services in.
 *
 * * Uses APSActivator to inject and manage the subclass fields annotated with @OSGiService and @Managed.
 *   Also provides a public `injectToInstance(Object instance)` method to manage injections of other
 *   instances. This is an alternative to initServices() and cleanupServices(). See APSActivator for
 *   more information.
 *
 * * Calls overrideable initServices(ClientContext), initGUI() in that order to setup.
 *
 * * Registers a session listener and calls overrideable cleanupServices() when the session dies.
 *
 * * Provides getters for both the BundleContext and the ClientContext.
 *
 * This version requires Vaadin 7 to work!
 */
public abstract class APSVaadinOSGiApplication
        extends UI
        implements OSGiBundleContextProvider, APSSessionListener.APSSessionDestroyedListener {
    //
    // Private Constants
    //

    /** The short message for non OSGi deployment. */
    private static final String NON_OSGI_DEPLOYMENT_SHORT_MESSAGE = "Application wrongly deployed!";

    /** The long message for non OSGi deployment. */
    private static final String NON_OSGI_DEPLOYMENT_LONG_MESSAGE = "This application is an OSGi bundle and must be deployed as such in " +
            "an OSGi container for it to be able to do its work!";

    //
    // Private Members
    //

    APSLogger logger;

    /** The client context. */
    private WebClientContext clientContext;

    /** We use part of the APSActivator functionality to inject into @OSGiService and @Managed annotated fields. */
    private APSActivator activator;

    private UserNotifier userNotifier = new VaadinUserNotifier();

    /** A login handler. */
    private APSAdminWebLoginHandler loginHandler = null;


    //
    // Methods
    //

    /**
     * @return The Vaadin session.
     */
    protected WrappedSession getHttpSession() {
        return VaadinServletService.getCurrentRequest().getWrappedSession();
    }

    /**
     * This will return this war bundles _BundleContext_. This is only available if this war is
     * deployed in an R4.2+ compliant OSGi container.
     *
     * @return The OSGi bundle context.
     */
    @Override
    public BundleContext getBundleContext() {
        VaadinServlet vaadinServlet = VaadinServlet.getCurrent();
        if (vaadinServlet != null) {
            ServletContext servletContext = vaadinServlet.getServletContext();
            if (servletContext != null) {
                BundleContext bundleContext = (BundleContext) servletContext.getAttribute("osgi-bundlecontext");

                if (bundleContext == null) {
                    Notification.show(
                            NON_OSGI_DEPLOYMENT_SHORT_MESSAGE,
                            NON_OSGI_DEPLOYMENT_LONG_MESSAGE,
                            Notification.Type.ERROR_MESSAGE);
                    this.logger.error(NON_OSGI_DEPLOYMENT_SHORT_MESSAGE + " " + NON_OSGI_DEPLOYMENT_LONG_MESSAGE);
                }

                return bundleContext;
            }
            else {
                this.logger.error("Failed to get ServletContext!");
            }
        }
        else {
            this.logger.error("Failed to get VaadinServlet!");
        }

        return null;
    }

    /**
     * Returns the user notifier for presenting messages to the user.
     */
    protected UserNotifier getUserNotifier() {
        return this.userNotifier;
    }

    /**
     * Initializes the vaadin application.
     */
    @Override
    public void init(VaadinRequest request) {

        this.logger = new APSLogger(System.err);
        this.logger.setLoggingFor("APSVaadinOSGiApp" + getClass().getSimpleName());
        this.logger.start(getBundleContext());

        UserNotifier messager = new VaadinUserNotifier();
        this.clientContext = new WebClientContext(messager, this, request);

        this.activator = new APSActivator(this);
        try {
            this.activator.start(getBundleContext());
        }
        catch (Exception e) {
            this.logger.error("Failed to start activator!", e);
        }

        handleLogin(this.clientContext);

        initServices(this.clientContext);

        // For the following to return an APSSessionListener instance it has to be specified as a listener in web.xml!
        APSSessionListener sessionListener = (APSSessionListener)getHttpSession().getAttribute(APSSessionListener.class.getName());
        if (sessionListener != null) {
            sessionListener.addDestroyedListener(this);
        }

        initGUI();
    }

    /**
     * Initializes the gui part of the applicaiton.
     */
    protected void initGUI() {}

    /**
     * Initializes the service setup.
     *
     * @param clientContext The client context for accessing services.
     */
    protected void initServices(WebClientContext clientContext) {}

    /**
     * Called when the session is about to die to cleanup anything setup in _initServices()_.
     *
     * This method should be overriden by subclasses who need cleanup.
     *
     * @param clientContext The client cntext for accessing services.
     */
    protected void cleanupServices(WebClientContext clientContext) {}

    /**
     * @return The client context.
     */
    protected WebClientContext getClientContext() {
        return this.clientContext;
    }

    /**
     * Called when session is destroyed.
     */
    @Override
    public void sessionDestroyed() {
        cleanupServices(this.clientContext);
    }

    /**
     * This gets called to handle a login. This implementation does nothing at all!
     * But if subclasses override this they can do their own login handling or just
     * call defaultLoginHandler().
     *
     * @param clientContext
     */
    protected void handleLogin(WebClientContext clientContext) {
        // Do nothing. This needs to be overridden to handle login.
    }

    /**
     * Provides a default login handler that anyone can use.
     */
    protected void defaultLoginHandler() {

        if (this.loginHandler == null) {
            this.loginHandler = new APSAdminWebLoginHandler(this.clientContext.getBundleContext()) {

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

        // TODO: Fix.
        if (!this.loginHandler.hasValidLogin()) {
            Window notAuthWindow = new Window("Login required");
            notAuthWindow.setClosable(false);
            notAuthWindow.setSizeFull();
            VerticalLayout nawvl = new VerticalLayout();
            Label loginMessage = new Label("<font size='+2'>Please login!</font>", ContentMode.HTML);
            nawvl.addComponent(loginMessage);
            notAuthWindow.setContent(nawvl);
            UI.getCurrent().addWindow(notAuthWindow);
        }

    }

}
