# The aps-apis bundle

This contains the general APIs for standard services that other bundles should implement. The APIs are actually one of the main points of APS. Its goal is to define trivially easy to use APIs for different things. Any  needed complexity should be hidden within the API implementation and users should only have to deal with the simple API.

The project does provide a lot of implementations of the APIs. They are in 2 categories:

1. Intended to be deployed and used as is (ex: aps-vertx-provider).

2. A "default" implementation that can be copied and modified / configured to own need. Of course implementations can also be written from scratch.

For (1) there is of course nothing to stop it from being treated as (2) :-).

The aps-apis bundle also contains some base functionality like a better service tracker and a generic bundle activator that does dependency injections. 

## Tools

### APSServiceTracker

This does the same thing as the standard service tracker included with OSGi, but does it better with more options and flexibility. One of the differences between this tracker and the OSGi one is that this throws an _APSNoServiceAvailableException_ if the service is not available. Personally I think this is easier to work with than having to check for a null result. I also think that trying to keep bundles and services  up are better than pulling them down as soon as one depencency goes away for a short while, for example due to redeploy of newer version. This is why APSServiceTracker takes a timeout and waits for a service to come back before failing.

__Note:__ that in previous version APSServiceTracker did all callbacks in a separate thread. This is no longer the case, and shouldn't have been from the beginning.

There are several variants of constructors, but here is an example of one of the most used ones within the APS services:

    APSServiceTracker<Service> tracker =
        new APSServiceTracker<Service>(context, Service.class, "20 seconds");
    tracker.start();

Note that the third argument, which is a timeout can also be specified as an int in which case it is always in miliseconds. The string variant supports the a second word of "sec\[onds\]" and "min\[utes\]" which indicates the type of the first numeric value. "forever" means just that and requires just one word. Any other second words than those will be treated as milliseconds. The APSServiceTracker also has a set of constants for the timeout string value:

    public static final String SHORT_TIMEOUT = "3 seconds";
    public static final String MEDIUM_TIMEOUT = "30 seconds";
    public static final String LARGE_TIMEOUT = "2 minutes";
    public static final String VERY_LARGE_TIMEOUT = "5 minutes";
    public static final String HUGE_LARGE_TIMEOUT = "10 minutes";
    public static final String NO_TIMEOUT = "forever";

On bundle stop you should do:

    tracker.stop(context);

So that the tracker unregisters itself from receiving bundle/service events.

#### Services and active service

The tracker tracks all instances of the service being tracked. It however have the notion of an active service. The active service is the service instance that will be returned by allocateService() (which is internally used by all other access methods also). On startup the active service will be the first service instance received. It will keep tracking other instances comming in, but as long as the active service does not go away it will be the one used. If the active service goes away then the the one that is at the beginning of the list of the other tracked instances will become active. If that list is empty there will be no active, which will trigger a wait for a service to become available again if allocateService() is called.

#### Providing a logger

You can provide an APSLogger (see further down about APSLogger) to the tracker:

	tracker.setLogger(apsLogger);

When available the tracker will log to this.

#### Tracker as a wrapped service

The tracker can be used as a wrapped service:

	Service service = tracker.getWrappedService();
	Service service = tracker.getWrappedService(boolean cacheCallsUntilServiceAvailable);

This gives you a proxied _service_ instance that gets the real service, calls it, releases it and return the result. This handles transparently if a service has been restarted or one instance of the service has gone away and another came available. It will wait for the specified timeout for a service to become available and if that does not happen the _APSNoServiceAvailableException_ will be thrown. This is of course a runtime exception which makes the service wrapping possible without loosing the possibility to handle the case where the service is not available.

