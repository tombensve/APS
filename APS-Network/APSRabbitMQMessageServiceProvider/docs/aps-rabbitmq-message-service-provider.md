# APS RabbitMQ Message Service Provider

This service provides an implementation of APSMessageService using [RabbitMQ](http://www.rabbitmq.com/).

## APSMessageService API

[Javadoc](http://apidoc.natusoft.se/APS/se/natusoft/osgi/aps/api/net/messaging/service/APSMessageService.html)

public _interface_ __APSClusterService__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This service defines a synchronized cluster.







__void clusterUpdated(String key, JSONValue value)__

Receives an updated value.

_Parameters_

> _key_ - The key of the updated value. 

> _value_ - The actual value. 

__void update(String key, JSONValue value)__

Updates a keyed value to the cluster.

_Parameters_

> _key_ - This uniquely specifies what value this is. How it is used is upp tp the actual cluster using it. 

> _value_ - The modified value to update. 

__void addUpdateListener(UpdateListener updateListener)__

Adds an update listener.

_Parameters_

> _updateListener_ - The update listener to add. 

__void removeUpdateListener(UpdateListener updateListener)__

Removes an update listener.

_Parameters_

> _updateListener_ - The listener to remove. 

__JSONObject getNamedObject(String name)__

Gets named cluster-wide object. If it does not exist it will be created.

_Parameters_

> _name_ - The name of the cluster object to get. 

_Throws_

> _UnsupportedOperationException_ - if this feature is not supported. 

__List<JSONValue> getNamedList(String name)__

Gets a cluster-wide named list. If it does not exist it will be created.

_Parameters_

> _name_ - The name of the list to get. 

_Throws_

> _UnsupportedOperationException_ - if this feature is not supported. 



__List<UpdateListener> listeners = Collections.synchronizedList(new LinkedList<UpdateListener>())__

 The listeners.





__protected void updateListeners(String key, JSONValue value)__

Updates all listeners.

_Parameters_

> _key_ - The key of the update. 

> _value_ - The value of the update. 

__protected List<UpdateListener> getListeners()__

Returns the listeners.

}

----

    

public _interface_ __APSMessageService__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This defines a simple message service. Can be implemented by using a message bus like RabbitMQ, Active MQ, etc or just a simple tcpip server or whatever.

Since the actual members are outside of this service API, it doesn't really know who they are and doesn't care, all members are defined by configuration to make a cluster of members.





__void addMessageListener(APSMessageListener listener)__

Adds a listener for types.

_Parameters_

> _listener_ - The listener to add. 

__void removeMessageListener(APSMessageListener listener)__

Removes a messaging listener.

_Parameters_

> _listener_ - The listener to remove. 

__void sendMessage(JSONObject message) throws APSMessagingException__

Sends a message.

_Parameters_

> _message_ - The message to send. 

_Throws_

> _se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException_ - on failure. 



__void messageReceived(JSONObject message)__

This is called when a message is received.

_Parameters_

> _message_ - The received message. 

public _static_ _abstract_ _class_ __AbstractMessageServiceProvider__ implements  APSMessageService    [se.natusoft.osgi.aps.api.net.messaging.service] {

Provides an abstract implementation of the APSMessageService interface.







__protected void sendToListeners(JSONObject message)__

Sends a message to the registered listeners.

_Parameters_

> _message_ - The message to send. 

__protected List<APSMessageListener> getMessageListeners()__

Returns the message listeners.

}

----

    

