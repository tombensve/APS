# APSStreamedJSONRPCProtocolProvider

This provides JSONRPC protocol. It provides both version 1.0 and 2.0 of the protocol. It requires a transport that uses it and services provided by aps-external-protocol-extender to be useful. 

JSONRPC version 1.0 protocol as described at [http://json-rpc.org/wiki/specification](http://json-rpc.org/wiki/specification).

JSONRPC version 2.0 protocol as describved at [http://jsonrpc.org/spec.html](http://jsonrpc.org/spec.html).

JSONHTTP version 1.0 which is not any standard protocol at all. It requires both service name and method name on the url, and in case of HTTP GET also arguments as ?params=arg:...:arg where values are strings or primitives. For POST, PUT, and DELETE a JSON array of values need to be written on the stream.

JSONREST version 1.0 extending JSONHTTP and providing 'true' for _supportsREST()_ which will make the http transport always map methods starting with post, put, get, or delete to the http method. This can thereby deliver a true REST API.

## Examples

Here is some examples calling services over http with diffent protocols using curl (requires aps-ext-protocol-http-transport-provider.jar to be deployed):

	curl --data '{"jsonrpc": "2.0", "method": "getPlatformDescription", "params": [], "id": 1}' http://localhost:8080/apsrpc/JSONRPC/2.0/se.natusoft.osgi.aps.api.core.platform.service.APSPlatformService 

yields:

	{"id": 1, "result": {"description": "My personal development environment.", "type": "Development", "identifier": "MyDev"}, "jsonrpc": "2.0"}
		
while

	curl --get http://localhost:8080/apsrpc/JSONHTTP/1.0/se.natusoft.osgi.aps.api.core.platform.service.APSPlatformService/getPlatformDescription
	
yields

    {"description": "My personal development environment.", "type": "Development", "identifier": "MyDev"}
       
and 

     curl --get http://localhost:8080/apsrpc/JSONHTTP/1.0/se.natusoft.osgi.aps.api.misc.session.APSSessionService/createSession\(Integer\)?params=5
     
yields
 
	{"id": "6d25d646-11fc-44c3-b74d-29b3d5c94920", "valid": true}
	
In this case we didn’t just use _createSession_ as method name, but _createSession(Integer)_ though with parentheses escaped to not confuse the shell. This is because there is 2 variants of createSession: createSession(String, Integer) and createSession(Integer). If we don’t specify clearly we might get the wrong one and in this case that happens and will fail due to missing second parameter. Also note the _params=5_.  On get we cannot pass any data on the stream to the service, we can only pass parameters on the URL which is done by specifying url parameter _params_ with a colon (”:”) separated list of parameters as value. In this case only String and primitives are supported for parameters.  

These examples only works if you have disabled the ”requireAuthentication” configuration (network/rpc-http-transport). 

## See also

Se the documentation for _APSExtProtocolHTTPTransportProvider_ for an HTTP transport through which these protocols can be used.

Se the documentation for _APSExternalProtocolExtender_ for a description of how services are made available and what services it provides for transport providers.