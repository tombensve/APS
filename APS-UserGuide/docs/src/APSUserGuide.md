# Application Platform Services (APS)

OSGi Application Platform Services - A "smorgasbord" of OSGi services that focuses on ease of use and good enough functionality for many but wont fit all. It can be seen as osgi-ee-light-and-easy. The services are of platform type: configuration, database, JPA, etc, with companion web applications for administration.

All services that require some form of administration have an admin web application for that, that plugs into the general apsadminweb admin web application.

All administrations web applications are WABs and thus require that the OSGi server supports WABs. 

Another point of APS is to be as OSGi server independent as possible, but as said above the admin web applications do need support for WABs. 

APS is made using basic OSGi functionality and is not using blueprint and other fancy stuff! Each bundle has an activator that does setup, creates trackers, loggers, and manually dependency injects them into the service providers it publishes.

## Features

### Current

* A configuration service that works with annotated configuration models where each config value can be described/documented. The configuration model can be structured with sub models that there can be one or many of. Each top level configuration model registered with the configuration service will be available for publishing in the admin web. The configuration service also supports different configuration environments and allows for configuration values to be different for different configuration environments, but doesn´t require them to be.

* A filesystem service that provides a persistent filesystem outside of the OSGi server. The configuration service makes use of this to store configurations. Each client can get its own filesystem area, and can´t access anything outside of its area.

* A platform service that simply identifies the local installation and provides a description of it. It is basically a read only service that provides configured information about the installation.

* A JPA service that is easier and more clearly defined than the osgi-ee JPA API, and allows for multiple JPA contexts. It works as an extender picking up persistence.xml whose defined persistence unit name can then be looked up using the service. A client can only lookup its own persistence units. It is based on OpenJPA.

* A data source service. Only provides connection information, no pooling (OpenJPA provides its own pooling)!

* External protocol extender that allows more or less any OSGi service to be called remotely using any deployed protocol service and transport. Currently provides JSONRPC 1.0 & 2.0, JSONHTTP, and JSONREST protocols, and an http transport. Protocols have a defined service API whose implementations can just be dropped in to make them available. Transport providers can make use of any deployed protocol. The APSExternalProtocolService now provides support for REST services where there is a method for post, put, get,and delete, and the http transport makes use of this in conjunction with any protocol that indicates it can support REST like JSONREST.

* A group service that can send data to each member over transport safe multicast.

* A service discovery service using the group service.

* A session service (not http!). This is used by apsadminweb to keep a session among several different administration web applications.

* An administration web service to which administration web applications can register themselves with an url and thus be available in the .../apsadminweb admin gui.

* A user service. Provides basic user management including roles/groups. Is accompanied with a admin GUI (plugnis into apsadminweb) for administration of users. (org.osgi.service.useradmin.UserAdmin felt uncomplete. It did not provide what I wanted).

* A user authentication service. This does nothing more that authenticating a user and have a really simple API. APS provides an implementation that makes use of the user service, but it is easy to make another implementation that authenticates against an LDAP for example or something else. The Admin web applications uses the authentication service for authenticating admin users. 

* A far better service tracker that does a better job at handling services coming and going. Supports service availability wait and timeout and can be wrapped as a proxy to the service. Instead of returning null it throws an exception if no service becomes available within the timeout, and is thus much easier to handle.

### Planned

* An implementation of the standard OSGi LogService since not all servers provide one.

* A log veiwer web application supporting reqular expression filters on log information and a live log view. This is waiting on Vaadin 7.1 which will support server push.  Another alternative is to go pure GWT and use Errai for this, but I rather continue with Vaadin having all admin webs looking and feeling the same. 

* Synchronizing configurations between installations so that all configuration for all configuration environments can be edited in one place and automatically be distributed to each installation. This would also make it possible to configure installations without deployed admin webs.

* Anything else relevant I come up with and consider fun to do :-).

### Ideas

* A JCR (Java Content Repository) service and a content publishing GUI (following the general APS ambition - reasonable functionality and flexibility, ease of use. Will fit many, but not everyone).

* Since JBoss is apparently having trouble getting WABs to work (they are still using PAX, but claim that they have solved this in 7.2 that will not build when checked out from GitHub and don't seem to be released anytime soon) I am considering to add support for their WAR->OSGi service bridge though I haven't had much luck in getting that to work either so far. 

* Support for being able to redeploy a web application and services live without loosing session nor user transactions. With OSGi it should be teoretically possible. For a limited number of redeployments at least. It is very easy to run into the ”perm gen space” problem, but according to Frank Kieviet ([Classloader leaks: The dreaded permgen space](http://frankkieviet.blogspot.se/2006/10/classloader-leaks-dreaded-permgen-space.html)) it is caused by bad code and can be avoided. 

### What is new in version 0.9.1

* Now have full REST support in aps-external-protocol-extender and aps-ext-protocol-http-transport-provider.

* Documentation have been cleaned up a bit.

## Requirements

The administration web application(s) are currently WABs and thus require a server supporting WAB deployments. I have developed/tested this on Glassfish and Virgo. I am however considering seeing if it is possible to also support both Glassfish and JBoss JEE WAR to OSGi bridges. They are unfortunately very server specific since there are no such standard. Other than that all services are basic OSGi services and should theoretically run in any R4 compatible OSGi server. 

## Pre Setup

The Filesystem service is part of the core and used by other services. It should preferably have its filesystem root outside of the server installation. The BundleContext.getDataFile(String) returns a path within the deploy cache and is only valid for as long a a bundle is deployed. The point with the FilesystemService is to have a more permanent filesystem outside of the application server installation. To provide the FilesystemService root the following system property have to be set and available in the JVM instance:

        aps.filesystem.root=<root>

How to do this differs between servers. In Glassfish you can supply system properties with its admin gui. 

If this system property is not set the default root will be BundleContext.getFile(). This can work for development setup, but not for more serious installations!

After this path has been setup and the server started, all other configuration can be done in http://…/apsadminweb/. 

__Please note__ that the /apsadminweb by default require no login! This so that _”Configurations tab, Configurations/persistence/datasources”_ can be used to setup a datasource called ”APSSimpleUserServiceDS” needed by APSSimpleUserService. If you use the provided APSAuthService implementation that uses APSSimpleUserService then you need to configure this datasource before APSSimpleUserService can be used. See the documentation for APSSimpleUserService further down in this document for more information on the datasource configuration. After that is setup go to _”Configurations tab, Configurations/aps/adminweb”_ and enable the ”requireauthentication” config. After having enabled this and saved, do a browser refresh and then provide userid and password when prompted.

## Javadoc

The complete javadoc for all services can be found at [http://apidoc.natusoft.se/APS](http://apidoc.natusoft.se/APS).

