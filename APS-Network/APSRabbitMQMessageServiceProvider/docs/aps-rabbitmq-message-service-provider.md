# APS RabbitMQ Message Service Provider

This service provides an implementation of APSMessageService using [RabbitMQ](http://www.rabbitmq.com/).

## APSMessageService API

[Javadoc](http://apidoc.natusoft.se/APS/se/natusoft/osgi/aps/api/net/messaging/service/APSMessageService.html)

public _interface_ __APSMessageService__   [se.natusoft.osgi.aps.api.net.messaging.service] {

Defines a message service.

__boolean queueExists(String name)__

Checks if the named queue exists.

_Returns_

> true if queue exists, false otherwise.

_Parameters_

> _name_ - The name of the queue to check. 

__Queue defineQueue(String name) throws APSMessageException__

Defines a queue that lives as long as the queue providing service lives.

If the queue already exist nothing happens. If the queue is of another type an _APSMessageException_ could possibly be throws depending on implementation and underlying service used.

_Parameters_

> _name_ - The name of the queue to define. 

_Throws_

> _APSMessageException_ - (possibly) on trying to redefine type. 

__Queue defineDurableQueue(String name) throws APSMessageException, UnsupportedOperationException__

Defines a queue that lives for a long time.

If the queue already exist nothing happens. If the queue is of another type an _APSMessageException_ could possibly be thrown depending on implementation and underlying service used.

_Parameters_

> _name_ - The name of the queue to define. 

_Throws_

> _APSMessageException_ - (possibly) on trying to redefine type. 

> _UnsupportedOperationException_ - If this type of queue is not supported by the implementation. 

__Queue defineTemporaryQueue(String name) throws APSMessageException, UnsupportedOperationException__

Defines a queue that is temporary and gets deleted when no longer used.

If the queue already exist nothing happens. If the queue is of another type an _APSMessageException_ could possibly be thrown depending on implementation and underlying service used.

_Parameters_

> _name_ - The name of the queue to define. 

_Throws_

> _APSMessageException_ - (possibly) on trying to redefine type. 

> _UnsupportedOperationException_ - If this type of queue is not supported by the implementation. 

__Queue getQueue(String name)__

Returns the named queue or null if no such queue exists.

_Parameters_

> _name_ - The name of the queue to get. 

public _interface_ __Queue__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This represents a specific named queue.

__String getName()__

Returns the name of the queue.

__Message createMessage()__

Creates a new message.

__Message createMessage(byte[] content)__

Creates a new message.

_Parameters_

> _content_ - The content of the message. 

__void sendMessage(Message message) throws APSMessageException__

Sends a message.

_Parameters_

> _message_ - The message to send. 

_Throws_

> _APSMessageException_

__void addMessageListener(Message.Listener listener)__

Adds a listener to received messages.

_Parameters_

> _listener_ - The listener to add. 

__void removeMessageListener(Message.Listener listener)__

Removes a listener from receiving messages.

_Parameters_

> _listener_ - The listener to remove. 

__void delete() throws APSMessageException, UnsupportedOperationException__

Deletes this queue.

_Throws_

> _APSMessageException_ - on failure. 

> _UnsupportedOperationException_ - if this is not allowed by the implementation. 

__void clear() throws APSMessageException, UnsupportedOperationException__

Clears all the messages from the queue.

_Throws_

> _APSMessageException_ - on failure. 

> _UnsupportedOperationException_ - If this is not allowed by the implementation. 

public _interface_ __Message__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This represents a message to send/receive.

__void setBytes(byte[] content)__

Sets the message content bytes overwriting any previous content.

_Parameters_

> _content_ - The content to set. 

__byte[] getBytes()__

Returns the message content bytes.

__OutputStream getOutputStream()__

Returns an OutputStream for writing message content. This will replace any previous content.

__InputStream getInputStream()__

Returns an InputStream for reading the message content.

public _interface_ __Listener__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This needs to be implemented to receive messages.

__void receiveMessage(String queueName, Message message)__

Called when a message is received.

_Parameters_

> _queueName_ - The name of the queue that delivered the message. 

> _message_ - The received message. 

public _class_ __Provider__ implements  Message    [se.natusoft.osgi.aps.api.net.messaging.service] {

A simple default implementation of message.

__public Provider()__

Creates a new Provider.









public _static_ _class_ __APSMessageException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.net.messaging.service] {

Thrown on sendMessage(). Please note that this is a runtime exception!

__public APSMessageException(String message)__

Creates a new _APSMessageException_.

_Parameters_

> _message_ - The exception message. 

__public APSMessageException(String message, Throwable cause)__

Creates a new _APSMessageException_.

_Parameters_

> _message_ - The exception message. 

> _cause_ - The cause of this exception. 

}

----

    

