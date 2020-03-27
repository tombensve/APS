# Application Platform Services (APS)

__Version:__ 1.0.0 (working up to ...)

__License:__ [Apache 2.0](lics/Apache-2.0.md)

__JDK Level:__ This now builds with all tests working on JDK 11. No, there is no JPMS usage! And it is also using a beta of Groovy 3.0. Have not run into any problem with Groovy being beta so far. 

## This project is work in progress and currently cannot be expected to be completely stable!!

Work is slow, whenever time permits.

To be very clear: **This is currently, and probably for a long time comming, a playground where I'm having fun.**

The __original (and still active) goal__ with this is to make a very easy to use web platform.

There is currently no real web app, only components demo. I'm working on comming up with a an idea for a more real web app to do with it as a more real test of if this is a good idea or not. 

## Building

This code base do contain some react frontend code using javascript. Thereby __npm__
(Node Package Manager) needs to be installed on the machine before this will fully
build.

Bash scripts are run with maven-exec-plugin! If this will work with the Git bash shell on Windows or not I don't know. I don't have a windows machine to test on. On windows 10 it is possible to install a Linux concurrently with Windows. That is the safest bet to build on windows. 

Docker containers are built so Docker must also be available. Tested with **docker desktop community 2.1.0.5 (mac version)**. Have read that Docker sometimes have bad compatibility with itself. 

### Docker build

There is now also a [docker-build](docker-build/) folder in the root. It contains _bin/create-build-image.sh_ which creates a docker image for building. This build is much slower, but since the build by default runs tests that start web server and a vert.x cluster it is safe to run in a docker image without conflicting with anyone else on the same network. The _bin/do-build.sh_ is a convenience for running the dockerized build. It creates a volume for the ~/.m2/repository path within the container that is also made available at ~/apsbuildm2. Since this is a volume it is remembered between builds. But this volume can be deleted and recreated to test that the build is not dependent on old stuff still left in ~/.m2/repository. This without affecting enything else. I did discover that what I've previously commited did not build without old stuff left in my ~/.m2/repository! My bad! 

There is a file called _Dockerfile-build-local-fs-in-container_ in the root. The default Docker build actually copies the source into the container forcing the container and image to be removed and recreated to rebuild. The alternative Dockerfile builds the local checkout within the container. The reason for not using the second as default is that it takes a full hour to build, while copying the source into the container takes abut 10 minutes. 

Do note that the Docker build should work on any platform!

----

[Using a maven plugin for Docker is not a given!](https://medium.com/containers-101/using-docker-from-maven-and-maven-from-docker-1494238f1cf6) 

Citations:

"**There have been cases in the past where Docker has broken compatibility even between its own client and server, so a Maven plugin that uses the same API will instantly break as well**. In less extreme cases, Docker has presented new features that will not automatically transfer to your favorite Maven plugin. You need to decide if this delay is important to you or not."

"I have seen at least two companies that instead of using a dedicated Docker plugin, **are just calling the native Docker executable via the maven exec plugin.** This means that the Docker version that is injected in the Maven lifecycle is always the same as the Docker daemon that will actually run the image. This solution is not very elegant but it is more resistant to API breakage and guarantees the latest Docker version for the Maven build process."

----

I decided to not use a maven plugin for Docker and run Docker commands directly in bash scripts via maven-exec-plugin instead.

---- 

## About

APS is intended to be a fullstack web application platform, with microservice architecture thinking, and be very easy to use / develop with. APS is also intended to be message driven, specificly using JSON messages on both frontend and backend. Services should be message driven, that is listen to messages, act on them, produce new messages. This creates a high level of decoupling. A service for example only know about itself, the message(s) it reads, the message(s) it produces. The messages are central. 

## Frontend

(<https://github.com/tombensve/APS/tree/master/APS-Webs/APSWebTemplate/src/main/js/aps-webtemplate-frontend>)

APS makes use of React for frontend, but are not totally locked into React. It however do use ES6 code. I don't think it would be impossible to adapt to Vue.js. What is central for APS frontend is the _APSEventBus_, that is all components are connected to APSEventBus and communication over it. The frontend is message driven just like the backend. There is one _oddidy_ in the APS frontend: It talks to backend over the bus by sending messages! In other words, no REST! There is however support for form behavior, just not an HTML form. Components can interact with each other using messages and many do via inherited functionality from APSComponent base component class. They don't have to have specific knowlede of each other, just be on the same bus. APSEventBus on the frontend works more or less the same as APSBus on the backend having routers. There is one local router and one that uses Vert.x EventBus to reach backend. 

## Backend

The backend and the bridge between frontend and backend is handled by [Vert.x](https://vertx.io/). APS however provides a thin abstraction layer over Vert.x and other code for some things. One central such is APSBus. This have a slighly different adressing scheme than the frontend counterpart APSEventBus, but otherwise behave very similarly by requiring APSBusRouter implementations as Services. For this level of services the OSGi service model is used to provide APSBusRouter implementations. There is one implementation that sends messages locally within JVM. There is one that uses Vert.x EventBus. There is one that sends messages over AMQP (via Vert.x). APSBus forwards calls to found implementations of APSBusRouter to handle.    

Backend is based on OSGi light, only parts of the 4 base OSGi APIs. APS currently runs on its own simple backend (APSRuntime) implementing the base OSGi APIs to ~85% (what is needed/used by APS), but without any modularity. The runtime forces the APSActivator which does dependency injections! It is started with:

    java -jar aps-platform-booter-1.0.0.jar --dependenciesDir path/to/dependenciesdir --bundlesDir path/to/bundlesdir 

Where dependencies dir contains all dependency jars, and budlesdir contains all bundles to deploy.

I was contacted by someone who had seen this repo and asked about APSRuntime as used in unit tests to deploy and run services. Since APSRuntime is a bit bound to APS I decided to break it out into a separate repo that has no dependencies on APS: [OSGishTestRunner](https://github.com/tombensve/OSGishTestRunner). DO NOTE that this is very basic OSGi only!! No declarative services, etc, just basic Bundle and events. 

Lots of fun ideas, too little time ...

Tommy

Professional Code Geek, working with Java since the beginning, C & C++ before that. 



