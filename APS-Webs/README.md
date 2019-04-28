# APS Web support

This contains utilities to make things easier from a Java perspective. 

**ALWAYS REMEMBER** that the backend is stateless!! 

This project contains support code. It provides a model representing each frontend component. These models can be created and added to each other in the same way you would build actual GUI components. It is easy to think that these actually synchronize with the frontend, but that is not the case! These are a convenience. The model structure is built, and used, and not saved anywhere! You have to rebuild it from scratch if you want to change the gui.

The `APSWebManager` class is a utility. It listens to and sends messages to the client. The first thing a new client does is to send a message saying "hello I'm a new client, and I have this address". `APSWebManager` will listen to this and call registered handler which in turn should use the component models to build a GUI. 

