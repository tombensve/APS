# APSExternalProtocolExtender

This is an OSGi bundle that makes use of the OSGi extender pattern. It listens to services being registered and unregistered and if the services bundles _MANIFEST.MF_ contains `APS-Externalizable: true` the service is made externally available. If the _MANIFEST.MF_ contains `APS-Externalizable: false` however making the service externally available is forbidden. A specific service can also be registered containing an _aps-externalizable_ property with value _true_ to be externalizable. This overrides any other specification.

The exernal protocol extender also provides a configuration where services can be specified with their fully qualified name to be made externally available. If a bundle however have specifically specified false for the above manifest entry then the config entry will be ignored.

So, what is meant by _made externally available_ ? Well what this bundle does is to analyze with reflection all services that are in one way or the other specified as being externalizable (manifest or config) and for all callable methods of the service an _APSExternallyCallable_ object will be created and saved locally with the service name. _APSExternallyCallable_ extends _java.util.concurrent.Callable_, and adds the possibility to add parameters to calls and also provides meta data for the service method, and the bundle it belongs to. There is also an _APSRESTCallable_ that extends _APSExternallyCallable_ and also takes an http method and maps that to a appropriate service method.

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

The _StreamedRPCProtocol_ extends _RPCProtocol_ and provides a method for parsing a request from an _InputStream_ returning an _RPCRequest_ object. This request object contains the name of the service, the method, and the parameters. This is enough for using _APSExternalProtocolService_ to do a call to the service. The request object is also used to write the call response on an OutputStream. There is also a method to write an error response.

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

public _interface_ __APSExternalProtocolService__   [se.natusoft.osgi.aps.api.external.extprotocolsvc] {

This service makes the currently available externalizable services available for calling. It should be used by a bundle providing an externally available way of calling a service (JSON over http for example) to translate and forward calls to the local service. The locally called service is not required to be aware that it is called externally.

__Never cache any result of this service!__ Always make a new call to get the current state. Also note that it is possible that the service represented by an APSExternallyCallable have gone away after it was returned, but before you do call() on it! In that case an APSNoServiceAvailableException will be thrown. Note that you can register as an APSExternalProtocolListener to receive notifications about externalizable services coming and going, and also protocols coming and going to keep up to date with the current state of things.

__public Set<String> getAvailableServices()__

Returns all currently available services.

__public List<APSExternallyCallable> getCallables(String serviceName)__

Returns all APSExternallyCallable for the named service object.

_Parameters_

> _serviceName_ - The name of the service to get callables for. 

__public Set<String> getAvailableServiceFunctionNames(String serviceName)__

Returns the names of all available functions of the specified service.

_Parameters_

> _serviceName_ - The service to get functions for. 

__public APSExternallyCallable getCallable(String serviceName, String serviceFunctionName)__

Gets an APSExternallyCallable for a specified service name and service function name.

_Returns_

> An APSExternallyCallable instance or null if the combination of service and serviceFunction is not available.

_Parameters_

> _serviceName_ - The name of the service object to get callable for. 

> _serviceFunctionName_ - The name of the service function of the service object to get callable for. 

__public List<RPCProtocol> getAllProtocols()__

_Returns_

> All currently deployed providers of RPCProtocol.

__public RPCProtocol getProtocolByNameAndVersion(String name, String version)__

Returns an RPCProtocol provider by protocol name and version.

_Returns_

> Any matching protocol or null if nothing matches.

_Parameters_

> _name_ - The name of the protocol to get. 

> _version_ - The version of the protocol to get. 

__public List<StreamedRPCProtocol> getAllStreamedProtocols()__

_Returns_

> All currently deployed providers of StreamedRPCProtocol.

__public StreamedRPCProtocol getStreamedProtocolByNameAndVersion(String name, String version)__

Returns a StreamedRPCProtocol provider by protocol name and version.

_Returns_

> Any matching protocol or null if nothing matches.

_Parameters_

> _name_ - The name of the streamed protocol to get. 

> _version_ - The version of the streamed protocol to get. 

__public void addExternalProtocolListener(APSExternalProtocolListener externalServiceListener)__

Add a listener for externally available services.

_Parameters_

> _externalServiceListener_ - The listener to add. 

__public void removeExternalProtocolListener(APSExternalProtocolListener externalServiceListener)__

Removes a listener for externally available services.

_Parameters_

> _externalServiceListener_ - The listener to remove. 

}

----

    

