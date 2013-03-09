# APSWebTools

This is not an OSGi bundle! This is a plain jar containing utilities for web applications. Specifically APS administration web applications. This jar has to be included in each web application that wants to use it.

Among other things it provides support for being part of the APS administration web login (APSAdminWebLoginHandler). Since the APS administration web is built using Vaadin it has Vaadin support classes. APSVaadinOSGiApplication is a base class used by all APS administration webs.

## APIs

The following are the APIs for a few selected classes. The complete javadoc for this library can be found at [http://apidoc.natusoft.se/APSWebTools](http://apidoc.natusoft.se/APSWebTools).

----

public _class_ __APSAdminWebLoginHandler__ extends  APSLoginHandler  implements  APSLoginHandler.HandlerInfo    [se.natusoft.osgi.aps.tools.web] {

>  This is a login handler to use by any admin web registering with the APSAdminWeb to validate that there is a valid login available. 



__public APSAdminWebLoginHandler(BundleContext context)__

>  Creates a new APSAdminWebLoginHandler.  

_Parameters_

> _context_ - The bundle context. 

__public void setSessionIdFromRequestCookie(HttpServletRequest request)__

>  Sets the session id from a cookie in the specified request.  

_Parameters_

> _request_ - The request to get the session id cookie from. 

__public void saveSessionIdOnResponse(HttpServletResponse response)__

>  Saves the current session id on the specified response.  

_Parameters_

> _response_ - The response to save the session id cookie on. 









}

----

    

public _class_ __APSLoginHandler__ implements  LoginHandler    [se.natusoft.osgi.aps.tools.web] {

>  This class validates if there is a valid logged in user and also provides a simple login if no valid logged in user exists. 

> This utility makes use of APSAuthService to login auth and APSSessionService for session handling. Trackers for these services are created internally which requires the shutdown() method to be called when no longer used to cleanup. 

> The bundle needs to import the following packages for this class to work: 

       se.natusoft.osgi.aps.api.auth.user;version="[0.9,2)",
       se.natusoft.osgi.aps.api.misc.session;version="[0.9,2)"

> 



















__public APSLoginHandler(BundleContext context, HandlerInfo handlerInfo)__

>  Creates a new VaadinLoginDialogHandler.  

_Parameters_

> _context_ - The bundles BundleContext. 

__protected void setHandlerInfo(HandlerInfo handlerInfo)__

>  Sets the handler info when not provided in constructor.  

_Parameters_

> _handlerInfo_ - The handler info to set. 

__public void shutdown()__

>  Since this class internally creates and starts service trackers this method needs to be called on shutdown to cleanup! 

__public String getLoggedInUser()__

>  This returns the currently logged in user or null if none are logged in. 



__public boolean hasValidLogin()__

>  Returns true if this handler sits on a valid login. 

__public boolean login(String userId, String pw)__

>  Logs in with a userid and a password. 

> This method does not use or modify any internal state of this object! It only uses the APSAuthService that this object sits on. This allows code sitting on an instance of this class to use this method for validating a user without having to setup its own service tracker for the APSAuthService when this object is already available due to the code also being an APSAdminWeb member. It is basically a convenience.  

_Returns_

> true if successfully logged in, false otherwise.

_Parameters_

> _userId_ - The id of the user to login. 

> _pw_ - The password of the user to login. 

__public boolean login(String userId, String pw, String requiredRole)__

>  Logs in with a userid and a password, and a required role. 

> This method does not use or modify any internal state of this object! It only uses the APSAuthService that this object sits on. This allows code sitting on an instance of this class to use this method for validating a user without having to setup its own service tracker for the APSAuthService when this object is already available due to the code also being an APSAdminWeb member. It is basically a convenience.  

_Returns_

> a valid User object on success or null on failure.

_Parameters_

> _userId_ - The id of the user to login. 

> _pw_ - The password of the user to login. 

> _requiredRole_ - If non null the user is required to have this role for a successful login. If it doesn't null will 

public _static_ _interface_ __HandlerInfo__   [se.natusoft.osgi.aps.tools.web] {

>  Config values for the login handler. 

__String getSessionId()__

>  

_Returns_

> An id to an APSSessionService session.

__void setSessionId(String sessionId)__

>  Sets a new session id.  

_Parameters_

> _sessionId_ - The session id to set. 

__String getUserSessionName()__

>  

_Returns_

> The name of the session data containing the logged in user if any.

__String getRequiredRole()__

>  

_Returns_

> The required role of the user for it to be considered logged in.

}

----

    

public _interface_ __LoginHandler__   [se.natusoft.osgi.aps.tools.web] {

>  This is a simple API for doing a login. 

__public boolean hasValidLogin()__

>  Returns true if this handler sits on a valid login. 

__boolean login(String userId, String pw)__

>  Logs in with a userid and a password.  

_Returns_

> true if successfully logged in, false otherwise.

_Parameters_

> _userId_ - The id of the user to login. 

> _pw_ - The password of the user to login. 

__public void shutdown()__

>  If the handler creates service trackers or other things that needs to be shutdown when no longer used this method needs to be called when the handler is no longer needed. 

}

----

    

