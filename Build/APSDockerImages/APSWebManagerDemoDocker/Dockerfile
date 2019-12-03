FROM openjdk:11
COPY imgsrc/aps-platform-deployment/bin/* /aps-platform/bin/
COPY imgsrc/aps-platform-deployment/bundles/* /aps-platform/bundles/
COPY imgsrc/aps-platform-deployment/dependencies/* /aps-platform/dependencies/
EXPOSE 8880/tcp
# Dont! This will run immediately on creation!
#RUN /aps-platform/bin/run.sh
