# APSSessionService

This service provides session storage functionality. You can create a session, get an existing session by its id, and close a session. Each session can hold any number of named objects. 

Why a session service ? To begin with, this is not an HttpSession! That said, it was created to handle a single session among several web applications. This for the APS administration web which are made up of several web applications working toghether. This is explained in detail in the APSAdminWeb documentation. 

## APIs
