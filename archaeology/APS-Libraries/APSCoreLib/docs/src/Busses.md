## APSBus

Since there are many types of busses out there and that APS is based on Vert.x with its own bus (_EventBus_) APS provides a very simple, generic bus API called __APSBus__. It should be documented elsewhere in this document. 

This bundle contains an implementation of APSBus. The _APSBus_ implementation just tracks all published `APSBusRouter` implementations. Each `APSBusRouter` implementations must also provide a resource file in _aps/bus/routers_ with the name of each bus router, one per line. APSBus will find all these and wait for them to become available as services before publishing itself as a service. 

If an `APSBusRouter` implementation does not do this, then it is possible that APSBus will not see it. It is also possible that it will se it, but miss another implementation instead. This due to it actually not knowing the available implementations nor their names. It just count the entries in all found _routers_  files, and waits for that amount of `APSBusRouter` services to be published. The names are just for show and are logged on startup. 

