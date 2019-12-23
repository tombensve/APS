FROM aps-runtime-base-jdk11

# We already inherit everything else we need to run, so we only need to add our webb app.
#COPY imgsrc/aps-platform-deployment/bundles/* /aps-platform/bundles/
#COPY imgsrc/aps-platform-deployment/dependencies/* /aps-platform/dependencies/
COPY target/aps-web-template-1.0.0.jar /aps-platform/bundles/
# The web server port to expose.
EXPOSE 8880/tcp

# Dont! This will run immediately on creation!
#RUN /aps-platform/bin/run.sh
