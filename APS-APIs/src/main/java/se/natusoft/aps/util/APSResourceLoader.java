package se.natusoft.aps.util;

import java.io.InputStream;

/**
 * This because getClass().getResourceAsStream(path) fails in Groovy 3.x!!
 *
 * Thereby I decided to create this resource loading tool in Java and add to it as needed.
 *
 * Works fine when this is called from Groovy code!
 */
public class APSResourceLoader {

    public static InputStream asInputStream( String path ) {
        return APSResourceLoader.class.getResourceAsStream( path );
    }
}
