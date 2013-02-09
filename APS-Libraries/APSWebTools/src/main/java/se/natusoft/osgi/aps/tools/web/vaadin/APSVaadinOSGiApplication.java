/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         0.9.0
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

import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.tools.web.ClientContext;
import se.natusoft.osgi.aps.tools.web.OSGiBundleContextProvider;
import se.natusoft.osgi.aps.tools.web.UserMessager;

import javax.servlet.http.HttpSession;

/**
 * APS base class for Vaadin application providing OSGi support.
 * <p/>
 * This does the following:
 * <ul>
 *     <li>Looks up the bundle context in the servlet context and informs the user that it must be
 *     deployed in an OSGi server to function if not found.</li>
 *
 *     <li>Creates a ClientContext containing the BundleContext, but can also be used to store
 *     services in.</li>
 *
 *     <li>Calls overridable initServices(ClientContext), initGUI() in that order to setup.</li>
 *
 *     <li>Registers a session listener and calls overridable cleanupServices() when the session dies.</li>
 *
 *     <li>provides getters for both the BundleContext and the ClientContext.</li>
 * </ul>
 */
public abstract class APSVaadinOSGiApplication
        extends com.vaadin.Application
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

    /** The client context. */
    private ClientContext clientContext;

    //
    // Methods
    //

    /**
     * @return The http session.
     */
    private HttpSession getHttpSession() {
        ApplicationContext ctx = getContext();
        WebApplicationContext webCtx = (WebApplicationContext) ctx;
        return webCtx.getHttpSession();
    }

    /**
     * This will return this war bundles BundleContext. This is only available if this war is
     * deployed in an R4.2+ compliant OSGi container. 
     * 
     * @return The OSGi bundle context. 
     */
    @Override
    public BundleContext getBundleContext() {
        BundleContext bundleContext = (BundleContext)getHttpSession().getServletContext().getAttribute("osgi-bundlecontext");
        
        if (bundleContext == null && this.getMainWindow() != null) {
           this.getMainWindow().showNotification(
                   NON_OSGI_DEPLOYMENT_SHORT_MESSAGE,
                   NON_OSGI_DEPLOYMENT_LONG_MESSAGE,
                                Notification.TYPE_ERROR_MESSAGE);
            System.err.println(NON_OSGI_DEPLOYMENT_SHORT_MESSAGE + " " + NON_OSGI_DEPLOYMENT_LONG_MESSAGE);
        }
        
        return bundleContext;
    }

    /**
     * Initializes the vaadin application.
     */
    public void init() {
        UserMessager messager = new VaadinUserMessager();
        this.clientContext = new ClientContext(messager, this);

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
    protected void initServices(ClientContext clientContext) {}

    /**
     * Called when the session is about to die to cleanup anything setup in initServices().
     * <p/>
     * This method should be overriden by subclasses who need cleanup.
     *
     * @param clientContext The client cntext for accessing services.
     */
    protected void cleanupServices(ClientContext clientContext) {}

    /**
     * Intercepts setMainWindow() and supplies it to the VaadinUserMessager created in init() and used to
     * display user messages.
     *
     * @param mainWindow
     */
    public void setMainWindow(Window mainWindow) {
        super.setMainWindow(mainWindow);
        ((VaadinUserMessager)this.clientContext.getMessager()).setMessageWindow(mainWindow);
    }

    /**
     * @return The client context.
     */
    public ClientContext getClientContext() {
        return this.clientContext;
    }

    /**
     * Called when session is destroyed.
     */
    @Override
    public void sessionDestroyed() {
        cleanupServices(this.clientContext);
    }
}
