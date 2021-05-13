package se.natusoft.aps.core;

import se.natusoft.aps.exceptions.APSNoServiceAvailableException;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * APS is currently using ServiceLoader and that is the plan for now. That said however,
 * I will not use java.util.ServiceLoader directly, but wrap it with this class, which
 * will be used and will use ServiceLoader internally.
 *
 * A Set of found services are delivered and no part of ServiceLoader itself is referenced
 * outside of this.
 *
 * I'm thereby locking the acquirement of services to this API but not provider of functionality.
 *
 * Why "APSServiceLocator" and not "APSServiceLoader" ? Because I think the term "locator" is
 * clearer, and more generic and does not directly associate to current implementation.
 */
public class APSServiceLocator {

    /** Keep same instance of a ServiceLoader for a service. */
    private final static Map<Class<?>, ServiceLoader<?>> loaders = new LinkedHashMap<>();

    /**
     * Returns a possibly cached loader.
     *
     * @param serviceApi Api to get services implementing.
     * @return A ServiceLoader.
     */
    private static <T> ServiceLoader<T> getLoader( Class<T> serviceApi ) {
        @SuppressWarnings( "unchecked" )
        ServiceLoader<T> loader = ( ServiceLoader<T> ) loaders.get( serviceApi );
        if ( loader == null ) {
            loader = ServiceLoader.load( serviceApi );
            loaders.put( serviceApi, loader );
        }

        return loader;
    }

    /**
     * Locates a set of services by service API class.
     *
     * @param serviceApi The class of the service interface.
     * @return A Set of found services.
     */
    public static <T> List<T> apsServices( Class<T> serviceApi ) {

        List<T> services = new LinkedList<>();
        getLoader( serviceApi ).forEach( services::add );

        if ( services.isEmpty() ) throw new APSNoServiceAvailableException( "No '" + serviceApi.getName() +
                "' service found!" );

        return services;
    }

    /**
     * Locates just one service instance.
     *
     * @param serviceApi The API class of the service to locate.
     * @return An instance of the service.
     * @exception APSNoServiceAvailableException if not found.
     */
    public static <T> T apsService( Class<T> serviceApi ) {
        List<T> services = apsServices( serviceApi );
        return services.get( 0 );
    }

    /**
     * Locates a set of services by service API and annotation.
     *
     * @param serviceApi The class of the service interface.
     * @param annotation An annotation to require the service to have.
     * @return A Set of matching services.
     * @exception APSNoServiceAvailableException if not found.
     */
    public static <T> List<T> apsServiceByAnnotation( Class<T> serviceApi, Class<Annotation> annotation ) {

        List<T> services = new LinkedList<>();
        getLoader( serviceApi ).forEach( s -> {
            if ( s.getClass().isAnnotationPresent( annotation ) ) {
                services.add( s );
            }
        } );

        if ( services.isEmpty() ) throw new APSNoServiceAvailableException( "No '" + serviceApi.getName() +
                "' service found!" );

        return services;
    }
}
