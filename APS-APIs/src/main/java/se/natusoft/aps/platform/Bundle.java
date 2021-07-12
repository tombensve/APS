package se.natusoft.aps.platform;

import se.natusoft.docutations.Note;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

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
interface Bundle {

    int UNINSTALLED = 0x00000001;
    int INSTALLED = 0x00000002;

    int RESOLVED = 0x00000004;

    int STARTING = 0x00000008;

    int STOPPING = 0x00000010;

    int ACTIVE = 0x00000020;

    int START_TRANSIENT = 0x00000001;

    int START_ACTIVATION_POLICY = 0x00000002;

    int STOP_TRANSIENT = 0x00000001;

    int SIGNERS_ALL = 1;

    int SIGNERS_TRUSTED = 2;

    int getState();

    void start( int options ) throws BundleException;

    void start() throws BundleException;

    void stop( int options ) throws BundleException;

    void stop() throws BundleException;

    void update( InputStream input ) throws BundleException;

    void update() throws BundleException;

    void uninstall() throws BundleException;

    Dictionary<String,String>getHeaders();

    long getBundleId();

    String getLocation();

    org.osgi.framework.ServiceReference[] getRegisteredServices();

    ServiceReference[] getServicesInUse();

    boolean hasPermission( Object permission );

    URL getResource( String name );

    Dictionary<String,String>getHeaders( String locale );

    String getSymbolicName();

    Class loadClass( String name ) throws ClassNotFoundException;

    Enumeration<URL>getResources( String name ) throws IOException;

    Enumeration<String>getEntryPaths( String path );

    URL getEntry( String path );

    long getLastModified();

    Enumeration<URL> findEntries( String path, String filePattern,
                                              boolean recurse );

    BundleContext getBundleContext();

    Map getSignerCertificates( int signersType );

    Version getVersion();
}
