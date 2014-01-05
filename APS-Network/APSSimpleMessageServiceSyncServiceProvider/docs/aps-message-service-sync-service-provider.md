# APS Message Service Sync Service Provider

As this long name suggests this service provides an implementation of APSSyncService using APSMessageService to do the synchronization.

## APSSyncService API

public _interface_ __APSSyncService__   [se.natusoft.osgi.aps.api.net.sync.service] {

Defines a data synchronization service.

__Please note__ that this API is very similar to the APSMessageService! There are differences in implementations between synchronization and sync, reusing the same API would be confusing and also require services to register extra properties to identify type of service provided.

__SyncGroup joinSyncGroup(String name)__

Joins a synchronization group.

_Returns_

> joined group.

_Parameters_

> _name_ - The name of the group to join. 

__String getName()__

Returns the name of the group.

__Message createMessage()__

Creates a new message.

__Message createMessage(byte[] content)__

Creates a new message.

_Parameters_

> _content_ - The content of the message. 

__void sendMessage(Message message) throws APSSyncException__

Sends a message.

_Parameters_

> _message_ - The message to send. 

_Throws_

> _se.natusoft.osgi.aps.api.net.sync.service.APSSyncService.APSSyncException_

__void addMessageListener(Message.Listener listener)__

Adds a listener to received messages.

_Parameters_

> _listener_ - The listener to add. 

__void removeMessageListener(Message.Listener listener)__

Removes a listener from receiving messages.

_Parameters_

> _listener_ - The listener to remove. 

__void addReSyncListener(ReSyncListener reSyncListener)__

Adds a resynchronization listener.

_Parameters_

> _reSyncListener_ - The listener to add. 

__void removeReSyncListener(ReSyncListener reSyncListener)__

Removes a Resynchronization listener.

_Parameters_

> _reSyncListener_ - The listener to remove. 

__void reSyncAll()__

Triggers a re-synchronization between all data and all members.

__void leaveSyncGroup()__

Leaves the synchronization group.

public _interface_ __ReSyncListener__   [se.natusoft.osgi.aps.api.net.sync.service] {

This is called when a total re-synchronization is requested.

__void reSyncAll(SyncGroup group)__

Request that all data be sent again.

_Parameters_

> _group_ - The group making the request. 

public _interface_ __Message__   [se.natusoft.osgi.aps.api.net.sync.service] {

This represents a message to send/receive.

__SyncGroup getSyncGroup()__

Returns the sync group the message belongs to.

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

public _interface_ __Listener__   [se.natusoft.osgi.aps.api.net.sync.service] {

This needs to be implemented to receive messages.

__void receiveMessage(Message message)__

Called when a message is received.

_Parameters_

> _message_ - The received message. 

public _class_ __Provider__ implements  Message    [se.natusoft.osgi.aps.api.net.sync.service] {

A simple default implementation of message.

__public Provider(SyncGroup group)__

Creates a new Provider.

_Parameters_

> _group_ - The group the message belongs to. 











public _static_ _class_ __APSSyncException__ extends  APSRuntimeException    [se.natusoft.osgi.aps.api.net.sync.service] {

Thrown on sendMessage(). Please note that this is a runtime exception!

__public APSSyncException(String message)__

Creates a new _APSSyncException_.

_Parameters_

> _message_ - The exception message. 

__public APSSyncException(String message, Throwable cause)__

Creates a new _APSSyncException_.

_Parameters_

> _message_ - The exception message. 

> _cause_ - The cause of this exception. 

}

----

    