The _cacheCallsUntilServiceAvailable_ parameter means just that. This makes the service non blocking.  Otherwise any call to a method when service is not available will result in a wait() on the thread until a service is available. When this parameter is `true` however any calls to the service before a service is available will be cached and executed later when a service is available. Do note that the tracker wrapper provides a java.lang.reflect.Proxy implementation of the service interface. Under the surface it will do an _invoke_ on the actual service object and this invoke can be saved for later in a lambda. There is however a big __warning__ with this: This feature will obviously only work for methods that don't provide a return value! Since method calls may possible be done in the future they cannot return any value. And no, _Future<?>_ cannot be used since it blocks, and we are trying to avoid blocking here!

If you don't like this, don't use the _getWrappedService(true)_. The _.onActiveServiceAvailable(callback)_ method can be used to receive the service instance when it is available.

#### Using the tracker in a similar way to the OSGi standard tracker

To get a service instance you do:

	Service service = tracker.allocateService();

Note that if the tracker has a timeout set then this call will wait for the service to become available if it is currently not available until an instance becomes available or the timeout time is reached. It will throw _APSNoServiceAvailableException_ on failure in any case.

When done with the service do:

	tracker.releaseService();

#### Accessing a service by tracker callback

Note that the _onServiceAvailable_, _onServiceLeaving_, etc have historical reasons for the names, but do now accept multiple calls without overwriting previous callback. The reason for this is that **APSActivator** reuses a tracker instance for tracking the same service in different classes. This allow for each class to do an _onServiceAvailable(...)_ and be called back when service is available. The easiest use of **APSServiceTracker** & **APSActivator** is to inject tracker as a proxied instance of the service API, by declaring its type to be the service interface. There are however times when you need to know when a service is available and this provides that.

There are a few variants to get a service instance by callback. When the callbacks are used the actual service instance will only be allocated during the callback and then released again.

##### onServiceAvailable

This will result in a callback when any instance of the service becomes available. If there is more than one service instance published then there will be a callback for each.

        tracker.onServiceAvailable(new OnServiceAvailable<Service>() {
            @Override
            public void onServiceAvailable(
                Service service,
                ServiceReference serviceReference
            ) throws Exception {
            	// Do something.
            }
        });

##### onServiceLeaving

This will result in a callback when any instance of the service goes away. If there is more than one service instance published the there will be a callback for each instance leaving.

        onServiceLeaving(new OnServiceLeaving<Service>() {

            @Override
            public void onServiceLeaving(
                ServiceReference service,
                Class serviceAPI
            ) throws Exception {
            	// Handle the service leaving.
            }
        });

Note that since the service is already gone by this time you don't get the service instance, only its reference and the class representing its API. In most cases both of these parameters are irellevant.

##### onActiveServiceAvailable

This does the same thing as onServiceAvailable() but only for the active service. It uses the same _OnServiceAvailable_ interface.

##### onActiveServiceLeaving

This does the same thing as onServiceLeaving() but for the active service. It uses the same _OnServiceLeaving_ interface.

##### withService

Runs the specified callback providing it with a service to use. This will wait for a service to become available if a timeout has been provided for the tracker.

Don't use this in an activator start() method! onActiveServiceAvailable() and onActiveServiceLeaving() are safe in a start() method, this is not!

        tracker.withService(new WithService<Service>() {
            @Override
            public void withService(
                Service service,
                Object... args
            ) throws Exception {
                // do something here.
            }
        }, arg1, arg2);

If you don't have any arguments this will also work:

        tracker.withService(new WithService<Service>() {
            @Override
            public void withService(
                Service service
            ) throws Exception {
                // do something here
            }
        });

##### withServiceIfAvailable

This does the same as withService(...) but without waiting for a service to become available. If the service is not available at the time of the call the callback will not be called. No exception is thrown by this!

##### withAllAvailableServices

This is used exactly the same way as withService(...), but the callback will be done for each tracked service instance, not only the active.

##### onTimeout (since 0.9.3)

This allows for a callback when the tracker times out waiting for a service. This callback will be called just before the _APSNoServiceAvailableException_ is about to be thrown.

    tracker.onTimeout(new OnTimeout() {
        @Override
        public void onTimeout() {
            // do something here
        }
    });

