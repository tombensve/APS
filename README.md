# Application Platform Services (APS)

Copyright © 2013 Natusoft AB

__Version:__ 0.10.0

__Author:__ Tommy Svensson (tommy@natusoft.se)

---

_A "smorgasbord" of OSGi services that focuses on ease of use and good enough functionality for many but wont fit all._

It can be seen as osgi-ee-ish that is easy to use. The services are of platform type: configuration, database, JPA, etc.

Please note that APS uses Java 7 code! It will not run in any J2ME environment! My intention with APS is a platform for writing modular web applications with easy administration.

Short feature list:

* Uses only basic OSGi functionallity, no SCR or blueprint.

* Provides a far more flexible and usable service tracker than the standard one, that allows you to set a timeout and throws an exception on timeout. The tracker can also provide a Proxied implementation of the service interface using the tracker to make calls. 

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

