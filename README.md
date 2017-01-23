# Application Platform Services (APS)

Copyright © 2013 Natusoft AB

__Version:__ 1.0.0

This repo is used for development and will be delived to APS repo when released. 

__Author:__ Tommy Svensson (tommy@natusoft.se)

---

_A "smorgasbord" of OSGi services that focuses on ease of use and good enough functionality for many but wont fit all._

It can be seen as osgi-ee-ish that is easy to use. The services are of platform type: configuration, database, JPA, etc.

Please note that APS uses Java 8 code! It will not run in any J2ME environment! My intention with APS is a platform for writing modular web applications with easy administration.

Short feature list:

* Uses only basic OSGi functionallity, no SCR or blueprint, etc. 

* _APSTools/APSActivator_ provides a generic BundleActivator implementation that makes use of annotations to do DI and more:
  * @Managed - basic DI but with special feature for providing name for APSLogger when injecting such. 
  * @OSGiServiceProvider - Register a class as an OSGi service using first implemented interface if not specified in annotation. Properties for the service registration can also be provided. It also supports special instance factories for registering multiple instances.
  * @OSGiService - As default this annotation should be used on a service interface member type and will then have an instance of _APSServiceTracker_ wrapped as proxied service. That is, each call on a service method will allocate the service the standard OSGi way, call the service method, release the service, and then return the method call result. Note that _APSServiceTracker_ works different that the standard _SeriveTracker_ since it never returns a null service, but throws an _APSNoServiceAvailable_ exception instead after the service has not become available for a specified timeout time. _APSServiceTracker_ can also be the member type for this annotation in which case an _APSServiceTracker_ instance will be injected directly instead of being wrapped as a service.

* _APSOSGiTestTools_ provides testing support by implementing the 4 basic OSGi interfaces enough to deploy and run in test. Contains an easy to use API for deploying from target/classes or maven GAV which will use ~/.m2/repository to lookup code to deploy. The API (Java) is designed so that it aalso works as DSL when used from Groovy. Just let JUnit tests extend _APSOSGiTestTools_, deploy dependent on services, deploy service to test, deploy local in test Bundle that will get a BundleContext and can lookup, inject, whatever to test. Very easy and clean. Support for mocking configuration, but otherwise little mocking is requred. You just deploy and run as you would in a real server. 

* Provides a far more flexible and usable service tracker than the standard one, that allows you to set a timeout and throws an exception on timeout. The tracker can also provide a Proxied implementation of the service interface using the tracker to make calls. APS has the goal of keeping things upp rather than tear things down when services temporarily goes away.

* Provides an authenticationable and pluggable administration web which different administration applications can plug into participating in a common login (SSO). APSConfigAdminWeb and APSUserAdminWeb are two such plugin admin applications.

* Provides a high level configuration service where annotated configuration models are automatically registered and populated using the extender pattern. 
   * Advanced structured config models with config values and submodels and lists of values and submodels.
   * Supports multiple configuration environments.
   * A web application (WAB) for editing/publishing configurations.
   * Can synchronize configuration between installations.

* Provides easy to use authentication and user services. These are easier and more comprehensable than the UserAdmin service!
   * Provides an easy to use admin web for administration of users and roles.

* Provides a JPA service (using OpenJPA) that is more flexible and easier to use than the EntityManagerFactoryBuilder.

* Provides remote calls via aps-external-protocol-extender that exposes services specified in MANIFEST.MF or in configuration to be remotely available using pluggable transports and protocols. 
   * Provides an HTTP transport.
   * Provides a JSONRPC 1.0, 2.0, and a very simplistic JSON protocol.
   * Protocols are just OSGi services.

* More useful utilities and services ...

---

Documentation ([Markdown](https://github.com/tombensve/APS/blob/master/APS-UserGuide/docs/APS-UserGuide.md) | [PDF](https://github.com/tombensve/APS/blob/master/APS-UserGuide/docs/APS-UserGuide.pdf))

[Licenses](https://github.com/tombensve/APS/blob/master/lics/licenses.md)

