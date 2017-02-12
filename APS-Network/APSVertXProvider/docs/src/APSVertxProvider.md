## aps-vertx-provider

This provides reusable instances of Vertx. The _APSVertxService_ API takes a name. For the same name you get the same instance. The other APS services using Vertx uses the name "default". You can specify any name. If an instance for the specified name does not exist it will be created. Note however that created instances are not stopped until the bundle is stopped!

**Note**: The returned Vertx instance is a Groovy instance! You can always to getDelegate() on it to get the orignal Java instance. Note however that all Vertx using APS services and a lot of the other APS services are implemented in Groovy. So the Groovy runtime (2.4.7+) must be deployed as a bundle for this and much of APS to work. APSToolsLib and APS-APIs are pure java. Most of the other bundles are dependent on Groovy.

### API

The simple API looks like this:

