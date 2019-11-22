FROM openjdk:11

COPY /docker-build/files/settings.xml /root/.m2/settings.xml
COPY /apache-maven /apache-maven/
COPY /node-v12.13.1-linux-x64 /node-v12.13.1-linux-x64/
COPY / /source-code/

RUN mkdir /m2repo
RUN chmod 755 /source-code/docker-build/bin/container/make-m2-repo-link.sh
RUN /source-code/docker-build/bin/container/make-m2-repo-link.sh

VOLUME /m2repo

