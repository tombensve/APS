# Note

Currently a lot of what is written in README.md files are for myself since I can only work on this sporadically I need to document a lot to remind myself. When done I will cleanup a lot of these texts.

# APSWebManager

This sub project provides React web components that are connected to an eventbus. These components publishes change events on the bus, and some also listens to messages on the bus. The bus is backed by Vert.x EventBus. Component messages can be routed

- locally (client)
- backend
- whole cluster (generally not recommended)
- all clients
- all backends.

This is still within the specific web app. Each web app has a unique address on the bus so there is no interference with other code using the same bus.

These components can be used as any React components, but all gui specific properties are under guiProps property. This to easily be able to supply properties from JSON. But every property can be manually set as any other property.

There is a component actually called `APSWebManager`, if this is used it is the only component needed under `<App>`. This component listens on the bus for a JSON gui spec and renders the components in this spec within itself. Each component will be connected to the bus, and JSON spec includes routing properties. As a common feature of APSComponent base component, every component can act as a collector which listens to other components and saves their latest published data. Any event sent by a collector includes the collected data. If such a component is routed to the backend it will basically serve as a submit component. This does not require a form! Why not a form ? Well forms are REST based and APSWebManager only communicates over the eventbus, and it is not possible to "post" a form to the eventbus. 

Do **note** that there is absolutely no security what so ever yet!

The frontend code is [here](src/main/js/aps-webmanager-frontend).

## Current architectural thinking

The current rendering is a bit primitive and mixes gui and data!! This is work in progress and I do QD:s to test concepts.

My current plan is to create a model "_service_" (for the lack of a better name). It will hold a set of JSON data structures whose values can be set, and fetched via bus messages. GUI components will refer to the id of the model entry containing data to handle by the component. Components will also listen to model changes. The model will have a synchronized copy on the backend. I am considering routing all model change events to both front and backend independently of origin. That will allow for great flexibility and require less messages. I currently cannot come up with any gotchas for that, but is fully aware that there might be such. My general thinking is to use clear, flexibile and reusable messages that many functionalitys can listen/react to. I am however not entirely convinced that in the end that whishful thinking will work for all cases. 

## How to use

Due to limitations in react, or maybe just generic frontend package handling, the app page URL must be specified in package.json, which locks it to just one very specific path. So this projexct, APSWebManager should be seen as a template, which should be copied in whole and called something else, package.json updated with valid URL. APSWebManager itself just renders components in a row to be able to test and verify.

### Delivering GUI to web

TBD 

# Probably only builds on unixes!

The pom calls a build.sh script to deal with frontend build. This might have problems if built on windows! On windows the git installation do include a bash. __However__ there is no guarantee that maven will use gits bash implementation when trying to execute _build.sh_. Possibly if the maven job is started from a git bash.
