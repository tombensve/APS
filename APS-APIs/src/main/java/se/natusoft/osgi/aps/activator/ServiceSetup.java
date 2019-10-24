package se.natusoft.osgi.aps.activator;

import java.util.LinkedList;
import java.util.Properties;

/**
 * Originally generated with BeanAnnotation processor, but since jkd 9+ javax.annotation.* resides
 * in the java.se.ee module and maven refused to accept --add-module java.se.ee, but I realize now
 * that i probably misspelled maven arg. There is however a point in not doing that since that would
 * guarantee build failure with any JDK < 9. APS is not dependent on anything above JDK 8! I'm building
 * with JDK 11 for the moment just to make sure it also works.
 *
 * So the solution was to just copy the generated bean and clean it up a bit. This is not a big complex
 * bean, so I was really lazy using the annotation processor, and in the long run cost me more than
 * just doing it myself from start :-).
 */
@SuppressWarnings("WeakerAccess")
public class ServiceSetup {
    public ServiceSetup() {
    }

    private LinkedList<String> serviceAPIs = new LinkedList<>();
    public void setServiceAPIs(LinkedList<java.lang.String> value) {
        this.serviceAPIs = value;
    }
    public LinkedList<java.lang.String> getServiceAPIs() {
        return this.serviceAPIs;
    }

    private Properties props = new Properties();
    public void setProps(Properties value) {
        this.props = value;
    }
    public Properties getProps() {
        return this.props;
    }

    private Object serviceInstance;
    public void setServiceInstance(Object value) {
        this.serviceInstance = value;
    }
    public Object getServiceInstance() {
        return this.serviceInstance;
    }

}
