package se.natusoft.aps.core;

import se.natusoft.aps.core.annotation.APSProperty;
import se.natusoft.aps.core.annotation.APSService;
import se.natusoft.aps.core.annotation.APSServiceProvider;
import se.natusoft.osgi.aps.exceptions.APSStartException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This manages and keeps track of services.
 *
 * APS started out using OSGi, but for many reasons I will not list here I moved to
 * a simpler service architecture. I'm basically using `java.util.ServiceLoader`.
 * But like OSGi APS allows for multiple implementations of service APIs. Each
 * service implementation provides a set of properties using APS specific annotations.
 * This is the reason for this class and not using ServiceLoader directly. Each
 * implementation provides properties, and this class allows for looking up a service
 * using a primitive query on a service properties. Clients might want to use all
 * implementations or a subset or just one.
 *
 * Do note that APSService is a base interface that all APS services implements!
 * It is used to represent a service in generic form.
 *
 * Actually APSService is another reason for this class and not using ServiceLoader
 * directly. It contains a start() and stop() method where start() is called on
 * first use, and stop() is called on shutdown.
 */
public class APSServiceLoader<S> {

    //
    // Private Members
    //

    /** Our singleton instance. */
    private static final APSServiceLoader<?> instance = new APSServiceLoader<>();

    /** Holds all loaded services. */
    private final Map<Class<S>, List<APSServiceEntry<S>>> loaded = new LinkedHashMap<>();

    //
    // Methods
    //

    /**
     * @return the singleton instance.
     */
    public static APSServiceLoader<?> getInstance() {
        return instance;
    }


    /**
     * Returns a list of APSServiceEntry instances holding both service and properties.
     *
     * This caches already loaded instances.
     *
     * @param serviceAPI The interface class of the service.
     *
     * @return A list of implementations of the service. Note that each implementation has a
     * personal set of properties that can be queried.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private List<APSServiceEntry<S>> loadInternalServiceEntries( Class<S> serviceAPI ) {
        List<APSServiceEntry<S>> serviceProviders = this.loaded.get( serviceAPI );

        // TODO: Something fails in here ...

        if ( serviceProviders == null ) {

            ServiceLoader<S> availableServices = ServiceLoader.load( serviceAPI );
            serviceProviders = new LinkedList<>();
            this.loaded.put( serviceAPI, serviceProviders );

            for ( S service : availableServices ) {

                // Copy annotated properties to a HashMap that we save together with the service instance.
                Map<String, String> mprops = new LinkedHashMap<>();
                for ( APSProperty prop : service.getClass().getAnnotation( APSServiceProvider.class ).properties() ) {
                    mprops.put( prop.name(), prop.value() );
                }

                serviceProviders.add( new APSServiceEntry<>( mprops, service ) );
            }
        }

        return serviceProviders;
    }

    /**
     * This takes the passed injecte instance and injects all @APSService annotated fields found with loaded
     * service.
     *
     * Call this in constructor passing 'this' as argument.
     *
     * If field is an array all found services will be injected otherwise only the first even if there
     * are more than one.
     *
     * See this as convenience functionality! If this is used, do not call anything else in this class!
     *
     * @param injecte     The instance to inject.
     */
    public void injectServices( Object injecte ) {
        injectServices( injecte, null );
    }

    /**
     * This takes the passed injecte instance and injects all @APSService annotated fields found with loaded
     * service.
     *
     * Call this in constructor passing 'this' as argument.
     *
     * If field is an array all found services will be injected otherwise only the first even if there
     * are more than one.
     *
     * See this as convenience functionality! If this is used, do not call anything else in this class!
     *
     * @param injecte     The instance to inject.
     * @param filterQuery Filters injected services.
     */
    public void injectServices( Object injecte, String filterQuery ) {

        System.err.println("injectee=" + injecte);

        for ( Field field : injecte.getClass().getDeclaredFields() ) {

            System.err.println("Field: " + field.getName());

            field.setAccessible( true );

            APSService apsService = field.getAnnotation( APSService.class );

            System.err.println("apsService=" + apsService);

            // This however have the above annotation!
            Annotation[] annotations = field.getAnnotations();
            System.out.println( annotations[0].toString());
            Annotation annotation = annotations[ 0 ];
            System.out.println(">" + annotation.annotationType() + "<");


            if ( apsService != null ) {

                if ( field.getType().isInterface() ) {

                    field.setAccessible( true );

                    List<?> services;
                    if ( filterQuery == null ) {


                        //noinspection unchecked,rawtypes
                        services = APSServiceLoader.getInstance().loadService( (Class) field.getType() );

                        // Next noinspection comment is for the word starting with 'raw' below ... sigh!
                    } else //noinspection SpellCheckingInspection
                    {

                        // IDEA Bug: need rawtypes twice here!
                        //noinspection unchecked,rawtypes,rawtypes
                        services = APSServiceLoader.getInstance().loadService( (Class) field.getType(), filterQuery );
                    }

                    if ( services != null && !services.isEmpty() ) {
                        try {
                            if ( field.getType().isArray() ) {

                                field.set( injecte, services.toArray() );
                            } else {
                                field.set( injecte, services.get( 0 ) );
                            }
                        } catch ( IllegalAccessException iae ) {
                            throw new APSStartException( iae.getMessage(), iae );
                        }
                    } else {
                        throw new APSStartException( "No service(s) to inject for " + field.getName() + "!" );
                    }
                }
            }
        }
    }

