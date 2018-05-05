## aps-vertx-event-bus-messaging-provider

This publishes 3 services: `MessagePublisher`, `MessageSender`, and `MessageSubscriber`.

Each of these use Vertx EventBus under the surface. These messages are not persistent and are sent within a Vertx cluster.

The difference between publisher and sender is that publisher sends to everyone listening on the destination, while sender only sends to one. Vertx uses round robin for sender to spread out to nodes in the cluster.

The aps-vertx-provider also publishes the EventBus instance as a service, which is what this implementation is using. The EventBus can of course be used directly instead of this service. This service uses the official APS messaging API and thus all kinds of messaging can be done in the same way. It is also in general a good idea encapsulate the real implementation so that it is easy to change to something else if needed/wanted in the future. This service provides that.

For more information on Vertx see: <http://vertx.io/docs/vertx-core/groovy/>

### Lookup

This properties for all 3 services contains:

    aps-protocol-name:    vertx-eventbus
    service-category:     network
    service-function:     messaging
    messaging-persistent: false
    messaging-clustered:  true
    service-provider:     aps-vertx-event-bus-messaging-provider:publisher/sender/subscriber

