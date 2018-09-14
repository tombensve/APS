# Application Platform Services (APS)

Copyright © 2013 Natusoft AB

__Version:__ 1.0.0 (working up to ...)

__License:__ [Apache 2.0](lics/Apache-2.0.md)

__This project is currently work in progress and cannot be expected to be stable!!__

Work is slow, whenever time permits.

To be very clear: **This is currently, and probably for a long time comming, a playground where I'm having fun.** The original (and still active) goal with this is to make a very easy to use web platform, currently based on OSGi & Vertx. APS is however only using the basic 4 OSGi APIs, and currently java8 & Groovy code so it will probably **not** run in most embedded OSGi containers.

This project is now using 2 exceptional frameworks: __Vert.x & React__. These both belong to the same category: Things that just work! I have the highest respect for the people behind both of these. These both also supply outstanding documentation.

---

__To build__ this you must first follow the instructions here: https://github.com/tombensve/maven-bundle-plugin/blob/master/README.md

This because maven-bundle-plugins usage of bnd causes groovy code to confuse bnd into thinking there is code in default package. After a support discussion with bnd there are options in bnd for solving this, but those are not availabe when bnd is used via maven-bundle-plugin. bnd's own maven plugin is far more primitive however so switching is a big job. 

This might however solve itself ...

---

I started using OSGi (only the base 4 APIs) as a base platform because of the modularity of OSGi. I wanted smal individual deployments of functionality and a nice clean way of interacting. OSGi provided that. But it was just a means to an end, and it has caused problems along with solutions. 

APS have always been about keeping things simple. Easy to use APIs providing only basic functionality with no configurational options API wise. My intentions is to encapsulate complexity and provide the easiest way possible to use it for all other code. Each specific implementation is responsible for any configuration needed and I made a more structured configuraton service for other services to use. OSGi basically only gives you properties. My goal with configuration is to provide easy to understand structured configuration and a web app to edit such configuration that is easy to use and comprehend even for non developers. 

React is a web component framework that follows my ideas of simplicity and small code to the extreme! Love at fist sight :-). 

Vert.x I stumbled upon accidentally, and realized that this is "the shit" I'm been looking for! When I started out with this project (yes, its been a longtime playground) I felt stuck with the JEE web APIs. JEE was always problematic and worked agains my goals. Yes, I've worked a lot with JEE, since version 1.0. That one sucked! Really sucked! Every single EJB call was remote! We had a market research app and it took over 24 hours to analyze answers. We had to move the analyzing part to SQL and run it within the database, and that is probaby still the best option for such still today. JEE however got better over time, but has become a big fat thing that still keeps learning about reality, while others have run it by. I have worked a lot with JEE, I know JEE, but it is no longer "the shit", if it ever really was. Getting back on track, Vert.x fullfilled my needs and then some. And it is truly easy to set up a web server, etc. So Vert.x also fits within my goals of keeping things simple and codewise small. 

I started my React web by wrapping mostly React-Bootstrap components and make them send events on a local bus. The bus is a facade and takes bus routers, one being a local bus router that calls local subscribers directly, and one being Vertx bus router that sends and receives messages on the Vertx evenbus bridge. Each component gets bus routing data that decides how their messages are routed. Most only go local. I'm not going to go into more details here, but having component interact with each other without any knowledge of each other, only acting on and/or sending messages has worked really nice, and actually makes the code very much simpler. When the Vert.x eventbus JS client detects that it no longer is connected to a backend, my code gets called and sends a message on the local bus. On the top of the page an alert message pops up informing about the situation. When the eventbus client gets contact with backend again my code sends another message, and the alert is closed. This code knows nothing about the gui nor any components. It only deals with messages, which is my point. Making software behave just like hardware does seems to be a good way to go. The risk is making runtime relationships between different parts of the code less obvious. That has always been my worry. But so far I don't see that. The messages travelling on the bus of course beomes central and must be well defined and documented.  

This means that the backend will also act on and send messages on the bus. This makes me consider doing the same on the backend as on the frontend, a bus facade and a local in VM bus router and a Vert.x eventbus router, and using the same routing principles (which I'm also trying to keep simple). This in turn means that OSGi as a base platform really isn't needed, it can be replaced with something even simpler and smaller. The modularity of OSGi makes handlng of 3rd party libraries of possibly different versions for different deployables very nice. But JDK 11 is alreay on its way and Java have modularity since 9, and that gives a good reason to go up past 8, which seemed problematic with OSGi. 

To my big surprise there are some people that have starred this project on github! If it is due to my OSGi tools like APSActivator, APSServiceTracker, etc it might be time to clone a copy. I have however put a lot of effort into these so I might just extract to most useful tools into a separate git repo.  

Tommy
 
Professinal Code Geek