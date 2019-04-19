# Prerequsites

This code base do contain some react frontend code using javascript. Thereby __npm__
(Node Package Manager) needs to be installed on the machine before this will fully
build.

Since Groovy is used instructions on the following page needs to be followed : https://github.com/tombensve/maven-bundle-plugin/blob/master/README.md.

This because maven-bundle-plugins usage of bnd causes groovy code to confuse bnd into thinking there is code in default package. After a support discussion with bnd there are options in bnd for solving this, but those are not availabe when bnd is used via maven-bundle-plugin. bnd's own maven plugin is far more primitive however so switching is a big job.

I'm hoping Groovy 3.0 will solve this, but I wouldn't bet on it.


# Application Platform Services (APS)

Copyright © 2013 Natusoft AB

__Version:__ 1.0.0 (working up to ...)

__License:__ [Apache 2.0](lics/Apache-2.0.md)

__This project is currently work in progress and cannot be expected to be stable!!__

Work is slow, whenever time permits.

To be very clear: **This is currently, and probably for a long time comming, a playground where I'm having fun.**

The __original (and still active) goal__ with this is to make a very easy to use web platform, currently based on Vert.x & React.

This project is now using 2 exceptional frameworks: __Vert.x & React__. These both belong to the same category: Things that just work! I have the highest respect for the people behind both of these. These both also supply outstanding documentation (far from all does!).

I started using OSGi (only the base 4 APIs) as a base platform because of the modularity of OSGi. I wanted small individual deployments of functionality and a nice clean way of interacting. OSGi provided that. I did for a short while try to drop OSGi but it actually made things more complicated, so OSGi stays. The OSGi maintainers are working on making it run on Java 9+ also. So OSGi is not going away, and there is not an '==' between Java 9+ and OSGi!! Java9+ only provides modularity, not the eminent service platform of OSGi.

**Do note:** that APS will run in any OSGi container (well, felix, Karaf, Knopplerfish, Virgo (not sure this is still kicking)). That said, I woudn't exactly call APS a straight OSGi platform. APS started out a long time ago, and provides a lot of own solutions rather than official OSGi stadard solutions. It does its own thing, especially with APSActivator (looks though all bundle classes and instantiates and dependency injects based on APS annotations). It just uses base OSGi as a base. APS is also largely coded in Groovy.

The _APSOSGiTestTools_ is actually an implementation of the 4 basic OSGi APIs, but without the classloading. It uses the JUnit classpath. This makes it trivially easy to test bundles and interaction between bundles. My plan is to clone this and then add classloading also and make it deploy bundles embedded in a jar so that it can be run with a java -jar, not requiring and external container. Do note that this will __not__ be OSGi compatbile! It will only run code only using base 4 APIs and none of the OSGi annotations will apply. So APS apps will run on any OSGi container, but most OSGi apps will not run on the APS specific mini container. It is intended to be as tiny as possible and only run APS.

APS have always been about keeping things simple. Easy to use APIs providing only basic functionality with no configurational options API wise. My intentions is to encapsulate complexity and provide the easiest way possible to use for all other code. APS also provides APIs for common things like messaging with very easy/basic APIs. Each specific implementation is responsible for any configuration needed and there is a more structured configuraton service using JSON for configuration data, for other services to use instead of the primitive properties provided by OSGi.

React is a web component framework that follows my ideas of simplicity and small code to the extreme! Love at fist sight :-).

Vert.x I stumbled upon accidentally, and realized that this is "the shit" I'm been looking for! When I started out with this project (yes, its been a longtime playground) I felt stuck with the JEE web APIs. The requirement of a web container felt limiting, and rather heavyweight.

Vert.x fulfilled my needs and then some. And it is truly easy to set up an HTTP server, handle requests, etc. So Vert.x also fits within my goals of keeping things simple and codewise small. All functionality in library form and no outside container to deploy in (well, there are verticles, but they are not required). You can write small lightweight code that only contains what you need for the specific app, and nothing else. It does pull in a lot of 3rd party libraries, but it still feels a lot smaller than the average JEE container. A _Vertx_ instance is made available as an OSGi service.

APS adds a thin layer on top of Vert.x. In many cases Vert.x is used directly, but for messages there is an APS specific API that uses the concept of routes (not to be confused with http routes, 'route' has become a popular word :-)). Using the APS messaging API it is also possible to install other busses, like RabbitMQ for example accessible through same api, but different routes. It is actually possible to send the same message on multiple busses at the same time by specifying multiple, comma separated routes. If that is useful or not is a completely different question.

The general architecture of an APS app is then intended to be message driven, requireing well defined messages, and with very loose coupling mostly only having dependencies between functionallity and the messages it works with, making the messages central. This offers a high level of flexibility, and I'm counting on less code. The frontend code and the backend code will then also look quite similar working with messages. I also beleive that resuing the same messages over the whole application is something to strive for, but requires very well though trough messages that can be both reusable and specific for a specific message. APS includes a very simple but useful JSON schema definition and a validator that validates a JSON structure against a schema. The schema forces simple JSON structures which I think is a good thing. Optimally there should be a JSON schema done for each message. It will both document the messages and provide validation of them.

Do note that APS needs a clustered Vert.x and will create/join such on startup. It can however be told to start a non clustered Vert.x by system property on start. This can be useful when running in test.

About testing: APS provide a testing tool called APSOSGiTestTools. It actually implements a primitive APS level OSGi container but without classloading (as mentioned above), using junit classpaths instead. So most tests run as they would in a real deployment and thus also starts Vert.x. This could cause problems if multiple builds are run concurrently on the same machine, like in a Jenkins for example. But if Vert.x is run unclustered it would probably work if HTTP service tests and similar use random ports. There is no good support for that yet in APS. I'm also considering trying to run tests within docker containers.

And yes, I'm aware of the security warnings in GitHub for this project. Since this is still a work in progress they are not that relevant yet. They will be taken care of in time.

Lots of fun ideas, and far to little time ...

Tommy

Professional Code Geek
