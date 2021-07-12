package se.natusoft.aps.platform;

import se.natusoft.aps.exceptions.APSException;
import se.natusoft.docutations.Note;

import java.io.File;
import java.io.InputStream;
import java.util.Dictionary;

@Note(
        {
                "APS used to be OSGi services deployed in an OSGi container. That is no longer the case!",
                "APS services will not work if deployed in an OSGi container, some might, but in general",
                "not! APS is still using some OSGi APIs, but APS provides implementation of those. ",
                "Thereby all dependencies on OSGi is removed and those few OSGi APIs used are copied from",
                "OSGi. They are however renamed with an APS prefix and moved to aps package, to lessen ",
                "confusion. In time the contents also might change from the OSGi original. Doing this API",
                "rename and package move allows for that. This should no longer be seen as OSGi, it hasn't",
                "been for some time."
        }
)
public interface BundleContext {

    String getProperty(String key);

    Bundle getBundle();

    Bundle installBundle(String location, InputStream input)
            throws BundleException;

    Bundle installBundle(String location) throws APSException;

    Bundle getBundle(long id);

    Bundle[] getBundles();

    void addServiceListener( org.osgi.framework.ServiceListener listener, String filter) throws InvalidSyntaxException;

    void addServiceListener( ServiceListener listener);

    void removeServiceListener( ServiceListener listener);

    void addBundleListener(BundleListener listener);

    void removeBundleListener(BundleListener listener);

    void addFrameworkListener(FrameworkListener listener);

    void removeFrameworkListener(FrameworkListener listener);

    ServiceRegistration registerService(String[] clazzes, Object service, Dictionary properties);

    ServiceRegistration registerService(String clazz, Object service,
                                               Dictionary properties);

    ServiceReference[] getServiceReferences(String clazz, String filter)
            throws InvalidSyntaxException;

    ServiceReference[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException;

    ServiceReference getServiceReference(String clazz);

    Object getService(ServiceReference reference);

    boolean ungetService(ServiceReference reference);

    File getDataFile( String filename);

    Filter createFilter(String filter) throws InvalidSyntaxException;

}
