# Application Platform Services (APS)

__Version:__ 1.0.0 (working up to ...)

__License:__ [Apache 2.0](lics/Apache-2.0.md)

__JDK Level:__ This now builds with all tests working on JDK 11. No, there is no JPMS usage! And it is also using a beta of Groovy 3.0. Have not run into any problem with Groovy being beta so far. 

## This project is work in progress and currently cannot be expected to be completely stable!!

Work is slow, whenever time permits.

To be very clear: **This is currently, and probably for a long time comming, a playground where I'm having fun.**

The __original (and still active) goal__ with this is to make a very easy to use web platform.

This project is now using 2 exceptional frameworks: __Vert.x & React__. These both belong to the same category: Things that just work! I have the highest respect for the people behind both of these. These both also supply outstanding documentation (which is not always the case with all frameworks and libraries!).

**Note**: To be very clear, as said above "this is a playground where I'm having fun", and that status will remain even when I feel this is done! Why ? Because it really cannot have any other status! If anyone thinks what I'm doing here is interesting then clone it and do what you want with it, but you are on your own! 

## Building

This code base do contain some react frontend code using javascript. Thereby __npm__
(Node Package Manager) needs to be installed on the machine before this will fully
build.

Bash scripts are run with maven-exec-plugin! If this will work with the Git bash shell on Windows or not I don't know. I don't have a windows machine to test on. On windows 10 it is possible to install a Linux concurrently with Windows. That is the safest bet to build on windows. 

Docker containers are built so Docker must also be available. Tested with **docker desktop community 2.1.0.5 (mac version)**. Have read that Docker sometimes have bad compatibility with itself. 

### Docker build

There is now also a [docker-build](docker-build/) folder in the root. It contains _bin/create-build-image.sh_ which creates a docker image for building. This build is much slower, but since the build by default runs tests that start web server and a vert.x cluster it is safe to run in a docker image without conflicting with anyone else on the same network. The _bin/do-build.sh_ is a convenience for running the dockerized build. It creates a volume for the ~/.m2/repository path within the container that is also made available at ~/apsbuildm2. Since this is a volume it is remembered between builds. But this volume can be deleted and recreated to test that the build is not dependent on old stuff still left in ~/.m2/repository. This without affecting enything else. I did discover that what I've previously commited did not build without old stuff left in my ~/.m2/repository! My bad! But I've also warned that this is a work in progress!

## Docker

_Build/APSDockerImages/APSWebManagerDemoDocker_ builds a Docker container running the web demo, exposing port 8880. When it is running http://localhost:8880/apsweb/ will give you a page showing a test of components. 

----

[Using a maven plugin for Docker is not a given!](https://medium.com/containers-101/using-docker-from-maven-and-maven-from-docker-1494238f1cf6) 

Citations:

"**There have been cases in the past where Docker has broken compatibility even between its own client and server, so a Maven plugin that uses the same API will instantly break as well**. In less extreme cases, Docker has presented new features that will not automatically transfer to your favorite Maven plugin. You need to decide if this delay is important to you or not."

"I have seen at least two companies that instead of using a dedicated Docker plugin, **are just calling the native Docker executable via the maven exec plugin.** This means that the Docker version that is injected in the Maven lifecycle is always the same as the Docker daemon that will actually run the image. This solution is not very elegant but it is more resistant to API breakage and guarantees the latest Docker version for the Maven build process."

----

I decided to not use a maven plugin for Docker and run Docker commands directly in bash scripts via maven-exec-plugin instead.

## Runtime of the APS kind

The previous version of Vert.x required a few jars that easily deployed as OSGi bundles into a Felix or Karaf along with APS code. With current Vert.x version there are more dependencies and I spent 3 days trying to make Felix happy, but every resolved dependency seemed to add 3 more required dependencies. I was upp in far more jars than what is currently required to run!! Next version of vert.x will drop OSGi Manifest. I do like the OSGi service model, and I had already implemented enough of the 4 base OSGi APIs to be able to deploy and run in unit tests. So I renamed this to APSPlatform and made a simple booter that deploys bundles in a pointed to directory. This is now the runtime used. 

It does provide the OSGi service model, but does not modularise at all. This also opens up for JPMS modularisation instead. For JPMS however, as soon as there is a _ModuleInfo.java_ then **everything** must be JPMS modularised even third party libs, which is more than a little nasty! Apparently some people realized this problem and created <https://github.com/moditect/moditect>. Not an optimal solution either, but better than nothing.

---- 

## About

APS is intended to be a fullstack web application platform, with microservice architecture thinking, and be very easy to use / develop with. APS is also intended to be message driven, specificly using JSON messages on both frontend and backend. Services should be message driven, that is listen to messages, act on them, produce new messages. This creates a high level of decoupling. A service for example only know about itself, the message(s) it reads, the message(s) it produces. The messages are central. APS also provides a very simple JSON schema definition and validator. This also makes it limited not allowing for 100% of structures possible with JSON. Or seen in another perspective, it forces trivial JSON message structures. Personally I don't see that as a bad thing. There is however nothing forcing this validation. It is a utility to be used if wanted.  

## Frontend

(<https://github.com/tombensve/APS/tree/master/APS-Webs/APSWebTemplate/src/main/js/aps-webtemplate-frontend>)

APS makes use of React for frontend, but are not totally locked into React. It however do use ES6 code. I don't think it would be impossible to adapt to Vue.js. What is central for APS frontend is the _APSEventBus_, that is all components are connected to APSEventBus and communication over it. The frontend is message driven just like the backend. There is one _oddidy_ in the APS frontend: It talks to backend over the bus by sending messages! In other words, no REST! There is however support for form behavior, just not an HTML form. Components can interact with each other using messages and many do via inherited functionality from APSComponent base component class. They don't have to have specific knowlede of each other, just be on the same bus.

## Backend

The backend and the bridge between frontend and backend is handled by [Vert.x](https://vertx.io/). APS however provides a thin abstraction layer over Vert.x and other code for some things. One central such is APSBus. This have a slighly different adressing scheme than the frontend counterpart APSEventBus, but otherwise behave very similarly by requiring APSBusRouter implementations as Services. For this level of services the OSGi service model is used to provide APSBusRouter implementations. There is one implementation that sends messages locally within JVM. There is one that uses Vert.x EventBus. There is one that sends messages over AMQP (via Vert.x). APSBus forwards calls to found implementations of APSBusRouter to handle. Deploy those implementations wanted. The backend bus has an adress scheme called _target_ in the form of "id:address" where id can be "local", "cluster", whatever there is an APSBusRouter implementation supporting. What the "address" part is depends on the implementation. For "cluster" and "local" it is just a unique name that an application service subscribes to and a client sends to.   

Backend is based on OSGi light, only parts of the 4 base OSGi APIs. APS currently runs on its own simple backend (APSRuntime) implementing these base OSGi APIs, but currenlty without any modularity.  The runtime is started with:

    java -jar aps-platform-booter-1.0.0.jar --dependenciesDir path/to/dependenciesdir --bundlesDir path/to/bundlesdir 

Where dependencies dir contains all dependency jars, and budlesdir contains all bundles to deploy.

`Build/APSDockerImages/APSWebTemplateDemoDocker/imgsrc/aps-platform-deployment/`contains a sample deployment structure.

(TODO: Make code examples and then provide link to them here ...) 

Lots of fun ideas, too little time ...

Tommy

Professional Code Geek, working with Java since the beginning, C & C++ before that. 



