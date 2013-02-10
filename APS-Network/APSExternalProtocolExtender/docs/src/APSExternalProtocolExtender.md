# APSExternalProtocolExtender

This is an OSGi bundle that makes use of the OSGi extender pattern. It listens to services being registered and unregistered and if the services bundles _MANIFEST.MF_ contains ”APS-Externalizable: true” the service is made externally available. If the _MANIFEST.MF_ contains ”APS-Externalizable: false” however making the service externally available is forbidden.

The exernal protocol extender also provides a configuration where services can be specified with their fully qualified name to be made externally available. If a bundle however have specifically specified false for the above manifest entry then the config entry will be ignored. 

So, what is meant by ”made externally available” ? Well what this bundle does is to analyze with reflection all services that are in one way or the other specified as being externalizable (manifest or config) and for all callable methods of the service an _APSExternallyCallable_ object will be created and saved locally with the service name.  _APSExternallyCallable_ extends _java.util.concurrent.Callable_, and adds the possibility to add parameters to calls and also provides meta data for the service method, and the bundle it belongs to. 

## The overall structure

The complete picture for making services externally callable looks like this:

	Transport --> APSExternalProtocolService --+--> Callable services and service methods. <--+
	                                           |                                              |
	                                           +--> Communication protocols ------------------+

This bundle provides the glue between the services and the protocols. Transports and protocols have to be provided by other bundles.

## APSExternalProtocolService

This bundle registers an _APSExternalProtocolService_ that will provide all _APSExternallyCallable_ instances (or rather copies of them since you can modify the one you get back by providing arguments). This service also provides getters for available remote protocols and you can register with it to receive information about chages for services and protocols.

### Protocols

There is a base API for protocols: RPCProtocol. APIs for different types of protocols should extend this. There is currently only one type of protocol available: _StreamedRPCProtocol_. The protocol type APIs are service APIs and services implementing them must be provided by other bundles. This bundle looks for and keeps track of all such service providers.

The _StreamedRPCProtocol_ provides a method for parsing a request from an InputStream returning an RPCRequest object.  This request object contains the name of the service, the method, and the parameters. This is enough for using _APSExternalProtocolService_ to do a call to the service. The request object is also used to write the call response on an OutputStream. There is also a method to write an error response. 

It is the responsibility of the transport provider to use a protocol to read and write requests and responses and to use the request information to call a service method. 

### Getting information about services and protocols.

A transport provider can register themselves with the _APSExternalProtocolService_ by implementing the _APSExternalProtocolListener_ interface. They will then be notified when a new externalizable service becomes available or is leaving and when a protocol becomes available or is leaving. 

## See also

_APSExtProtocolHTTPTransportProvider_ - Provides a HTTP transport.

 _APSStreamedJSONRPCProtocolProvider_ - Provides version 1.0 and 2.0 of JSONRPC.

## APIs
