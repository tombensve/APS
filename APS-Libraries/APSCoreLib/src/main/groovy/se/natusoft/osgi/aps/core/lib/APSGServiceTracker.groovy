package se.natusoft.osgi.aps.core.lib

import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.tracker.WithService

import java.util.concurrent.TimeUnit

/**
 * This extends APSServiceTracker and makes it more Groovy.
 *
 * @param < Service >
 */
@SuppressWarnings("GroovyUnusedDeclaration")
class APSGServiceTracker<Service> extends APSServiceTracker<Service> {

    //
    // Constructors
    //

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * __Note:__ This instance will fail immediately if tracked service is not available!
     *
     * @param context The bundles context.
     * @param aClass The class of the service to track.
     */
    APSGServiceTracker( BundleContext context, Class aClass ) {
        super( context, aClass )
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param aClass The class of the service to track.
     * @param timeout The time in seconds to wait for a service to become available. Setting timeout to 0 (or lower) will have
     *                the effect of not waiting at all, and will fail immediately if tracked service is not available. But you
     *                should consider using a constructor that does not supply a timeout instead in that case.
     */
    APSGServiceTracker( BundleContext context, Class aClass, int timeout ) {
        super( context, aClass, timeout )
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param aClass The class of the service to track.
     * @param timeout The time to wait for a service to become available. Setting timeout to 0 (or lower) will have
     *                the effect of not waiting at all, and will fail immediately if tracked service is not available.
     *                But you should consider using a constructor that does not supply a timeout instead in that case.
     * @param timeUnit The unit of time in timeout.
     */
    APSGServiceTracker( BundleContext context, Class aClass, long timeout, TimeUnit timeUnit ) {
        super( context, aClass, timeout, timeUnit )
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param aClass The class of the service to track.
     * @param timeout The time to wait for a service to become available. Formats: "5 min[utes]" / "300 sec[onds]" /
     *                "300000 mili[seconds]" / "forever". Setting timeout value to 0 (or lower) will have the effect
     *                of not waiting at all, and will fail immediately if tracked service is not available. But you
     *                should consider using a constructor that does not supply a timeout instead in that case.
     */
    APSGServiceTracker( BundleContext context, Class aClass, String timeout ) {
        super( context, aClass, timeout )
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param aClass The class of the service to track.
     * @param additionalSearchCriteria An LDAP search string not including the service! The final search string will
     *                                 be "(&(objectClass=service)additionalSearchCriteria)". This parameter should
     *                                 thereby always start with an '(' and end with an ')'!
     * @param timeout The time to wait for a service to become available. Formats: "5 min[utes]" / "300 sec[onds]" /
     *                "300000 mili[seconds]" / "forever". Setting timeout value to 0 (or lower) will have the effect
     *                of not waiting at all, and will fail immediately if tracked service is not available. But you
     *                should consider using a constructor that does not supply a timeout instead in that case.
     */
    APSGServiceTracker( BundleContext context, Class aClass, String additionalSearchCriteria, String timeout ) {
        super( context, aClass, additionalSearchCriteria, timeout )
    }

    /**
     * Creates a new _APSServiceTracker_ instance.
     *
     * @param context The bundles context.
     * @param aClass The class of the service to track.
     * @param additionalSearchCriteria An LDAP search string not including the service! The final search string will
     *                                 be "(&(objectClass=service)additionalSearchCriteria)". This parameter should
     *                                 thereby always start with an '(' and end with an ')'!
     * @param timeout The time to wait for a service to become available. Setting timeout to 0 (or lower) will have
     *                the effect of not waiting at all, and will fail immediately if tracked service is not available.
     *                But you should consider using a constructor that does not supply a timeout instead in that case.
     * @param timeUnit The unit of time in timeout.
     */
    APSGServiceTracker( BundleContext context, Class aClass, String additionalSearchCriteria, long timeout, TimeUnit timeUnit ) {
        super( context, aClass, additionalSearchCriteria, timeout, timeUnit )
    }

    //
    // Methods
    //

    /**
     * Does the same as withAllAvailableServices(...) but using a Groovy closure as callback.
     *
     * @param handler The closure to call.
     */
    void g_withAllAvailableServices( Closure handler ) {
        super.withAllAvailableServices( new WithService<Service>() {
            void withService( Service service, Object... args ) throws Exception {
                handler.call( service )
            }
        } )
    }

    /**
     * Does the same as withService(...) but using a Groovy closure as callback.
     *
     * @param handler The closure to call.
     */
    void g_withService( Closure handler ) {
        super.withService( new WithService<Service>() {
            void withService( Service service, Object... args ) throws Exception {
                handler.call( service )
            }
        } )
    }

    /**
     * Does the same as withServiceIfAvailable(...) but using a Groovy closure as callback.
     *
     * @param handler The closure to call.
     */
    void g_withServiceIfAvailable( Closure handler ) {
        super.withServiceIfAvailable( new WithService<Service>() {
            void withService( Service service, Object... args ) throws Exception {
                handler.call( service )
            }
        } )
    }
}
