# Application Platform Services (APS)

Copyright © 2019 Natusoft AB

__Version:__ 1.0.0 (working up to ...)

__License:__ [Apache 2.0](lics/Apache-2.0.md)

__JDK Level:__ This now builds with all tests working on JDK 11. No, there is no JPMS usage! And it is also using a beta of Groovy 3.0. Have not run into any problem with Groovy being beta so far. 

## This project is work in progress and currently cannot be expected to be completely stable!!

Work is slow, whenever time permits.

To be very clear: **This is currently, and probably for a long time comming, a playground where I'm having fun.**

The __original (and still active) goal__ with this is to make a very easy to use web platform.

This project is now using 2 exceptional frameworks: __Vert.x & React__. These both belong to the same category: Things that just work! I have the highest respect for the people behind both of these. These both also supply outstanding documentation (which is not always the case with all frameworks and libraries!).

## Building

This code base do contain some react frontend code using javascript. Thereby __npm__
(Node Package Manager) needs to be installed on the machine before this will fully
build.

Some sub modules runs a bash script to build frontend stuff. On Windows this might cause a problem if not build under the Git Bash shell or some other bash windows port. The important thing is that when "_exec-maven-plugin_" runs "_${basedir}/src/main/js/aps-webmanager-frontend/build.sh_" then "_/bin/bash_" must be found. 

On windows 10 it is possible to install a Linux concurrently with Windows. That is the safest bet to build on windows. The Git Bash might work, but I don't have a windows to test on.  

### Docker build

There is now also a _docker-build_ folder in the root. It contains _bin/create-build-image.sh_ which creates a docker image for building. This build is much slower, but since the build by default runs tests that start web server and a vert.x cluster it is safe to run in a docker image without conflicting with anyone else on the same network. The _bin/do-build.sh_ is a convenience for running the dockerized build. It creates a volume for the ~/.m2/repository path within the container that is also made available at ~/apsbuildm2. Since this is a volume it is remembered between builds. But this volume can be deleted and recreated to test that the build is not dependent on old stuff still left in ~/.m2/repository. This without affecting enything else. I did discover that what I've previously commited did not build without old stuff left in my ~/.m2/repository! My bad! Sorry! But I've also warned that this is a work in progress!

Do note that the whole project source is copied into the image at buildtime (running bin/create-build-image.sh)! There might be smarter ways of making the container build from original outside of container, but I have yet not found the words to search for such a solution that google will not misunderstand. The _create-build-image.sh_ script however is nice enough to delete existing image if found before recreating a new one. It will even stop the container if it is running! So it can be run at anytime to create a new build image with the latest. 

## Docker

_Build/APSDockerImages/APSWebManagerDemoDocker_ builds a Docker container running the web demo, exposing port 8880. When it is running http://localhost:8880/apsweb/ will give you a page showing a test of components. 

## Runtime of the APS kind

The previous version of Vert.x required a few jars that easily deployed as OSGi bundles into a Felix or Karaf along with APS code. With current Vert.x version there are more dependencies and I spent 3 days trying to make Felix happy, but every resolved dependency seemed to add 3 more required dependencies. I was upp in far more jars than what is currently required to run!! Next version of vert.x will drop OSGi Manifest. I do like the OSGi service model, and I had already implemented enough of the 4 base OSGi APIs to be able to deploy and run in unit tests. So I renamed this to APSPlatform and made a simple booter that deploys bundles in a pointed to directory. This is now the runtime used. It does provide the OSGi service model, but does not modularise at all. I might add that in an as simple way as possible in the future. But this also opens up for JPMS modularisation instead. Though OSGi is better. For JPMS as soon as there is a _ModuleInfo.java_ then **everything** must be JPMS modularised even third party libs, which is more than a little nasty! Apparently some people realized this problem and created <https://github.com/moditect/moditect>. Not an optimal solution either, but better than nothing.

---- 

APS started out a long time ago, in a galaxy far ... eh ... wait no, sorry! It provides a lot of own solutions. It does its own thing, especially with APSActivator (looks though all bundle classes and instantiates and dependency injects based on APS annotations). It just uses base OSGi as a base. APS is also largely coded in Groovy.

APS have always been about keeping things simple. Easy to use APIs providing only basic functionality with no configurational options API wise. My intentions is to encapsulate complexity and provide the easiest way possible to use for all other code. APS also provides APIs for common things like messaging with very easy/basic APIs. Each specific implementation is responsible for any configuration needed and there is a more structured configuraton service using JSON for configuration data, for other services to use instead of the primitive properties provided by OSGi.

APS is intended to be a fullstack web application platform, with microservice architecture thinking. That is were Vert.x comes in. There are layers to APS. At the bottom there is the OSGi bundles and service model. This is for providing platform basics like Vert.x, which is setup and published as an OSGi service. There are also services that add a thin layer between application code and base layer things like Vert.x, for example APSBus. It provides a generic and very simple API. Under the surface it searches for APSBusRouter services published as OSGi services and forwards to them. Each APSBusRouter implemtation looks at the target string in the API  and determines if it is something it handles. There is a simple in JVM memory APSBusRouter. There is a Vert.x EventBus APSBusRouter. There is an AMQP APSBusRouter (currently using Vert.x code for that). The javascript frontend also has an APSBus that works similar. This gives one super easy API for all messaging, and more can easily be added at any time. Kafka for example. 

APS does one odd thing between front and backend. There is no REST! Actually there is nothing stopping a REST API, but in general APS uses the Vert.x EventBus between front and backend. Under the surface there is a websocket. This allows for 2 way communication at any time.

In general, application backend and frontend code should use messages to do things following the microservice way. APSRuntimes can be started on different nodes with different services deployed and join the same Vert.x cluster. Vert.x itself has something called Verticles that can be started and stopped similar to OSGi bundles but much more primitive. APS adds a thin layer and provides more isolation. APS also adds limitations. APS requires all messages to be in JSON for example to keep things simple, even if it is physically, technologically possible to send data in other formats. Its a principle.

APS also provides very simple JSON schemas, which forces very simple JSON structures, which IMHO is a good thing. JSON schema validation is not in any way required, but available if wanted. 

The APS frontend currently forces React and provides a set of components that are bus aware, and can talk to each other both locally and between front and backend. This because React makes this very simple. It is of course possible to produce own bus aware components! Some components do interact with each other locally without having any real knwolede of each others existance, just sending and listening to messages. For examle a button can be enabled or disabled depending on text in all required text fields. This is handled transparently by listening to and acting on messages in combination with component properties. Components can belong to named groups to simulate forms (remember NO REST!). In the web component demo app components are interacting with each other without knowing each others existance. Components can have "collector" property. This means that they listen to and save data sent by other components with same group name. This data is forwarded when collector component sends to backend. This simulates form POST functionality. This is however in no way required to be used! No component is required to talk directly to backend. There can be local frontend services that listends to component messages and who decides to send messages to backend and listen to backend messages and send local frontend messages to components. It is more or less possible to do whatever you want. The only thing that really should be done is to use the bus.

Lots of fun ideas, to little time ...

Tommy

Professional Code Geek, working with Java since the beginning, C & C++ before that. 



