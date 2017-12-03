# Application Platform Services (APS)

Copyright © 2013 Natusoft AB

__Version:__ 1.0.0

This repo is used for development. __This repo is thereby work in progress and cannot be expected to be stable nor build completely!!__ Releases will be done to _APS_ repo. Why do development in a separate repo ? Well, not sure, but it felt like a good idea at the time :-). I however see no problems with it either.

Work is slow, whenever time permits.

__Author:__ Tommy Svensson (tommy@natusoft.se)

---

To be very clear: This is currently, and probably for a long time comming, a playground where I'm having fun. The original (and still active) goal with this is to make a very easy to use web platform based on OSGi. APS is however only using the basic 4 OSGi APIs, and currently java8 & Groovy code so it will probably not run in most embedded OSGi containers. 

I have decided to base this project on Vertx rather than traditional EE APIs. I'm also turning OSGi upside down by publishing consuming services that will be called with produced data when available, in a reactive style, which fits Vertx.

Almost all of what is in this version will be replaced, some things just removed. As I said, this is currently a playground, that I play with when I have the time.

There is however one thing that is currenlty useful and does not depend on any other bundle: APSToolsLib. This contains a nicer service tracker and something the maven-bundle-plugin people never considered, and might consider me completely crazy: APSActivator. It is a generic bundle activator that makes use of annotations to publish services, etc. It will inject into classes and instantiate annotated classes. It goes through all classes in the bundle and checks them for annotations. This of course means that all the bundles classes will **always** be loaded! maven-bundle-plugin warns about seeing an external activator and suggests that it is probably an error, but it isn't! Note that it is the maven-bundle-plugin that complains. All OSGi containers I've tested (Karaf, Glassfish, Virgo, KnopplerFish) have no problem what so ever with this. 

The APSServiceTracker is also a bit different in that it does not like to tear services down, it and APSActivator both work to keep services up. The tracker by providing a timeout and throwing an APSNoServiceAvailableException on timeout. The side effect of this is that you sometimes need to start a thread in Bundle.start(...) to avoid a deadlock. This also means that later failures cannot stop the bundle on failed start! But it is soo much nicer to keep things up IMHO!

Note that the branch names are YouTrack (JetBrains) issues. I'm using YouTrack as a service from JetBrains using the cheapest license. Here is a read only access of the YouTrack board:

https://natusoft.myjetbrains.com/youtrack/agiles/90-4/94-6

ytguest / salamiPizza

Do note that all idea i throw in there does not get implemented for different reasons. That is why I have a "Cancelled" column in the board.

/Tommy