### APSLogger

This provides logging functionality. The no args constructor will log to System.out by default. The OutputStream constructor will logg to the specified output stream by default.

The APSLogger can be used by just creating an instance and then start using the info(...), error(...), etc methods. But in that case it will only log to System.out or the provided OutputStream. If you however do this:

	APSLogger logger = new APSLogger();
	logger.start(context);

then the logger will try to get hold of the standard OSGi LogService and if that is available log to that. If the log service is not available it will fallback to the OutputStream.

If you call the _setServiceRefrence(serviceRef);_ method on the logger then information about that service will be provied with each log.

### APSActivator

This is a BundleActivator implementation that uses annotations to register services and inject tracked services. Any bundle can use this activator by just importing the _se.natusoft.osgi.aps.activator_ and _se.natusoft.osgi.aps.activator.annotation_ packages.

This is actually a rather trivial class that just scans the bundle for classes and inspects all classes for annotations and act on them. 

**Please note** that it does _class.getDeclaredFields()_ and _class.getDeclaredMethods()_! This means that it will only see the bottom class of an inheritance hiearchy!

The following annotations are available:

**@OSGiServiceProvider** - This should be specified on a class that implements a service interface and should be registered as an OSGi service. _Please note_ that the first declared implemented interface is used as service interface unless you specify serviceAPIs={Svc.class, ...}.

    public @interface OSGiProperty {
        String name();
        String value();
    }

    public @interface OSGiServiceInstance {

        /** Extra properties to register the service with. */
        OSGiProperty[] properties() default {};

        /**
         * The service API to register instance with. If not specified the first
         * implemented interface will be used.
         */
        Class[] serviceAPIs() default {};
    }

    public @interface OSGiServiceProvider {
        /** Extra properties to register the service with. */
        OSGiProperty[] properties() default {};

        /**
         * The service API to register instance with. If not specified the first
         * implemented interface will be used.
         */
        Class[] serviceAPIs() default {};

        /**
         * This can be used as an alternative to properties() and also supports
         * several instances.
         */
        OSGiServiceInstance[] instances() default {};

        /**
         * An alternative to providing static information. This class will be
         * instantiated if specified and provideServiceInstancesSetup() will
         * be called to provide implemented service APIs, service properties,
         * and a service instance. In this last, it differs from
         * instanceFactoryClass() since that does not provide an instance.
         * This allows for more easy configuration of each instance.
         */
        Class<? extends APSActivatorServiceSetupProvider>
            serviceSetupProvider()
            default APSActivatorServiceSetupProvider.class;

        /**
         * This can be used as an alternative and will instantiate the
         * specified factory class which will deliver one set of
         * Properties per instance.
         */
        Class<? extends APSActivator.InstanceFactory> instanceFactoryClass()
            default APSActivator.InstanceFactory.class;

        /**
         * If true this service will be started in a separate thread.
         * This means the bundle start will continue in parallel and
         * that any failures in startup will be logged, but will
         * not stop the bundle from being started. If this is true
         * it wins over required service dependencies of the service
         * class. Specifying this as true allows you to do things that
         * cannot be done in a bunde activator start method, like
         * calling a service tracked by APSServiceTracker, without
         * causing a deadlock.
         */
        boolean threadStart() default false;
    }

Do note that for the _serviceSetupProvider()_ another solution is to use the _@BundleStart_ (see below) and just create instances of your service and register them with the BundleContext. But if you use _@OSGiServiceProvider_ to instantiate and register other "one instance" services, then using _serviceSetupProvider()_ would look a bit more consistent.

**@OSGiService** - This should be specified on a field having a type of a service interface to have a service of that type injected, and continuously tracked. Any call to the service will throw an APSNoServiceAvailableException (runtime) if no service has become available before the specified timeout. It is also possible to have APSServiceTracker as field type in which case the underlying configured tracker will be injected instead.

