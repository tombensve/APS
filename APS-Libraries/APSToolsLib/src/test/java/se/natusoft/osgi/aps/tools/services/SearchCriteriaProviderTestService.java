package se.natusoft.osgi.aps.tools.services;

import se.natusoft.osgi.aps.tools.annotation.activator.*;
import se.natusoft.osgi.aps.tools.apis.APSActivatorSearchCriteriaProvider;

@OSGiServiceProvider
public class SearchCriteriaProviderTestService implements TestService {

    @OSGiService(searchCriteriaProvider = MySearchCriteriaProvider.class)
    private TestService service;

    @Override
    public String getServiceInstanceInfo() {
        return service.getServiceInstanceInfo();
    }

    public static class MySearchCriteriaProvider implements APSActivatorSearchCriteriaProvider {

        /**
         * This should return a String starting with '(' and ending with ')'. The final ServiceListener
         * criteria will be (&(objectClass=MyService)(_providedSearchCriteria()_))
         * <p/>
         * Whatever is returned it will probably  reference a property and a value that the service you
         * are looking for where registered with.
         */
        @Override
        public String provideSearchCriteria() {
            return "(instance=second)";
        }
    }
}
