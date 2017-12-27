package se.natusoft.osgi.aps.core.lib

import org.osgi.framework.BundleContext

class Svc {

    /**
     * Registers an OSGi service.
     *
     * @param context
     * @param clazz
     * @param service
     * @param properties
     * @return
     */
    static reg( BundleContext context, Class clazz, Object service, Map<String, String> properties ) {
        Properties props = new Properties()
        props = [one: "two"]
        properties.each { String key, String value ->
            props.setProperty( key, value )
        }
        context.registerService( clazz.getTypeName(), service, props )
    }
}
