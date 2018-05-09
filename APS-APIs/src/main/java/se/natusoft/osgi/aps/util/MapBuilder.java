package se.natusoft.osgi.aps.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds a simple map.
 */
public class MapBuilder {

    private Map<String, Object> map = new LinkedHashMap<>();
    private String key;

    /**
     * Builds a Map by taking key, value, key, value ... from an Object array.
     *
     * @param keyval The key value array.
     */
    private Map<String, Object> keyval( Object... keyval ) {

        int i = 0;
        while ( i < keyval.length ) {

            map.put( keyval[ i ].toString().replace( ":", "" ), keyval[ i + 1 ] );
            i += 2;
        }

        return map;
    }

    public MapBuilder k( String key ) {

        if ( this.key != null ) throw new IllegalStateException( "Expecting a key, not a value!" );

        this.key = key;
        return this;
    }

    public MapBuilder v( Object value ) {

        if ( this.key == null ) throw new IllegalStateException( "Expecting a value, not a key!" );

        this.map.put( this.key, value );
        this.key = null;
        return this;
    }

    public Map<String, Object> toMap() {

        return this.map;
    }

    public static Map<String, Object> map( Object... keyval ) {

        return new MapBuilder().keyval( keyval );
    }

    public static MapBuilder map() {

        return new MapBuilder();
    }
}
