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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-09-16: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web;

import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserService;
import se.natusoft.osgi.aps.api.auth.user.model.User;
import se.natusoft.osgi.aps.api.misc.session.APSSession;
import se.natusoft.osgi.aps.api.misc.session.APSSessionService;
import se.natusoft.osgi.aps.tools.APSServiceTracker;

/**
 * This class validates if there is a valid logged in user and also provides a simple login if no valid
 * logged in user exists.
 * <p/>
 * This utility makes use of APSSimpleUserService to login auth and APSSessionService for session handling.
 * Trackers for these services are created internally which requires the shutdown() method to be called
 * when no longer used to cleanup.
 * <p/>
 * The bundle needs to import the following packages for this class to work:
 * <pre>
 *     <code>
 *        se.natusoft.osgi.aps.api.auth.user;version="[1.0,2)",
 *        se.natusoft.osgi.aps.api.auth.user.model;version="[1.0,2)",
 *        se.natusoft.osgi.aps.api.misc.session;version="[1.0,2)"
 *     </code>
 * </pre>
 */
public class APSLoginHandler implements LoginHandler {
    //
    // Private Members
    //

    /** The currently logged in user. */
    private User loggedInUser = null;

    // In data

    /** The bundle context of the bundle using this class. */
    private BundleContext context = null;

    /** Config for this instance. */
    private HandlerInfo handlerInfo = null;

    // Used services

    /** A service tracker for APSSimpleUserService. */
    private APSServiceTracker<APSSimpleUserService> userServiceTracker = null;

    /** A tracker wrapped instance of the APSSimpleUserService. */
    private APSSimpleUserService userService = null;

    /** A service tracker for APSSessionService. */
    private APSServiceTracker<APSSessionService> sessionServiceTracker = null;

    /** A tracker wrapped instance of the APSSessionService. */
    private APSSessionService sessionService = null;

    //
    // Constructors
    //

    /**
     * Creates a new VaadinLoginDialogHandler.
     *
     * @param context The bundles BundleContext.
     */
    public APSLoginHandler(BundleContext context, HandlerInfo handlerInfo) {
        this.context = context;
        this.handlerInfo = handlerInfo;

        // Setup trackers for our used services.
        this.userServiceTracker =
                new APSServiceTracker<APSSimpleUserService>(this.context, APSSimpleUserService.class, APSServiceTracker.LARGE_TIMEOUT);
        this.userServiceTracker.start();
        this.userService = this.userServiceTracker.getWrappedService();

        this.sessionServiceTracker =
                new APSServiceTracker<APSSessionService>(this.context, APSSessionService.class, APSServiceTracker.LARGE_TIMEOUT);
        this.sessionServiceTracker.start();
        this.sessionService = this.sessionServiceTracker.getWrappedService();
    }

    //
    // Methods
    //

    /**
     * Sets the handler info when not provided in constructor.
     *
     * @param handlerInfo The handler info to set.
     */
    protected void setHandlerInfo(HandlerInfo handlerInfo) {
        this.handlerInfo = handlerInfo;
    }

    /**
     * Since this class internally creates and starts service trackers this method needs to be called on shutdown
     * to cleanup!
     */
    public void shutdown() {
        if (this.userServiceTracker != null) {
            this.userServiceTracker.stop(this.context);
            this.userServiceTracker = null;
            this.userService = null;
        }

        if (this.sessionServiceTracker != null) {
            this.sessionServiceTracker.stop(this.context);
            this.sessionServiceTracker = null;
            this.sessionService = null;
        }
    }

    /**
     * This returns the currently logged in user or null if none are logged in.
     */
    public User getLoggedInUser() {
        return this.loggedInUser;
    }

    /**
     * First tries to get a session if a session id is available, and if that fails a new session is created.
     * In any case a session is always returned.
     */
    private APSSession getOrCreateSession() {
        APSSession apsSession = null;
        if (this.handlerInfo.getSessionId() != null) {
            apsSession = this.sessionService.getSession(this.handlerInfo.getSessionId());
        }
        if (apsSession == null) {
            apsSession = this.sessionService.createSession(APSSessionService.DEFAULT_TIMEOUT);
            this.handlerInfo.setSessionId(apsSession.getId());
        }
        return apsSession;
    }

    /**
     * Returns true if this handler sits on a valid login.
     */
    public boolean hasValidLogin() {
        APSSession session = getOrCreateSession();
        this.loggedInUser = (User)session.retrieveObject(this.handlerInfo.getUserSessionName());

        if (this.loggedInUser == null || !this.loggedInUser.isAuthenticated()) {
            this.loggedInUser = null;
            return false;
        }

        return true;
    }

    /**
     * Logs in with a userid and a password.
     *
     * @param userId The id of the user to login.
     * @param pw The password of the user to login.
     *
     * @return true if successfully logged in, false otherwise.
     */
    public boolean login(String userId, String pw) {
        boolean loggedIn = false;

        User user = login(userId, pw, this.handlerInfo.getRequiredRole());
        if (user != null) {
            this.loggedInUser = user;
            APSSession apsSession = getOrCreateSession();
            apsSession.saveObject(this.handlerInfo.getUserSessionName(), user);
            loggedIn = true;
        }

        return loggedIn;
    }

    /**
     * Logs in with a userid and a password.
     * <p/>
     * This method does not use or modify any internal state of this object! It only uses the APSUserService that this object sits on.
     * This allows code sitting on an instance of this class to use this method for validating a user without having to setup its own
     * service tracker for the APSUserService when this object is already available due to the code also being an APSAdminWeb member.
     * It is basically a convenience.
     *
     * @param userId The id of the user to login.
     * @param pw The password of the user to login.
     * @param requiredRole If non null the user is required to have this role for a successful login. If it doesn't null will
     *                     be returned even if the user exists and the password is correct!
     *
     * @return a valid User object on success or null on failure.
     */
    public User login(String userId, String pw, String requiredRole) {
        User user = null;

        if (userId != null && pw != null) {
            User _user = this.userService.getUser(userId);
            if (_user != null) {
                if (this.userService.authenticateUser(_user, pw, APSSimpleUserService.AUTH_METHOD_PASSWORD)) {
                    if (requiredRole != null) {
                        if (_user.hasRole(requiredRole)) {
                            user = _user;
                        }
                    }
                    else {
                        user = _user;
                    }
                }
            }
        }

        return user;
    }

    //
    // Inner Classes
    //

    /**
     * Config values for the login handler.
     */
    public static interface HandlerInfo {

        /**
         * @return An id to an APSSessionService session.
         */
        String getSessionId();

        /**
         * Sets a new session id.
         *
         * @param sessionId The session id to set.
         */
        void setSessionId(String sessionId);

        /**
         * @return The name of the session data containing the logged in user if any.
         */
        String getUserSessionName();

        /**
         * @return The required role of the user for it to be considered logged in.
         */
        String getRequiredRole();
    }

}
