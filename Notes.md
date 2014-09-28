# APS Project Scratchpad for ideas

## Messaging Ideas

Define the following services:

* APSMessageService - A simple plain grouped messaging service.

* APSEventService - A service that can send events within a group. Should support both local and remote members. Consider using JSON format for events.

* APSSyncService A service that uses one or both of the above to synchronize. This also requires APSNetTimeService, or possibly that should be merged with the sync service.


