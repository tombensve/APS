# APSExternalProtocolExtender

This is an OSGi bundle that makes use of the OSGi extender pattern. It listens to services being registered and unregistered and if the services bundles _MANIFEST.MF_ contains `APS-Externalizable: true` the service is made externally available. If the _MANIFEST.MF_ contains `APS-Externalizable: false` however making the service externally available is forbidden. A specific service can also be registered containing an _aps-externalizable_ property with value _true_ to be externalizable. It is also possible as an alternative to true/false specify a list of fully qualified service names to make only those services externally available. This overrides any other specification.

The exernal protocol extender also provides a configuration where services can be specified with their fully qualified name to be made externally available. If a bundle however have specifically specified false for the above manifest entry then the config entry will be ignored. 

So, what is meant by _made externally available_ ? Well what this bundle does is to analyze with reflection all services that are in one way or the other specified as being externalizable (manifest or config) and for all callable methods of the service an _APSExternallyCallable_ object will be created and saved locally with the service name.  _APSExternallyCallable_ extends _java.util.concurrent.Callable_, and adds the possibility to add parameters to calls and also provides meta data for the service method, and the bundle it belongs to. There is also an _APSRESTCallable_ that extends _APSExternallyCallable_ and also takes an http method and maps that to a appropriate service method.

## The overall structure

The complete picture for making services externally callable looks like this:

![EPERelations.png](http://download.natusoft.se/Images/APS/APS-Network/APSExternalProtocolExtender/docs/images/EPERelations.png)

This bundle provides the glue between the services and the protocols. Transports and protocols have to be provided by other bundles. 

The flow is like this:

1. Transport gets some request and an InputStream. 
2. Transport gets some user selected protocol (The APSExtProtocolHTTPTransportProvider allows specification of both protocol, protocol version, and service to call in the URL).
3. Transport calls _APSExternalProtocolService_ to get requested protocol. 
4. Transport calls protocol to parse InputStream and it returns an _RPCRequest_.
5. Transport uses the information in the RPCRequest to call a service using _APSExternalProtocolService_.
6. Transport takes the result from the call and passes to the protocol along with an OutputStream to write response on.

## APSExternalProtocolService

This bundle registers an _APSExternalProtocolService_ that will provide all _APSExternallyCallable_ instances (or rather copies of them since you can modify the one you get back by providing arguments). This service also provides getters for available remote protocols and you can register with it to receive information about chages for services and protocols.

### Protocols

There is a base API for protocols: _RPCProtocol_. APIs for different types of protocols should extend this. The protocol type APIs are service APIs and services implementing them must be provided by other bundles. This bundle looks for and keeps track of all such service providers.

The _StreamedRPCProtocol_ extends _RPCProtocol_ and provides a method for parsing a request from an _InputStream_ returning an _RPCRequest_ object.  This request object contains the name of the service, the method, and the parameters. This is enough for using _APSExternalProtocolService_ to do a call to the service. The request object is also used to write the call response on an OutputStream. There is also a method to write an error response. 

It is the responsibility of the transport provider to use a protocol to read and write requests and responses and to use the request information to call a service method. An exception is the case of http transports supporting REST that must take the responibility for returning an http status.

### Getting information about services and protocols.

A transport provider can register themselves with the _APSExternalProtocolService_ by implementing the _APSExternalProtocolListener_ interface. They will then be notified when a new externalizable service becomes available or is leaving and when a protocol becomes available or is leaving. 

## WARNING - Non backwards compatible changes!

This version have non backwards compatible changes! _StreamedRPCProtocol_ have changed in parameters for _parseRequest(...)_ and _isRest()_ is gone. _RPCProtocol_ have changes in parameters for crateRPCError(...). The error code is now gone. These changes was a necessity! The old was really bad and tried to solve REST support in a very stupid way. It is now handled very much more elegantly without any special support for it with _is_methods! 

The _APSExtProtocolHTTPTransportProvider_ now checks if an _RPCError_ (returned by createRPCError(...)) object actually is an _HTTPError_ subclass providing an HTTP error code to return. 

_parseRequest(...)_ parameters now also contain the class of the service and a new RequestIntention enum. The service class is only for inspecting methods for annoations or other possible meta data. The JSONREST protocol for example uses this to find annotations indicating GET, PUT, DELETE, etc methods, which is far more flexible than the old solution of requiring a get(), put(), etc method. The RequestIntention enum provides the following values: CREATE, READ, UPDATE, DELETE, UNKNOWN. That is CRUD + UNKNOWN. It will be UNKNOWN if the transport cannot determine such information. These are basically to support REST protocols without being too HTTP specific. Other transports can possible also make use of them.

## See also

_APSExtProtocolHTTPTransportProvider_ - Provides a HTTP transport.

 _APSStreamedJSONRPCProtocolProvider_ - Provides version 1.0 and 2.0 of JSONRPC, JSONHTTP and JSONREST.

## APIs
