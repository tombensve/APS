# Application Platform Services (APS)

Copyright © 2013 Natusoft AB

__Version:__ 1.0.0

This repo is used for development. __This repo is thereby work in progress and cannot be expected to be stable nor build completely!!__ Releases will be done to _APS_ repo. Why do development in a separate repo ? Well, not sure, but it felt like a good idea at the time :-). I however see no problems with it either.

Work is slow, whenever time permits.

---

To be very clear: This is currently, and probably for a long time comming, a playground where I'm having fun. The original (and still active) goal with this is to make a very easy to use web platform based on OSGi. APS is however only using the basic 4 OSGi APIs, and currently java8 & Groovy code so it will probably not run in most embedded OSGi containers.

I have decided to base this project on Vertx rather than traditional EE APIs.

Almost all of what is in this version will be replaced, some things just removed. As I said, this is currently a playground, that I play with when I have the time.

There is however one thing that is currenlty useful and does not depend on any other bundle: APSToolsLib. This contains a nicer service tracker and something the maven-bundle-plugin people never considered, and might consider me completely crazy: APSActivator. It is a generic bundle activator that makes use of annotations to publish services, etc. It will inject into classes and instantiate annotated classes. It goes through all classes in the bundle and checks them for annotations. maven-bundle-plugin warns about seeing an external activator and suggests that it is probably an error, but it isn't! Note that it is the maven-bundle-plugin that complains. All OSGi containers I've tested (Karaf, Glassfish, Virgo, KnopplerFish) have no problem what so ever with this.

Yes, I know that the OSGi APIs contains annotations for injecting and publishing services, etc. These produce XML files that are then used by the OSGi container. This so that they are compatible with J2ME java not supporting annotations. These are features supported by the container. APSActivator is a plain OSGi activator and will work with any OSGi container, but requires Java SE.

APSActivator also interacts with APSServiceTracker allowing annotations based configuration of the tracker. It also allows injecting a tracker as a proxied service that uses the tracker, allocates the service, calls it, releases the service and returns any eventual return value.   

The APSServiceTracker is also a bit different in that it does not like to tear services down, it and APSActivator both work to keep services up. The tracker provides a timeout and throws an APSNoServiceAvailableException on timeout. The side effect of this is that you (currently) sometimes need to start a thread in Bundle.start(...) to avoid a deadlock. This also means that later failures cannot stop the bundle on failed start! But it is soo much nicer to keep things up IMHO!

There is also OSGIServiceTestTools that can be extended by tests and provide Groovy DSL, and is very small and easy implementing only the basic 4 APIs minus deployment which is handled by the DSL API instead. It does not do classloading (yet)! This makes it trivially easy to run bundles in tests and provide test client bundles. OSGIServiceTestTools can be called from Java also though with more parentesis and dots. It is actually written in Java.

/Tommy
