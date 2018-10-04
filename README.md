# Application Platform Services (APS)

Copyright © 2013 Natusoft AB

__Version:__ 1.0.0 (working up to ...)

__License:__ [Apache 2.0](lics/Apache-2.0.md)

__This project is currently work in progress and cannot be expected to be stable!!__

Work is slow, whenever time permits.

To be very clear: **This is currently, and probably for a long time comming, a playground where I'm having fun.** The original (and still active) goal with this is to make a very easy to use web platform, currently based on Vert.x & React.

This project is now using 2 exceptional frameworks: __Vert.x & React__. These both belong to the same category: Things that just work! I have the highest respect for the people behind both of these. These both also supply outstanding documentation.

---

_This problem will go away when I drop OSGi (see below)!_

__To build__ this you must first follow the instructions here: https://github.com/tombensve/maven-bundle-plugin/blob/master/README.md

This because maven-bundle-plugins usage of bnd causes groovy code to confuse bnd into thinking there is code in default package. After a support discussion with bnd there are options in bnd for solving this, but those are not availabe when bnd is used via maven-bundle-plugin. bnd's own maven plugin is far more primitive however so switching is a big job.

---

I started using OSGi (only the base 4 APIs) as a base platform because of the modularity of OSGi. I wanted small individual deployments of functionality and a nice clean way of interacting. OSGi provided that. But it was just a means to an end, and it has caused problems along with solutions.

APS have always been about keeping things simple. Easy to use APIs providing only basic functionality with no configurational options API wise. My intentions is to encapsulate complexity and provide the easiest way possible to use for all other code. Each specific implementation is responsible for any configuration needed and I made a more structured configuraton service for other services to use. OSGi basically only gives you properties. My goal with configuration is to provide easy to understand structured configuration and a web app to edit such configuration that is easy to use and comprehend even for non developers.

React is a web component framework that follows my ideas of simplicity and small code to the extreme! Love at fist sight :-).

Vert.x I stumbled upon accidentally, and realized that this is "the shit" I'm been looking for! When I started out with this project (yes, its been a longtime playground) I felt stuck with the JEE web APIs. The requirement of a web container felt limiting, and rather heavyweight.

Vert.x fulfilled my needs and then some. And it is truly easy to set up an HTTP server, handle requests, etc. So Vert.x also fits within my goals of keeping things simple and codewise small. All functionality in library form and no outside container to deploy in (well, there are verticles, but they are not required). You can write small lightweight code that only contains what you need for the specific app, and nothing else. It does pull in a lot of 3rd party libraries, but it still feels a lot smaller than the average JEE container.

Another goal is to be able to package a complete web app in a single jar file that can be started with _java -jar ..._. The same jar file can be started on multiple nodes and will automatically be load balanced between the nodes due to vert.x being rather smart in doing a round robin to listeners of the same address in a cluster on send. There is of course nothing inhibiting multiple, separate jars for each service if you want to restart/redeploy a specific service without restarting everything.

APS adds a thin layer on top of Vert.x. In many cases you use Vert.x directly, but for messages there is an APS specific API that uses the concept of routes (not to be confused with http routes, 'route' has become a popular word :-)). Using the APS messaging API it is also possible to install other busses, like RabbitMQ for example accessible through same api, but different routes. It is actually possible to send the same message on multiple busses at the same time by specifying multiple, comma separated routes. If that is useful or not is a completely differnt question.

Do note that APS needs a clustered Vert.x and will create/join such on startup. It can however be told to start a non clustered Vert.x by system property on start. This can be useful when running in test.

#### <=== OSGi (out), Vert.x (new platform base) ===>

I've decided to drop OSGi, and make a very much simpler base based on Vert.x directly, which provides everything that is neded. The modularity of OSGi makes handlng of 3rd party libraries of possibly different versions for different deployables very nice. But JDK 11 is alreay on it way and Java have modularity since 9, and that gives a good reason to go up past 8, which seemed problematic with OSGi.

To my big surprise there are some people that have starred this project on github! If it is due to my OSGi tools like APSActivator, APSServiceTracker, etc it might be time to clone a copy. I have however put a lot of effort into these so I might just extract the most useful tools into a separate git repo.

Tommy

Professional Code Geek
