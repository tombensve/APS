# Application Platform Services (APS)

Copyright © 2019 Natusoft AB

__Version:__ 1.0.0 (working up to ...)

__License:__ [Apache 2.0](lics/Apache-2.0.md)

__JDK Level:__ This now builds with all tests working on JDK 11! No, there is no JPMS usage! And it is also using a beta of Groovy 3.0. Have not run into any problem with Groovy being beta so far. 

## This project is currently work in progress and cannot be expected to be completely stable!!

Work is slow, whenever time permits.

To be very clear: **This is currently, and probably for a long time comming, a playground where I'm having fun.**

The __original (and still active) goal__ with this is to make a very easy to use web platform, currently based on Vert.x & React.

This project is now using 2 exceptional frameworks: __Vert.x & React__. These both belong to the same category: Things that just work! I have the highest respect for the people behind both of these. These both also supply outstanding documentation (which is not always the case with all frameworks and libraries!).

### Prerequsites

This code base do contain some react frontend code using javascript. Thereby __npm__
(Node Package Manager) needs to be installed on the machine before this will fully
build.

Some sub modules runs a bash script to build frontend stuff. On Windows this might cause a problem if not build under the Git Bash shell or some other bash windows port. The important thing is that when "_exec-maven-plugin_" runs "_${basedir}/src/main/js/aps-webmanager-frontend/build.sh_" then "_/bin/sh_" must be found. 

---- 

APS will run in any OSGi container (well, felix, Karaf, Knopplerfish, Virgo (not sure this is still kicking)). That said, I woudn't exactly call APS a straight OSGi platform. APS started out a long time ago, and provides a lot of own solutions rather than official OSGi stadard solutions. It does its own thing, especially with APSActivator (looks though all bundle classes and instantiates and dependency injects based on APS annotations). It just uses base OSGi as a base. APS is also largely coded in Groovy.

The _APSOSGiTestTools_ maven module is actually an implementation of the 4 basic OSGi APIs, but without the classloading. It uses the JUnit classpath. This makes it trivially easy to test bundles and interaction between bundles. It has a flexible Groovy deploy DSL. Note that this supports only the parts of the 4 base OSGi APIs used by APS!

APS have always been about keeping things simple. Easy to use APIs providing only basic functionality with no configurational options API wise. My intentions is to encapsulate complexity and provide the easiest way possible to use for all other code. APS also provides APIs for common things like messaging with very easy/basic APIs. Each specific implementation is responsible for any configuration needed and there is a more structured configuraton service using JSON for configuration data, for other services to use instead of the primitive properties provided by OSGi.

React is a web component framework that follows my ideas of simplicity and small code! 

Vert.x I stumbled upon accidentally, and realized that this is "the shit" I'm been looking for! When I started out with this project (yes, its been a longtime) I felt stuck with the JEE web APIs. The requirement of a web container felt limiting, and rather heavyweight.

Vert.x fulfilled my needs and then some. And it is truly easy to set up an HTTP server, handle requests, etc. So Vert.x also fits within my goals of keeping things simple and codewise small. All functionality in library form and no outside container to deploy in (well, there are verticles, but they are not required). It does pull in a lot of 3rd party libraries (based on Netty), but it still feels a lot smaller than the average JEE container. 

APS adds a thin layer on top of Vert.x. In many cases Vert.x is used directly, but for messages there is an APS specific API called _APSBus_. It has a `send(target, message)` and `subscribe(target, handler)` methods basically. _APSBus_ actually doesn't do any message sending nor subscribing. It looks for all OSGi published APSBusRouter services, and passes on the call to each found. It is up to each APSBusRouter to look at the target string and determine if it is a target that it supports. If its not, it does nothing, if it is, it handles the action. Each APSBusRouter implementation have a unique target id, which starts the target string followed by a colon ("\<id\>:\<address\>"). The following target ids are currently available: 

- "local" - This sends messages locally within JVM instance (directly, no threading). 

- "cluster" - This uses Vert.x EventBus for messages. If the next part of target is "all:" then a publish is done, otherwise a send. For publish the "all:" part is removed and the rest is used as address. This is provided by the APSVertxProvider bundle.

- "amqp" - This currenlty uses Vert.x AMQP bridge, which apparently will be removed in the next major version of Vert.x. So this will need to be redone using the Java client for RabbitMQ. This is currently provided by APSVertxProvider bundle.

- No support for JMS, but all that is needed is to publish an implementation of APSBusRouter that uses JMS APIs.

Note that all APS messages are required to be JSON! This is a limitation, but keeps things simple. On the backend a JSON object is represented as `Map<String, Object>`, and a JSON array as `List<Object>`. On the frontend it of course becomes JS objects and arrays. Note that APS is mostly written in Groovy which allows you to interact with Maps and Lists in a very similar way as JavaScript, and it also allows for static Maps, not just arrays. `[ key: value ]` is a Groovy Map. `[value, value, ...]` is a Groovy list. Map can be referenced as `jsonMap['body']['type']`.   

