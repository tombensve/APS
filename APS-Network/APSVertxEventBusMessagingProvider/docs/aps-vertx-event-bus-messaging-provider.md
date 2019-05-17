## aps-vertx-event-bus-messaging-provider

This publishes 2 services: `MessageSender`, and `MessageSubscriber`.

Each of these use Vertx EventBus under the surface. These messages are not persistent and are sent within a Vert.x cluster.

Vert.x has a send() method that sends to one subscriber. If there are more than one subscriber on the address it does a round robin on the subscribers. Vert.x also has a publish() method that always send to all subscribers.

The APS API does not reflect the Vert.x API. To do a publish the (possibly resolved) destination must start with "all:".

The aps-vertx-provider also publishes the EventBus instance as a service, which is what this implementation is using. The EventBus can of course be used directly instead of this service. This service uses the official APS messaging API and thus all kinds of messaging can be done in the same way. It is also in general a good idea encapsulate the real implementation so that it is easy to change to something else if needed/wanted in the future. This service provides that.

For more information on Vertx see: [http://vertx.io/docs/vertx-core/groovy/](http://vertx.io/docs/vertx-core/groovy/)

### Lookup

This properties for both services contains:

        aps-protocol-name:    vertx-eventbus
        service-category:     network
        service-function:     messaging
        messaging-persistent: false
        messaging-clustered:  true
        service-provider:     aps-vertx-event-bus-messaging-provider:sender/subscriber

