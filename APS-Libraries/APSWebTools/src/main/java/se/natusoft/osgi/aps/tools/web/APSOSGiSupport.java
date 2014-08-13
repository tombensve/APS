/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         1.0.0
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
package se.natusoft.osgi.aps.tools.web;

import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.tools.APSActivator;

import javax.servlet.http.HttpSession;

/**
 * Support class.
 *
 * Use like this:
 *
 *     APSOSGiSupport support = APSOSGiSupport.getFromSession(session);
 *     if (support == null) {
 *         support = new APSOSGiSupport(this, session);
 *     }
 *
 * `this` should implements _APSOSGiSupport.APSOSGiSupportCallbacks_.
 *
 * This does the following:
 *
 * * Looks up the bundle context in the servlet and caches it locally withing the instance.
 *
 * * Uses APSActivator to manage and inject fields annotated with @OSGiService and @Managed of the
 *   instance passed as first argument to constructor. `void injectToInstance(Object instance)` is
 *   public and can be used to inject into other instances. Se APSActivator for more info on how
 *   this works.
 *
 * * Calls APSOSGiSupportCallbacks.initServices(BundleContext), APSOSGiSupportCallbacks.initGUI() in that order to setup.
 *
 * * Registers a session listener and calls APSOSGiSupportCallbacks.cleanupServices() when the session dies.
 *
 * * Provides getter for the cached BundleContext.
 *
 */
public class APSOSGiSupport implements OSGiBundleContextProvider, APSSessionListener.APSSessionDestroyedListener {
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

    private APSOSGiSupportCallbacks callbacks;

    /** We use part of the APSActivator functionality to inject into @OSGiService and @Managed annotated fields. */
    private APSActivator activator;

    private BundleContext bundleContext;

    //
    // Constructors
    //

    /**
     * Creates a new Instance and stores in in the session.
     *
     * @param callbacks Callbacks to call to init and cleanup. This should be a container created instance like a
     *                  servlet.
     * @param session The session.
     */
    public APSOSGiSupport(APSOSGiSupportCallbacks callbacks, HttpSession session) {
        this.callbacks = callbacks;

        this.activator = new APSActivator(callbacks);

        this.bundleContext = (BundleContext)session.getServletContext().getAttribute("osgi-bundlecontext");

        if (this.bundleContext == null) {
            System.err.println(NON_OSGI_DEPLOYMENT_SHORT_MESSAGE + " " + NON_OSGI_DEPLOYMENT_LONG_MESSAGE);
        }
        else {
            try {
                this.activator.start(this.bundleContext);
            }
            catch (Exception e) {
                System.err.println("Failed to start activator! Things might not work as expected!");
                e.printStackTrace(System.err);
            }

            this.callbacks.initServices(this.getBundleContext());

            // For the following to return an APSSessionListener instance it has to be specified as a listener in web.xml!
            APSSessionListener sessionListener = (APSSessionListener)session.getAttribute(APSSessionListener.class.getName());
            if (sessionListener != null) {
                sessionListener.addDestroyedListener(this);
            }

            this.callbacks.initGUI();

            session.setAttribute(APSOSGiSupport.class.getName(), this);
        }
    }

    //
    // Methods
    //

    /**
     * Returns an instance stored in the session or null if not stored.
     *
     * @param session The session to get instance from.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static APSOSGiSupport getFromSession(HttpSession session) {
        return (APSOSGiSupport)session.getAttribute(APSOSGiSupport.class.getName());
    }

    /**
     * This will return this war bundles _BundleContext_. This is only available if this war is
     * deployed in an R4.2+ compliant OSGi container. 
     * 
     * @return The OSGi bundle context. 
     */
    public BundleContext getBundleContext() {
        return this.bundleContext;
    }


    /**
     * Called when session is destroyed.
     */
    @Override
    public void sessionDestroyed() {
        try {
            this.activator.stop(getBundleContext());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        this.callbacks.cleanupServices(getBundleContext());
    }

    //
    // Inner Classes
    //

    public static interface APSOSGiSupportCallbacks {
        /**
         * Initializes the gui part of the application.
         */
        void initGUI();

        /**
         * Initializes the service setup.
         *
         * @param bundleContext The bundle context for accessing services.
         */
        void initServices(BundleContext bundleContext);

        /**
         * Called when the session is about to die to cleanup anything setup in _initServices()_.
         *
         * This method should be overridden by subclasses who need cleanup.
         *
         * @param bundleContext The bundle context for accessing services.
         */
        void cleanupServices(BundleContext bundleContext);


    }
}
