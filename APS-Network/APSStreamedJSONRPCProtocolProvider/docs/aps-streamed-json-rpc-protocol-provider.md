# APSStreamedJSONRPCProtocolProvider

This provides JSONRPC protocol. It provides both version 1.0 and 2.0 of the protocol. It requires a transport that uses it and services provided by aps-external-protocol-extender to be useful.

The 1.0 version of the JSONRPC protocol are described at [http://json-rpc.org/wiki/specification](http://json-rpc.org/wiki/specification).

The 2.0 version of the JSONRPC protocol are describved at [http://jsonrpc.org/spec.html](http://jsonrpc.org/spec.html).

JSONHTTP version 1.0 which is not any standard protocol at all. It requires both service name and method name on the url, and in case of HTTP GET also arguments as ?arg=value,... where values are strings or primitives. For POST, PUT, and DELETE a JSON array of values need to be written on the stream.

JSONREST version 1.0 extending JSONHTTP and providing 'true' for _supportsREST()_ which will make the http transport always map methods starting with post, put, get, or delete to the http method. This can thereby deliver a true REST API.

## See also

Se the documentation for _APSExtProtocolHTTPTransportProvider_ for an HTTP transport through which these protocols can be used.

Se the documentation for _APSExternalProtocolExtender_ for a description of how services are made available and what services it provides for transport providers.

