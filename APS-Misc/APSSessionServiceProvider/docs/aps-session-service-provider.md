# APSSessionService

This service provides session storage functionality. You can create a session, get an existing session by its id, and close a session. Each session can hold any number of named objects.

Why a session service ? To begin with, this is not an HttpSession! That said, it was created to handle a single session among several web applications. This for the APS administration web which are made up of several web applications working toghether. This is explained in detail in the APSAdminWeb documentation.

## APIs

public _interface_ __APSSession__   [se.natusoft.osgi.aps.api.misc.session] {

>  This represents an active session. 

__String getId()__

>  

_Returns_

> The id of this session.

__boolean isValid()__

>  

_Returns_

> true if this session is still valid.

__void saveObject(String name, Object object)__

>  Saves an object in the session. Will do nothing if the session is no longer valid.  

_Parameters_

> _name_ - The name to store the object under. 

> _object_ - An object to store in the session. 

__Object retrieveObject(String name)__

>  Returns a object stored under the specified name or null if no object is stored under that name. 

> If isValid() returns false then this will always return null.  

_Parameters_

> _name_ - The name of the object to get. 

}

----

    

public _interface_ __APSSessionService__   [se.natusoft.osgi.aps.api.misc.session] {

>  This is not a http session! It is a simple session that can be used by any code running in the same OSGi server. 

__public static int NO_TIMEOUT = 0__

>  Specifies no timeout. 

__public static int DEFAULT_TIMEOUT = -1__

>  Specifies the default timeout. 

__public static int SHORT_TIMEOUT = 5__

>  A relatively short timeout. 

__public static int MEDIUM_TIMEOUT = 10__

>  A medium timeout. 

__public static int LONG_TIMEOUT = 20__

>  A relatively long timeout. 

__public APSSession createSession(int timeoutInMinutes)__

>  Creates a new session.  

_Parameters_

> _timeoutInMinutes_ - The timeout in minutes. 

__public APSSession createSession(String sessionId, int timeoutInMinutes)__

>  Creates a new session. 

> The idea behind this variant is to support distributed sessions. The implementation must use a session id that is unique enough to support this. The APS implementation uses java.util.UUID.  

_Parameters_

> _sessionId_ - The id of the session to create. 

> _timeoutInMinutes_ - The timeout in minutes. 

__APSSession getSession(String sessionId)__

>  Looks up an existing session by its id.  

_Returns_

> A valid session having the specified id or null.

_Parameters_

> _sessionId_ - The id of the session to lookup. 

__void closeSession(String sessionId)__

>  Closes the session represented by the specified id. After this call APSSession.isValid() on an APSSession representing this session will return false.  

_Parameters_

> _sessionId_ - The id of the session to close. 

}

----

    

