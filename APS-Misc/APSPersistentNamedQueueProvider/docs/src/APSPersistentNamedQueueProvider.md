# aps-persistent-named-queue-provider

This provides an implementation of **APSNamedQueueService** that makes use of **APSFilesystemService** for storage. Clients just create or get a queue by a unique name. It is then possible to push bytes to the queue or to pull bytes from the queue. It is rather simple.

## APIS