If _required=true_ is specified and this field is in a class annotated with _@OSGiServiceProvider_ then the class will not be registered as a service until the service dependency is actually available, and will also be unregistered if the tracker for the service does a timeout waiting for a service to become available. It will then be reregistered again when the dependent service becomes available again. Please note that unlike iPOJO the bundle is never stopped on dependent service unavailability, only the actual service is unregistered as an OSGi service. A bundle might have more than one service registered and when a dependency that is only required by one service goes away the other service is still available.

The non blocking variant of _APSServiceTracker.getWrappedService(true)_ as described above can also be achieved with this annotation by setting _nonBlocking = true_.

    public @interface OSGiService {

        /**
         * The timeout for a service to become available. Defaults
         * to 30 seconds.
         */
        String timeout() default "30 seconds";

        /**
         * Any additional search criteria. Should start with
         * '(' and end with ')'. Defaults to none.
         */
        String additionalSearchCriteria() default "";

        /**
         * This should specify a Class implementing
         * APSActivatorSearchCriteriaProvider. If specified it will
         * be used instead of additionalSearchCriteria() by
         * instantiating the Class and calling its method to get
         * a search criteria back. This allows for search criteria
         * coming from configuration, which a static annotation String
         * does not.
         */
        Class<? extends APSActivatorSearchCriteriaProvider>
            searchCriteriaProvider()
            default APSActivatorSearchCriteriaProvider.class;

        /**
         * If set to true the service using this service will not
         * be registered until the service becomes available.
         */
        boolean required() default false;

        /**
         * If this is set to true and a proxied implementation of the service is injected rather than the tracker directly
         * then any call made to the proxy will be cached if the service is not available and then later run when the
         * service becomes available. This of course means that methods returning a value will always return null when
         * service is not currently available since the real call will be made in the future. Returning a Future instead
         * in this case does not work since 'Future's are blocking, and we try to avoid blocking here.
         *
         * __YOU HAVE TO BE VERY CAREFUL WHEN SETTING THIS TO TRUE! NO CALLS RETURNING A VALUE!__
         *
         * The point of this is to be non blocking. By default with a proxied implementation tracker.allocateService() will
         * be called, and this blocks waiting for the service to become available if it is not.
         *
         * @return true or false (default).
         */
        boolean nonBlocking() default false;
    }

**@Managed** - This will have an instance managed and injected. There will be a unique instance for each name specified with the default name of "default" being used if none is specified. There are 2 field types handled specially: BundleContext and APSLogger. A BundleContext field will get the bundles context injected. For an APSLogger instance the 'loggingFor' annotation property can be specified. Please note that any other type must have a default constructor to be instantiated and injected!

    public @interface Managed {

        /**
         * The name of the instance to inject. If the same is used
         * in multiple classes the same instance will be injected.
         */
        String name() default "default";

        /**
         * A label indicating who is logging. If not specified the
         * bundle name will be used. This is only
         * relevant if the injected type is APSLogger.
         */
        String loggingFor() default "";
    }

**@Schedule** - Schedules a Runnable using a ScheduledExecutionService. @Schedule is handled after all injections have been done.

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Schedule {

        /**
         * The defined executor service to schedule this on. This should be the name of it. If left blank an internal
         * ScheduledExecutorService will be used.
         */
        String on() default "";

        /** The amount of time to wait for the (first) execution. */
        long delay();

        /** If specified how long to wait between runs. */
        long repeat();

        /** The time unit used for the above values. Defaults to seconds. */
        TimeUnit timeUnit() default TimeUnit.SECONDS;

        /** Possibility to affect the size of the thread pool when such is created internally for this (on="..." not provided!). */
        int poolSize() default 2;
    }

