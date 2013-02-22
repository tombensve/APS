# APSStreamedJSONRPCProtocolProvider

This provides JSONRPC protocol. It provides both version 1.0 and 2.0 of the protocol. It requires a transport that uses it and services provided by aps-external-protocol-extender to be useful. 

The 1.0 version of the JSONRPC protocol are described at [http://json-rpc.org/wiki/specification](http://json-rpc.org/wiki/specification).

The 2.0 version of the JSONRPC protocol are describved at [http://jsonrpc.org/spec.html](http://jsonrpc.org/spec.html).

## See also

Se the documentation for _APSExtProtocolHTTPTransportProvider_ for an HTTP transport through which these protocols can be used.

Se the documentation for _APSExternalProtocolExtender_ for a description of how services are made available and what services it provides for transport providers.