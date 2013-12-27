# APS Net Time Servcie Provider

This provides a service for converting time between a common network time and local time. The actual net time is provided by APSGroupsService which must be running for this service provider to work. This also means that it will only work between hosts on the same subnet and multicast must be supported on that subnet.

The idea with this service is that no matter what the local host time is, time critical data passed on the network can be reasonably compared between hosts, by agreeing on a common _now_ time and the diff between local time and this common time.

As said above, this implementation have limitations!

## APSNetTimeService

[Javadoc](http://apidoc.natusoft.se/APS/se/natusoft/osgi/aps/api/net/time/service/APSNetTimeService.html)

public _interface_ __APSNetTimeService__   [se.natusoft.osgi.aps.api.net.time.service] {

This service provides network neutral time. Even with NTP it is difficult to keep the same time on different servers. This service creates a network timezone and broadcasts the network time. It supports converting local time to network time and converting network time to local time.

Please note that the network time will not be accurate down to milliseconds, but will be reasonable correct for most usages.

__public long netToLocalTime(long netTime)__

Converts from net time to local time.

_Returns_

> local time.

_Parameters_

> _netTime_ - The net time to convert. 

__public Date netToLocalTime(Date netTime)__

Converts from net time to local time.

_Returns_

> local time.

_Parameters_

> _netTime_ - The net time to convert. 

__public long localToNetTime(long localTime)__

Converts from local time to net time.

_Returns_

> net time.

_Parameters_

> _localTime_ - The local time to convert. 

__public Date localToNetTime(Date localTime)__

Converts from local time to net time.

_Returns_

> net time.

_Parameters_

> _localTime_ - The local time to convert. 

}

----

    

