# APSWebManager

This sub project provides React web components that are connected to an eventbus. These components publishes change events on the bus, and some also listens to messages on the bus. The bus is backed by Vert.x EventBus. Component messages can be routed

- locally (client)
- backend
- whole cluster (generally not recommended)
- all clients
- all backends.

This is still within the specific web app. Each web app has a unique address on the bus so there is no interference with other code using the same bus.

These components can be used as any React components, but all gui specific properties are under guiProps property. This to easily be able to supply properties from JSON. But every property can be manually set as any other property.

There is a component actually called `APSWebManager`, if this is used it is the only component needed under `<App>`. This component listens on the bus for a JSON gui spec and renders the components in this spec within itself. Each component will be connected to the bus, and JSON spec includes routing properties. As a common feature of APSComponent base component every component can act as a collector which listens to other components and saves their latest published data. Any event sent by a collector includes the collected data. If such a component is routed to the backend it will basically serve as a submit component. This does not require a form!

The frontend code is [here](src/main/js/aps-webmanager-frontend).