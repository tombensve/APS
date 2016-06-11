# APS RabbitMQ Message Service Provider

This service provides an implementation of APSMessageService using [RabbitMQ](http://www.rabbitmq.com/).

__Note:__ This implementation does not support _contentType_ in the API. When sending messages the _contentType_ will be ignored, and when messages are received the _contentType_ will always be "UNKNOWN".

A good suggestion is to always use JSON or XML as content.

## APSMessageService API

[Javadoc](http://apidoc.natusoft.se/APS/se/natusoft/osgi/aps/api/net/messaging/service/APSMessageService.html)

public _interface_ __APSSimpleMessageService__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This defines a simple message service. Can be implemented by using a message bus like RabbitMQ, Active MQ, etc or just a simple tcpip server or whatever.

Since the actual members are outside of this service API, it doesn't really know who they are and doesn't care, all members are defined by configuration.

The term 'target' is used in this API. This can be anything relevant for routing a message to its destination. It depends a lot on the underlaying implementation. JMS for example have both a Queues that can be sent to using queue names, but also topics. If JMS is used then the target can be either a queue or a topic. The target just represents some destination of the message.

__@SuppressWarnings("unused") String APS_MESSAGE_SERVICE_PROVIDER = "aps-message-service-provider"__

Multiple providers of this service can be deployed at the same time. Using this property when registering services for a provider allows clients to lookup a specific provider.



__void messageReceived(String target, byte[] message)__

This is called when a message is received.

_Parameters_

> _target_ - The target the message was sent to. 

> _message_ - The received message. 

__void addMessageListener(String target, MessageListener listener)__

Adds a listener for types.

_Parameters_

> _target_ - The target to listen to. 

> _listener_ - The listener to add. 

__void removeMessageListener(String target, MessageListener listener)__

Removes a messaging listener.

_Parameters_

> _target_ - The target to stop listening to. 

> _listener_ - The listener to remove. 

__void sendMessage(String target, byte[] message) throws APSMessagingException__

Sends a message.

_Parameters_

> _target_ - The target of the message. 

> _message_ - The message to send. 

_Throws_

> _APSMessagingException_ - on failure. 













}

----

    

