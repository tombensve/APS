# APS RabbitMQ Message Service Provider

This service provides an implementation of APSMessageService using [RabbitMQ](http://www.rabbitmq.com/).

## APSMessageService API

[Javadoc](http://apidoc.natusoft.se/APS/se/natusoft/osgi/aps/api/net/messaging/service/APSMessageService.html)

public _interface_ __APSClusterService__ extends  APSMessageService    [se.natusoft.osgi.aps.api.net.messaging.service] {

All of APSMessageService, APSSynchronizedMapService, and APSSyncService are part of some cluster which they communicate with. This represents that cluster in a general form.

Since APSClusterService extends APSMessageService both are provided by the same bundle. The sync services can also be part of the same bundle, but can also be implemented on top of APSMessageService.

__String getName()__

Returns the name of the cluster.

__boolean isMasterNode()__

If the implementation has the notion of a master and this node is the master then true is returned. In all other cases false is returned.

__APSCommonDateTime getCommonDateTime()__

Returns the network common DateTime that is independent of local machine times.

}

----

    

public _interface_ __APSMessageService__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This defines a simple message service. Can be implemented by using a message bus like RabbitMQ, Active MQ, etc or just a simple tcpip server or whatever.

Since the actual members are outside of this service API, it doesn't really know who they are and doesn't care, all members are defined by configuration to make a cluster of members.

__public static final String APS_MESSAGE_SERVICE_PROVIDER = "aps-message-service-provider"__

Multiple providers of this service can be deployed at the same time. Using this property when registering services for a provider allows clients to lookup a specific provider.

__public static final String APS_MESSAGE_SERVICE_INSTANCE_NAME = "aps-message-service-instance-name"__

Each configured instance of this service should have this property with a unique instance name so that client can lookup a specific instance of the service.

__void addMessageListener(APSMessageListener listener)__

Adds a listener for types.

_Parameters_

> _listener_ - The listener to add. 

__void removeMessageListener(APSMessageListener listener)__

Removes a messaging listener.

_Parameters_

> _listener_ - The listener to remove. 

__void sendMessage(APSMessage message) throws APSMessagingException__

Sends a message.

_Parameters_

> _message_ - The message to send. 

_Throws_

> _se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException_ - on failure. 

public _static_ _abstract_ _class_ __AbstractMessageServiceProvider__ implements  APSMessageService    [se.natusoft.osgi.aps.api.net.messaging.service] {

Provides an abstract implementation of the APSMessageService interface.







__protected void sendToListeners(APSMessage message)__

Sends a message to the registered listeners.

_Parameters_

> _message_ - The message to send. 

}

----

    

public _interface_ __APSSynchronizedMapService__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This service makes a synchronized named map available.

__Map<String, String> getNamedMap(String name)__

Returns a named map into which objects can be stored with a name.

If the named map does not exists it should be created and an empty map be returned.

__List<String> getAvailableNamedMaps()__

Returns the available names.

__boolean supportsNamedMapPersistence()__

Returns true if the implementation supports persistence for stored objects. If false is returned objects are in memory only.

}

----

    

public _interface_ __APSSyncService__   [se.natusoft.osgi.aps.api.net.messaging.service] {

This defines a data synchronization service.

__public static final String SYNC_PROVIDER = "aps-sync-provider"__

A property key that should be registered with each service instance to indicate the specific implementation of the service. This to allow multiple implementations to be deployed and clients can ask for a specific if needed.

__public static final String SYNC_INSTANCE_NAME = "aps-sync-instance-name"__

There should be one service instance registered for each configured synchronization group. Each instance should include this property with a unique name so that clients can get the synchronizer for the correct group.

__void syncData(APSSyncDataEvent syncEvent) throws APSMessagingException__

Synchronizes data.

_Parameters_

> _syncEvent_ - The sync event to send. 

_Throws_

> _APSMessagingException_ - on failure. 

__void resync()__

Triggers a resynchronization.

__void resync(String key)__

Triggers a resynchronization of the specified key.

_Parameters_

> _key_ - The key to resync. 

__void addSyncListener(Listener listener)__

Adds a synchronization listener.

_Parameters_

> _listener_ - The listener to add. 

__void removeSyncListener(Listener listener)__

Removes a synchronization listener.

_Parameters_

> _listener_ - The listener to remove. 



__void syncDataReceived(APSSyncEvent syncEvent)__

Called to deliver a sync event. This can currently be one of:

* APSSyncDataEvent

* APSReSyncEvent

_Parameters_

> _syncEvent_ - The received sync event. 

}

----

    

