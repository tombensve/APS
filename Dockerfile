FROM openjdk:8
#ADD m2 m2/
##ADD apache-maven apache-maven/

COPY /docker-build/files/settings.xml /root/.m2/settings.xml
COPY /apache-maven /apache-maven/
COPY / /source-code/

RUN mkdir /m2repo
RUN chmod 755 /source-code/docker-build/bin/container/make-m2-repo-link.sh
RUN /source-code/docker-build/bin/container/make-m2-repo-link.sh

VOLUME /m2repo

