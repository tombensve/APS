# APSDiscoveryService

This is actually a service directory that also multicasts on the network to find other instances of itself and makes the services in other instances available also.

A _service_ here means anything that can be called either with an URL or a host and port. It does not specifically indicate what type of service it is. Each published service however has an id that can be used to better identify it. Basically clients of services have to know the id of the service they want and by that know what it is and how to talk to it.

## APIs

public _class_ __APSDiscoveryPublishException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.net.discovery.exception] {

Thrown on service publish problems.

__public APSDiscoveryPublishException(String message)__

Creates a new _APSDiscoveryPublishException_ instance.

_Parameters_

> _message_ - The exception messaging. 

__public APSDiscoveryPublishException(String message, Throwable cause)__

Creates a new _APSDiscoveryPublishException_ instance.

_Parameters_

> _message_ - The exception messaging. 

> _cause_ - The cause of this exception. 

}

----

    

public _interface_ __ServiceDescription__   [se.natusoft.osgi.aps.api.net.discovery.model] {

Describes a service.

__String getDescription()__

A short description of the service.

__String getServiceId()__

An id/name of the service.

__String getVersion()__

The version of the service.

__String getServiceHost()__

The targetHost of the service.

__int getServicePort()__

The targetPort of the service.

__String getServiceURL()__

An optional URL to the service.

}

----

    

public _class_ __ServiceDescriptionProvider__ implements  ServiceDescription    [se.natusoft.osgi.aps.api.net.discovery.model] {

Describes a service.













__public ServiceDescriptionProvider()__

Creates a new ServiceDescirption.

__public String toString()__

Returns a string representation of this object.

__public String getDescription()__

A short description of the service.

__public void setDescription(String description)__

Sets a short description of the service.

_Parameters_

> _description_ - The description to set. 



__public void setServiceId(String serviceId)__

Sets the id of the service.

_Parameters_

> _serviceId_ - The service id to set. 



__public void setVersion(String version)__

Sets the version of the service.

_Parameters_

> _version_ - The version to set. 



__public void setServiceHost(String serviceHost)__

Sets the targetHost of the service.

_Parameters_

> _serviceHost_ - The service targetHost to set. 



__public void setServicePort(int servicePort)__

Sets the targetPort of the service.

_Parameters_

> _servicePort_ - The service targetPort to set. 



____

Sets an url to the service.

_Parameters_

> _serviceURL_ - The service url to set. 





}

----

    

public _interface_ __APSSimpleDiscoveryService__   [se.natusoft.osgi.aps.api.net.discovery.service] {

A network service discovery.

__public List<ServiceDescription> getRemotelyDiscoveredServices()__

Returns all remotely discovered services.

__public List<ServiceDescription> getLocallyRegisteredServices()__

Returns the locally registered services.

__public List<ServiceDescription> getAllServices()__

Returns all known services, both locally registered and remotely discovered.

__public List<ServiceDescription> getService(String serviceId, String version)__

Returns all discovered services with the specified id.

_Parameters_

> _serviceId_ - The id of the service to get. 

> _version_ - The version of the service to get. 

__public void publishService(ServiceDescription service) throws APSDiscoveryPublishException__

Publishes a local service. This will announce it to other known APSSimpleDiscoveryService instances.

_Parameters_

> _service_ - The description of the servcie to publish. 

_Throws_

> _APSDiscoveryPublishException_ - on problems to publish (note: this is a runtime exception!). 

__public void unpublishService(ServiceDescription service) throws APSDiscoveryPublishException__

Recalls the locally published service, announcing to other known APSSimpleDiscoveryService instances that this service is no longer available.

_Parameters_

> _service_ - The service to unpublish. 

_Throws_

> _APSDiscoveryPublishException_ - on problems to publish (note: this is a runtime exception!). 

}

----

    

