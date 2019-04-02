# The APS bus driven components.

## General
Most of these components are just wraps of React-Bootstrap components. But for components that React-Bootstrap does not supply other components are used. A few are just home baked.

The point of these components and the reason for wrapping components and not using the original straight off is that these interact on a local event bus. Components can both receive and send messages. Most only send.

Instead of registering event listeners per component these all just sends events on the same bus. Depending on the 'routing' information in the header of messages sent the messages are routed just locally in client or out on the network to a backend, or to the network cluster reaching both backends and clients.

Messages can have multiple routes, but only client + backend makes any sense.

## APSComponent

This is a common base component for 99% of the APS components. It handles most generic functionality of the APS components.

## Build bug

For some reason I have still not been able to figure out, for a correct web app, that corresponds to current code, this project has to be built twice in a row. I have double, tripple, quadrouple checked the pom and that things are being done in the correct order, that the final web files packed into the jar file (later served by Vert.x from classpath) are the correct and currently built files, but still it seems to build the jar before all the js files under aps-webmanager-frontend/build is in place. But looking at pom and build output things are in the correct order, but the jar still contains the previously built frontend code. If you look under target it is clear that it is the previous version of the frontend code that got included. But why that is ...

For now it has to be built twice without clean in between to get correct frontend code. I've looked at this several times, fresh, but failing to find the why of this. There is clearly something I'm blind to. If it were maven threading the build, things could be done in unexpected order, but I've tried to turn off parallelism for this build, but it still create the jar before the frontend is done with its build. 