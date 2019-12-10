# APSRuntimeBase Docker image

This builds a Docker image containing a simple APSRuntime setup of:

    aps-platform/
        bin/
            aps-platform-booter-1.0.0.jar
            run.sh
        bundles/
            required core bundles
        dependencies/
            all external dependencies

This contains all dependency jars needed to run and all APS bundles always required to run APS, core functionality. Need to restructure the project a bit to make it more clear what is core and what is optional. 

Nothing will happen if this image is run as is! This image is intended to be base image for runtime images. The APSWebTemplateDemoDocker now builds a Docker image based on APSRuntimeBase and only adds _aps-web-template-1.0.0.jar_ web application under _bundles/_. The rest is inherited from base image.
