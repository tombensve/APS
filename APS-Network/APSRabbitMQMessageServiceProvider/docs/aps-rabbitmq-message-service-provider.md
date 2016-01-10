# APS RabbitMQ Message Service Provider

This service provides an implementation of APSMessageService using [RabbitMQ](http://www.rabbitmq.com/).

__Note:__ This implementation does not support _contentType_ in the API. When sending messages the _contentType_ will be ignored, and when messages are received the _contentType_ will always be "UNKNOWN".

A good suggestion is to always use JSON or XML as content.

## APSMessageService API

[Javadoc](http://apidoc.natusoft.se/APS/se/natusoft/osgi/aps/api/net/messaging/service/APSMessageService.html)

public _interface_ __APSSimpleClusterService__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This service defines a synchronized cluster.





__void clusterUpdated(String clusterName, String name, TypedData data)__

Receives an updated value.

_Parameters_

> _clusterName_ - The name of the cluster the updated data belongs to. 

> _name_ - The name of the updated data. 

> _data_ - The updated data. 

__void provideData(String clusterName, String name, TypedData typedData)__

Creates/updates a value in a cluster.

_Parameters_

> _clusterName_ - The name of a cluster to store in. 

> _name_ - The name of the value to store. 

> _typedData_ - The value to store. 

_Throws_

> _IllegalArgumentException_ - on any problem with clusterName. 

__TypedData retrieveData(String clusterName, String name)__

Gets a value stored in a named cluster. Returns null if it does not exists.

_Parameters_

> _clusterName_ - The name of the cluster to get data from. 

> _name_ - The name of the cluster data to get. 

__void addUpdateListener(String clusterName, UpdateListener updateListener)__

Adds an update listener.

_Parameters_

> _clusterName_ - The name of the cluster to listen for changes in. 

> _updateListener_ - The update listener to add. 

__void removeUpdateListener(String clusterName, UpdateListener updateListener)__

Removes an update listener.

_Parameters_

> _clusterName_ - The name of the cluster to remove update listener from. 

> _updateListener_ - The listener to remove. 



__Map<String, List<UpdateListener>> listeners = Collections.synchronizedMap(new HashMap<>())__

 The listeners.





__protected void updateListeners(String clusterName, String name, TypedData data)__

Updates all listeners.

_Parameters_

> _clusterName_ - The name of the cluster the updated data belongs to. 

> _name_ - The name of the updated data. 

> _data_ - The actual data. 

__protected List<UpdateListener> getListeners(String clusterName)__

Returns the listeners.

_Parameters_

> _clusterName_ - The name of the cluster to get listeners for. 

}

----

    

public _interface_ __APSSimpleMessageService__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This defines a simple message service. Can be implemented by using a message bus like RabbitMQ, Active MQ, etc or just a simple tcpip server or whatever.

Since the actual members are outside of this service API, it doesn't really know who they are and doesn't care, all members are defined by configuration.





__void messageReceived(String topic, TypedData message)__

This is called when a message is received.

_Parameters_

> _topic_ - The topic the message belongs to. 

> _message_ - The received message. 

__void addMessageListener(String topic, MessageListener listener)__

Adds a listener for types.

_Parameters_

> _topic_ - The topic to listen to. 

> _listener_ - The listener to add. 

__void removeMessageListener(String topic, MessageListener listener)__

Removes a messaging listener.

_Parameters_

> _topic_ - The topic to stop listening to. 

> _listener_ - The listener to remove. 

__void sendMessage(String topic, TypedData message) throws APSMessagingException__

Sends a message.

_Parameters_

> _topic_ - The topic of the message. 

> _message_ - The message to send. 

_Throws_

> _APSMessagingException_ - on failure. 









__protected void sendToListeners(String topic, TypedData message)__

Sends a message to the registered listeners.

_Parameters_

> _message_ - The message to send. 

__protected List<MessageListener> lookupMessageListeners(String topic)__

Returns the message listeners for a topic.

_Parameters_

> _topic_ - The topic to get listeners for. 

}

----

    

