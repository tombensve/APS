package se.natusoft.osgi.aps.tools.apis;

/**
 * Classes implementing this interface can be specified in @OSGiService annotation. In that case
 * the class will be instantiated and the method below will be called, and the resulting String
 * will be used as additional search criteria (in addition to service class name).
 */
public interface APSActivatorSearchCriteriaProvider {

    /**
     * This should return a String starting with '(' and ending with ')'. The final ServiceListener
     * criteria will be (&(objectClass=MyService)(_providedSearchCriteria()_))
     *
     * Whatever is returned it will probably  reference a property and a value that the service you
     * are looking for where registered with.
     */
    String provideSearchCriteria();
}
