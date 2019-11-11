# APS Runtime

This is an implementation of the parts of the 4 base OSGi APIs used by APS. It reads nothing from the manifest and does not support modularisation. This can work in conjunction with JPMS, but it is not modularised at all right now. 

The **APSRuntime** class is the core of this. It provides both the runtime and is used for running tests. Most tests extends this class and use its deployment DSL looking like this:

    deploy "my-bundle" with new APSActivator() fromJar bundleJar
    
    deploy 'aps-vertx-provider' with new APSActivator() from 'APS-Network/APSVertxProvider/target/classes'
    
    deploy 'vertx-client' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/VertxClient.class'
    
    deploy 'aps-vertx-cluster-data-store-provider' with new APSActivator() from 'se.natusoft.osgi.aps', 'aps-vertx-cluster-datastore-service-provider',
    '1.0.0'

When deploying on APSRuntime running it as

    java -jar aps-platform-booter-1.0.0.jar --dependenciesDir .../dependencies --bundlesDir .../bundles

then the following is done:

    runtime.deploy( bundle.getName() ).with( new APSActivator() ).fromJar( bundle );

Note that the above is how it looks from Java. The previous examples are Groovy code where you can skip things like dots and parentesis. 

Also note that it always creates an APSActivator! The deploy APi does allow for other BundleActivators, but I decided to force the _APSActivator_, partly for simplicity, but also because it is quite powerful and nice. It scans all classes in the bundle and does dependency injection based on annotations. It is however slighthly more intelligent than that and have intimate knowledge of ceartain objects like APSServiceTracker for example, which it can inject as an instance of APSServiceTracker if that is the type, or an instance of the tracked service if that is the type. In the latter case the injected instance will under the surface use the APSServiceTracker to locate the service and forward calls to it. If the annotation specifies `nonBlocking=true` then the service can be called before the service is actually available as long as the service call uses a reactive API with void return and callback for result. It will just cache the call until the service becomes available and then execute it. There are of course timeouts! 

APSActivator also allows for _@Initializer_ annotations on parameter free methods which will be called when all injections are done. 

The _aps-platfrom-booter-1.0.0.jar_ will put all jars in _--dependenciesDir_ on the classpath and all jars in _--bundlesDir_ will also be added to classpath and then deployed as described above. That is the difference between the 2 directories. Theoretically all jars can be put in the bundle dir, it will just be slightly slower due to scanning all jars for annotations which will only exist in bundle type jars.

APSActivator is fully documented elsewhere in the full documentation.


## (Un)supported OSGi APIs

The following is a list of what is not supported.

### Bundle

Partly supported.

#### Not supported

- getState() -- always returns 0.
- start(otions)
- start()
- stop(options)
- stop()
- update(input)
- update()
- uninstall()
- getLocation()
- hasPermission() -- always return true.
- getLastModified() -- always return 0.
- getSignerCertificates( signersType ) -- returns null.

### BundleContext

Partly supported.

#### Not supported

- installBundle( String location, InputStream input ) -- throws BundleException (APSRuntime has API for that).
- installBundle( String location ) -- throws BundleException (APSRuntime has API for that).
- addFrameworkListener( FrameworkListener listener )
- removeFrameworkListener( FrameworkListener listener ) 
- ungetService( ServiceReference reference ) -- does nothing, always returns true.
- getDataFile( String filename ) -- Throws RuntimeException.

### ServiceReference

Partly supported.

#### Not supported

- isAssignableTo(Bundle bundle, String className) -- always returns true, should probably always reurn false ...
- compareTo(Object reference) -- always return 0.

### ServiceRegistration

Fully supported. 

## Why

So why this and not a full OSGi container ? 

APS started with a full OSGi container. But it is also heavily dependent on Vert.x. The previous version of Vert.x where deployable in an OSGi container with only a _relatively_ small set of dependencies that could be deployed as OSGi bundles. With versin 3.8.0 I spent 3 days trying to find and resolve all dependencies required to deploy in Felix. After 3 days I gave up. For each jar deployed there were 2-5 more required. It just never ended. APS is not using 100% of everything in Vert.x, but when deploying now 100% of all dependencies seemed to be needed. Deploying in APSRuntime I have a lot fewer dependencies required to run, more or less what I compile with.

I'm not a fan of JPMS, I think OSGi is much better. But JPMS is now the Java standard and Vert.x hints at adopting it, and APSRuntime can co-exist with JPMS, not so with full OSGi. Its probably going to be rather slow, but in the long run more java code are going to adopt JPMS. APSRuntime still uses the OSGi service model which is much nicer than the old ServiceLoader based service model used by JPMS. OSGi is rather brilliant in allowing a set of properties to be associated with a service registration, and then allowing a query on those properties when looking up a service. This provides a flexibility ServiceLoader doesn't have. 