# APSGroups

Provides network groups where named groups can be joined as members and then send and receive data messages to the group. This is based on multicast and provides a verified multicast delivery with acknowledgements of receive to the sender and resends if needed. The sender will get an exception if not all members receive all data. Member actuality is handled by members announcing themselves relatively often and will be removed when an announcement does not come in expected time. So if a member dies unexpectedly (network goes down, etc) its membership will resolve rather quickly. Members also
tries to inform the group when they are doing a controlled exit. 

Please note that this does not support streaming! That would require a far more complex protocol. APSGroups waits in all packets of a message before delivering the message.

## OSGi service usage

The APSGroupsService can be used as an OSGi service and as a standalone library. This section describes the service.
 
### Getting the service

	APSServiceTracker<APSGroupService> apsGroupsServiceTracker = 
		new APSServiceTracker<APSGroupsService>(bundleContext, APSConfigService.class,
		APSServiceTracker.LARGE_TIMEOUT);
	APSGroupsService apsGroupsService = apsGroupsServiceTracker.getWrappedService();
	
### Joining a group

	GroupMember groupMember = apsGroupsService.joinGroup("mygroup");
	
### Sending a message

To send a message you create a message, get its output stream and write whatever you want to send on that
output stream, close it and then send it. _Note_ that since the content of the message is any data you want, 
all members of the groups must know how the data sent looks like. In other words, you have to define your
own message protocol for your messages. Note that you can wrap the OutputStream in an ObjectOutputStream
and serialize any java object you want.
	
	Message message = groupMember.createNewMessage();
	OutputStream msgDataStream = message.getOutputStream();
	try {
		...
		msgDataStream.close();
		groupMember.sendMessage(message);
	}
	catch (IOException ioe) {
		...
	}
	
Note that the `groupMember.sendMessage(message)` does throw an IOException on failure to deliver the message to all members.

### Receiving a message

To receive a message you have to register a message listener with the GroupMember object.

	MessageListener msgListener = new MyMsgListener();
	groupMember.addMessageListener(myMsgListener); 

and then handle received messages:

	public class MyMsgListener implements MessageListener {
		public void messageReceived(Message message) {
			InputStream msgDataStream = message.getInputStream();
			...
		}
	}

### Leaving a group

	apsGroupsService.leaveGroup(groupMember);
	
## Library usage

The bundle jar file can also be used as a library outside of an OSGi server, with an API that has no other dependencies than what is in the jar. The API is then slightly different, and resides under the se.natusoft.apsgroups package.

### Setting up

	APSGroups apsGroups = new APSGroups(config, logger);
	apsGroups.connect();
	
The config passed as argument to APSGroups will be explained further down under "Configuration".

The _logger_ is an instance of an implementation of the APSGroupsLogger interface. Either you provide your own
implementation of that or your use the APSGroupsSystemOutLogger implementation. 
	
### Joining a group

	GroupMember groupMember = apsGroups.joinGroup("mygroup");

### Sending and receiving messages

Sending and receiving works exactly like the OSGi examples above. 

### Leaving a group

	apsGroups.leaveGroup(groupMember);

### Shutting down

	apsGroups.disconnect();
	
## Net time

All APSGroups instances connected will try to sync their time. I call this synced time "net time". 

It works like this: When an APSGroups instance comes up it waits a while for NET\_TIME packets. If it gets such a packet then it enters receive mode and takes the time in the received NET\_TIME packet and stores a diff to that time and local time. This diff can then be used to translate back and forth between local and net time. If no such packet arrives in expected time it enters send mode and starts sending NET\_TIME packets itself using its current net time. If a NET\_TIME packet is received when in send mode it directly goes over to listen mode. If in listen mode and no NET\_TIME packet comes in reasonable time it goes over to send mode. So among all instances on the network only one is responsible for sending NET\_TIME. If that instance leaves then there might be a short fight for succession, but it will resolve itself rather quickly.

The GroupMember contains a few _create\*_ methods to produce a _NetTime_ object instance. See the API further down for more information on these.

## Configuration

### OSGi service 

The OSGi service provides a configuration model that gets managed by the APSConfigService. It can be configured in the APS adminweb (http://host:port/apsadminweb/). Here are some screenshots of the config admin:

![/apsconfigadmin web gui for configuring APSGroups 1](http://download.natusoft.se/Images/APS/APS-Network/APSGroups/docs/images/groups-config-1.png)
![/apsconfigadmin web gui for configuring APSGroups 2](http://download.natusoft.se/Images/APS/APS-Network/APSGroups/docs/images/groups-config-2.png)
![/apsconfigadmin web gui for configuring APSGroups 3](http://download.natusoft.se/Images/APS/APS-Network/APSGroups/docs/images/groups-config-3.png)
![/apsconfigadmin web gui for configuring APSGroups 4](http://download.natusoft.se/Images/APS/APS-Network/APSGroups/docs/images/groups-config-4.png)

As  can bee seen in the above screenshots transports need to be configured for communication to work. If you only need to talk to members on the same subnet the multicast transport is enough! The multicast transport makes sure that all transmitted data is received by all known group members. It will do resends if required, and throw an exception on failure of any member to acknowledge all sent packets. 

If you need to talk to members on a different subnet then you need to use the TCP transports. Note that there are 2 of these: _TCP\_SENDER_, and _TCP\_RECEIVER_. One receiver must be configured and can receive messages from anyone. A sender is needed for each APSGroups installation you want to talk to, and should point to the receiver of that installation. Note that for a receiver you only need to specify a port. The host part is ignored by the receiver.

### Library 

The library wants an implementation of the APSGroupsConfig interface as its first argument to APSGroups(config, logger) constructor. Either you implement your own or use the APSGroupsConfigProvider implementation. This is a plain java bean with both setters and getters for the config values. It comes with quite reasonable default values. It contains exactly the same properties as shown in the screenshots above.

## APIs
