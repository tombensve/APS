# APSJSONService

This provides exactly the same functionallity as APSJSONLib. It actually wraps the library as a service. The reason for that is that I wanted to be able to redeploy the library without forcing a redeploy of the Bunde using it. A redeploy of the library will force a redeploy of this service, but not the client of this service. The APS clients of this service uses APSServiceTracker wrapped as a service and thus handles this service leaving and returning without having to care about it.

This service and the library existrs for internal use. It is here and can be used by anyone, but in most cases like serializing java beans back and forth to JSON (which this can do) Jacksson would still be a better choice and offers more flexibility. In the long run I’m going to see if I can replace the internal use of this with Jacksson as well. 
