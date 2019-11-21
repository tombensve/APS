# Application Platform Services (APS)

Copyright © 2019 Natusoft AB

__Version:__ 1.0.0 (working up to ...)

__License:__ [Apache 2.0](lics/Apache-2.0.md)

__JDK Level:__ This now builds with all tests working on JDK 11. No, there is no JPMS usage! And it is also using a beta of Groovy 3.0. Have not run into any problem with Groovy being beta so far. 

## This project is currently work in progress and cannot be expected to be completely stable!!

Work is slow, whenever time permits.

To be very clear: **This is currently, and probably for a long time comming, a playground where I'm having fun.**

The __original (and still active) goal__ with this is to make a very easy to use web platform, currently based on Vert.x & React.

This project is now using 2 exceptional frameworks: __Vert.x & React__. These both belong to the same category: Things that just work! I have the highest respect for the people behind both of these. These both also supply outstanding documentation (which is not always the case with all frameworks and libraries!).

### Prerequsites

This code base do contain some react frontend code using javascript. Thereby __npm__
(Node Package Manager) needs to be installed on the machine before this will fully
build.

Some sub modules runs a bash script to build frontend stuff. On Windows this might cause a problem if not build under the Git Bash shell or some other bash windows port. The important thing is that when "_exec-maven-plugin_" runs "_${basedir}/src/main/js/aps-webmanager-frontend/build.sh_" then "_/bin/bash_" must be found. 

It now also builds a Docker container and thus Docker must be installed for it to build.

---- 

APS no longer runs in an OSGi container due to Vertx 3.8.0 which have too many transitive dependencies actually not used by APS, but must be deployed in OSGi container to make it happy. I spend 3 days trying to resolve those and get it running in Felix, but for every new bundle jar deployed several more were required, so I finally gave up. Why deploy a lot of code that is really not used ? 

I have now adapted and renamed APSOSGiTestTool to aps-runtime and added an aps-platform-booter which loads all jars using URLClassLoader, and deploys those of bundle type. No modularisation now! This does however not conflict with JPMS in other ways than JPMS requireing everything to be JPMS modularized, which APS currently isn't due to the ton of work required to make it so. As said above APS is no longer full OSGi, but still uses the OSGi service model, and thus some parts of the OSGi APIs. 

APS started out a long time ago, and provides a lot of own solutions. It does its own thing, especially with APSActivator (looks though all bundle classes and instantiates and dependency injects based on APS annotations). It just uses base OSGi as a base. APS is also largely coded in Groovy.

The _APSRuntime_ maven module is actually an implementation of the 4 basic OSGi APIs, but without the classloading. It uses the JUnit classpath when used in tests. This makes it trivially easy to test bundles and interaction between bundles. It has a flexible Groovy deploy DSL. Note that this supports only the parts of the 4 base OSGi APIs used by APS!

APS have always been about keeping things simple. Easy to use APIs providing only basic functionality with no configurational options API wise. My intentions is to encapsulate complexity and provide the easiest way possible to use for all other code. APS also provides APIs for common things like messaging with very easy/basic APIs. Each specific implementation is responsible for any configuration needed and there is a more structured configuraton service using JSON for configuration data, for other services to use instead of the primitive properties provided by OSGi.

React is a web component framework that follows my ideas of simplicity and small code! 

Vert.x I stumbled upon accidentally, and realized that this is "the shit" I'm been looking for! When I started out with this project (yes, its been a longtime) I felt stuck with the JEE web APIs. The requirement of a JEE web container felt limiting, and rather heavyweight.

Vert.x fulfilled my needs and then some. And it is truly easy to set up an HTTP server, handle requests, etc. So Vert.x also fits within my goals of keeping things simple and codewise small. All functionality in library form and no outside container to deploy in (well, there are verticles, but they are not required). It does pull in a lot of 3rd party libraries (based on Netty), but it still feels a lot smaller than the average JEE container. 

APS adds a thin layer on top of Vert.x. In many cases Vert.x is used directly, but for messages there is an APS specific API called _APSBus_. It has a `send(target, message)` and `subscribe(target, handler)` methods basically. _APSBus_ actually doesn't do any message sending nor subscribing. It looks for all OSGi published APSBusRouter services, and passes on the call to each found. It is up to each APSBusRouter to look at the target string and determine if it is a target that it supports. If its not, it does nothing, if it is, it handles the action. Each APSBusRouter implementation have a unique target id, which starts the target string followed by a colon ("\<id\>:\<address\>"). The following target ids are currently available: 

- "local" - This sends messages locally within JVM instance (directly, no threading). 

- "cluster" - This uses Vert.x EventBus for messages. If the next part of target is "all:" then a publish is done, otherwise a send. For publish the "all:" part is removed and the rest is used as address. This is provided by the APSVertxProvider bundle.

- "amqp" - This currenlty uses Vert.x AMQP bridge, which apparently will be removed in the next major version of Vert.x. So this will need to be redone using some other client. This is currently provided by APSVertxProvider bundle.

- No support for JMS, but all that is needed is to publish an implementation of APSBusRouter that uses JMS APIs.

Note that all APS messages are required to be JSON! This is a limitation, but keeps things simple. On the backend a JSON object is represented as `Map<String, Object>`, and a JSON array as `List<Object>`. On the frontend it of course becomes JS objects and arrays. Note that APS is mostly written in Groovy which allows you to interact with Maps and Lists in a very similar way as JavaScript, and it also allows for static Maps, not just arrays. `[ key: value ]` is a Groovy Map. `[value, value, ...]` is a Groovy list. Map can be referenced as `jsonMap['body']['type']`.   

The general architecture of an APS app is then intended to be message driven. APS includes a very simple but useful JSON schema definition and a validator that validates a JSON structure against a schema. The schema forces simple JSON structures which I think is a good thing. It will both document the messages and provide validation of them.

There are actually 2 service models: OSGi and Messaging. The OSGi service model are used for the platform. APSActivator (a generic BundleActivator with annotation based dependency injection) makes use of the fact the OSGi bundle provides a list of all classes in the bundle to do dependency injections. APSBus also makes use of OSGi services to find bus routers. But for applications, services are intended to be provided by listening to messages and reacting on them, possibly producing new messages. Both can be mixed, but messaging covers the whole Vert.x cluster, and possibly outside of that, while OSGi services are local only.  

Do note that APS needs a clustered Vert.x and will create/join such on startup. It can however be told to start a non clustered Vert.x by system property on start. This can be useful when running in test. Some of the APS tests actually does this (VertxProvider bundle do some things that requires a cluster, and will thus produce a warnig when Vert.x is started in unclusetered mode. Those functions are however not used by the test in that case, but you get the warning). 

APSRuntime is also used for unit tests. It then uses the junit classpaths. So most tests run as they would in a real deployment and thus also starts Vert.x. This could cause problems if multiple builds are run concurrently on the same machine, like in a Jenkins for example. But if Vert.x is run unclustered it would probably work if HTTP service tests and similar use random ports. There is no good support for that yet in APS. 

----

[JPMS](JPMS.md)

----

Lots of fun ideas, and far to little time ...

Tommy

Professional Code Geek, working with Java since the beginning, C & C++ before that. 



