package se.natusoft.osgi.aps.tools;

import org.osgi.framework.*;
import se.natusoft.osgi.aps.tools.annotation.*;
import se.natusoft.osgi.aps.tools.exceptions.APSActivatorException;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This class can be specified as bundle activator in which case you use the following annotations:
 *
 * * **@APSOSGiServiceProvider** -
 *   This should be specified on a class that implements a service interface and should be registered as
 *   an OSGi service. _Please note_ that the first declared implemented interface is used as service interface!
 *
 * * **@APSOSGiService** -
 *   This should be specified on a field having a type of a service interface to have a service of that type
 *   injected, and continuously tracked. Any call to the service will throw an APSNoServiceAvailableException
 *   (runtime) if no service has become available before the specified timeout. It is also possible to have
 *   APSServiceTracker as field type in which case the underlying configured tracker will be injected instead.
 *
 * * **@APSInject** -
 *   This will have an instance injected. There will be a unique instance for each name specified with the
 *   default name of "default" being used in none is specified. There are 2 field types handled specially:
 *   BundleContext and APSLogger. A BundleContext field will get the bundles context injected. For an APSLogger
 *   instance the 'loggingFor' annotation property can be specified.
 *
 * * **@APSBundleStart** -
 *   This should be used on a method and will be called on bundle start. The method should take no arguments.
 *   If you need a BundleContext just declare a field of BundleContext type and it will be injected. The use
 *   of this annotation is only needed for things not supported by this activator. Please note that a method
 *   annotated with this annotation can be static!
 *
 * * **@APSBundleStop** -
 *   This should be used on a method and will be called on bundle stop. The method should take no arguments.
 *   This should probably be used if @APSBundleStart is used. Please note that a method annotated with this
 *   annotation can be static!
 *
 * All injected service instances for @APSOSGiService will be APSServiceTracker wrapped
 * service instances that will automatically handle services leaving and coming. They will throw
 * APSNoServiceAvailableException on timeout!
 *
 * Most methods are protected making it easy to subclass this class and expand on its functionality.
 */
public class APSActivator implements BundleActivator {

    //
    // Private Members
    //

    private APSLogger activatorLogger;

    private List<ServiceRegistration> services;
    private Map<String, APSServiceTracker> trackers;
    private Map<String, Object> namedInstances;
    private List<ShutdownMethod> shutdownMethods;

    private Map<Class, Object> managedInstances;

    //
    // Methods
    //

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
        this.services = new LinkedList<>();
        this.trackers = new HashMap<>();
        this.namedInstances = new HashMap<>();
        this.shutdownMethods = new LinkedList<>();
        this.managedInstances = new HashMap<>();

        this.activatorLogger = new APSLogger(System.out);
        this.activatorLogger.setLoggingFor("APSActivator");
        this.activatorLogger.start(context);

        Bundle bundle = context.getBundle();

        List<String> classEntryPaths = new LinkedList<>();
        getClassEntries(bundle, classEntryPaths, "/");

