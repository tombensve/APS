# Application Platform Services (APS)

Copyright © 2013 Natusoft AB

__Version:__ 1.0.0

__This project is currently work in progress and cannot be expected to be stable!!__

Work is slow, whenever time permits.

To be very clear: **This is currently, and probably for a long time comming, a playground where I'm having fun.** The original (and still active) goal with this is to make a very easy to use web platform based on OSGi & Vertx. APS is however only using the basic 4 OSGi APIs, and currently java8 & Groovy code so it will probably **not** run in most embedded OSGi containers.

This project is now using 2 exceptional frameworks: __Vert.x & React__. These both belong to the same category: Things that just work! I have the highest respect for the people behind both of these. It gives me a warm feeling to know that there is people out there who knows what they are doing, and are doing things very well. These both also supply outstanding documentation.

---

__To build__ this you must first follow the instructions here: https://github.com/tombensve/maven-bundle-plugin/blob/master/README.md

---

There is one thing that is currenlty useful and does not depend on any other bundle. APS-APIs, which now also contains the formarly APSToolsLib. APSToolsLib was so central to all APS bundles that it just made more sense to put it in APS-APIs, which might change name to APSPlatform. There is a nicer service tracker and something the maven-bundle-plugin people never considered, and might consider me completely crazy: APSActivator. It is a generic bundle activator that makes use of annotations to publish services, etc. It will inject into classes and instantiate annotated classes. It goes through all classes in the bundle and checks them for annotations. maven-bundle-plugin warns about seeing an external activator and suggests that it is probably an error, but it isn't! Note that it is the maven-bundle-plugin that complains. All OSGi containers I've tested (Karaf (felix), Glassfish, Virgo, KnopplerFish) have no problem what so ever with this.

Yes, I know that the OSGi APIs contains annotations for injecting and publishing services, etc. These produce XML files that are then used by the OSGi container. This so that they are compatible with J2ME java not supporting annotations. These are features supported by the container. APSActivator is a plain OSGi activator and will work with any OSGi container, but requires Java SE.

APSActivator also interacts with APSServiceTracker allowing annotations based configuration of the tracker. It also allows injecting a tracker as a proxied service that uses the tracker, allocates the service, calls it, releases the service and returns any eventual return value.

The APSServiceTracker is also a bit different in that it does not like to tear services down, it and APSActivator both work to keep services up. The tracker provides a timeout and throws an APSNoServiceAvailableException on timeout.

The APS apis will move to reactive APIs to avoid forced threading. The `@OSGiService` annotaiton now contains a `boolean nonBlocking()` attribute. When set to true a reactive API is required. That is no return values only handler callbacks to deliver results. What happens in this case is that if the service called isn't yet available, the call will be cached, and when the service becomes available the cached calls will be made. This makes things resolve themselves when services depend on other services and the order of deployments is unknown. That is one rule of APS: there should be no deployment order requirements. The default for `nonBlocking` is false, and in this case when a proxied service is called and the service is not available it will block waiting for the service to become available or throw an APSNoServiceAvailableException if this takes too long time. But I'm moving away from that towards non blocking and reactive APIs since that works very well and does not need to start threads just to avoid deadlocks.

There is also OSGIServiceTestTools that can be extended by tests and provide Groovy DSL, and is very small and easy implementing only the basic 4 APIs minus deployment which is handled by the DSL API instead. It does not do classloading (yet), it uses junit classpath! This makes it trivially easy to run bundles in tests and provide test client bundles. OSGIServiceTestTools can be called from Java also though with more parentesis and dots. It is actually written in Java.

In the end I'm planning for a minimalistic OSGi container supporting only the 4 base APIs. It won't be a fullfledged OSGi container. The goal with this is also to provide an executable jar with both container and all app bundles kind of like Spring Boot. When this is available it will also be used for running tests instead of OSGiTestTools (which I'm considering renaming to APSTestTools instead since that would be a clearer name).

## Tests

Using the OSGiServiceTestTools all the tests deploy both the tested bundle and real dependent bundles rather than mocking things, and runs things as it would be run normally. This sometimes causes test dependences that create circular dependencies in maven. To go around that problem these bundles have a separate test maven project that only contains the tests. Bundles of course only interact with each other using the interfaces in APS-APIs. This is a side effect of OSGiTestTools not supporting classloading, but using JUnit classpath instead. So in the long run this problem will go away.

----

In general this works very well mostly due the the simplicity of the core OSGi APIs. There is a clear reason why I stayed with only those. Keep things as simple as possible and as small as possible is always my goal. 

/Tommy
