/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         0.9.2
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2011-09-11: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web.vaadin;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This is registered in web.xml and installs itself in the session on session create
 * using its class name as key. Web applications can then get this from the session
 * and register themselves as "destroyed" listeners on this to be called when
 * _sessionDestroyed()_ is called on this.
 *
 * The main reason for this is to be able to shutdown and release any service trackers
 * that were started on session startup.
 */
public class APSSessionListener implements HttpSessionListener{
    //
    // Private Members
    //

    /** The listener to forward destroy events to. */
    private List<APSSessionDestroyedListener> destroyedListeners = new ArrayList<APSSessionDestroyedListener>();

    //
    // Methods
    //

    /**
     * Adds a session destroyed listener to forward session destroyed events to.
     *
     * @param destroyedListener
     */
    public void addDestroyedListener(APSSessionDestroyedListener destroyedListener) {
        this.destroyedListeners.add(destroyedListener);
    }

    /**
     * Gets called when a session is created.
     *
     * @param httpSessionEvent
     */
    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        httpSessionEvent.getSession().setAttribute(APSSessionListener.class.getName(), this);
    }

    /**
     * Gets called when a session is destroyed.
     *
     * @param httpSessionEvent
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        for (APSSessionDestroyedListener destroyedListener : this.destroyedListeners) {
            destroyedListener.sessionDestroyed();
        }
    }

    //
    // Inner Classes
    //

    /**
     * This should be implemented by class wanting to receive the sessionDestroyed event.
     */
    public static interface APSSessionDestroyedListener {

        /**
         * Gets called when the session is destroyed.
         */
        public void sessionDestroyed();
    }
}
