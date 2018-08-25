# Web Manager

## Components

The components are mostly based on React-Bootstrap components. The APS components wrap other components to provide components that connect to a common bus. All relevant component events are are sent as messages on the bus. No code has to attach listeners to specific components. Any code that wants to react to a component being triggered does so by joining the bus and listen to messages. So the code reacting on messages really has no direct association with any component, they just react to messages.

All components has a listenTo and a sendTo property which points out and address to send or listen to. Most components only send messages. 

This separation of component rendering code and event handler code makes it rather easy to render components after a JSON specification. That is what WebManager does. It listens for a JSON GUI spec on the bus, and when it sees such, it renders it. When a user interacts with the rendered GUI messges will be sent on the bus.  

## Bus

On the frontend there is a local `LocalEventBus` that is used to send and receive messages. This is basically a wrapper. It does not do anything other than pass on to 'EventBusRouter's. There are 2 'EventBusRouter's: `LocalEventBusRouter`and `VertxEventBusRouter`. These are both added to `LocalEventBus`. 

### LocalEventBusRouter

This looks at the header of a message and if _routing_ contains _client_ then it passes the message to all registered local subscribers. This never goes out on the network, it only works internatlly in the client. This is used to communicate between different components locally.

### VertxEventBusRouter

This looks at the header of a message and if _routing_ contains _backend_ it does a send on the Vert.x client event bus bridge. A send does a round robin to to listeners of specified address. If _routing_ contains _cluster_ it does a publish on the Vert.x client event bus bridge. This goes to all listeners of the address in the whole cluster, including other clients.

## Messaging

A note: _Vert.x uses Hazelcast by default to handle the cluster. Hazelcast by default manages a cluster on the same subnet only, by multicast. Hazelcast can of course be configured to create larger clusters. But that is a Hazelcast thing, not APS nor Vert.x._ 

### Addresses

Each message must be addressed! An address is just a string. It can be anything. If you send to an address it will be received if there is something also listening to exactly the same address. See an address as a group name as is common in other messaging solutions.

As said above when a send is done on the Vert.x event bus it does a round robin between each listener of the "sent to" address. This to support load balancing.  

#### Address Strategy

Note: (app) refers to a specific application as a wildcard, and (UUID) refers to a generated UUID value.

From client perspective:

- Client 

     - "aps:(app):client:(UUID)"
        - Network: listen
        - Local: listen, send  

- Backend

    - "aps:(app):backend" 
       - Network: send

- Cluster

    - "aps:(app):all" : Every backend and client of (app).
       - Network: listen, send 

    - "aps:(app):all:backend" : Every backend of (app). 
       - Network: send 

    - "aps:(app):all:client" : Every client of (app).
        - Network: listen, send
   
**Note 1:**  Only clients have a unique address. They are the only ones that needs a unique address due to being the only unique thing. 

**Note 2:**  Vert.x does a round robin on "send" to same address. Only backend messages are delivered with a send. All cluster messages are delived with a "publish" and will always go to every subscriber.

**Note 3:** Both on client side and on backend, code is not interacting with the Vert.x bus, but local busses with message routers. So what messages are sent on the Vert.x bus and which message method is used is a routing question. Senders and receivers don't need to care. 

### Routing Strategy

My first though was to hide routing as much as possible, but that creates a lot of limitations. 

Components have indirect routing information passed to them by the creater of the component, and in general all routing is handled by the `APSComponent` base class for the APS components. Individual components should ignore routing completely. They just send and possibly receive messages not caring about anything else than the message.

Some components need to talk to each other locally, and some need to reach a backend. It is up to the code that creates the components to determine that by supplying a routing property of:

    {
        outgoing: "client/backend/all/all:backend/all:client",
        incoming: "client/all/all:client"
    }

Both outgoing and incomming can have more than one route. Routes are comma separated within the string. No spaces. 

### Messages

When a message is sent, there is no from, there is only a to. My goal is that all code should only react to messages, and a reaction can possibly be a new message. But there should not be any specific reply message. A message might be reacted to by a client that updates something in a GUI, or it might be reacted to in the backend. Whatever sends the message sholdn't care. Where messages end upp is a routing question. 

The `content` part can be missing or empty depending on `type`.

In the following specification any entry starting with '?' is optional. 

#### General

    {
        "aps": {
            "origin": "(address)",
            "app": "(app)",
            "type": "(message type)"
        },
        content: {
            ...message type specific data
        }
    }

#### --> Avail 

Client tells a backend that it exists and are ready for a GUI JSON document. 

This is not a requirement, components can be created and a gui built with client code like any other React GUI. There are 2 components that works together and use this message: `APSPage` and `APSWebManager`. They will as default inform the backend that they are upp and running and the backend will send a JSON document with components to create. `APSWebMAnager` can however also be used with a property that tells it to not send the message, and only act as container, but create and supplying a common event bus to all children. 

    {
        "aps": {
            "origin": "(address)"
            "app": "(app)",
            "type": "avail"
        }
    }

#### <-- Gui

Client receives a gui, most probably from backend, but can really be send from anywhere including client itself.

{
    "aps": {
        "origin": "(address)",
        "app": "(app)",
        "type": "gui"
    },
    "content": {
        "id": "(comp id)",
        "name": "(comp name)",
        ?"group": "(name of group component belongs to if any.)",
        "type": "(comp type)",
        ?"class": "(class class ...)",
        ?"disabled": true/false,
        ?"enabled": "groupNotEmpty:(group)/namedComponentsNotEmpty:(names)"
        "collectGroups": "(space separated list of groups to listen to and collect data from.)",
        "(type specific)": "(...)",
        ...,
        "children": [
            ...
        ]
    } 
} 

#### --> Create

    {
        "aps": {
            "origin": 'address'
            "app": 'app',
            "type": "create"
        }
        "content": {
    
        }
    }


