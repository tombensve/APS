# Note

Currently a lot of what is written in README.md files are for myself since I can only work on this sporadically I need to document a lot to remind myself. When done I will cleanup a lot of these texts.

**React suck**: Building react app is of course not backwards compatbile!! Installing a fresh react on a new machine will of course fail to build this before actually trying to build anything! Will probably have to create a new react app and copy sources from the old hoping there is some validity in them yet. Should probably build this within a docker container, but then the base image might no longer exist the next time I wan't to build ...

## TODO

This currenlty provides both frontend and backend in same maven module and same jar file when built. These should be separated and run in separate Docker containers to make it more representative in how it is indended to be used.

# Prerequisites

Since this project also contains javascript code that is built via maven-exec-plugin __npm__
(Node Package Manager) must be installed before this project can build. The called on
`src/main/js/aps-webtemplate-frontend/build.sh` script checks for node_modules directory and
does and `npm install` if it does not exist. This and general building of js code requires
_npm_ to be installed on the machine building on.

# APSWebTemplate

This sub project provides React web components that are connected to an eventbus. These components publishes change events on the bus, and some also listens to messages on the bus. The bus is backed by Vert.x EventBus. Component messages can be routed

- locally (client).
- backend.
- whole cluster (probably a bad idea).
- all clients (probably a bad idea).
- all backends (probably a bad idea).

This is still within the specific web app. Each web app has a unique address on the bus so there is no interference with other code using the same bus.

These components can be used as any React components, but all gui specific properties are under guiProps property. This to easily be able to supply properties from JSON. But every property can be manually set as any other property.

There is a component actually called `APSWebManager`, if this is used it is the only component needed under `<App>`. This component listens on the bus for a JSON gui spec and renders the components in this spec within itself. Each component will be connected to the bus, and JSON spec includes routing properties. 

As a common feature of APSComponent base component, every component can act as a collector which listens to other components and saves their latest published data. Any event sent by a collector includes the collected data. If such a component is routed to the backend it will basically serve as a submit component. This does not require a form! Why not a form ? Well forms are REST based and APSWebManager only communicates over the eventbus, and it is not possible to "post" a form to the eventbus.

The frontend code is [here](src/main/js/aps-webtemplate-frontend).

## How to use

Due to limitations in react, or maybe just generic frontend package handling, the app page URL must be specified in package.json, which locks it to just one very specific path. So this project, APSWebTemplate should be seen as a template, which should be copied in whole and called something else, package.json updated with valid URL. APSWebTemplate itself just renders components in a row to be able to test and verify.

# Probably only builds on unixes!

The pom calls a build.sh script to deal with frontend build. This might have problems if built on windows! On windows the git installation do include a bash. __However__ there is no guarantee that maven will use gits bash implementation when trying to execute _build.sh_. Possibly if the maven job is started from a git bash. A windows 10 with a coexisting Linux should work.

Note that it is also possible to build in a Docker container! This is slower than building locally, but should work the same everywhere. See `docker-build/` folder in root.
