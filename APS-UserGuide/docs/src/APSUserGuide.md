# Application Platform Services (APS)

----
  
Please note that this project have been going on for quite some time and have changed architecture on the way. This mostly due to limited time working on it, and partly to being a playground.
  
The documentation is put together from multiple sources in different maven sub modules. A lot of this documentation is currently quite out of date. There are for example code examples done before lambdas! There are also maven modules that will be removed due to no longer being relevant for the current architecture, like JPA (which I originally did just to have something that worked on any OSGi container, rather than a container specific integrated solution). JPA is not compatible with current architecture which mostly works with JSON data.  
   
----

OSGi Application Platform Services - A "smorgasbord" of OSGi services that focuses on ease of use and good enough functionality for many but wont fit all. It can be seen as osgi-ee-light-and-easy. The services are of platform type: configuration, database, JPA, etc, with companion web applications for administration.

All services that require some form of administration have an admin web application for that, that plugs into the general apsadminweb admin web application.

All administrations web applications are WABs and thus require that the OSGi server supports WABs. 

Another point of APS is to be as OSGi server independent as possible, but as said above the admin web applications do need support for WABs. 

APS is made using basic OSGi functionality and is not using blueprint and other fancy stuff! Each bundle has an activator that does setup, creates trackers, loggers, and manually dependency injects them into the service providers it publishes.

## Features

### Current

* A far better service tracker that does a better job at handling services coming and going. Supports service availability wait and timeout and can be wrapped as a proxy to the service. Instead of returning null it throws an exception if no service becomes available within the timeout, and is thus much easier to handle.

* A configuration manager that extends deployed bundles by reading their configuration schema, their default configuration file, and their configuration id, and then loads and publishes an `APSConfig` instance with the bundles configuration. All active configurations are stored in a cluster (vertx/hazelcast). There will be a config web to edit configurations.

* A filesystem service that provides a persistent filesystem outside of the OSGi server. The configuration service makes use of this to store configurations. Each client can get its own filesystem area, and can´t access anything outside of its area.

## Pre Setup

The Filesystem service is part of the core and used by other services. It should preferably have its filesystem root outside of the server installation. The BundleContext.getDataFile(String) returns a path within the deploy cache and is only valid for as long a a bundle is deployed. The point with the FilesystemService is to have a more permanent filesystem outside of the application server installation. To provide the FilesystemService root the following system property have to be set and available in the JVM instance:

        aps.filesystem.root=<root>

How to do this differs between servers. In Glassfish you can supply system properties with its admin gui. 

If this system property is not set the default root will be BundleContext.getFile(). This can work for development setup, but not for more serious installations!

After this path has been setup and the server started, all other configuration can be done in http://.../apsadminweb/. 

