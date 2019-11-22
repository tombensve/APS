# Building APS in a Docker container

Files in here + ../Dockerfile are to easily create a Docker container for building APS and to run such a build.

Note here that the docker container is using exactly the same installation of maven as is used to locally. It is copied on container creation due to not finding a maven image with jdk 11.

There are however 2 (overcomeable) problems:

## Nodejs 

I have found no base image with both java and node.js. "https://nodejs.org/dist/v12.13.1/node-v12.13.1-linux-x64.tar.xz" is now downloaded and unpacked in source root by _create-build-image.sh_ to be included in created image. It is deleted locally again after image is created.

## Filesystem changes apparently lags behind in docker container

Test run before the jar file is created reads target/classes for a list of classes avilable instead for deploying bundle being built. This is a workaround for tests who don't have a jar yet. Normally the contents of the jar is read to get a class list that is used for dependency injection. But tests that need to deploy themselves into the runtime do deploy the target/classes folder. Only names of classes are discovered there. But when run in docker the target/classes does not exist in filesystem that the JVM running the test can see. It gets "No such file or directory" when trying to read target/classes in the docker container. After the build is done the folder exists. I think this is related to how docker versions the filesystem. Anyhow, APSRuntime which is also used for tests do support faking jar content by providing an array of string entires of jar file instead of reading actuall jar or target/classes.  

