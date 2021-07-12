package se.natusoft.aps.platform;

import org.osgi.framework.Bundle;
import se.natusoft.docutations.Note;

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
public interface ServiceReference
{
        Object getProperty(String key);

        String[] getPropertyKeys();

        org.osgi.framework.Bundle getBundle();

        org.osgi.framework.Bundle[] getUsingBundles();

        boolean isAssignableTo( Bundle bundle, String className);

        int compareTo(Object reference);
}