**@BundleStart** - This should be used on a method and will be called on bundle start. The method should take no arguments. If you need a BundleContext just inject it with _@Managed_. The use of this annotation is only needed for things not supported by this activator. Please note that a method annotated with this annotation can be static (in which case the class it belongs to will not be instantiaded). You can provide this annotation on as many methods in as many classes as you want. They will all be called (in the order classes are discovered in the bundle).

    public @interface BundleStart {

        /**
         * If true the start method will run in a new thread.
         * Any failures in this case will not fail
         * the bundle startup, but will be logged.
         */
        boolean thread() default false;
    }

**@BundleStop** - This should be used on a method and will be called on bundle stop. The method should take no arguments. This should probably be used if _@BundleStart_ is used. Please note that a method annotated with this annotation can be static!

    public @interface BundleStop {}


TODO: Since APS is nowmore built on top of vertx, servlets no longer need to be supported. Vaadin (traditional) is not supported either due to requiring servlets. Vaadin web components that works with Angular and React is however supported.

#### Usage as BundleActivator

The _APSActivator_ class has 2 constructors. The default constructor without arguments are used for BundleActivator usage. In this case you just specify this class as your bundles activator, and then use the annotations described above. Thats it!

#### Other Usage

Since the activator usage will manage and create instances of all annotated classes this will not always work in all situations. One example is web applications where the web container is responsible for creating servlets. If you specifiy APSActivator as an activator for a WAB bundle and then use the annotations in a servlet then APSActivator will have a managed instance of the servlet, but it will not be the same instance as the web contatiner will run.

Therefore APSActivator has another constructor that takes a vararg of instances: `public APSActivator(Object... instances)`. There is also a `public void addManagedInstance(Object instance)` method. These allow you to add an already existing instance to be managed by APSActivator. In addition to the provided existing instances it will still scan the bundle for classes to manage. It will however not double manage any class for which an existing instance of has already been provided. Any annotated class for which existing instances has not been provided will be instantiated by APSActivator.

**Please note** that if you create an instance of APSActivator in a servlet and provide the servlet instance to it and start it (you still need to do _start(BundleContext)_ and _stop(BundleContext)_ when used this way!), then you need to catch the close of the servlet and do _stop_ then.

There are 2 support classes:

* \[APSVaadinWebTools\]: APSVaadinOSGiApplication - This is subclassed by your Vaading application.

* \[APSWebTools\]: APSOSGiSupport - You create an instance of this in a servlet and let your servlet implement the _APSOSGiSupportCallbacks_ interface which is then passed to the constructor of APSOSGiSupport.

Both of these creates and manages an APSActivator internally and catches shutdown to take it down. They also provide other utilities like providing the BundleContext. See _APSWebTools_ for more information.

**Be warned** that this is currently very untested! No APS code uses this yet.

### APSContextWrapper

This provides a static wrap(...) method:

	Service providedService = APSContextWrapper.wrap(serviceProvider, Service.class);

where _serviceProvider_ is an instance of a class that implements _Service_. The resulting instance is a java.lang.reflect.Proxy implementation of _Service_ that ensures that the _serviceProvider_ ClassLoader is the context class loader during each call to all service methods that are annotated with @APSRunInBundlesContext annotation in _Service_. The wrapped instance can then be registered as the OSGi service provider.

Normally the threads context class loader is the original service callers context class loader. For a web application it would be the web containers context class loader. If a service needs its own bundles class loader during its execution then this wrapper can be used.

### ID generators

There is one interface:

    /**
     * This is a generic interface for representing IDs.
     */
    public interface ID extends Comparable<ID> {

        /**
         * Creates a new unique ID.
         *
         * @return A newly created ID.
         */
        public ID newID();

        /**
         * Tests for equality.
         *
         * @param obj The object to compare with.
         *
         * @return true if equal, false otherwise.
         */
        @Override
        public boolean equals(Object obj);

        /**
         * @return The hash code.
         */
        @Override
        public int hashCode();

    }

that have 2 implementations:

* IntID - Produces int ids.
* UUID - Produces java.util.UUID Ids.

### Javadoc

The javadoc for this can be found at <http://apidoc.natusoft.se/APSToolsLib/>.