    /**
     * Returns a list of implementations of specified service API.
     *
     * Note that in many cases there will be only one!
     *
     * @param serviceAPI The service API to get service(es) for.
     *
     * @return A List of instances.
     */
    public List<S> loadService( Class<S> serviceAPI ) {

        return loadInternalServiceEntries( serviceAPI )
                .stream()
                .map( APSServiceEntry::getService )
                .collect( Collectors.toList() );
    }

    /**
     * Returns a list of implementations of specified service API, but filtered
     * using specified filter. Each service can have a set of properties via annotations
     * and the values of those can be used to shorten the list of implementations.
     *
     * @param serviceAPI  The service API to get service(es) for.
     * @param filterQuery A query to shorten the result of services. See JavaDoc for filter(...)
     *                    method below to see filter syntax.
     *
     * @return The filtered service list.
     */
    public List<S> loadService( Class<S> serviceAPI, String filterQuery ) {

        return filter( loadInternalServiceEntries( serviceAPI ), filterQuery )
                .stream()
                .map( APSServiceEntry::getService )
                .collect( Collectors.toList() );
    }

    /**
     * Filters a list of services.
     *
     * Filter syntax: op!prop:value,...
     *
     * I'm trying to keep this extremely simple!
     *
     * Operations:
     *
     * == => Equals.
     * != => Not equals.
     * <> => Contains.
     * >< => Does not contain.
     *
     * @param toFilter    A List of APSServiceEntry instances to filter.
     * @param filterQuery A string in above specified format.
     *
     * @return A new possibly smaller list of APSServiceEntry instances.
     */
    public List<APSServiceEntry<S>> filter( List<APSServiceEntry<S>> toFilter, String filterQuery ) {

        List<APSServiceEntry<S>> filtered = new LinkedList<>();

        for ( APSServiceEntry<S> entry : toFilter ) {

            boolean include = true;

            for ( String part : filterQuery.split( "," ) ) {
                String[] criteria = part.split( "!" );
                String op = criteria[ 0 ].trim();

                String[] propAndValue = criteria[ 1 ].trim().split( ":" );
                String propName = propAndValue[ 0 ];
                String propValue = propAndValue[ 1 ];

                switch ( op ) {
                    case "==":
                        if ( !entry.getProperties().get( propName ).equals( propValue ) ) include = false;
                        break;

                    case "!=":
                        if ( entry.getProperties().get( propName ).equals( propValue ) ) include = false;
                        break;

                    case "<>":
                        if ( !entry.getProperties().get( propName ).contains( propValue ) ) include = false;
                        break;

                    case "><":
                        if ( entry.getProperties().get( propName ).contains( propValue ) ) include = false;
                }
            }

            if ( include ) {
                filtered.add( entry );
            }
        }

        return filtered;
    }

    //
    // Inner Types
    //

    /**
     * Holds a ServiceLoader loaded service instance along with properties for the service.
     *
     * The properties comes from \@APSServiceProvider annotation on each service implementation.
     * These can be used to filter the list of service implementations.
     *
     * @param <T> The type of the service.
     */
    public static class APSServiceEntry<T> {

        //
        // Private Members
        //

        /** Properties provided by implementations and that can be filtered on. */
        private final Map<String, String> properties;

        /** The actual service instance represented by the base APSService interface. */
        private final T service;

        //
        // Constructor
        //

        /**
         * Creates a new APSServiceEntry.
         *
         * @param properties A set of properties for identifying / filtering service
         *                   implementations of same API.
         * @param service    A ServiceLoader loaded service instance.
         */
        public APSServiceEntry( Map<String, String> properties, T service ) {
            this.properties = properties;
            this.service = service;
        }

        //
        // Getters
        //

        /**
         * @return The service id properties.
         */
        public Map<String, String> getProperties() {
            return this.properties;
        }

        /**
         * @return The service instance held by this entry.
         */
        public T getService() {
            return this.service;
        }
    }

}
