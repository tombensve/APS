# APSDockerImages

This contains subprojects for building different docker images for serving web content. 

Currently there is only the demo / test & debug of components web, but what is done here can be duplicated for any (web) application.

## Bash script usage

I first started with __docker-maven-plugin__ at <https://dmp.fabric8.io/>. It has a lot of features, but in the end I really don't like _in the middle functionality_ that I have no control over when it does not give me much more that what the end product gives me. Producing a docker image is not difficult, just using docker directly, and it gives access to whatever docker can do and not just what a plugin has decided to support. And when using some product, actually using the product just makes sense :-). 

There is however a potential catch here. I'm using .sh scripts (bash) called from _maven-exec-plugin_. I'm not sure if that will work on Windows. Possibly if the maven build job is done in a git bash shell, but I don't know how the integration with windows works there. On the other side Microsoft these days support running [Linux concurrently with Windows](https://docs.microsoft.com/en-us/windows/wsl/install-win10) on windows 10. 

## Setting up the docker image

The image must include jar files all of which are available under _~/.m2/repository/..._. The _Dockerfile_ however does not seem to have any way of specifiying building users home directory (~ in bash). I have googled a lot on this, and found no way to specify this path. So I use bash scripts to setup what should be in the image and then copy from that in Dockerfile. There is a script (setup-deployment.sh) that does this:

    vertxver=3.8.0
    nettyver=4.1.19.Final
    groovyver=3.0.0-beta-3
    hazelcastver=3.12.4
    jacksonannotationsver=2.9.0
    jacksoncorever=2.9.0
    
    dl=$(dirname $0)/../../../bin/downloadJar.sh
    bundles=$(dirname $0)/../imgsrc/aps-platform-deployment/bundles
    deps=$(dirname $0)/../imgsrc/aps-platform-deployment/dependencies
    bin=$(dirname $0)/../imgsrc/aps-platform-deployment/bin
    
    # === Dependencies ===
    # Base
    ${dl} io.vertx                   vertx-core          ${vertxver}              ${deps}
    ${dl} io.vertx                   vertx-web           ${vertxver}              ${deps}
    ${dl} io.vertx                   vertx-web-common    ${vertxver}              ${deps}
    ${dl} io.vertx                   vertx-hazelcast     ${vertxver}              ${deps}
    ...

The `downloadJar.sh` script first looks under ~/.m2/repository/... for the jar file, and if not found there it downloads it from Bintrays jcenter. An

    aps-platform-deployment/
        bin/
            run.sh
        bundles/
        dependencies/

structure is setup and jars copied to it. The run.sh script will run everything. This structure is then copied by the Dockerfile. This way maven can download all required jars in addition to those build by the project, and then get them into the docker image. 
 
The `build-docker-image.sh` script builds the actual image, and `run-docker-image` starts an instance of it. 

I tried to use mavens resource expansion to expand _env.Home_ in a Dockerfile, but that got more messy due to artifact paths under ~/.m2/repository. The above script that puts jars in place is much cleaner, and it also makes the Dockerfile trivial:

    FROM openjdk:11
    COPY imgsrc/aps-platform-deployment/bin/* /aps-platform/bin/
    COPY imgsrc/aps-platform-deployment/bundles/* /aps-platform/bundles/
    COPY imgsrc/aps-platform-deployment/dependencies/* /aps-platform/dependencies/
    EXPOSE 8880/tcp
    RUN /aps-platform/bin/run.sh