        for (String entryPath : classEntryPaths) {
            try {
                Class entryClass =
                        bundle.loadClass(
                                entryPath.substring(0, entryPath.length() - 6).replace(File.separatorChar, '.')
                        );
                handleFieldInjections(entryClass, context);
                handleServiceRegistrations(entryClass, context);
                handleMethods(entryClass, context);
            }
            catch (ClassNotFoundException cnfe) {
                this.activatorLogger.error("Failed to get class for bundle class entry!", cnfe);
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

        for (ShutdownMethod shutdownMethod : this.shutdownMethods) {
            try {
                shutdownMethod.method.invoke(shutdownMethod.object, null);

                this.activatorLogger.info("Called bundle shutdown method '" + shutdownMethod.object.getClass() +
                        "." + shutdownMethod.method.getName() + "() for bundle: " +
                        context.getBundle().getSymbolicName() + "!");
            }
            catch (Exception e) {
                this.activatorLogger.error("Bundle stop problem!", e);
                failure = e;
            }
        }

        for (ServiceRegistration serviceRegistration : this.services) {
            try {
                serviceRegistration.unregister();
            }
            catch (Exception e) {
                this.activatorLogger.error("Bundle stop problem!", e);
                failure = e;
            }
        }

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

        if (failure != null) {
            throw new APSActivatorException("Bundle stop not entirely successful!", failure);
        }
    }

    // ---- Service Registration ---- //

    protected void handleServiceRegistrations(Class managedClass, BundleContext context) {
        APSOSGiServiceProvider serviceProvider = (APSOSGiServiceProvider)managedClass.getAnnotation(APSOSGiServiceProvider.class);
        if (serviceProvider != null) {
            Properties serviceProps = new Properties();
            for (OSGiProperty prop : serviceProvider.properties()) {
                serviceProps.setProperty(prop.name(), prop.value());
            }

            serviceProps.put(Constants.SERVICE_PID, managedClass.getName());
            Class[] interfaces = managedClass.getInterfaces();
            if (interfaces != null && interfaces.length >= 1) {
                ServiceRegistration serviceReg =
                        context.registerService(
                                interfaces[0].getName(),
                                getManagedInstance(managedClass),
                                serviceProps
                        );

                this.services.add(serviceReg);
                this.activatorLogger.info("Registered '" + managedClass.getName() + "' as a service provider of '" +
                        interfaces[0].getName() + "' for bundle: " + context.getBundle().getSymbolicName() + "!");
            }
        }
    }

    // ---- Field Injections ---- //

    protected void handleFieldInjections(Class managedClass, BundleContext context) {
        for (Field field : managedClass.getDeclaredFields()) {
            handleServiceInjections(field, managedClass, context);
            handleInstanceInjections(field, managedClass, context);
        }
    }

    protected void handleServiceInjections(Field field, Class managedClass, BundleContext context) {
        APSOSGiService service = field.getAnnotation(APSOSGiService.class);
        if (service != null) {
            String trackerKey = field.getType().getName() + service.additionalSearchCriteria();
            APSServiceTracker tracker = this.trackers.get(trackerKey);
            if (tracker == null) {
                tracker = new APSServiceTracker<>(context, field.getType(), service.additionalSearchCriteria(),
                        service.timeout());
                this.trackers.put(trackerKey, tracker);
            }
            tracker.start();
            Object managedInstance = getManagedInstance(managedClass);
            if (field.getType().equals(APSServiceTracker.class)) {
                injectObject(managedInstance, tracker, field);
            }
            else {
                injectObject(managedInstance, tracker.getWrappedService(), field);
            }

            this.activatorLogger.info("Injected tracked service '" + field.getType().getName() +
                    (service.additionalSearchCriteria().length() > 0 ? " " + service.additionalSearchCriteria() : "") +
                    "' " + "into '" + managedClass.getName() + "." + field.getName() + "' for bundle: " +
                    context.getBundle().getSymbolicName() + "!");
        }
    }

    protected void handleInstanceInjections(Field field, Class managedClass, BundleContext context) {
        APSInject log = field.getAnnotation(APSInject.class);
        if (log != null) {
            String namedInstanceKey = log.name() + field.getType().getName();
            Object namedInstance = this.namedInstances.get(namedInstanceKey);
            if (namedInstance == null) {
                if (field.getType().equals(APSLogger.class)) {
                    namedInstance = new APSLogger(System.out);
                    if (log.loggingFor().length() > 0) {
                        ((APSLogger)namedInstance).setLoggingFor(log.loggingFor());
                    }
                    ((APSLogger)namedInstance).start(context);
                }
                else if (field.getType().equals(BundleContext.class)) {
                    namedInstance = context;
                }
                else {
                    try {
                        namedInstance = field.getType().newInstance();
                    }
                    catch (InstantiationException | IllegalAccessException e) {
                        throw new APSActivatorException("Failed to instantiate: " + managedClass.getName() +
                                "." + field.getName() + "!", e);
                    }
                }
                this.namedInstances.put(namedInstanceKey, namedInstance);
            }
            Object managedInstance = getManagedInstance(managedClass);
            injectObject(managedInstance, namedInstance, field);

            this.activatorLogger.info("Injected '" + namedInstance.getClass().getName() +
                    "' instance for name '" + log.name() + "' " +
                    "into '" + managedClass.getName() + "." + field.getName() + "' for bundle: " +
                    context.getBundle().getSymbolicName() + "!");
        }
    }

    // ---- Methods ---- //

    protected void handleMethods(Class managedClass, BundleContext context) {
        for (Method method : managedClass.getDeclaredMethods()) {
            handleStartupMethods(method, managedClass, context);
            handleShutdownMethods(method, managedClass, context);
        }
    }

    protected void handleStartupMethods(Method method, Class managedClass, BundleContext context) {
        APSBundleStart bundleStart = method.getAnnotation(APSBundleStart.class);
        if (bundleStart != null) {
            if (method.getParameterTypes().length > 0) {
                throw new APSActivatorException("An @APSBundleStart method must take no parameters! [" +
                    managedClass.getName() + "." + method.getName() + "(?)]");
            }
            Object managedInstance = null;
            if (!Modifier.isStatic(method.getModifiers())) {
                managedInstance = getManagedInstance(managedClass);
            }
            try {
                method.invoke(managedInstance, null);

                this.activatorLogger.info("Called bundle start method '" + managedClass.getName() +
                        "." + method.getName() + "()' for bundle: " + context.getBundle().getSymbolicName() + "!");
            } catch (IllegalAccessException e) {
                throw new APSActivatorException("Failed to call start method! [" +
                           managedClass.getName() + "." + method.getName() + "()]", e);
            }
            catch (InvocationTargetException ite) {
                throw new APSActivatorException("Called start method failed!", ite.getCause());
            }
        }
    }

    protected void handleShutdownMethods(Method method, Class managedClass, BundleContext context) {
        APSBundleStop bundleStop = method.getAnnotation(APSBundleStop.class);
        if (bundleStop != null) {
            ShutdownMethod shutdownMethod = new ShutdownMethod();
            shutdownMethod.method = method;
            shutdownMethod.object = null;
            if (!Modifier.isStatic(method.getModifiers())) {
                shutdownMethod.object = getManagedInstance(managedClass);
            }

            this.shutdownMethods.add(shutdownMethod);
        }
    }

    // ---- Support ---- //

    /**
     * Returns a managed instance of a class.
     *
     * @param managedClass The managed class to get instance for.
     */
    protected Object getManagedInstance(Class managedClass) {
        Object managedInstance = this.managedInstances.get(managedClass);
        if (managedInstance == null) {
            try {
                managedInstance = managedClass.newInstance();
                this.managedInstances.put(managedClass, managedInstance);
                this.activatorLogger.info("Instantiated '" + managedClass.getName() + "': "+ managedInstance);
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new APSActivatorException("Failed to instantiate activator managed class!", e);
            }
        }

        return managedInstance;
    }

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
    protected void getClassEntries(Bundle bundle, List<String> entries, String startPath) {
        Enumeration<String> entryPathEnumeration = bundle.getEntryPaths(startPath);
        while (entryPathEnumeration.hasMoreElements()) {
            String entryPath = entryPathEnumeration.nextElement();
            if (entryPath.endsWith("/")) {
                getClassEntries(bundle, entries, entryPath);
            }
            else if (entryPath.endsWith(".class")) {
                entries.add(entryPath);
            }
        }
    }

    //
    // Inner Classes
    //

    private class ShutdownMethod {
        private Method method;
        private Object object;
    }
}
