# APS Net Time Servcie Provider

This provides a service for converting time between a common network time and local time. The actual net time is provided by APSGroupsService which must be running for this service provider to work. This also means that it will only work between hosts on the same subnet and multicast must be supported on that subnet. 

The idea with this service is that no matter what the local host time is, time critical data passed on the network can be reasonably compared between hosts, by agreeing on a common _now_ time and the diff between local time and this common time. 

As said above, this implementation have limitations! 

## APSNetTimeService

[Javadoc](http://apidoc.natusoft.se/APS/se/natusoft/osgi/aps/api/net/time/service/APSNetTimeService.html) 
