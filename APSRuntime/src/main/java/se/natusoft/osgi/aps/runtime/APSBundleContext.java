/*
 *
 * PROJECT
 *     Name
 *         APS OSGi Test Tools
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides tools for testing OSGi services.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2015-01-23: Created!
 *
 */
package se.natusoft.osgi.aps.runtime;

import org.osgi.framework.*;

import java.io.File;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * This is a BundleContext implementation.
 */
public class APSBundleContext implements BundleContext {

    //
    // Private Members
    //

    private APSBundle bundle;

    private Properties props = new Properties();

    private List<BundleListener> bundleListeners = new LinkedList<>();

    //
    // Constructors
    //

    /**
     * Creates a new TestBundleContext.
     *
     * @param bundle The contexts Bundle.
     */
    @SuppressWarnings("WeakerAccess") // Dont want to limit to package access!
    public APSBundleContext( APSBundle bundle ) {
        this.bundle = bundle;
    }

    //
    // Methods
    //

    /**
     * Provides properties for the bundle. By default the Bundle properties are empty.
     *
     * @param props The properties to provide.
     */
    public void setProperties( Properties props ) {
        this.props = props;
    }

    /**
     * Returns the Bundles properties. There is an empty Properties instance available by default.
     */
    public Properties getProperties() {
        return this.props;
    }

    //
    // BundleContext Methods
    //

    @Override
    public String getProperty( String key ) {
        return this.props.getProperty( key );
    }

    @Override
    public Bundle getBundle() {
        return this.bundle;
    }

    /**
     * Not supported!
     */
    @Override
    public Bundle installBundle( String location, InputStream input ) throws BundleException {
        throw new BundleException( "installBundle(...) is not supported!" );
    }

    /**
     * Not supported!
     */
    @Override
    public Bundle installBundle( String location ) throws BundleException {
        throw new BundleException( "installBundle(...) is not supported!" );
    }

    /**
     * Not supported!
     */
    @Override
    public Bundle getBundle( long id ) {
        return this.bundle;
    }

    /**
     * Not supported!
     */
    @Override
    public Bundle[] getBundles() {
        return new Bundle[]{ this.bundle };
    }

    @SuppressWarnings("RedundantThrows") // It is expected by the BundleContext API!
    @Override
    public void addServiceListener( ServiceListener listener, String filter ) throws InvalidSyntaxException {
        this.bundle.getServiceRegistry().addServiceListener( listener, filter );
    }

    @Override
    public void addServiceListener( ServiceListener listener ) {
        this.bundle.getServiceRegistry().addServiceListener( listener );
    }

    @Override
    public void removeServiceListener( ServiceListener listener ) {
        this.bundle.getServiceRegistry().removeServiceListener( listener );
    }

    /**
     * Not supported!
     */
    @Override
    public void addBundleListener( BundleListener listener ) {
        this.bundleListeners.add( listener );
    }

    /**
     * Not supported!
     */
    @Override
    public void removeBundleListener( BundleListener listener ) {
        this.bundleListeners.remove( listener );
    }

    /**
     * Sends bundle event to all listeners.
     *
     * @param bundle The Bundle the event is for.
     * @param type The event type.
     */
    void bundleEvent( Bundle bundle, int type ) {
        BundleEvent bundleEvent = new BundleEvent( type, bundle );
        for (BundleListener bundleListener : this.bundleListeners) {
            bundleListener.bundleChanged( bundleEvent );
        }
    }

    /**
     * Not supported!
     */
    @Override
    public void addFrameworkListener( FrameworkListener listener ) {
        throw new RuntimeException( "Not yet supported!" );
    }

    /**
     * Not supported!
     */
    @Override
    public void removeFrameworkListener( FrameworkListener listener ) {
        throw new RuntimeException( "Not yet supported!" );
    }

    @Override
    public ServiceRegistration registerService( String[] clazzes, Object service, Dictionary properties ) {
        return registerService( clazzes[ 0 ], service, properties );
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceRegistration registerService( String clazz, Object service, Dictionary properties ) {
        if ( properties.get( Constants.OBJECTCLASS ) == null ) {
            properties.put( Constants.OBJECTCLASS, new String[]{ clazz } );
        }
        APSServiceRegistration sr = new APSServiceRegistration( clazz, new APSServiceReference( this, properties ), this.bundle );
        try {
            this.bundle.getServiceRegistry().registerService( sr, service, Class.forName( clazz ) );
        } catch ( ClassNotFoundException cnfe ) {
            throw new IllegalArgumentException( "Bad value passed for 'clazz' parameter!", cnfe );
        }
        return sr;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public ServiceReference[] getServiceReferences( String clazz, String filter ) throws InvalidSyntaxException {
        return this.bundle.getServiceRegistry().getServiceReferences( clazz, filter );
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public ServiceReference[] getAllServiceReferences( String clazz, String filter ) throws InvalidSyntaxException {
        return this.bundle.getServiceRegistry().getAllServiceReferences( clazz, filter );
    }

    @Override
    public ServiceReference getServiceReference( String clazz ) {
        return this.bundle.getServiceRegistry().getServiceReference( clazz );
    }

    @Override
    public Object getService( ServiceReference reference ) {
        return this.bundle.getServiceRegistry().getService( reference );
    }

    /**
     * Does nothing!
     */
    @Override
    public boolean ungetService( ServiceReference reference ) {
        return true;
    }

    /**
     * Not supported!
     */
    @Override
    public File getDataFile( String filename ) {
        throw new RuntimeException( "Not supported!" );
    }

    /**
     * Not supported!
     */
    @Override
    public Filter createFilter( String filter ) throws InvalidSyntaxException {
        return FrameworkUtil.createFilter(filter);
    }
}
