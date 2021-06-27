# Application Platform Services (APS)

----
  
Please note that this project have been going on for quite some time and have changed architecture on the way. This mostly due to limited time working on it, and partly to being a playground.
  
The documentation is put together from multiple sources in different maven submodules. A lot of this documentation is currently quite out of date. There are for example code examples done before lambdas! 
   
----

APS Platform Services - A "smorgasbord" of APSPlatform services that focuses on ease of use and good enough functionality for many but won't fit all. It is originally based on OSGi, but is not longer OSGi compliant, and actually haven't been for a while. The services are of platform type: configuration, database, JPA, etc.

## Features

### Current

* A service tracker (used OSGi before). Supports service availability wait and timeout and can be wrapped as a proxy to the service. Instead of returning null it throws an exception if no service becomes available within the timeout, and is thus much easier to handle.

* A configuration manager that extends deployed bundles by reading their configuration schema, their default configuration file, and their configuration id, and then loads and publishes an `APSConfig` instance with the bundles configuration. All active configurations are stored in a cluster (vertx/hazelcast). **NOTE: There will be a rethink of configuration handling!**

* (A filesystem service that provides a persistent filesystem outside of the OSGi server. The configuration service makes use of this to store configurations. Each client can get its own filesystem area, and can´t access anything outside of its area.) _Bad idea, will probably be removed!_ 