public _interface_ __APSExternallyCallable<ReturnType>__ extends  Callable<ReturnType>    [se.natusoft.osgi.aps.api.external.extprotocolsvc.model] {

This API represents one callable service method.

__public String getServiceName()__

_Returns_

> The name of the service this callable is part of.

__public String getServiceFunctionName()__

_Returns_

> The name of the service function this callable represents.

__public DataTypeDescription getReturnDataDescription()__

_Returns_

> A description of the return type.

__public List<ParameterDataTypeDescription> getParameterDataDescriptions()__

_Returns_

> A description of each parameter type.

__public Bundle getServiceBundle()__

_Returns_

> The bundle the service belongs to.

__public Class getServiceClass()__

Returns the class of the service implementation.

__ReturnType call(Object... arguments) throws Exception__

Calls the service method represented by this APSExternallyCallable.

_Returns_

> The return value of the method call if any or null otherwise.

_Parameters_

> _arguments_ - Possible arguments to the call. 

_Throws_

> _Exception_ - Any exception the called service method threw. 

}

----

    

public _interface_ __APSExternalProtocolListener__   [se.natusoft.osgi.aps.api.external.extprotocolsvc.model] {

A listener for externally available services. Please note that this means that the service is available for potential external protocol exposure! For it to be truly available there also has to be a protocol and transport available. It is probably only transports that are interested in this information!

__public void externalServiceAvailable(String service, String version)__

This gets called when a new externally available service becomes available.

_Parameters_

> _service_ - The fully qualified name of the newly available service. 

> _version_ - The version of the service. 

__public void externalServiceLeaving(String service, String version)__

This gets called when an externally available service no longer is available.

_Parameters_

> _service_ - The fully qualified name of the service leaving. 

> _version_ - The version of the service. 

__public void protocolAvailable(String protocolName, String protocolVersion)__

This gets called when a new protocol becomes available.

_Parameters_

> _protocolName_ - The name of the protocol. 

> _protocolVersion_ - The version of the protocol. 

__public void protocolLeaving(String protocolName, String protocolVersion)__

This gets called when a new protocol is leaving.

_Parameters_

> _protocolName_ - The name of the protocol. 

> _protocolVersion_ - The version of the protocol. 

}

----

    

public _interface_ __APSRESTCallable__ extends  APSExternallyCallable    [se.natusoft.osgi.aps.api.external.extprotocolsvc.model] {

This is a special variant of APSExternallyCallable that supports a HTTP REST call.

This is only available when a service have zero or one method whose name starts with put, zero or one method whose name starts with post, and so on. There has to be at least one method of put, post, get or delete.

APSExternalProtocolService can provide an instance of this is a service matches the criteria.

This is only of use for HTTP transports! aps-ext-protocol-http-transport-provider does make use of this for protocols that indicate they support REST.

__public boolean supportsPut()__

_Returns_

> true if the service supports the PUT method.

__public boolean supportsPost()__

_Returns_

> true if the service supports the POST method.

__public boolean supportsGet()__

_Returns_

> true if the service supports the GET method.

__public boolean supportsDelete()__

_Returns_

> true if the service supports the DELETE method.

__public void selectMethod(HttpMethod method)__

This selects the method to call with this callable.

_Parameters_

> _method_ - The selected method to call. 

public _static_ _enum_ __HttpMethod__   [se.natusoft.osgi.aps.api.external.extprotocolsvc.model] {

This defines the valid choices for selectMethod(...).

}

----

    



}

----

    

public _class_ __APSRESTException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.net.rpc.errors] {

This is a special exception that services can throw if they are intended to be available as REST services through the aps-external-protocol-extender + aps-ext-protocol-http-transport-provider. This allows for better control over status codes returned by the service call.



__public APSRESTException(int httpStatusCode)__

Creates a new _APSRESTException_.

_Parameters_

> _httpStatusCode_ - The http status code to return. 

__public APSRESTException(int httpStatusCode, String message)__

Creates a new _APSRESTException_.

_Parameters_

> _httpStatusCode_ - The http status code to return. 

> _message_ - An error messaging. 

__public int getHttpStatusCode()__

Returns the http status code.

}

----

    

