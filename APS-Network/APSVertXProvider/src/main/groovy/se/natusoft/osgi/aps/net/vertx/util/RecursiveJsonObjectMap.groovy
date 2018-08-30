package se.natusoft.osgi.aps.net.vertx.util

import io.vertx.core.json.JsonObject
import se.natusoft.osgi.aps.exceptions.APSValidationException

/**
 * Vert.x JsonObject class is not entirely nice. It do provide a getMap() that
 * returns the JSON object as a Map<String, Object>. The problem is that this
 * Map is not recursive. If you take the result of a get(...) and it is another
 * JSON object you will get a JsonObject, and not JsonObject.getMap() back.
 *
 * Wrapping a JsonObject with this object will give you a correct recursive
 * Map structure by delegating most Map calls to JsonObject.getMap(). Each
 * JsonObject returned gets wrapped by this class.
 *
 * So why only access though Map API and not use the API JsonObject provides ?
 *
 * Well, its not that I don't like the JsonObject API. Its that I'm coding in
 * Groovy and thus can access Map:s just like JavaScript objects but using
 * [ & ] instead of { & }. For example: message["aps"]["type"]. This is
 * quite readable and less to write than message.getObject("aps").getString("type").
 *
 * My first solution was to do toString() on JsonObject and then parse that with
 * Jackson Jr into a Map. Even if it was less code to write for me it didn't
 * feel right. parse JSON -> regenerate3 JSON -> parse JSON. This class is just
 * a wrapper and uses the original JsonObject provided by Vert.x. So nothing is
 * reparsed again.
 *
 * This object is also read only! It will throw an APSValidationException on
 * any attempt to update the Map.
 */
class RecursiveJsonObjectMap implements Map<String, Object> {

    private JsonObject jsonObject

    RecursiveJsonObjectMap( JsonObject jsonObject ) {
        this.jsonObject = jsonObject
    }

    @Override
    int size() {
        return this.jsonObject.map.size(  )
    }

    @Override
    boolean isEmpty() {
        return this.jsonObject.map.isEmpty(  )
    }

    @Override
    boolean containsKey( Object key ) {
        return this.jsonObject.map.containsKey( key )
    }

    @Override
    boolean containsValue( Object value ) {
        return this.jsonObject.map.containsValue( value )
    }

    @Override
    Object get( Object key ) {
        Object value = this.jsonObject.map.get( key )
        if ( value instanceof JsonObject ) {
            value = new RecursiveJsonObjectMap( value as JsonObject )
        }

        value
    }

    @Override
    Set<String> keySet() {
        this.jsonObject.map.keySet(  )
    }

    @Override
    Collection<Object> values() {
        return this.jsonObject.map.values(  )
    }

    @Override
    Set<Entry<String, Object>> entrySet() {
        return this.jsonObject.map.entrySet(  )
    }

    Object put( String key, Object value ) {
        throw new APSValidationException( "This Map is read only!" )
    }

    Object remove( Object key ) {
        throw new APSValidationException( "This Map is read only!" )
    }

    void putAll( Map<? extends String, ? extends Object> m ) {
        throw new APSValidationException( "This Map is read only!" )
    }

    @Override
    void clear() {
        throw new APSValidationException( "This Map is read only!" )
    }

    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append( "[ " )

        this.jsonObject.map.keySet().each { key ->
            Object value = this.get( key )
            sb.append( " ${ key }: " )
            sb.append( value.toString() )
            sb.append( " " )
        }

        sb.append( " ]" )

        return sb.toString()
    }
}