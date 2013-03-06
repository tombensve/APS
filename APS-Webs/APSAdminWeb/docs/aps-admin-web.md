# APSAdminWeb

![APSAdminWeb screenshot](http://download.natusoft.se/Images/APS/APS-Webs/APSAdminWeb/docs/images/APSAdminWeb.png)

This is a web app for administration of APS. It is really only a shell for different administraion webs. It relys on the _aps-admin-web-service-provider_ bundle which publishes the _APSAdminWebService_. Other bundles providing administration web apps register themselves with this service and for each registration APSAdminWeb creates a tab in its gui. Se _APIs_ further down for the APSAdminService API. Clicking on ”Refresh” will make APSAdminWeb reload the admin webs registered in _APSAdminWebService_.

The APSAdminWeb is accessed at __http://host:port/apsadminweb__. What you see there depends on what other admin webs are deployed. Anybody can make an admin web and register it with the _APSAdminWebService_. The admin webs delivered with APS are mainly done using Vaadin. This is in no way a requirement for an admin web. An admin web app can be made in any way what so ever. A side effect of this is that different tabs might have different look and feel. But I offer that for flexibility.

The following APS bundles provides a tab in APSAdminWeb:

* _aps-config-admin-web.war_ - Allows advanced configuration of bundles/services using APSConfigService.

* _aps-user-admin-web-war_ - Administration of users and groups for APSSimpleUserService.

* _aps-ext-protocol-http-transport-provider.war_ - Provides a web gui with help for setting up and calling services remotely, and also shows all available services and allows calling them from the web gui for testing/debugging purposes.

## Authentication

The APSAdminWeb requires a login to be accessed. A userid and a password will be asked for. The entered information will be validated by the APSAuthService. The _aps-simple-user-service-auth-service-provider.jar_ bundle provides an implementation of this service that uses the _APSSimpleUserService_ service. The APSAuthService is however simple enough to implement yourself to provide login to whatever you want/need.

![APSAdminWeb login screenshot](http://download.natusoft.se/Images/APS/APS-Webs/APSAdminWeb/docs/images/APSAdminWeb-login.png)

## Making an admin web participating in the APSAdminWeb login.

There is an APSSessionService that was made just for handling this. It is not a HTTP session, just a service hanling sessions. It is provided by the _aps-session-service-provider.jar_ bundle. When a session is created you get a session id (an UUID) that needs to be passed along to the other admin webs through a cookie. _APSWebTools_ (_aps-web-tools.jar_ (not a bundle!)) provides the APSAdminWebLoginHandler class implementing the LoginHandler interface and handles all this for you.

You need to provide it with a BundleContext on creation since it will be calling both the _APSAuthService_ and _APSSessionService_:

        this.loginHandler = new APSAdminWebLoginHandler(bundleContext);
        

Then to validate that there is a valid login do:

        this.loginHandler.setSessionIdFromRequestCookie(request);
        if (this.loginHandler.hasValidLogin()) {
            ...
        }
        else {
            ...
        }

## APSAdminWebService APIs

public _interface_ __APSAdminWebService__   [se.natusoft.osgi.aps.apsadminweb.service] {

>  This service registers other specific administration web applications to make them available under a common administration gui. 

__public void registerAdminWeb(AdminWebReg adminWebReg) throws IllegalArgumentException__

>  Registers an admin web application.  

_Parameters_

> _adminWebReg_ - Registration information for the admin web. 

_Throws_

> _IllegalArgumentException_ - if the admin web has already been registered or if it is using the 

__public void unregisterAdminWeb(AdminWebReg adminWebReg)__

>  Unregisters a previously registered admin web. This is failsafe. If it has not been registered nothing happens.  

_Parameters_

> _adminWebReg_ - Registration information for the admin web. Use the same as registered with. 

__public List<AdminWebReg> getRegisteredAdminWebs()__

>  

_Returns_

> All currently registered admin webs.

}

----

    

public _class_ __AdminWebReg__   [se.natusoft.osgi.aps.apsadminweb.service.model] {

>  This model holds information about a registered admin web application. 









__public AdminWebReg(String name, String version, String description, String url)__

>  Creates a new AdminWebReg instance.  

_Parameters_

> _name_ - A (short) name of the admin web. 

> _version_ - The version of the admin web. 

> _description_ - A longer description of the admin web. 

> _url_ - The deployment url of the admin web. 

__public String getName()__

>  

_Returns_

> The (short) name of the admin web.

__public String getVersion()__

>  

_Returns_

> The version of the admin web.

__public String getDescription()__

>  

_Returns_

> The description of the admin web.

__public String getUrl()__

>  

_Returns_

> The deployment url of the admin web.







}

----

    