public _enum_ __ErrorType__   [se.natusoft.osgi.aps.api.net.rpc.errors] {

This defines what I think is a rather well though through set of error types applicable for an RPC call. No they are not mine, they come from Matt Morley in his JSONRPC 2.0 specification at [http://jsonrpc.org/spec.html](http://jsonrpc.org/spec.html).

I did however add the following:

* SERVICE_NOT_FOUND - Simply because this can happen in this case!

* AUTHORIZATION_REQUIRED - This is also a clear possibility.

* BAD_AUTHORIZATION

__PARSE_ERROR__

Invalid input was received by the server. An error occurred on the server while parsing request data.

__INVALID_REQUEST__

The request data sent is not a valid.

__METHOD_NOT_FOUND__

The called method does not exist / is not available.

__SERVICE_NOT_FOUND__

The called service does not exist / is not available.

__INVALID_PARAMS__

The parameters to the method are invalid.

__INTERNAL_ERROR__

Internal protocol error.

__SERVER_ERROR__

Server related error.

__AUTHORIZATION_REQUIRED__

Authorization is required, but none was supplied.

__BAD_AUTHORIZATION__

Bad authorization was supplied.

}

----

    

public _interface_ __HTTPError__ extends  RPCError    [se.natusoft.osgi.aps.api.net.rpc.errors] {

Extends _RPCError_ with an HTTP status code. HTTP transports can make use of this information.

__public int getHttpStatusCode()__

_Returns_

> Returns an http status code.

}

----

    

public _interface_ __RPCError__   [se.natusoft.osgi.aps.api.net.rpc.errors] {

This represents an error in servicing an RPC request.

__public ErrorType getErrorType()__

The type of the error.

__public String getErrorCode()__

A potential error code.

__public String getMessage()__

Returns an error messaging. This is also optional.

__public boolean hasOptionalData()__

True if there is optional data available. An example of optional data would be a stack trace for example.

__public String getOptionalData()__

The optional data.

}

----

    

public _class_ __RequestedParamNotAvailableException__ extends  APSException    [se.natusoft.osgi.aps.api.net.rpc.exceptions] {

This exception is thrown when a parameter request cannot be fulfilled.

__public RequestedParamNotAvailableException(String message)__

Creates a new _RequestedParamNotAvailableException_ instance.

_Parameters_

> _message_ - The exception messaging. 

__public RequestedParamNotAvailableException(String message, Throwable cause)__

Creates a new _RequestedParamNotAvailableException_ instance.

_Parameters_

> _message_ - The exception messaging. 

> _cause_ - The cause of this exception. 

}

----

    

public _abstract_ _class_ __AbstractRPCRequest__ implements  RPCRequest    [se.natusoft.osgi.aps.api.net.rpc.model] {

This provides a partial implementation of RPCRequest.













__public AbstractRPCRequest(String method)__

Creates a new AbstractRPCRequest.

_Parameters_

> _method_ - The method to call. 

__public AbstractRPCRequest(RPCError error)__

Creates a new AbstractRPCRequest.

_Parameters_

> _error_ - An RPCError indicating a request problem, most probably of ErrorType.PARSE_ERROR type. 

__public AbstractRPCRequest(String method, Object callId)__

Creates a new AbstractRPCRequest.

_Parameters_

> _method_ - The method to call. 

> _callId_ - The callId of the call. 

__protected Map<String, Object> getNamedParameters()__

_Returns_

> The named parameters.

__protected List<Object> getParameters()__

_Returns_

> The sequential parameters.







__public void setServiceQName(String serviceQName)__

Sets the fully qualified name of the service to call. This is optional since not all protocol delivers a service name this way.

_Parameters_

> _serviceQName_ - The service name to set. 









__public void addParameter(Object parameter)__

Adds a parameter. This is mutually exclusive with addParameter(name, parameter)!

_Parameters_

> _parameter_ - The parameter to add. 



}

----

    

public _enum_ __RequestIntention__   [se.natusoft.osgi.aps.api.net.rpc.model] {

The intention of a request.

}

----

    

public _interface_ __RPCExceptionConverter__   [se.natusoft.osgi.aps.api.net.rpc.model] {

An instance of this can be passed to RPCRequest to convert the cauth exception to an RPCError.

__RPCError convertException(Exception e)__

This should be called on any service exception to convert the exception to an RPCError.

_Parameters_

> _e_ - The exception to convert. 

}

----

    

public _interface_ __RPCRequest__   [se.natusoft.osgi.aps.api.net.rpc.model] {

This represents a request returned by protocol implementations.

__boolean isValid()__

Returns true if this request is valid. If this returns false all information except _getError()_ is __invalid__, and _getError()_ should return a valid _RPCError_ object.

__RPCError getError()__

Returns an _RPCError_ object if `isValid() == false`, _null_ otherwise.

__RPCExceptionConverter getExceptionConverter()__

If an exception occurred during the request call, and this returns non null, then the returned converter should be called with the occurred exception to provide an RPCError.

This allows for a specific protocol implementation to handle its own exceptions and provide an appropriate RPCError.

__String getServiceQName()__

Returns a fully qualified name of service to call. This will be null for protocols where service name is not provided this way. So this cannot be taken for given!

__String getMethod()__

Returns the method to call. This can return _null_ if the method is provided by other means, for example a REST protocol where it will be part of the URL.

__boolean hasCallId()__

Returns true if there is a call id available in the request.

A call id is something that is received with a request and passed back with the response to the request. Some RPC implementations will require this and some wont.

__Object getCallId()__

Returns the method call call Id.

A call id is something that is received with a request and passed back with the response to the request. Some RPC implementations will require this and some wont.

__int getNumberOfParameters()__

Return the number of parameters available.

__<T> T getIndexedParameter(int index, Class<T> paramClass) throws RequestedParamNotAvailableException__

Returns the parameter at the specified index.

_Returns_

> The parameter object or null if indexed parameters cannot be delivered.

_Parameters_

> _index_ - The index of the parameter to get. 

> _paramClass_ - The expected class of the parameter. 

_Throws_

> _RequestedParamNotAvailableException_ - if requested parameter is not available. 

}

----

    

public _interface_ __RPCProtocol__   [se.natusoft.osgi.aps.api.net.rpc.service] {

This represents an RPC protocol provider. This API is not enough in itself, it is a common base for different protocols.

__String getServiceProtocolName()__

_Returns_

> The name of the provided protocol.

__String getServiceProtocolVersion()__

_Returns_

> The version of the implemented protocol.

__String getRequestContentType()__

_Returns_

> The expected content type of a request. This should be verified by the transport if it has content type availability.

__String getResponseContentType()__

_Returns_

> The content type of the response for when such can be provided.

__String getRPCProtocolDescription()__

_Returns_

> A short description of the provided service. This should be in plain text.

__RPCError createRPCError(ErrorType errorType, String message, String optionalData, Throwable cause)__

Factory method to create an error object.

_Returns_

> An RPCError implementation or null if not handled by the protocol implementation.

_Parameters_

> _errorType_ - The type of the error. 

> _message_ - An error messaging. 

> _optionalData_ - Whatever optional data you want to pass along or null. 

> _cause_ - The cause of the error. 

}

----

    

public _interface_ __StreamedRPCProtocol__ extends  RPCProtocol    [se.natusoft.osgi.aps.api.net.rpc.service] {

This represents an RPC protocol provider that provide client/service calls with requests read from an InputStream or having parameters passes as strings and responses written to an OutputStream.

HTTP transports can support both _parseRequests(...)_ and _parseRequest(...)_ while other transports probably can handle only _parseRequests(...)_. __A protocol provider can return null for either of these!__ Most protocol providers will support _parseRequests(...)_ and some also _parseRequest(...)_.

__List<RPCRequest> parseRequests(String serviceQName, Class serviceClass, String method, InputStream requestStream, RequestIntention requestIntention) throws IOException__

Parses a request from the provided InputStream and returns 1 or more RPCRequest objects.

_Returns_

> The parsed requests.

_Parameters_

> _serviceQName_ - A fully qualified name to the service to call. This can be null if service name is provided on the stream. 

> _serviceClass_ - The class of the service to call. Intended for looking for method annotations! Don't try to be "smart" here! 

> _method_ - The method to call. This can be null if method name is provided on the stream. 

> _requestStream_ - The stream to parse request from. 

> _requestIntention_ - The intention of the request (CRUD + UNKNOWN). 

_Throws_

> _IOException_ - on IO failure. 

__RPCRequest parseRequest(String serviceQName, Class serviceClass, String method, Map<String, String> parameters, RequestIntention requestIntention) throws IOException__

Provides an RPCRequest based on in-parameters. This variant supports HTTP transports.

Return null for this if the protocol does not support this!

_Returns_

> The parsed requests.

_Parameters_

> _serviceQName_ - A fully qualified name to the service to call. This can be null if service name is provided on the stream. 

> _serviceClass_ - The class of the service to call. Intended for looking for method annotations! Don't try to be "smart" here! 

> _method_ - The method to call. This can be null if method name is provided on the stream. 

> _parameters_ - parameters passed as a 

> _requestIntention_ - The intention of the request (CRUD + UNKNOWN). 

_Throws_

> _IOException_ - on IO failure. 

__void writeResponse(Object result, RPCRequest request, OutputStream responseStream) throws IOException__

Writes a successful response to the specified OutputStream.

_Parameters_

> _result_ - The resulting object of the RPC call or null if void return. If is possible a non void method also returns null! 

> _request_ - The request this is a response to. 

> _responseStream_ - The OutputStream to write the response to. 

_Throws_

> _IOException_ - on IO failure. 

__boolean writeErrorResponse(RPCError error, RPCRequest request, OutputStream responseStream) throws IOException__

Writes an error response.

_Returns_

> true if this call was handled and an error response was written. It returns false otherwise.

_Parameters_

> _error_ - The error to pass back. 

> _request_ - The request that this is a response to. 

> _responseStream_ - The OutputStream to write the response to. 

_Throws_

> _IOException_ - on IO failure. 

}

----

    

