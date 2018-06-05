# Application Platform Services (APS)

Copyright © 2013 Natusoft AB

__Version:__ 1.0.0

This repo is used for development. __This repo is thereby work in progress and cannot be expected to be stable nor build completely!!__ Releases will be done to _APS_ repo. Why do development in a separate repo ? Well, not sure, but it felt like a good idea at the time :-). I however see no problems with it either.

Work is slow, whenever time permits.

---

To be very clear: This is currently, and probably for a long time comming, a playground where I'm having fun. The original (and still active) goal with this is to make a very easy to use web platform based on OSGi. APS is however only using the basic 4 OSGi APIs, and currently java8 & Groovy code so it will probably **not** run in most embedded OSGi containers.

I have decided to base this project on Vertx rather than traditional EE APIs.

Almost all of what is in this version will be replaced, some things just removed. As I said, this is currently a playground, that I play with when I have the time.

There is however one thing that is currenlty useful and does not depend on any other bundle. APS-APIs, which now also contains the formarly APSToolsLib. APSToolsLib was so central to all APS bundles that it just made more sense to put it in APS-APIs, which might change name to APSPlatform. There is a nicer service tracker and something the maven-bundle-plugin people never considered, and might consider me completely crazy: APSActivator. It is a generic bundle activator that makes use of annotations to publish services, etc. It will inject into classes and instantiate annotated classes. It goes through all classes in the bundle and checks them for annotations. maven-bundle-plugin warns about seeing an external activator and suggests that it is probably an error, but it isn't! Note that it is the maven-bundle-plugin that complains. All OSGi containers I've tested (Karaf, Glassfish, Virgo, KnopplerFish) have no problem what so ever with this.

Yes, I know that the OSGi APIs contains annotations for injecting and publishing services, etc. These produce XML files that are then used by the OSGi container. This so that they are compatible with J2ME java not supporting annotations. These are features supported by the container. APSActivator is a plain OSGi activator and will work with any OSGi container, but requires Java SE.

APSActivator also interacts with APSServiceTracker allowing annotations based configuration of the tracker. It also allows injecting a tracker as a proxied service that uses the tracker, allocates the service, calls it, releases the service and returns any eventual return value.   

The APSServiceTracker is also a bit different in that it does not like to tear services down, it and APSActivator both work to keep services up. The tracker provides a timeout and throws an APSNoServiceAvailableException on timeout.

The APS apis will move to reactive APIs to avoid forced threading. The `@OSGiService` annotaiton now contains a `boolean nonBlocking()` attribute. When set to true a reactive API is required. That is no return values only handler callbacks to deliver results. What happens in this case is that if the service called isn't yet available, the call will be cached, and when the service becomes available the cached calls will be made. This makes things resolve themselves when services depend on other services and the order of deployments is unknown. That is one rule of APS: there should be no deployment order requirements. The default for `nonBlocking` is false, and in this case when a proxied service is called and the service is not available it will block waiting for the service to become available or throw an APSNoServiceAvailableException if this takes too long time. But I'm moving away from that towards non blocking and reactive APIs since that works very well and does not need to start threads just to avoid deadlocks.

There is also OSGIServiceTestTools that can be extended by tests and provide Groovy DSL, and is very small and easy implementing only the basic 4 APIs minus deployment which is handled by the DSL API instead. It does not do classloading (yet), it uses junit classpath! This makes it trivially easy to run bundles in tests and provide test client bundles. OSGIServiceTestTools can be called from Java also though with more parentesis and dots. It is actually written in Java.

In the end I'm planning for a minimalistic OSGi container supporting only the 4 base APIs. It won't be a fullfledged OSGi container. The goal with this is also to provide an executable jar with both container and all app bundles kind of like Spring Boot. When this is available it will also be used for running tests instead of OSGiTestTools (which I'm considering renaming to APSTestTools instead since that would be a clearer name).

## Building

There is currenly a catch to building. A first time build must be done without executing any tests. After that it can be built again with tests. All bundles are using APIs in APS-APIs. This is a clear and easy dependency. But since almost all tests actually deploy the bundle being tested and dependent bundles in tests, which have more depenencies and implementation specific such.

For example, APSConfigManager makes use of APS-APIs APIs that are actually implemented using Vertx. APSVertxProvider has a dependency to APSConfig from APS-APIs for configuration, but its test requires deploying APSConfigManager and 2 other service implementations that makes use of Vertx. This only works if all code is built first and then tests are run in a second build.

I have decided that this is OK since I consider having my tests as "life like" as possible more important. By that I mean that deploy and use all real services instead of faking/mocking. The tests run things exactly the way it would be run when deployed for real. When testing there is usually a test bundle deployed also that performs the tests and assertions by calling the services of the other bundles.

----

This works very well mostly due the the simplicity of the core OSGi APIs. There is a clear reason why I stayed with only those. Keep things as simple as possible and as small as possible is always my goal. Sometimes that is just not possible, but you should always try :-).

/Tommy
