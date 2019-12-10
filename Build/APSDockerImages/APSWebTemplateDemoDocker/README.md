# APSWebTemplateDemoDocker

This builds a docker image that runs the components demo/verify web. 

When the image is running the following is available: <http://localhost:8880/apsweb/>.

There is a `bin/run-docker-image.sh` convenience script that basically does `docker run -p 8880:8880 --dns <dns address> --name "aps-web-template-demo" "aps-web-template" aps-platform/bin/run.sh` to run it. 

The other bin scripts are run by maven on build and clean. 

This image builds upon the "aps-runtime-base-jdk11" (produced by APSRuntimeBase).
