# aps-persistent-named-queue-provider

This provides an implementation of __APSNamedQueueService__ that makes use of __APSFilesystemService__ for storage. Clients just create or get a queue by a unique name. It is then possible to push bytes to the queue or to pull bytes from the queue. It is rather simple.

## APIS

public _interface_ __APSNamedQueueService__   [se.natusoft.osgi.aps.api.misc.queue] {

A named queue as a service. How long lived it is depends on the implementation.

__Note__ that there can be only one receiver per queue. Once an item is delivered it is gone from the queue!

__APSQueue createQueue(String name) throws APSIOException__

Creates a new queue.

_Parameters_

> _name_ - The name of the queue to create. If the named queue already exists, it is just returned, 

_Throws_

> _APSIOException_ - on failure to create queue. 

__void removeQueue(String name) throws APSResourceNotFoundException__

Removes the named queue.

_Parameters_

> _name_ - The name of the queue to remove. 

_Throws_

> _APSResourceNotFoundException_ - on failure to remove the queue. 

__APSQueue getQueue(String name) throws APSResourceNotFoundException__

Returns the named queue. If it does not exist, it is created.

_Parameters_

> _name_ - The name of the queue to get. 

_Throws_

> _APSResourceNotFoundException_ - on failure to get queue. 

}

----

    



__void push(byte[] item) throws APSIOException__

Pushes a new item to the end of the list.

_Parameters_

> _item_ - The item to add to the list. 

_Throws_

> _APSIOException_ - on any failure to do this operation. 

__byte[] pull(long timeout) throws APSIOTimeoutException__

Pulls the first item in the queue, removing it from the queue.

_Returns_

> The pulled item.

_Parameters_

> _timeout_ - A value of 0 will cause an immediate APSIOException if the queue is empty. Any 

_Throws_

> _APSIOException_ - on any failure to do this operation. 

__byte[] peek() throws APSIOException, UnsupportedOperationException__

Looks at, but does not remove the first item in the queue.

_Returns_

> The first item in the queue.

_Throws_

> _APSIOException_ - on any failure to do this operation. 

> _UnsupportedOperationException_ - If this operation is not supported by the implementation. 

__int size() throws APSIOException, UnsupportedOperationException__

Returns the number of items in the queue.

_Throws_

> _APSIOException_ - on any failure to do this operation. 

> _UnsupportedOperationException_ - If this operation is not supported by the implementation. 

__boolean isEmpty() throws APSIOException__

Returns true if this queue is empty.

_Throws_

> _APSIOException_ - on any failure to do this operation. 

__void release()__

Releases this APSQueue instance to free up resources. After this call this specific instance will be invalid and a new one have to be gotten from APSNamedQueueService.

}

----

    

