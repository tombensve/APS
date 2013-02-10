# APSDiscoveryService

This is actually a service directory that also multicasts on the network to find other instances of itself and makes the services in other instances available also. 

A _service_ here means anything that can be called either with an URL or a host and port. It does not specifically indicate what type of service it is. Each published service however has an id that can be used to better identify it. Basically clients of services have to know the id of the service they want and by that know what it is and how to talk to it. 

## APIs
