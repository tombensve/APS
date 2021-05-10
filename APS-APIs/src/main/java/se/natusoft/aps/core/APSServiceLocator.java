package se.natusoft.aps.core;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * APS is currently using ServiceLoader as is, and that is the plan for now. That said
 * however I will not use java.util.ServiceLoader directly, but wrap it with this class,
 * which will be used and will use ServiceLoader internally.
 */
public class APSServiceLocator<Service> {

    /**
     * Locates a set of services by service API class.
     *
     * @param service The class of the service interface.
     * @return A Set of found services.
     */
    public Set<Service> locate( Class<Service> service ) {

        Set<Service> svc = new LinkedHashSet<>();
        ServiceLoader.load( service ).forEach( svc::add );

        return svc;
    }

    /**
     * Locates a set of services by service API and annotation.
     *
     * @param service The class of the service interface.
     * @param annotation An annotation to require the service to have.
     * @return A Set of matching services.
     */
    public Set<Service> locate( Class<Service> service, Class<Annotation> annotation ) {

        Set<Service> svc = new LinkedHashSet<>();
        ServiceLoader.load( service ).forEach( s -> {
            if ( s.getClass().isAnnotationPresent( annotation ) ) {
                svc.add( s );
            }
        } );

        return svc;
    }
}
