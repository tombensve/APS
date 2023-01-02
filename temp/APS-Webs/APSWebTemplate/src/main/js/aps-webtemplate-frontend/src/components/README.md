# The APS bus driven components.

## General
Most of these components are just wraps of React-Bootstrap components. But for components that React-Bootstrap does not supply other components are used. A few are just home baked.

The point of these components and the reason for wrapping components and not using the original straight off is that these interact on a local event bus. Components can both receive and send messages. Most only send.

Instead of registering event listeners per component these all just sends events on the same bus. Depending on the 'routing' information in the header of messages sent the messages are routed just locally in client or out on the network to a backend, or to the network cluster reaching both backends and clients.

Messages can have multiple routes, but only client + backend makes any sense.

## APSComponent

This is a common base component for 99% of the APS components. It handles most generic functionality of the APS components.