The general architecture of an APS app is then intended to be message driven (\*1). This requires well defined messages. It provides very loose coupling, mostly only having dependencies between functionallity and the messages it works with. This makes the messages central. It offers a high level of flexibility, and I'm counting on less code. The frontend code and the backend code will then also look quite similar working with messages. I also beleive that resuing the same messages over the whole application is something to strive for, but requires very well though trough messages that can be both reusable and specific for a message. APS includes a very simple but useful JSON schema definition and a validator that validates a JSON structure against a schema. The schema forces simple JSON structures which I think is a good thing. It will both document the messages and provide validation of them.

\*1) This is a truth with modification! There are 2 architectures: OSGi and Messaging. OSGi are used for the platform. APSActivator makes use of the fact the OSGi bundle provides a list of all classes in the bundle to do dependency injections. APSBus also makes use of OSGi services to find bus routers. But for applications, services are intended to be provided by listening to messages and reacting on them, possibly producing new messages. For example, _APSWebManager.jsx_ starts listening to an address, then sends a message to backend that it is available, and its address. The backend then produces a JSON GUI spec that it sends to the address. When _APSWebManager.jsx_ receives it, it renders the GUI spec received. This is between frontend and backend, but even within backend, message based services should be used since such can easily be part of same jar or a different jar deployed on a different node joining same vert.x cluster. Not so for OSGi services.

Do note that APS needs a clustered Vert.x and will create/join such on startup. It can however be told to start a non clustered Vert.x by system property on start. This can be useful when running in test. Some of the APS tests actually does this (VertxProvider bundle do some things that requires a cluster, and will thus produce a warnig when Vert.x is started in unclusetered mode. Those functions are however not used by the test in that case, but you get the warning). 

About testing: APS provide a testing tool called APSOSGiTestTools. It actually implements a primitive APS level OSGi container but without classloading (as mentioned above), using junit classpaths instead. So most tests run as they would in a real deployment and thus also starts Vert.x. This could cause problems if multiple builds are run concurrently on the same machine, like in a Jenkins for example. But if Vert.x is run unclustered it would probably work if HTTP service tests and similar use random ports. There is no good support for that yet in APS. I'm also considering trying to run tests within docker containers, but that would require a Docker installation on the machine you are building on.

----

As the JDK has run forward at a rather quick pace now (at least compared to before) and there are things to consider. I've been thinking a lot, and done some test shots. 

One big question is _Going with JPMS or stick to OSGi ?_. I have now come to the conslusion that JPMS really sucks in comparison to what OSGi provides. I have found information on the net that are critical to JPMS (<https://developer.jboss.org/blogs/scott.stark/2017/04/14/critical-deficiencies-in-jigsawjsr-376-java-platform-module-system-ec-member-concerns?\_sscc=t>) and I can agree with them in much. My personal reflection is that the old ServiceLoader (appeared in Java 6) is not even close to the OSGi service model. Much more limited, for many of the APS services the actual service returend by the ServiceLoader would have to be a factory that also sets upp the service if not already done and then provides it. ServiceLoader doesn't really support prerequisites for being able to deliver a service. In OSGi you can do the setup you want/need and when you have an instance ready for use by others, you can publish it. This is far more flexible. The OSGi people clearly sat down and did some thinking before comming up with a solution. 

The next problem with JPMS is that if you are adding `module-info.java` making your code into a module then all dependencies must also be JPMS modules! Someone realized the problem with this and made __Moditect__(<https://github.com/moditect/moditect>). This will convert your non JPMS dependencies into JPMS modules. This seams rather extreme! So you can no longer have direct dependencies to maven artifacts, but have to run a job that downloads and converts them somewhere locally. At least it looks like it is possible to export all packages (`*`). Comparing to OSGi, with OSGi any jar file can just have a few extra OSGi MANIFEST.MF entries to be able to be dropped into an OSGi container. This will not affect any other use of the jar file. But when your jar file gets a _Module-info_ class into it, then it is no longer just a jar file. When I tested, as soon as a jar gets a Module-info.java it can no longer be accessed by other non module jars. There seem to be some special exception with the JDK internal APIs. Also note that no other JVM language, like Groovy, currently supporting modules themselves. There are workarounds though. 

My guess is that it is going to take a very long time before most 3rd party dependencies out there are JPMS enabled. When they are it means they are compiled by a JDK 9+ compiler and will not run on JDK 8 anymore. Then the question is, if you are not using JPMS yourself, can you use a JPMS dependency ? From what information I've been able to gather so far, the answer is no, even if you are running on a jdk9+ JVM. Thereby I don't beleive all currently available Java projects (frameworks, libraries, etc) will adopt JPMS. I'm not even sure all new ones will.

I have decided to stick with OSGi for APS but make it build and run on JDK 9+ (It currently runs on JDK 11). It is going to be interesting to see what the OSGi people will do (probably nothing since JPMS is simply not compatible with OSGi). Indifferent from OSGi, JPMS does not seem to support private in-module third party library dependencies, while OSGi even supports exporting packages from private internal dependencies. And this does not require these external jars to be provided externally. Just deploy your bundle and they will be available.

----

Lots of fun ideas, and far to little time ...

Tommy

Professional Code Geek, working with Java since 1.0, C & C++ before that. Have also done Motorola 6809 and Motorola 68000 assember code.



