# APS Platform

APS used to use OSGi, but no longer support full OSGi! It will not run in felix nor equinox, nor karaf, etc. 

The __why:__

* APS is based on Vert.x and 3.8.0 of vert.x is quite difficult to deploy in an OSGi container. The previous version worked. I spent 2 days trying to make Apache Felix happy with Vert.x but gave up after finding and deploying ~20 dependencies and dependencies of dependencies. It just never stopped requireing more dependencies. Part of the problem is that the OSGi container looks through the whole jars and tries to resovle everything it sees. In reallity with these kind of jars 100% of the functionality is seldom used. Compiling the code I can get away with only a few of the dependencies, and don't really need more to run code. 

* Vert.x is dropping OSGi bundle MANIFESTs completely in next major version. I've seen hints that they might adopt JPMS.

* Modularisation did have some niceties, but where not an absolute requirement. I do like OSGis variant far better than JPMS!

* I don't like JPMS since as soon as you put a Module-info.java in there __no non modularised dependencies can be used!!__ This currently means most of the libraries out there ...

What I've decided to do right now is to deploy and run on my own internal APSRuntime that implement ~85% of the 4 basic OSGi interfaces. APS doesn't use any other features of OSGi than that. This of course does not modularise, which is not a requirement to run. The APSRuntime were built to be able to deploy bundles in JUnit tests using a Groovy deploy DSL. So I've been able to deploy and tests bundles the same way as they would be used in reallity, leaving less surprises. 

So now I added a class that does this:

        APSPlatformBooterStage2 booter = new APSPlatformBooterStage2();

        for ( File bundle  : bundleDir.listFiles() ) {
            if (bundle.getName().endsWith( ".jar" )) {
                booter.deploy( bundle.getName() ).with( new APSActivator() ).fromJar( bundle );
            }
            else {
                System.err.println("Ignoring unknown file: " + bundle);
            }
        }

The class itself extends APSRuntime and you can see the code above using the deploy DSL (well this is Java, so you need the dots and parentesis, in Groovy you can skip them which makes it more readable) to deploy each bundle. All APS bundles makes use of APSActivator, which is now also a requirement. It is hardcoded in the deploy. APSActivator inspects all classes in bundle and instantiates and injects based on annotations, and with some special features. Works very well. 

So import and export of packages in MANIFEST.MF can now be skipped, doesn't hurt if it is there though, just have no effect. The OSGi service model is still in effect. The OSGi service model is very nice, and APSActivator supports publishing and consuming services via annotations. For now I stick with the OSGi APIs. If I'm forced into JPMS then I can still use the superior OSGi service model. 

## aps-platform-booter

This produces an executable jar that can be run with java -jar. The _-shaded_ variant is needed to execute. 

This takes 2 arguments:

__--dependenciesDir__ This takes a path to a folder containing all dependency jars needed to run. All the Vert.x, Netty, etc stuff goes into the dependenciesDir. 

__--bundlesDir__ This takes a path to a folder containing bundles to deploy. Only application bundles goes into the bundlesDir.

An URLClassLoader is used to load all jars. After that is done reflection is used to get the _APSPlatformBooterStage2_ class residing in APSRuntime that then runs the code above.

Thats it! A structure with these 2 folders need to be setup of course. 

