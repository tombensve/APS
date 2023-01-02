# This default version of the build in container Dockerfile requires a rerun
# of docker-build/bin/create-build-image.sh after source has been modified.
# This build takes about 11 minutes on my machine. The 'Dockerfile-build-local-fs-in-container'
# sounds like something you would want to do. Such a build however takes an hour.
# So recreateing image saves almost 50 minutes.
FROM openjdk:11

COPY /old-shit/docker-build/files/settings.xml /root/.m2/settings.xml
COPY /apache-maven /apache-maven/
COPY /node-v12.13.1-linux-x64 /node-v12.13.1-linux-x64/
COPY / /source-code/

RUN mkdir /m2repo
RUN chmod 755 /source-code/docker-build/bin/container/make-m2-repo-link.sh
RUN /source-code/docker-build/bin/container/make-m2-repo-link.sh

VOLUME /m2repo

