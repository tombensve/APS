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

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.WrappedSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.tools.APSActivator;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.web.OSGiBundleContextProvider;
import se.natusoft.osgi.aps.tools.web.UserNotifier;
import se.natusoft.osgi.aps.tools.web.WebClientContext;

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
        BundleContext bundleContext = (BundleContext)VaadinServlet.getCurrent().getServletContext().getAttribute("osgi-bundlecontext");

        if (bundleContext == null) {
            Notification.show(
                    NON_OSGI_DEPLOYMENT_SHORT_MESSAGE,
                    NON_OSGI_DEPLOYMENT_LONG_MESSAGE,
                    Notification.Type.ERROR_MESSAGE);
            this.logger.error(NON_OSGI_DEPLOYMENT_SHORT_MESSAGE + " " + NON_OSGI_DEPLOYMENT_LONG_MESSAGE);
        }

        return bundleContext;
    }

    protected UserNotifier getUserNotifier() {
        return this.userNotifier;
    }

    /**
     * Initializes the vaadin application.
     */
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
    public WebClientContext getClientContext() {
        return this.clientContext;
    }

    /**
     * Called when session is destroyed.
     */
    @Override
    public void sessionDestroyed() {
        if (this.activator != null) {
            try {
                this.activator.stop(getBundleContext());
            }
            catch (Exception e) {
                if (this.logger != null) {
                    this.logger.error("Failed to stop activator!", e);
                }
                else {
                    e.printStackTrace(System.err);
                }
            }
        }
        try {
            cleanupServices(this.clientContext);
        }
        finally {
            if (this.logger != null) {
                this.logger.stop(getBundleContext());
            }
        }
    }
}
