# Introduction

OSGi Application Platform Services - A "smorgasbord" of OSGi services that focuses on ease of use and good enough functionality for many but wont fit all. It can be seen as osgi-ee-light-and-easy. The services are of platform type: configuration, database, JPA, content, etc. 

All services that require some form of administration have an adming web application for that, that plugs into the general apsadminweb admin application.

All administrations web applications are WABs and thus require that the OSGi server supports WABs. 

Another point of APS is to be OSGi server independent. 

APS is made using basic OSGi functionality and is not using blueprint and other fancy stuff (of which I'm not a believer, I like to be in full controll :-)).   

## Features

### Current

* A configuration service that works with annotated configuration models where each config value can be described/documented. The configuration model can be structured with sub models that there can be one or many of. Each top level configuration model registered with the configuration service will be available for publishing in its admin web.

* A filesystem service that provides a persistent filesystem outside of the OSGi server. The configuration service makes use of this to store configurations.

* A platform service that simply identifies the local installation and provides a description of it. It is basically a read only service that provides configured information about the installation.

* A JPA service that is easier and more clearly defined than the osgi-ee JPA API, and allows for multiple JPA contexts. It works as an extender picking up persistence.xml whose defined persistence unit name can then be looked up using the service. A client can only lookup its own persistence units. It is based on OpenJPA.

* A data source service. Only provides connection information, no pooling (OpenJPA provides its own pooling)!

* External protocol extender that allows more or less any OSGi service to be called remotely using any deployed protocol service and transport. Currently provides JSONRPC 1.0 and 2.0 protocols, and an http transport. Protocols have two defined service APIs whose implementations can just be dropped in to make them available. The http transport can make use of any deployed protocol.

* A multicast discovery service.

* A session service (not http!). This is used by apsadminweb to keep a session among several different administration web applications.

* An administration web service to which administration web applications can register themselves with an url and thus be available in the .../apsadminweb admin gui.

* A user service. Provides basic user management including roles/groups. Is accompanied with a admin GUI (plugnis into apsadminweb) for administration of users. 

* A far better service tracker that does a better job at handling services coming and going. Supports service availability wait and timeout and can be wrapped as a proxy to the service. 

### Planned

* A log service with a log viewer GUI. The GUI should support server push and also allow for filtering of logs and configuration of what logs go to what log files.

* A JCR (Java Content Repository) service and a content publishing GUI (following the general APS ambition - reasonable flexibility, ease of use, but wont fit everyone).

* JDBC connection pool service (based on some open source connection pool implementation). Will use the Data source service to create connection pools.

* Since JBoss is apparently having trouble getting WABs to work (they are still using PAX, but claim that they have solved this in 7.2 that will not build when checked out from GitHub and don't seem to be released anytime soon) I am considering to add support for their WAR->OSGi service bridge though I haven't had much luck in getting that to work either so far. 

* Anything else relevant I come up with and consider fun to do :-).


# Setup

The Filesystem service is part of the core and used by other services. It should preferably have its filesystem root outside of the server installation. The BundleContext.getDataFile(String) returns a path within the deploy cache and is only valid for as long a a bundle is deployed. The point with the FilesystemService is to have a more permanent filesystem outside of the application server installation. To provide the FilesystemService root the following system property have to be set and available in the JVM instance:

        aps.filesystem.root=<root>

<!-- Note to self: This has to be easier! -->

How to do this differs between servers. In Glassfish you can supply system properties with its admin gui. 

After this path has been setup and the server started, all other configuration can be done in http://…/apsadminweb/. 

 
