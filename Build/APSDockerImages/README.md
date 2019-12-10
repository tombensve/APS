# APSDockerImages

This contains subprojects for building different docker images for serving web content. 

Currently there is only the demo / test & debug of components web, but what is done here can be duplicated for any (web) application.

## How docker is run from maven

I first started looking at __docker-maven-plugin__ at <https://dmp.fabric8.io/>. It has a lot of features, but in the end I really don't like _in the middle functionality_ that I have no control over when it does not give me much more than what the end product gives me. Producing a docker image is not difficult, just using docker directly, and it gives access to whatever docker can do and not just what a plugin has decided to support. 

The following is copied from a medium article (with some formatting added by me) at <https://medium.com/containers-101/using-docker-from-maven-and-maven-from-docker-1494238f1cf6> by Codefresh:

----

### Should you use a Maven plugin for Docker?

... Docker is one of the fastest moving technologies at the moment and in the past, there were several occasions where new Docker versions were not compatible with the old ones. __When you select a Maven plugin for Docker, you essentially trust the plugin developers that they will continuously update it, as new Docker versions appear.__

There have been cases in the past where Docker has broken compatibility even between its own client and server, so a Maven plugin that uses the same API will instantly break as well. In less extreme cases, Docker has presented new features that will not automatically transfer to your favorite Maven plugin. You need to decide if this delay is important to you or not.

For this reason, __it is crucial to understand how each Maven plugin actually communicates with the Docker environment and all the points where breakage can occur__.

I have seen at least two companies that instead of using a dedicated Docker plugin, __are just calling the native Docker executable via the maven exec plugin. This means that the Docker version that is injected in the Maven lifecycle is always the same as the Docker daemon that will actually run the image__. This solution is not very elegant but it is more resistant to API breakage and guarantees the latest Docker version for the Maven build process.

----

Hating things that easily break I for the above and other reasons choose to run docker directly via _maven-exec-plugin_, and apparently that just makes it a little bit better. 

The currenlty used version of "docker desktop community" (on mac) is `2.1.0.4 (39773)`. It comes with a broken non startable kubernetes, at least on mac. From what the above referenced article is saying, actually using docker seems like a risk of constantly having to adapt to docker incompatibilitites with previous versions of itself. They should have an LTS version. The following <https://github.com/moby/moby/issues/20424> discuss this, its from 2016 and still there seem to be no LTS ...

There is another potential catch here. I'm using .sh scripts (bash) called from _maven-exec-plugin_. I'm not sure if that will work on Windows. Possibly if the maven build job is done in a git bash shell, but I don't know how the integration with windows works there.  

## Setting up runtime images

The image must include jar files all of which are available under _~/.m2/repository/..._. The _Dockerfile_ however does not seem to have any way of specifiying files in users home directory (~ in bash). I have googled a lot on this, and found no way to specify this path. So I use bash scripts to setup what should be in the image and then copy from that in Dockerfile. There is a script (setup-deployment.sh) that does this:

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

Note that the specified dependencies in the above script is just what is needed to run. Not all transitive dependencies a full resolve of such would produce. This exact list does not exist anywhere in poms! It is just exactly the direct and transitivie dependencies needed to run. 

The `downloadJar.sh` script first looks under ~/.m2/repository/... for the jar file, and if not found there it downloads it from Bintrays jcenter. A

    aps-platform-deployment/
        bin/
            run.sh
        bundles/
        dependencies/

structure is setup and jars copied to it. The run.sh script will run everything. This structure is then copied by the Dockerfile. This way maven can download all required jars in addition to those built by the project, and then get them into the docker image. The reason for having 2 jar folders (bundles, dependencies) is that only those under bundles will have the content of the jar instpected to find all class files, which are then inspected for dependency injection. It would be possible to have everything in one folder, but then every singe jar file would be read and have its content inspected when only a few need that. Having a division between pure external dependencies and application bundles makes it very much cleaner.
 
The `build-docker-image.sh` script builds the actual image, and `run-docker-image.sh` starts an instance of it. 

### APSRuntimeBase

This builds a base image with APSRuntime and all dependencies and bundles needed to run anything. Specific run containers like APSWebTemplateDemoDocker base their image on the aps-runtime-base-jdk11 image produced by this, just adding their bundles.

Remember that APS is built upon a clustered Vert.x. Different services should be deployed in its own container. 

__TODO:__ <https://stackoverflow.com/questions/39812848/how-do-i-configure-vert-x-event-bus-to-work-across-cluster-of-docker-containers>

