/*
 *
 * PROJECT
 *     Name
 *         APS Tools Library
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides a library of utilities, among them APSServiceTracker used by all other APS bundles.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2013-08-02: Created!
 *
 */
package se.natusoft.osgi.aps.tools;

import org.osgi.framework.*;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceListener;
import se.natusoft.osgi.aps.tools.annotation.activator.*;
import se.natusoft.osgi.aps.tools.apis.APSActivatorSearchCriteriaProvider;
import se.natusoft.osgi.aps.tools.apis.APSActivatorServiceSetupProvider;
import se.natusoft.osgi.aps.tools.apis.ServiceSetup;
import se.natusoft.osgi.aps.tools.exceptions.APSActivatorException;
import se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable;
import se.natusoft.osgi.aps.tools.tracker.OnTimeout;
import se.natusoft.osgi.aps.tools.tuples.Tuple2;
import se.natusoft.osgi.aps.tools.tuples.Tuple4;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class can be specified as bundle activator in which case you use the following annotations:
 *
 * * **@OSGiServiceProvider** -
 *   This should be specified on a class that implements a service interface and should be registered as
 *   an OSGi service. _Please note_ that the first declared implemented interface is used as service interface!
 *   If any fields of this service class is annotated with @OSGiService(required=true) then the registration
 *   of the service will be delayed until the required service becomes available.
 *
 * * **@OSGiService** -
 *   This should be specified on a field having a type of a service interface to have a service of that type
 *   injected, and continuously tracked. Any call to the service will throw an APSNoServiceAvailableException
 *   (runtime) if no service has become available before the specified timeout. It is also possible to have
 *   APSServiceTracker as field type in which case the underlying configured tracker will be injected instead.
 *
 * * **@Managed** -
 *   This will have an instance injected. There will be a unique instance for each name specified with the
 *   default name of "default" being used in none is specified. There are 2 field types handled specially:
 *   BundleContext and APSLogger. A BundleContext field will get the bundles context injected. For an APSLogger
 *   instance the 'loggingFor' annotation property can be specified.
 *
 * * **@BundleStart** -
 *   This should be used on a method and will be called on bundle start. The method should take no arguments.
 *   If you need a BundleContext just declare a field of BundleContext type and it will be injected. The use
 *   of this annotation is only needed for things not supported by this activator. Please note that a method
 *   annotated with this annotation can be static!
 *
 * * **@BundleStop** -
 *   This should be used on a method and will be called on bundle stop. The method should take no arguments.
 *   This should probably be used if @BundleStart is used. Please note that a method annotated with this
 *   annotation can be static!
 *
 * * **@Initializer** -
 *   Methods annotated with this gets called after everything else have been done, when all instances
 *   have been created and all injections handled. This can be used as an alternative constructor
 *   which have access to all members, even injected ones.
 *
 * * **@ConfiguredInstance** -
 *   This points out configuration that is used to create instances of a service and is only used
 *   in conjunction with @OSGiServiceProvider. See the javadoc for this annotation for more info.
 *
 * All injected service instances for @OSGiService will be APSServiceTracker wrapped
 * service instances that will automatically handle services leaving and coming. They will throw
 * APSNoServiceAvailableException on timeout!
 *
 * Most methods are protected making it easy to subclass this class and expand on its functionality.
 */
public class APSActivator implements BundleActivator, OnServiceAvailable, OnTimeout {

    //
    // Constants
    //

    /** This property is set on instances configured from names. */
    public static final String SERVICE_INSTANCE_NAME = "aps-service-instance-name";

    /** This means that this class will find all annotated classes in a bundle and manage those. */
    public static final boolean ACTIVATOR_MODE = true;

    /**
     * This means that already existing instances are provided in constructor and/or addManagedInstance(...).
     * This way you can manage injections in a servlet for example. Please note that in addition to the
     * already provided instances like servlets or other framework instances all classes in the bundle
     * will still be scanned and handled! It will of course sort out any duplicates due to the already
     * passed instances can be instances of a bundle class.
     */
    public static final boolean EXISTING_INSTANCES_MODE = false;

    //
    // Support Classes
    //

    /**
     * This is used for certain variants of specifying instances.
     */
    private class InstanceRepresentative {
        private boolean service = true;
        private Object instance;
        private Properties props;
        private List<String> serviceAPIs = new LinkedList<>();

        private InstanceRepresentative(Object instance) { this.instance = instance; }
    }

    //
    // Private Members
    //

    private APSLogger activatorLogger;

    /** Active service registrations. */
    private List<ServiceRegistration> services;

    /** Currently tracked services (who are injected into managedInstances). */
    private Map<String, APSServiceTracker> trackers;

    /** Instances created and injected into managedInstances. */
    private Map<String, Object> namedInstances;

    /** Annotated shutdown methods. */
    private List<Tuple2<Method, Object>> shutdownMethods;

    /**
     * Services annotated as required. A service provider will not be registered if it has any required service
     * that is not available yet. It will be registered when all required services are available.
     */
    private List<Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>>> requiredServices;

    /** The managed instances to receive injections. This includes mostly service providers. */
    private Map<Class, List<InstanceRepresentative>> managedInstances;

    /** Annotated service listeners. */
    private List<ListenerWrapper> listeners;

    /** The bundles context received on bundle start. */
    private BundleContext context;

    /** The mode of this activator instance.  */
    private boolean activatorMode = ACTIVATOR_MODE;

    @SuppressWarnings("FieldCanBeLocal")
    private boolean supportsRequired = true;

    //
    // Constructors
    //

    /**
     * Creates a new APSActivator instance for activator usage.
     */
    public APSActivator() {}

    /**
     * Creates a new APSActivator instance for non activator usage.
     *
     * @param instances Instances to manage.
     */
    public APSActivator(Object... instances) {
        this.managedInstances = Collections.synchronizedMap(new HashMap<Class, List<InstanceRepresentative>>());
        this.activatorMode = EXISTING_INSTANCES_MODE;
        for (Object inst : instances) {
            System.out.println("[APSActivator] Adding instance of '" + inst.getClass().getName() + "' as already " +
                    "existing instance to managed!");
            addManagedInstance(inst);
        }
    }

    //
    // Methods
    //

    /**
     * Adds an instance to manage.
     *
     * @param instance The instance to add.
     */
    public void addManagedInstance(Object instance) {
        List<InstanceRepresentative> instances = new LinkedList<>();
        instances.add(new InstanceRepresentative(instance));
        this.managedInstances.put(instance.getClass(), instances);
    }

    /**
     * Called on start() to reset internal instances. These are null:ed on stop().
     */
    protected void initMembers() {
        if (this.services == null) {
            this.services = Collections.synchronizedList(new LinkedList<ServiceRegistration>());
            this.trackers = Collections.synchronizedMap(new HashMap<String, APSServiceTracker>());
            this.namedInstances = Collections.synchronizedMap(new HashMap<String, Object>());
            this.shutdownMethods = Collections.synchronizedList(new LinkedList<Tuple2<Method, Object>>());
            if (this.activatorMode) this.managedInstances =
                    Collections.synchronizedMap(new HashMap<Class, List<InstanceRepresentative>>());
            this.requiredServices =
                    Collections.synchronizedList(
                            new LinkedList<Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>>>()
                    );

            this.listeners = Collections.synchronizedList(new LinkedList<ListenerWrapper>());

            this.activatorLogger = new APSLogger(System.out);
            this.activatorLogger.setLoggingFor("APSActivator");
            this.activatorLogger.start(context);
        }
    }

    /**
     * Called when this bundle is started so the Framework can perform the
     * bundle-specific activities necessary to start this bundle. This method
     * can be used to register services or to allocate any resources that this
     * bundle needs.
     *
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being started.
     * @throws Exception If this method throws an exception, this
     *                   bundle is marked as stopped and the Framework will remove this
     *                   bundle's listeners, unregister all services registered by this
     *                   bundle, and release all services used by this bundle.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        initMembers();
        this.activatorLogger.info("Starting APSActivator for bundle '" + context.getBundle().getSymbolicName() +
                "' with activatorMode: " + this.activatorMode);
        Bundle bundle = context.getBundle();

        List<Class> classEntries = new LinkedList<>();
        if (!this.activatorMode) {
            classEntries.addAll(this.managedInstances.keySet());
        }
        collectClassEntries(bundle, classEntries, "/");

        // Classes annotated with @OSGiServiceProvider having threadStart=true need to run the
        // startup setup, injections, etc in a separate thread and this start method should not
        // care about when they finnish, nor what their results were. The others should have
        // have done all that before returning from this method.
        //
        // I however simplify things by running both variants using single thread ExecutorService,
        // and waiting for the "non threaded" variant to finnish before returning.
        ExecutorService parallelExecutorService = Executors.newSingleThreadExecutor();
        ExecutorService waitedForExecutorService = Executors.newSingleThreadExecutor();
        ExecutorService executorService;

        final InitMethods parallelInitMethods = new InitMethods();
        final InitMethods waitedForInitMethods = new InitMethods();
        InitMethods initMethods;

        for (Class entryClass : classEntries) {
            executorService = waitedForExecutorService;
            initMethods = waitedForInitMethods;

            OSGiServiceProvider serviceProvider = (OSGiServiceProvider)entryClass.getAnnotation(OSGiServiceProvider.class);
            if (serviceProvider != null && (serviceProvider.threadStart() || serviceProvider.serviceSetupProvider() != APSActivatorServiceSetupProvider.class || hasConfiguredInstance(entryClass))) {
                executorService = parallelExecutorService;
                initMethods = parallelInitMethods;
            }

            executorService.submit(new PerClassWorkRunnable(entryClass, context, initMethods));
        }

        parallelExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    callInitMethods(parallelInitMethods);
                    parallelInitMethods.clean();
                } catch (Exception e) {
                    activatorLogger.error("Failed to call callInitMethods(parallelInitMethods)!", e);
                }
            }
        });

        waitedForExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    callInitMethods(waitedForInitMethods);
                    waitedForInitMethods.clean();
                } catch (Exception e) {
                    activatorLogger.error("Failed to call callInitMethods(waitedForInitMethods)!", e);
                }
            }
        });

        parallelExecutorService.shutdown();
        waitedForExecutorService.shutdown();
        waitedForExecutorService.awaitTermination(30, TimeUnit.SECONDS);
    }

    /**
     * Returns the field annotated with @ConfiguredInstance or null if not found.
     *
     * @param clazz The class to search for the annotated field.
     */
    private Field getConfiguredInstance(Class clazz) {
        Field found = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(ConfiguredInstance.class) != null) {
                found = field;
                break;
            }
        }

        return found;
    }

    /**
     * Returns true if the specified class have a field annotated with @ConfiguredInstance. Otherwise false is returned.
     *
     * @param clazz The class to check.
     */
    private boolean hasConfiguredInstance(Class clazz) {
        return getConfiguredInstance(clazz) != null;
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle. In general, this
     * method should undo the work that the `BundleActivator.start()`
     * method started. There should be no active threads that were started by
     * this bundle when this bundle returns. A stopped bundle must not call any
     * Framework objects.
     *
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the
     *                   bundle is still marked as stopped, and the Framework will remove
     *                   the bundle's listeners, unregister all services registered by the
     *                   bundle, and release all services used by the bundle.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.services == null) {
            System.err.println("ERROR: Stopping non started instance of APSActivator!");
            return; // Not started!
        }

        Exception failure = null;

        this.activatorLogger.info("Stopping APSActivator for bundle '" + context.getBundle().getSymbolicName() +
                "' with activatorMode: " + this.activatorMode);

        for (ListenerWrapper listenerWrapper : this.listeners) {
            listenerWrapper.stop(context);
        }
        this.listeners = null;

        for (Tuple2<Method, Object> shutdownMethod : this.shutdownMethods) {
            try {
                //noinspection RedundantArrayCreation
                shutdownMethod.t1.invoke(shutdownMethod.t2, new Object[0]);

                this.activatorLogger.info("Called bundle shutdown method '" + shutdownMethod.t2.getClass() +
                        "." + shutdownMethod.t1.getName() + "() for bundle: " +
                        context.getBundle().getSymbolicName() + "!");
            }
            catch (Exception e) {
                this.activatorLogger.error("Bundle stop problem!", e);
                failure = e;
            }
        }
        this.shutdownMethods = null;

        for (ServiceRegistration serviceRegistration : this.services) {
            try {
                serviceRegistration.unregister();
            }
            catch (Exception e) {
                this.activatorLogger.error("Bundle stop problem!", e);
                failure = e;
            }
        }
        this.services = null;

        for (String trackerKey : this.trackers.keySet()) {
            APSServiceTracker tracker = this.trackers.get(trackerKey);
            try {
                tracker.stop(context);
            }
            catch (Exception e) {
                this.activatorLogger.error("Bundle stop problem!", e);
                failure = e;
            }
        }
        this.trackers = null;

        for (String namedInstanceKey : this.namedInstances.keySet()) {
            Object namedInstance = this.namedInstances.get(namedInstanceKey);
            if (namedInstance instanceof APSLogger) {
                try {
                    ((APSLogger)namedInstance).stop(context);
                }
                catch (Exception e) {
                    this.activatorLogger.error("Bundle stop problem!", e);
                    failure = e;
                }
            }
        }
        this.namedInstances = null;

        this.activatorLogger.stop(context);
        this.activatorLogger = null;

        if (failure != null) {
            throw new APSActivatorException("Bundle stop not entirely successful!", failure);
        }
    }

    // ---- Service Registration ---- //

    /**
     * Converts from annotation properties to a java.util.Properties instance.
     *
     * @param osgiProperties The annotation properties to convert.
     */
    protected Properties osgiPropertiesToProperties(OSGiProperty[] osgiProperties) {
        Properties props = new Properties();
        for (OSGiProperty prop : osgiProperties) {
            props.setProperty(prop.name(), prop.value());
        }
        return props;
    }

    /**
     * This is the first thing done to instantiate all instances needed. Do note that this handles both service providers
     * and client injections.
     *
     * @param managedClass The manages class to create instances of.
     * @throws Exception
     */
    protected void collectInjecteeAndServiceInstancesToManage(Class managedClass) throws Exception {
        OSGiServiceProvider serviceProvider = (OSGiServiceProvider)managedClass.getAnnotation(OSGiServiceProvider.class);
        if (serviceProvider != null) {
            collectServiceInstancesToManage(managedClass, serviceProvider);
        }
        else {
            collectInjecteeInstancesToManage(managedClass);
        }
    }

    /**
     * Handle collection of services to manage.
     *
     * @param managedClass The managed class to inspect.
     * @param serviceProvider The found @OSGiServiceProvider annotation on managedClass.
     *
     * @throws Exception
     */
    protected void collectServiceInstancesToManage(Class managedClass, OSGiServiceProvider serviceProvider) throws Exception {
        if (managedClass.getInterfaces().length == 0) {
            throw new APSActivatorException("Managed service provider class '" + managedClass.getName() + "' " +
                    "does not implement any service interface!");
        }

        if (hasConfiguredInstance(managedClass)) {
            handleConfiguredServiceInstances(managedClass);
        }
        else if (serviceProvider.instances().length > 0) {
            handleAnnotationInstancesServiceInstances(managedClass, serviceProvider);
        }
        else if (!serviceProvider.instanceFactoryClass().equals(InstanceFactory.class)) {
            handleAnnotationInstanceFactoryServiceInstances(managedClass, serviceProvider);
        }
        else if (!serviceProvider.serviceSetupProvider().equals(APSActivatorServiceSetupProvider.class)) {
            handleAnnotationServiceSetupProviderServiceInstances(managedClass, serviceProvider);
        }
        else {
            handleDefaultServiceInstance(managedClass, serviceProvider);
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleConfiguredServiceInstances(Class managedClass) throws Exception {
        // We know we always will get this since we wouldn't get here if this wasn't available!
        Field injectToField = getConfiguredInstance(managedClass);

        ConfiguredInstance configuredInstance = injectToField.getAnnotation(ConfiguredInstance.class);
        Class configClass = verifyAPSConfigClass(configuredInstance.configClass(), managedClass);

        // getNamedConfigEntryNames() are part of the APSConfig base class! We call it through reflection
        // since we don't have compile dependency on APS-APIs.
        Method getNamedConfigEntryNamesMethod = configClass.getSuperclass().getMethod("getNamedConfigEntryNames", Class.class, String.class);
        List<String> names =
                (List<String>)getNamedConfigEntryNamesMethod.invoke(null, configuredInstance.configClass(),
                        configuredInstance.instNamePath());

        for (String name : names) {
            Object instance = createInstance(managedClass);

            injectObject(instance, name, injectToField);

            InstanceRepresentative ir = new InstanceRepresentative(instance);
            ir.service = true;
            Properties props = new Properties();
            props.setProperty(configuredInstance.instanceNamePropertyKey(), name);
            ir.props = props;
            ir.instance = instance;
            OSGiServiceProvider serviceProvider = (OSGiServiceProvider)managedClass.getAnnotation(OSGiServiceProvider.class);
            if (serviceProvider == null) {
                throw new IllegalArgumentException("A managed class containing a @ConfiguredInstance annotation on a field must " +
                        "also have the class annotated with @OSGiServiceProvider!");
            }
            for (Class svcAPI : serviceProvider.serviceAPIs()) {
                ir.serviceAPIs.add(svcAPI.getName());
            }
            if (ir.serviceAPIs.isEmpty()) {
                if (managedClass.getInterfaces().length == 0) {
                    throw new IllegalArgumentException("An OSGi service class must implement at least one service API interface!");
                }
                ir.serviceAPIs.add(managedClass.getInterfaces()[0].getName());
            }

            addManagedInstanceRep(managedClass, ir);
        }
    }

    private Class verifyAPSConfigClass(Class toVerify, Class managedClass) {
        Class check = toVerify;
        while (!check.getSimpleName().equals("APSConfig")) {
            check = check.getSuperclass();
            if (check == null || check.getSimpleName().equals("Object"))
                throw new IllegalArgumentException("Bad config class in @ConfiguredInstance of " + managedClass.getName() + "!");
        }
        return toVerify;
    }

    protected void handleAnnotationInstancesServiceInstances(Class managedClass, OSGiServiceProvider serviceProvider) throws Exception {
        for (OSGiServiceInstance inst : serviceProvider.instances()) {
            InstanceRepresentative ir = new InstanceRepresentative(createInstance(managedClass));
            ir.props = osgiPropertiesToProperties(inst.properties());
            for (Class svcAPI : inst.serviceAPIs()) {
                ir.serviceAPIs.add(svcAPI.getName());
            }
            addManagedInstanceRep(managedClass, ir);
        }
    }

    protected void handleAnnotationInstanceFactoryServiceInstances(Class managedClass, OSGiServiceProvider serviceProvider) throws Exception {
        InstanceFactory instanceFactory = (InstanceFactory) getManagedInstanceRep(serviceProvider.instanceFactoryClass()).instance;
        if (instanceFactory == null) {
            instanceFactory = serviceProvider.instanceFactoryClass().newInstance();
        }
        for (Properties props : instanceFactory.getPropertiesPerInstance()) {
            InstanceRepresentative ir = new InstanceRepresentative(createInstance(managedClass));
            ir.props = props;
            String svcAPIList = props.getProperty(InstanceFactory.SERVICE_API_CLASSES_PROPERTY);
            if (svcAPIList != null) {
                Collections.addAll(ir.serviceAPIs, svcAPIList.split(":"));
            }
            else {
                ir.serviceAPIs.add(managedClass.getInterfaces()[0].getName());
            }
            addManagedInstanceRep(managedClass, ir);
        }
    }

    protected void handleAnnotationServiceSetupProviderServiceInstances(Class managedClass, OSGiServiceProvider serviceProvider) throws Exception {
        APSActivatorServiceSetupProvider setupProvider = serviceProvider.serviceSetupProvider().newInstance();
        for (ServiceSetup setup : setupProvider.provideServiceInstancesSetup()) {
            InstanceRepresentative ir = new InstanceRepresentative(setup.getServiceInstance());
            ir.props = setup.getProps();
            ir.serviceAPIs.addAll(setup.getServiceAPIs());
            addManagedInstanceRep(managedClass, ir);
        }
    }

    protected void handleDefaultServiceInstance(Class managedClass, OSGiServiceProvider serviceProvider) throws Exception {
        InstanceRepresentative ir = new InstanceRepresentative(createInstance(managedClass));
        ir.props = new Properties();
        if (serviceProvider.serviceAPIs().length > 0) {
            for (Class svcAPI : serviceProvider.serviceAPIs()) {
                ir.serviceAPIs.add(svcAPI.getName());
            }
        }
        else {
            ir.serviceAPIs.add(managedClass.getInterfaces()[0].getName());
        }
        addManagedInstanceRep(managedClass, ir);
    }


    /**
     * Handles collection of non service instances to manage.
     *
     * @param managedClass The managed class to inspect.
     *
     * @throws Exception
     */
    protected void collectInjecteeInstancesToManage(Class managedClass) throws Exception {
        done:
        for (Method method : managedClass.getDeclaredMethods()) {
            for (Annotation methodAnn : method.getDeclaredAnnotations()) {
                for (Class activatorMethodAnnClass : methodAnnotations) {
                    if (methodAnn.getClass().equals(activatorMethodAnnClass)) {
                        InstanceRepresentative ir = new InstanceRepresentative(createInstance(managedClass));
                        ir.service = false;
                        addManagedInstanceRep(managedClass, ir);
                        break done; // This will break out of the outer for loop!
                    }
                }
            }
        }
    }

    private static final Class[] methodAnnotations = {se.natusoft.osgi.aps.tools.annotation.activator.BundleListener.class, BundleStart.class, BundleStop.class, se.natusoft.osgi.aps.tools.annotation.activator.FrameworkListener.class, Initializer.class, se.natusoft.osgi.aps.tools.annotation.activator.ServiceListener.class};

    /**
     * Handles publishing of bundle services. If a published service has any dependencies to
     * other services that are marked as required then the publishing is delayed until all required
     * services are available. In this case the service will be unpublished if any of the required
     * services times out.
     *
     * @param managedClass The managed service class to instantiate and register as a service.
     * @param context The bundles context.
     * @throws Exception
     */
    protected void doServiceRegistrationsOfManagedServiceInstances(final Class managedClass, final BundleContext context) throws Exception {
        OSGiServiceProvider serviceProvider = (OSGiServiceProvider)managedClass.getAnnotation(OSGiServiceProvider.class);

        if (serviceProvider != null) {
            if (this.requiredServices.isEmpty()) {
                    registerServices(managedClass, context, this.services);
            }
            else {
                for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
                    this.activatorLogger.info("Registering for delayed start of '" + managedClass.getName() + "' " +
                        "due to service '" + requiredService.t1.getServiceClass().getName() + "'!");
                    requiredService.t1.onActiveServiceAvailable(this);
                    requiredService.t1.setOnTimeout(this);
                }
            }
        }
    }

    /**
     * This is used to start delayed service publishing. Each tracker of a required service will be calling
     * this method on first service becoming available. When all required services are available the delayed
     * service will be published.
     *
     * @param service The received service.
     * @param serviceReference The reference to the received service.
     *
     * @throws Exception
     */
    @Override
    public void onServiceAvailable(Object service, ServiceReference serviceReference) throws Exception {
        this.activatorLogger.info("Received service: " + service.getClass().getName());
        List<Class> uniqueClasses = new LinkedList<>();
        for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
            if (!uniqueClasses.contains(requiredService.t2)) {
                uniqueClasses.add(requiredService.t2);
            }
        }

        for (Class managedClass : uniqueClasses) {
            boolean allRequiredAvailable = true;
            for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
                if (requiredService.t2.equals(managedClass) && !requiredService.t1.hasTrackedService()) {
                    allRequiredAvailable = false;
                    this.activatorLogger.info("Not all required services are available yet for: " + managedClass.getName());
                    break;
                }
            }

            if (allRequiredAvailable) {
                this.activatorLogger.info("All required services are now available for: " + managedClass.getName());
                for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
                    if (requiredService.t2.equals(managedClass) && !requiredService.t3) {
                        this.activatorLogger.info("Registering services for: " + managedClass.getName());
                        registerServices(requiredService.t2, this.context, requiredService.t4);
                        this.services.addAll(requiredService.t4);
                        requiredService.t3 = true;
                    }
                }
            }
        }
    }

    /**
     * This gets called for required services when the tracker have timed out waiting for a service to become
     * available and is about to throw an APSNoServiceAvailableException. This will unpublish all published
     * services that have a requirement on the timed out service. The service will be republished later when
     * it becomes available again by onServiceAvailable() above.
     *
     * @throws RuntimeException
     */
    @Override
    public void onTimeout() throws RuntimeException {
        this.activatorLogger.warn("A required service have gone away!");
        List<Class> uniqueClasses = new LinkedList<>();
        for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
            if (!uniqueClasses.contains(requiredService.t2)) {
                uniqueClasses.add(requiredService.t2);
            }
        }

        for (Class managedClass : uniqueClasses) {
            boolean allRequiredAvailable = true;
            for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
                if (requiredService.t2.equals(managedClass) && !requiredService.t1.hasTrackedService()) {
                    allRequiredAvailable = false;
                    break;
                }
            }

            if (!allRequiredAvailable) {
                for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
                    if (requiredService.t2.equals(managedClass)) {
                        for (ServiceRegistration serviceRegistration : requiredService.t4) {
                            try {
                                serviceRegistration.unregister();
                                requiredService.t3 = false;
                                this.services.remove(serviceRegistration);
                                this.activatorLogger.warn("Removed registration for: " + serviceRegistration.getReference());
                            }
                            catch (Exception e) {
                                this.activatorLogger.error("Bundle stop problem!", e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Registers/publishes services annotated with @OSGiServiceProvider.
     *
     * @param managedClass The managed class to instantiate and register as an OSGi service.
     * @param context The bundle context.
     * @param serviceRegs The list to save all service registrations to for later deregistration.
     * @throws Exception
     */
    protected void registerServices(Class managedClass, BundleContext context, List<ServiceRegistration> serviceRegs) throws Exception {
        OSGiServiceProvider serviceProvider = (OSGiServiceProvider)managedClass.getAnnotation(OSGiServiceProvider.class);
        if (serviceProvider != null) {

            List<InstanceRepresentative> instanceReps = getManagedInstanceReps(managedClass);

            for (InstanceRepresentative instRep : instanceReps) {
                if (instRep.service) {
                    instRep.props.put(Constants.SERVICE_PID, managedClass.getName());
                    if (!instRep.serviceAPIs.isEmpty()) {
                        injectInstanceProps(instRep.instance, instRep.props);
                        for (String svcAPI : instRep.serviceAPIs) {
                            ServiceRegistration serviceReg =
                                    context.registerService(
                                            svcAPI,
                                            instRep.instance,
                                            instRep.props
                                    );

                            serviceRegs.add(serviceReg);
                            this.activatorLogger.info("Registered '" + managedClass.getName() + "' as a service provider of '" +
                                    svcAPI + "' for bundle: " + context.getBundle().getSymbolicName() + "!");
                        }
                    } else {
                        throw new IllegalArgumentException("The @OSGiServiceProvider annotated service of class '" +
                                managedClass.getName() + "' does not implement a service interface!");
                    }
                }
            }
        }
    }

    /**
     * Injects properties into the first member of Properties type found if any.
     *
     * @param instance The instance to inject into.
     * @param props The properties to inject.
     *
     * @throws Exception as always!
     */
    protected void injectInstanceProps(Object instance, Properties props) throws Exception {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.getType().equals(java.util.Properties.class)) {
                field.setAccessible(true);
                field.set(instance, props);
                break;
            }
        }
    }

    // ---- Field Injections ---- //

    /**
     * This handles all field injections by delegating to handlers of specific types of field injections.
     *
     * @param managedClass The managed class to inject into.
     * @param context The bundles context.
     */
    protected void doFieldInjectionsIntoManagedInstances(Class managedClass, BundleContext context) {
        for (Field field : managedClass.getDeclaredFields()) {
            doServiceInjection(field, managedClass, context);
            doInstanceInjection(field, managedClass, context);
        }
    }

    /**
     * Tracks and injects APSServiceTracker directly or as wrapped service instance using the tracker to
     * call the service depending on the field type.
     *
     * @param field The field to inject.
     * @param managedClass Used to lookup or create an instance of this class to inject into.
     * @param context The bundle context.
     */
    protected void doServiceInjection(Field field, Class managedClass, BundleContext context) {
        OSGiService service = field.getAnnotation(OSGiService.class);
        if (service != null) {
            String trackerKey = field.getType().getName() + service.additionalSearchCriteria();
            APSServiceTracker tracker = this.trackers.get(trackerKey);

            List<InstanceRepresentative> managedInstanceReps = getManagedInstanceReps(managedClass);
            for (InstanceRepresentative managedInstanceRep : managedInstanceReps) {
                if (tracker == null) {
                    String searchCriteria = service.additionalSearchCriteria();
                    Class<? extends APSActivatorSearchCriteriaProvider> searchCriteriaProviderClass = service.searchCriteriaProvider();
                    if (searchCriteriaProviderClass != APSActivatorSearchCriteriaProvider.class) {
                        try {
                            APSActivatorSearchCriteriaProvider searchCriteriaProvider;
                            if (APSActivatorSearchCriteriaProvider.SearchCriteriaProviderFactory.class.isAssignableFrom(managedClass)) {
                                searchCriteriaProvider =
                                        ((APSActivatorSearchCriteriaProvider.SearchCriteriaProviderFactory)managedInstanceRep.instance)
                                                .createSearchCriteriaProvider();
                            }
                            else {
                                searchCriteriaProvider = searchCriteriaProviderClass.newInstance();
                            }
                            searchCriteria = searchCriteriaProvider.provideSearchCriteria();
                            trackerKey = field.getType().getName() + searchCriteria;
                            tracker = this.trackers.get(trackerKey);
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new APSActivatorException("Failed to instantiate APSActivatorSearchCriteriaProvider " +
                                    "implementation '" + searchCriteriaProviderClass.getName() + "'!", e);
                        }
                    }
                    if (tracker == null) {
                        tracker = new APSServiceTracker<>(context, field.getType(), searchCriteria, service.timeout());
                        tracker.start();
                        this.trackers.put(trackerKey, tracker);
                    }
                }

                if (field.getType().equals(APSServiceTracker.class)) {
                    injectObject(managedInstanceRep.instance, tracker, field);
                }
                else {
                    injectObject(managedInstanceRep.instance, tracker.getWrappedService(), field);
                }
            }

            if (service.required() && this.supportsRequired) {
                Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService =
                        new Tuple4<>(tracker, managedClass, false, (List<ServiceRegistration>)new LinkedList<ServiceRegistration>());
                this.requiredServices.add(requiredService);
            }

            this.activatorLogger.info("Injected tracked service '" + field.getType().getName() +
                    (service.additionalSearchCriteria().length() > 0 ? " " + service.additionalSearchCriteria() : "") +
                    "' " + "into '" + managedClass.getName() + "." + field.getName() + "' for bundle: " +
                    context.getBundle().getSymbolicName() + " for " + managedInstanceReps.size() + " instance(s)!");
        }
    }

    /**
     * Handles injections of APSLogger, BundleContext, or other class types with default constructor.
     *
     * @param field The field to inject into.
     * @param managedClass Used to lookup or create an instance of this class to inject into.
     * @param context The bundle context.
     */
    protected void doInstanceInjection(Field field, Class managedClass, BundleContext context) {
        Managed managed = field.getAnnotation(Managed.class);
        if (managed != null) {
            String namedInstanceKey = managed.name() + field.getType().getName();
            Object namedInstance = this.namedInstances.get(namedInstanceKey);
            if (namedInstance == null) {
                if (field.getType().equals(APSLogger.class)) {
                    namedInstance = new APSLogger(System.out);
                    if (managed.loggingFor().length() > 0) {
                        ((APSLogger)namedInstance).setLoggingFor(managed.loggingFor());
                    }
                    ((APSLogger)namedInstance).start(context);
                }
                else if (field.getType().equals(BundleContext.class)) {
                    namedInstance = context;
                }
                else {
                    namedInstance = getManagedInstanceRep(field.getType()).instance;
                }
                this.namedInstances.put(namedInstanceKey, namedInstance);
            }
            else {
                this.activatorLogger.info("Got named instance for key '" + namedInstanceKey + "': " +
                        namedInstance.getClass().getName());
            }

            if (!isManagedClass(managedClass)) {
                InstanceRepresentative ir = new InstanceRepresentative(createInstance(managedClass));
                addManagedInstanceRep(managedClass, ir);
            }

            List<InstanceRepresentative> managedInstanceReps = getManagedInstanceReps(managedClass);

            for (InstanceRepresentative managedInstanceRep : managedInstanceReps) {
                injectObject(managedInstanceRep.instance, namedInstance, field);
            }

            this.activatorLogger.info("Injected '" + namedInstance.getClass().getName() +
                    "' instance for name '" + managed.name() + "' " +
                    "into '" + managedClass.getName() + "." + field.getName() + "' for bundle: " +
                    context.getBundle().getSymbolicName() + "!");
        }
    }

    // ---- Methods ---- //

    /**
     * Handles annotated methods.
     *
     * @param managedClass Used to lookup or create an instance of this class containing the method to call.
     * @param context The bundle context.
     * @param initMethods The init methods container to add any found init methods to.
     */
    protected void handleMethodInvocationsOnManagedInstances(Class managedClass, BundleContext context, InitMethods initMethods) {
        for (Method method : managedClass.getDeclaredMethods()) {
            doStartupMethodInvocations(method, managedClass, context);
            collectShutdownMethods(method, managedClass);
            registerListenerMethodWrappers(method, managedClass, context);
            collectInitMethods(method, managedClass, context, initMethods);
        }
    }

    /**
     * Handles methods annotated with @APSBundleStartup.
     *
     * @param method The annotated method to call.
     * @param managedClass Used to lookup or create an instance of this class containing the method to call.
     * @param context The bundle context.
     */
    @SuppressWarnings("RedundantArrayCreation")
    protected void doStartupMethodInvocations(final Method method, final Class managedClass, final BundleContext context) {
        BundleStart bundleStart = method.getAnnotation(BundleStart.class);
        if (bundleStart != null) {
            if (method.getParameterTypes().length > 0) {
                throw new APSActivatorException("An @BundleStart method must take no parameters! [" +
                    managedClass.getName() + "." + method.getName() + "(?)]");
            }

            if (bundleStart.thread()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Object managedInstance = null;
                            if (!Modifier.isStatic(method.getModifiers())) {
                                managedInstance = getManagedInstanceRep(managedClass).instance;
                            }
                            // Gigantically strange: When called from JUnit and the second argument is null
                            // you get an IllegalArgumentException with a "Wrong number of arguments!" message!
                            // An empty array however works fine!
                            method.invoke(managedInstance, new Object[0]);

                            APSActivator.this.activatorLogger.info("Called bundle start method '" + managedClass.getName() +
                                    "." + method.getName() + "()' for bundle: " + context.getBundle().getSymbolicName() + "!");
                        } catch (IllegalAccessException iae) {
                            APSActivator.this.activatorLogger.error("Failed to call start method! [" +
                                    managedClass.getName() + "." + method.getName() + "()]", iae);
                        }
                        catch (InvocationTargetException ite) {
                            APSActivator.this.activatorLogger.error("Failed to call start method! [" +
                                    managedClass.getName() + "." + method.getName() + "()]", ite.getCause());
                        }
                        catch (Exception e) {
                            APSActivator.this.activatorLogger.error("Failed to call start method! [" +
                                    managedClass.getName() + "." + method.getName() + "()]", e);
                        }
                    }
                }).start();
            }
            else {
                try {
                    Object managedInstance = null;
                    if (!Modifier.isStatic(method.getModifiers())) {
                        managedInstance = getManagedInstanceRep(managedClass).instance;
                    }
                    // Gigantically strange: When called from JUnit and the second argument is null
                    // you get an IllegalArgumentException with a "Wrong number of arguments!" message!
                    // An empty array however works fine!
                    method.invoke(managedInstance, new Object[0]);

                    this.activatorLogger.info("Called bundle start method '" + managedClass.getName() +
                            "." + method.getName() + "()' for bundle: " + context.getBundle().getSymbolicName() + "!");
                } catch (IllegalAccessException iae) {
                    throw new APSActivatorException("Failed to call start method! [" +
                               managedClass.getName() + "." + method.getName() + "()]", iae);
                }
                catch (InvocationTargetException ite) {
                    throw new APSActivatorException("Failed to call start method! [" +
                            managedClass.getName() + "." + method.getName() + "()]", ite.getCause());
                }
            }
        }
    }

    /**
     * Handles methods annotated with @BundleStop.
     *
     * @param method The annotated method to call.
     * @param managedClass Used to lookup or create an instance of this class containing the method to call.
     */
    protected void collectShutdownMethods(Method method, Class managedClass) {
        BundleStop bundleStop = method.getAnnotation(BundleStop.class);
        if (bundleStop != null) {
            Tuple2<Method, Object> shutdownMethod = new Tuple2<>(method, null);
            if (!Modifier.isStatic(method.getModifiers())) {
                shutdownMethod.t2 = getManagedInstanceRep(managedClass).instance;
            }

            this.shutdownMethods.add(shutdownMethod);
        }
    }

    /**
     * Handles listener methods, settingthem up to be called on relevant events.
     *
     * @param method The method to check for ServiceListener annotation.
     * @param managedClass The class of the method.
     * @param context The bundle context.
     */
    protected void registerListenerMethodWrappers(Method method, Class managedClass, BundleContext context) {
        se.natusoft.osgi.aps.tools.annotation.activator.ServiceListener serviceListener =
                method.getAnnotation(se.natusoft.osgi.aps.tools.annotation.activator.ServiceListener.class);
        if (serviceListener != null) {
            ServiceListenerWrapper serviceListenerWrapper =
                    new ServiceListenerWrapper(method, getManagedInstanceRep(managedClass).instance);
            serviceListenerWrapper.start(context);
            this.listeners.add(serviceListenerWrapper);
        }

        se.natusoft.osgi.aps.tools.annotation.activator.BundleListener bundleListener =
                method.getAnnotation(se.natusoft.osgi.aps.tools.annotation.activator.BundleListener.class);
        if (bundleListener != null) {
            BundleListenerWrapper bundleListenerWrapper =
                    new BundleListenerWrapper(method, getManagedInstanceRep(managedClass).instance);
            bundleListenerWrapper.start(context);
            this.listeners.add(bundleListenerWrapper);
        }

        se.natusoft.osgi.aps.tools.annotation.activator.FrameworkListener frameworkListener =
                method.getAnnotation(se.natusoft.osgi.aps.tools.annotation.activator.FrameworkListener.class);
        if (frameworkListener != null) {
            FrameworkListenerWrapper frameworkListenerWrapper =
                    new FrameworkListenerWrapper(method, getManagedInstanceRep(managedClass).instance);
            frameworkListenerWrapper.start(context);
            this.listeners.add(frameworkListenerWrapper);
        }
    }

    /**
     * Step 1 of initializer methods. These should be called after everything else is done and everything
     * is instantiated and injected. Therefore we only collect them now, and call them in callInitMethods().
     *
     * @param method The method to check for Initializer annotation.
     * @param managedClass The class of the method.
     * @param context The bundle context.
     * @param initMethods The init methods container to add any found init methods to.
     */
    @SuppressWarnings("UnusedParameters")
    protected void collectInitMethods(Method method, Class managedClass, BundleContext context, InitMethods initMethods) {
        Initializer initializer = method.getAnnotation(Initializer.class);
        if (initializer != null) {
            initMethods.addInitMethod(method, managedClass);
        }
    }

    /**
     * Step 2: Calls all initializer methods.
     *
     * @param initMethods The init methods to call.
     *
     * @throws Exception Any exceptions thrown by initializers are forwarded upp.
     */
    protected void callInitMethods(InitMethods initMethods) throws Exception {
        if (!initMethods.isEmpty()) {
            for (Tuple2<Method, Class> initMethod : initMethods) {
                Object instance = getManagedInstanceRep(initMethod.t2).instance;
                try {
                    initMethod.t1.invoke(instance, (Object[]) null);
                } catch (IllegalAccessException iae) {
                    throw new APSActivatorException("Failed to call init method (" + initMethod.t1.getName() +
                            ") on instance (" + instance + ")!", iae);
                } catch (InvocationTargetException ite) {
                    throw (Exception) ite.getCause();
                }
            }
        }
    }

    // ---- Support ---- //

    /**
     * Returns a managed instance of a class.
     *
     * @param managedClass The managed class to get instance for.
     */
    protected InstanceRepresentative getManagedInstanceRep(Class managedClass) {
        return getManagedInstanceReps(managedClass).get(0);
    }

    /**
     * Returns a managed instance of a class.
     *
     * @param managedClass The managed class to get instance for.
     */
    protected List<InstanceRepresentative> getManagedInstanceReps(Class managedClass) {
        List<InstanceRepresentative> instances = this.managedInstances.get(managedClass);
        if (instances == null) {
            InstanceRepresentative ir = new InstanceRepresentative(createInstance(managedClass));
            addManagedInstanceRep(managedClass, ir);
            instances = this.managedInstances.get(managedClass);
        }

        return instances;
    }

    /**
     * Returns true if the specified class already is managed. Otherwise false is returned.
     *
     * @param clazz The class to check.
     */
    protected boolean isManagedClass(Class clazz) {
        return this.managedInstances.containsKey(clazz);
    }

    /**
     * Adds a managed instance representative.
     *
     * @param managedClass The class of the managed instance.
     * @param instanceRepresentative The instance representative to add.
     */
    protected void addManagedInstanceRep(Class managedClass, InstanceRepresentative instanceRepresentative) {
        List<InstanceRepresentative> instances = this.managedInstances.get(managedClass);
        if (instances == null) {
            instances = new LinkedList<>();
            this.managedInstances.put(managedClass, instances);
        }

        // Cleanup InstanceRepresentative if needed.
        for (InstanceRepresentative ir : instances) {
            if (ir.instance == null) {
                ir.instance = createInstance(managedClass);
            }
        }

        // Add it.
        instances.add(instanceRepresentative);
    }

    /**
     * Creates a new instance of the specified class.
     *
     * @param managedClass The class to instantiate.
     */
    protected Object createInstance(Class managedClass) {
        Object instance;

        try {
            instance = managedClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new APSActivatorException("Failed to instantiate managed class! [" + e.getMessage() + "]", e);
        }

        return instance;
    }

    /**
     * Support method to do injections.
     *
     * @param injectTo The instance to inject into.
     * @param toInject The instance to inject.
     * @param field The field to inject to.
     */
    protected void injectObject(Object injectTo, Object toInject, Field field) {
        try {
            field.setAccessible(true);
            field.set(injectTo, toInject);
        }
        catch (IllegalAccessException iae) {
            throw new APSActivatorException("Failed to inject managed field [" + field + "] into [" +
                    injectTo.getClass() + "]", iae);
        }
    }

    /**
     * Populates the entries list with recursively found class entries.
     *
     * @param bundle The bundle to get class entries for.
     * @param entries The list to add the class entries to.
     * @param startPath The start path to look for entries.
     */
    protected void collectClassEntries(Bundle bundle, List<Class> entries, String startPath) {
        Enumeration entryPathEnumeration = bundle.getEntryPaths(startPath);
        while (entryPathEnumeration.hasMoreElements()) {
            String entryPath = entryPathEnumeration.nextElement().toString();
            if (entryPath.endsWith("/")) {
                collectClassEntries(bundle, entries, entryPath);
            }
            else if (entryPath.endsWith(".class")) {
                try {
                    String classQName = entryPath.substring(0, entryPath.length() - 6).replace(File.separatorChar, '.');
                    if (classQName.startsWith("WEB-INF.classes.")) {
                        classQName = classQName.substring(16);
                    }
                    try {
                        Class entryClass = bundle.loadClass(classQName);
                        // If not activatorMode is true then there will be classes in this list already on the first
                        // call to this method. Therefore we skip duplicates.
                        if (!entries.contains(entryClass)) {
                            entries.add(entryClass);
                        }
                    }
                    catch (NullPointerException npe) {
                        this.activatorLogger.error(npe.getMessage(), npe);
                        // Felix (when used by Karaf) seems to have problems here for a perfectly good classQName!
                        // No, it is not null, if it where null it would explain this!
                        //
                        // It is:
                        //
                        //     se.natusoft.osgi.aps.userservice.config.UserServiceInstConfig.UserServiceInstance
                        //
                        // that cause this NPE. It belongs to APSSimpleUserServiceProvider and is a perfectly OK
                        // class that compiles flawlessly. The classQName also contains a perfectly correct reference
                        // to the class.
                        //
                        // This is the exception thrown:
                        //
                        //   Caused by: java.lang.NullPointerException
                        //   at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.findClass(BundleWiringImpl.java:2015)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.findClassOrResourceByDelegation(BundleWiringImpl.java:1501)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.access$400(BundleWiringImpl.java:75)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.loadClass(BundleWiringImpl.java:1955)[org.apache.felix.framework-4.2.1.jar:]
                        //   at java.lang.ClassLoader.loadClass(ClassLoader.java:358)[:1.7.0_60]
                        //   at org.apache.felix.framework.BundleWiringImpl.getClassByDelegation(BundleWiringImpl.java:1374)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.searchImports(BundleWiringImpl.java:1553)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.findClassOrResourceByDelegation(BundleWiringImpl.java:1484)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.access$400(BundleWiringImpl.java:75)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.loadClass(BundleWiringImpl.java:1955)[org.apache.felix.framework-4.2.1.jar:]
                        //   at java.lang.ClassLoader.loadClass(ClassLoader.java:358)[:1.7.0_60]
                        //   at java.lang.ClassLoader.defineClass1(Native Method)[:1.7.0_60]
                        //   at java.lang.ClassLoader.defineClass(ClassLoader.java:800)[:1.7.0_60]
                        //   at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.findClass(BundleWiringImpl.java:2279)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.findClassOrResourceByDelegation(BundleWiringImpl.java:1501)[org.apache.felix.framework-4.2.1.jar:]
                        //    at org.apache.felix.framework.BundleWiringImpl.access$400(BundleWiringImpl.java:75)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.loadClass(BundleWiringImpl.java:1955)[org.apache.felix.framework-4.2.1.jar:]
                        //   at java.lang.ClassLoader.loadClass(ClassLoader.java:358)[:1.7.0_60]
                        //   at org.apache.felix.framework.Felix.loadBundleClass(Felix.java:1844)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleImpl.loadClass(BundleImpl.java:937)[org.apache.felix.framework-4.2.1.jar:]
                        //   at se.natusoft.osgi.aps.tools.APSActivator.collectClassEntries(APSActivator.java:962)[104:aps-tools-lib:1.0.0]

                    }
                }
                catch (ClassNotFoundException | NoClassDefFoundError cnfe) {
                    this.activatorLogger.warn("Failed to load bundle class!", cnfe);
                }
            }
        }
    }

    //
    // Inner Classes
    //

    /**
     * Provides a Runnable for each entry class that scans the class for annotations and acts on them.
     */
    private class PerClassWorkRunnable implements Runnable {

        private Class entryClass;
        private BundleContext context;
        private InitMethods initMethods;

        public PerClassWorkRunnable(Class entryClass, BundleContext context, InitMethods initMethods) {
            this.entryClass = entryClass;
            this.context = context;
            this.initMethods = initMethods;
        }

        @Override
        public void run() {
            try {
                collectInjecteeAndServiceInstancesToManage(entryClass);
                doFieldInjectionsIntoManagedInstances(entryClass, context);
                doServiceRegistrationsOfManagedServiceInstances(entryClass, context);
                handleMethodInvocationsOnManagedInstances(entryClass, context, initMethods);
            }
            catch (Exception e) {
                activatorLogger.error("Failed to execute PerClassWorkRunnable!", e);
            }
        }
    }

    /**
     * Holds a set if init methods that should be called lastly, after all other setup is done.
     */
    private static class InitMethods implements Iterable<Tuple2<Method, Class>> {
        private List<Tuple2<Method, Class>> initMethods = new LinkedList<>();

        public void addInitMethod(Method method, Class clazz) {
            this.initMethods.add(new Tuple2<>(method, clazz));
        }

        @Override
        public Iterator<Tuple2<Method, Class>> iterator() {
            return this.initMethods.iterator();
        }

        public boolean isEmpty() {
            return this.initMethods.isEmpty();
        }

        public void clean() {
            this.initMethods = new LinkedList<>();
        }
    }

    /**
     * Implementations of this provide properties for each instance of a service to publish.
     */
    public interface InstanceFactory {
        //
        // Constants
        //

        /**
         * This property should be set with a colon separated list of fully qualified names of the service interfaces
         * to register the service instance with.
         */
        public static final String SERVICE_API_CLASSES_PROPERTY = "aps.service.api.class";

        //
        // Methods
        //

        /**
         * Returns a set of Properties for each instance.
         */
        List<Properties> getPropertiesPerInstance();
    }

    // The following are wrappers for the OSGi framework listeners: ServiceListener, BundleListener, and FrameworkListener.
    // The point of these is that they implement the listener interfaces, but in turn forwards listener calls to annotated
    // method of class that does not need to implement the interfaces.

    /**
     * This is implemented by all listener wrappers.
     */
    protected interface ListenerWrapper {
        public void start(BundleContext context);
        public void stop(BundleContext context);
    }

    /**
     * Listens on and forwards service events.
     */
    protected class ServiceListenerWrapper implements ListenerWrapper, ServiceListener {

        private Method method;
        private Object instance;

        public ServiceListenerWrapper(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }

        public void start(BundleContext context) {
            context.addServiceListener(this);
        }

        public void stop(BundleContext context) {
            context.removeServiceListener(this);
        }

        /**
         * Receives notification that a service has had a lifecycle change.
         *
         * @param event The <code>ServiceEvent</code> object.
         */
        @Override
        public void serviceChanged(ServiceEvent event) {
            try {
                method.invoke(instance, event);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                APSActivator.this.activatorLogger.error("Failed to invoke ServiceListener method [" + method + "]!", e);
            }
        }
    }

    /**
     * Listens on and forwards bundle events.
     */
    protected class BundleListenerWrapper implements ListenerWrapper, BundleListener {

        private Method method;
        private Object instance;

        public BundleListenerWrapper(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }

        public void start(BundleContext context) {
            context.addBundleListener(this);
        }

        public void stop(BundleContext context) {
            context.removeBundleListener(this);
        }

        /**
         * Receives notification that a bundle has had a lifecycle change.
         *
         * @param event The <code>BundleEvent</code> object.
         */
        @Override
        public void bundleChanged(BundleEvent event) {
            try {
                method.invoke(instance,event);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                APSActivator.this.activatorLogger.error("Failed to invoke ServiceListener method [" + method + "]!", e);
            }
        }
    }

    /**
     * Listens on and forwards framework events.
     */
    protected class FrameworkListenerWrapper implements ListenerWrapper, FrameworkListener {

        private Method method;
        private Object instance;

        public FrameworkListenerWrapper(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }

        public void start(BundleContext context) {
            context.addFrameworkListener(this);
        }

        public void stop(BundleContext context) {
            context.removeFrameworkListener(this);
        }

        /**
         * Receives notification of a general FrameworkEvent object.
         *
         * @param event The <code>FrameworkEvent</code> object.
         */
        @Override
        public void frameworkEvent(FrameworkEvent event) {
            try {
                method.invoke(instance,event);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                APSActivator.this.activatorLogger.error("Failed to invoke ServiceListener method [" + method + "]!", e);
            }
        }
    }

}
