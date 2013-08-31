/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         0.9.2
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
import se.natusoft.osgi.aps.tools.exceptions.APSActivatorException;
import se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable;
import se.natusoft.osgi.aps.tools.tracker.OnTimeout;
import se.natusoft.osgi.aps.tools.tuples.Tuple2;
import se.natusoft.osgi.aps.tools.tuples.Tuple3;
import se.natusoft.osgi.aps.tools.tuples.Tuple4;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

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

    /** This means that this class will find all annotated classes in a bundle and manage those. */
    public static final boolean ACTIVATOR_MODE = true;

    /**
     * This means that already existing instances are provided in constructor and/or addManagedInstance(...).
     * This way you can manage injections in a servlet for example. Please note that in addition to the
     * already provided instances like servlets or other framework instances all classes in the bundle
     * will still be scanned and handled! It will ofcourse sort out any duplicates due to the already
     * passed instances can be instances of a bundle class.
     */
    public static final boolean EXISTING_INSTANCES_MODE = false;

    //
    // Private Members
    //

    private APSLogger activatorLogger;

    private List<ServiceRegistration> services;
    private Map<String, APSServiceTracker> trackers;
    private Map<String, Object> namedInstances;
    private List<Tuple2<Method, Object>> shutdownMethods;
    private List<Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>>> requiredServices;
    private Map<Class, List<Object>> managedInstances;
    private List<ListenerWrapper> listeners;

    private boolean supportsRequired = true;

    private BundleContext context;

    private boolean activatorMode = ACTIVATOR_MODE;

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
        this.managedInstances = Collections.synchronizedMap(new HashMap<Class, List<Object>>());
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
        List<Object> instances = new LinkedList<>();
        instances.add(instance);
        this.managedInstances.put(instance.getClass(), instances);
    }

    /**
     * Called on start() to reset internal instances.
     */
    protected void initMembers() {
        if (this.services == null) {
            this.services = Collections.synchronizedList(new LinkedList<ServiceRegistration>());
            this.trackers = Collections.synchronizedMap(new HashMap<String, APSServiceTracker>());
            this.namedInstances = Collections.synchronizedMap(new HashMap<String, Object>());
            this.shutdownMethods = Collections.synchronizedList(new LinkedList<Tuple2<Method, Object>>());
            if (this.activatorMode) this.managedInstances =
                    Collections.synchronizedMap(new HashMap<Class, List<Object>>());
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

        for (Class entryClass : classEntries) {
            OSGiServiceProvider serviceProvider = (OSGiServiceProvider)entryClass.getAnnotation(OSGiServiceProvider.class);
            if (serviceProvider != null && serviceProvider.threadStart()) {
                new StartThread(entryClass, context).start();
            }
            else {
                handleServiceInstances(entryClass, context);
                handleFieldInjections(entryClass, context);
                handleServiceRegistrations(entryClass, context);
                handleMethods(entryClass, context);
            }
        }
    }

    private class StartThread extends Thread {

        private Class entryClass;
        private BundleContext context;

        public StartThread(Class entryClass, BundleContext context) {
            this.entryClass = entryClass;
            this.context = context;
        }

        @Override
        public void run() {
            try {
                handleServiceInstances(this.entryClass, this.context);
                handleFieldInjections(this.entryClass, this.context);
                handleServiceRegistrations(this.entryClass, this.context);
                handleMethods(this.entryClass, this.context);
            }
            catch (Exception e) {
                APSActivator.this.activatorLogger.error("Threaded start: Failed to manage class: " + this.entryClass, e);
            }
        }
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
        Exception failure = null;

        this.activatorLogger.info("Stopping APSActivator for bundle '" + context.getBundle().getSymbolicName() +
                "' with activatorMode: " + this.activatorMode);

        for (ListenerWrapper listenerWrapper : this.listeners) {
            listenerWrapper.stop(context);
        }
        this.listeners = null;

        for (Tuple2<Method, Object> shutdownMethod : this.shutdownMethods) {
            try {
                shutdownMethod.t1.invoke(shutdownMethod.t2, null);

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
     * This is the first thing done to instantiate all service instances needed, so that they all can be injected later.
     *
     * @param managedClass The manages class to create instances of.
     * @param context The bundles context.
     * @throws Exception
     */
    protected void handleServiceInstances(Class managedClass, BundleContext context) throws Exception {
        OSGiServiceProvider serviceProvider = (OSGiServiceProvider)managedClass.getAnnotation(OSGiServiceProvider.class);
        if (serviceProvider != null) {
            int noInstances = 1;

            if (serviceProvider.instances().length > 0) {
                noInstances = serviceProvider.instances().length;
            }
            else if (!serviceProvider.instanceFactoryClass().equals(InstanceFactory.class)) {
                InstanceFactory instanceFactory = (InstanceFactory)getManagedInstance(serviceProvider.instanceFactoryClass());
                if (instanceFactory == null) {
                    instanceFactory = serviceProvider.instanceFactoryClass().newInstance();
                }
                noInstances = instanceFactory.getPropertiesPerInstance().size();
            }

            // This will just instantiate and cache the number of instances. Right now we don't care
            // about the instances themselves.
            getManagedInstances(managedClass, noInstances);
        }
    }

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
    protected void handleServiceRegistrations(final Class managedClass, final BundleContext context) throws Exception {
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
                    if (requiredService.t2.equals(managedClass) && requiredService.t3 == false) {
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
     * @param serviceRegs The list to save all service registrations to for later unregistration.
     * @throws Exception
     */
    protected void registerServices(Class managedClass, BundleContext context, List<ServiceRegistration> serviceRegs) throws Exception {
        OSGiServiceProvider serviceProvider = (OSGiServiceProvider)managedClass.getAnnotation(OSGiServiceProvider.class);
        if (serviceProvider != null) {
            List<Tuple3<Properties, Object, List<String>>> serviceInstData = new LinkedList<>();

            if (serviceProvider.properties().length > 0) {
                Tuple3<Properties, Object, List<String>> sd = new Tuple3<>();
                sd.t1 = osgiPropertiesToProperties(serviceProvider.properties());
                serviceInstData.add(sd);
            }
            else if (serviceProvider.instances().length > 0) {
                for (OSGiServiceInstance serviceInst : serviceProvider.instances()) {
                    Tuple3<Properties, Object, List<String>> sd = new Tuple3<>();
                    sd.t1 = osgiPropertiesToProperties(serviceInst.properties());
                    serviceInstData.add(sd);
                }
            }
            else if (!serviceProvider.instanceFactoryClass().equals(InstanceFactory.class)) {
                InstanceFactory instanceFactory = (InstanceFactory)getManagedInstance(serviceProvider.instanceFactoryClass());
                if (instanceFactory == null) {
                    instanceFactory = serviceProvider.instanceFactoryClass().newInstance();
                }
                for (Properties props : instanceFactory.getPropertiesPerInstance()) {
                    Tuple3<Properties, Object, List<String>> sd = new Tuple3<>();
                    sd.t1 = props;
                    serviceInstData.add(sd);
                }
            }
            else {
                Tuple3<Properties, Object, List<String>> sd = new Tuple3<>();
                sd.t1 = new Properties();
                serviceInstData.add(sd);
            }

            // The number of managedInstances and serviceInstData will always be the same. They both originate
            // from the same information.
            List<Object> managedInstances = getManagedInstances(managedClass);
            for (int i = 0; i < serviceInstData.size(); i++) {

                List<String> serviceAPIs = new LinkedList<>();

                if (serviceProvider.serviceAPIs().length > 0) {
                    for (Class svcAPI : serviceProvider.serviceAPIs()) {
                        serviceAPIs.add(svcAPI.getName());
                    }
                }
                else if (serviceProvider.instances().length > 0) {
                    OSGiServiceInstance svsInstAnn = serviceProvider.instances()[i];
                    for (Class svcAPI : svsInstAnn.serviceAPIs()) {
                        serviceAPIs.add(svcAPI.getName());
                    }
                }
                else if (!serviceProvider.instanceFactoryClass().equals(InstanceFactory.class)) {
                    String svcAPIList = serviceInstData.get(i).t1.getProperty(InstanceFactory.SERVICE_API_CLASSES_PROPERTY);
                    if (svcAPIList != null) {
                        for (String svcAPI : svcAPIList.split(":")) {
                            serviceAPIs.add(svcAPI);
                        }
                    }
                    else {
                        Class[] interfaces = managedClass.getInterfaces();
                        if (interfaces != null && interfaces.length > 0) {
                            serviceAPIs.add(interfaces[0].getName());
                        }
                    }
                }
                else {
                    Class[] interfaces = managedClass.getInterfaces();
                    if (interfaces != null && interfaces.length > 0) {
                        serviceAPIs.add(interfaces[0].getName());
                    }
                }

                Tuple3<Properties, Object, List<String>> sd = serviceInstData.get(i);
                sd.t3 = serviceAPIs;
                sd.t2 = managedInstances.get(i);
            }

            for (Tuple3<Properties, Object, List<String>> sd : serviceInstData) {
                sd.t1.put(Constants.SERVICE_PID, managedClass.getName());
                if (!sd.t3.isEmpty()) {
                    injectInstanceProps(sd.t2, sd.t1);
                    for (String svcAPI : sd.t3) {
                        ServiceRegistration serviceReg =
                                context.registerService(
                                        svcAPI,
                                        sd.t2,
                                        sd.t1
                                );

                        serviceRegs.add(serviceReg);
                        this.activatorLogger.info("Registered '" + managedClass.getName() + "' as a service provider of '" +
                                svcAPI + "' for bundle: " + context.getBundle().getSymbolicName() + "!");
                    }
                }
                else {
                    throw new IllegalArgumentException("The @OSGiServiceProvider annotated service of class '" +
                        managedClass.getName() + "' does not implement a service interface!");
                }
            }
        }
    }

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
    protected void handleFieldInjections(Class managedClass, BundleContext context) {
        for (Field field : managedClass.getDeclaredFields()) {
            handleServiceInjections(field, managedClass, context);
            handleInstanceInjections(field, managedClass, context);
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
    protected void handleServiceInjections(Field field, Class managedClass, BundleContext context) {
        OSGiService service = field.getAnnotation(OSGiService.class);
        if (service != null) {
            String trackerKey = field.getType().getName() + service.additionalSearchCriteria();
            APSServiceTracker tracker = this.trackers.get(trackerKey);

            if (tracker == null) {
                tracker = new APSServiceTracker<>(context, field.getType(), service.additionalSearchCriteria(),
                        service.timeout());
                this.trackers.put(trackerKey, tracker);
            }
            tracker.start();

            List<Object> managedInstances = getManagedInstances(managedClass);
            for (Object managedInstance : managedInstances) {
                if (field.getType().equals(APSServiceTracker.class)) {
                    injectObject(managedInstance, tracker, field);
                }
                else {
                    injectObject(managedInstance, tracker.getWrappedService(), field);
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
                    context.getBundle().getSymbolicName() + " for " + managedInstances.size() + " instance(s)!");
        }
    }

    /**
     * Handles injections of APSLogger, BundleContext, or other class types with default constructor.
     *
     * @param field The field to inject into.
     * @param managedClass Used to lookup or create an instance of this class to inject into.
     * @param context The bundle context.
     */
    protected void handleInstanceInjections(Field field, Class managedClass, BundleContext context) {
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
                    namedInstance = getManagedInstance(field.getType());
                }
                this.namedInstances.put(namedInstanceKey, namedInstance);
            }
            else {
                this.activatorLogger.info("Got named instance for key '" + namedInstanceKey + "': " +
                        namedInstance.getClass().getName());
            }

            List<Object> managedInstances = getManagedInstances(managedClass);
            for (Object managedInstance : managedInstances) {
                injectObject(managedInstance, namedInstance, field);
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
     */
    protected void handleMethods(Class managedClass, BundleContext context) {
        for (Method method : managedClass.getDeclaredMethods()) {
            handleStartupMethods(method, managedClass, context);
            handleShutdownMethods(method, managedClass, context);
            handleListenerMethods(method, managedClass, context);
        }
    }

    /**
     * Handles methods annotated with @APSBundleStartup.
     *
     * @param method The annotated method to call.
     * @param managedClass Used to lookup or create an instance of this class containing the method to call.
     * @param context The bundle context.
     */
    protected void handleStartupMethods(final Method method, final Class managedClass, final BundleContext context) {
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
                                managedInstance = getManagedInstance(managedClass);
                            }
                            method.invoke(managedInstance, null);

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
                        managedInstance = getManagedInstance(managedClass);
                    }
                    method.invoke(managedInstance, null);

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
     * @param context The bundle context.
     */
    protected void handleShutdownMethods(Method method, Class managedClass, BundleContext context) {
        BundleStop bundleStop = method.getAnnotation(BundleStop.class);
        if (bundleStop != null) {
            Tuple2<Method, Object> shutdownMethod = new Tuple2<>(method, null);
            if (!Modifier.isStatic(method.getModifiers())) {
                shutdownMethod.t2 = getManagedInstance(managedClass);
            }

            this.shutdownMethods.add(shutdownMethod);
        }
    }

    protected void handleListenerMethods(Method method, Class managedClass, BundleContext context) {
        se.natusoft.osgi.aps.tools.annotation.activator.ServiceListener serviceListener =
                method.getAnnotation(se.natusoft.osgi.aps.tools.annotation.activator.ServiceListener.class);
        if (serviceListener != null) {
            ServiceListenerWrapper serviceListenerWrapper =
                    new ServiceListenerWrapper(method, getManagedInstance(managedClass));
            serviceListenerWrapper.start(context);
            this.listeners.add(serviceListenerWrapper);
        }

        se.natusoft.osgi.aps.tools.annotation.activator.BundleListener bundleListener =
                method.getAnnotation(se.natusoft.osgi.aps.tools.annotation.activator.BundleListener.class);
        if (bundleListener != null) {
            BundleListenerWrapper bundleListenerWrapper =
                    new BundleListenerWrapper(method, getManagedInstance(managedClass));
            bundleListenerWrapper.start(context);
            this.listeners.add(bundleListenerWrapper);
        }

        se.natusoft.osgi.aps.tools.annotation.activator.FrameworkListener frameworkListener =
                method.getAnnotation(se.natusoft.osgi.aps.tools.annotation.activator.FrameworkListener.class);
        if (frameworkListener != null) {
            FrameworkListenerWrapper frameworkListenerWrapper =
                    new FrameworkListenerWrapper(method, getManagedInstance(managedClass));
            frameworkListenerWrapper.start(context);
            this.listeners.add(frameworkListenerWrapper);
        }
    }

    // ---- Support ---- //

    /**
     * Returns a managed instance of a class.
     *
     * @param managedClass The managed class to get instance for.
     */
    protected Object getManagedInstance(Class managedClass) {
        return getManagedInstances(managedClass, 1).get(0);
    }

    /**
     * Returns previously created instances.
     *
     * @param managedClass The managed class to get instances for.
     */
    protected List<Object> getManagedInstances(Class managedClass) {
        return getManagedInstances(managedClass, -1);
    }

    /**
     * Returns a managed instance of a class.
     *
     * @param managedClass The managed class to get instance for.
     * @param size The number of instances.
     */
    protected List<Object> getManagedInstances(Class managedClass, int size) {
        List<Object> managedInstances = this.managedInstances.get(managedClass);
        if (managedInstances == null) {
            managedInstances = new LinkedList<>();
        }

        if (managedInstances.isEmpty()) {
            if (size == -1) throw new IllegalStateException("Expected instances are not available!");
            try {
                for (int i = 0; i < size; i++) {
                    Object managedInstance = managedClass.newInstance();
                    managedInstances.add(managedInstance);
                    this.managedInstances.put(managedClass, managedInstances);
                    this.activatorLogger.info("Instantiated '" + managedClass.getName() + "': "+ managedInstance);
                }
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new APSActivatorException("Failed to instantiate activator managed class!", e);
            }
        }

        return managedInstances;
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
        Enumeration<String> entryPathEnumeration = bundle.getEntryPaths(startPath);
        while (entryPathEnumeration.hasMoreElements()) {
            String entryPath = entryPathEnumeration.nextElement();
            if (entryPath.endsWith("/")) {
                collectClassEntries(bundle, entries, entryPath);
            }
            else if (entryPath.endsWith(".class")) {
                try {
                    String classQName = entryPath.substring(0, entryPath.length() - 6).replace(File.separatorChar, '.');
                    if (classQName.startsWith("WEB-INF.classes.")) {
                        classQName = classQName.substring(16);
                    }
                    Class entryClass = bundle.loadClass(classQName);
                    // If not activatorMode is true then there will be classes in this list already on the first
                    // call to this method. Therefore we skip duplicates.
                    if (!entries.contains(entryClass)) {
                        entries.add(entryClass);
                    }
                }
                catch (ClassNotFoundException cnfe) {
                    this.activatorLogger.warn("Failed to load bundle class!", cnfe);
                }
                catch (NoClassDefFoundError ncdfe) {
                    this.activatorLogger.warn("Failed to load bundle class!", ncdfe);
                }
            }
        }
    }

    //
    // Inner Classes
    //

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

    protected interface ListenerWrapper {
        public void start(BundleContext context);
        public void stop(BundleContext context);
    }

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
                method.invoke(instance,event);
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                APSActivator.this.activatorLogger.error("Failed to invoke ServiceListener method [" + method + "]!", e);
            }
        }
    }

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
