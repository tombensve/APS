# Introduction

OSGi Application Platform Services - A "smorgasbord" of OSGi services that focuses on ease of use and platform functionality. It is not for all, but for many. It comes with administration WABs and an AdminWeb that other admin web apps can plugin to. Contains publishable structured persistent documentable configuration. It can be seen as osgi-ee-light-and-easy.

Another point of APS is to be OSGi server independent. 

Some of the features of APS:

* A configuration service that works with annotated configuration models where each config value can be described/documented. The configuration model can be structured with sub models that there can be one or many of. Each top level configuration model registered with the configuration service will be available for publishing in APSConfigAdminWeb.

* A filesystem service that provides a persistent filesystem outside of the OSGi server. The configuration service makes use of this to store configurations.

* A platform service that simply identifies the local installation and provides a description of it. It is basically a read only service that provides configured information about the installation.

* A simple to use JPA service.

* A data source service. 

* A multicast discovery service.

* External protocol extender that allows more or less any OSGi service to be called remotely using any deployed protocol service and transport. Currently provides JSONRPC 1.0 and 2.0 protocols, and an http transport.

* A user service

* A session service (not http!). This is used by apsadminweb to keep a session among several different administration web applications.

* An administration web service to which administration web applications can register themselves with an url and thus be available in the .../apsadminweb admin gui.

* A far better service tracker that does a better job at handling services coming and going. Supports service availability wait and timeout and can be wrapped as a proxy to the service. 

Planned features of APS:





