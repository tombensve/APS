package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.json.JsonObject
import se.natusoft.osgi.aps.json.JSON
import se.natusoft.osgi.aps.json.JSONArray
import se.natusoft.osgi.aps.json.JSONObject

/**
 * This handles type conversions for both sending and receiving. The Vertx event bus basically only supports
 * String and JsonObject (from Vertx) messages. This conversion always convert to String on out messages and
 * from String on in messages.
 *
 * The following types are supported:
 * - Map\<String, Object\>.
 * - JSONObject (aps-json-lib).
 * - JSONArray (aps-json-lib).
 * - String.
 * - JsonObject (Vertx).
 *
 * Any other type will have toString() done on it in any direction!
 */
@CompileStatic
@TypeChecked
class TypeConv {

    /**
     * For sending with Vertx String and JsonObject pass through.
     *
     * @param message The aps message to convert for sending.
     *
     * @return Vertx compatible message.
     */
    static Object apsToVertx( Object message ) {
        Object msg = null

        if ( message instanceof Map<String, Object> ) {
            // See comment when handling received message in APSVertxEventBusMessagingProvider.subscribe(...).
            msg = JSON.mapToString( message )
        }
        else if ( message instanceof JSONObject ) {
            msg = "JSONObject:" + JSON.jsonToString( (JSONObject) message )
        }
        else if ( message instanceof JSONArray ) {
            msg = "JSONArray:" + JSON.jsonToString( (JSONArray) message )
        }
        else if ( message instanceof String ) {
            msg = message
        }
        else if ( message instanceof JsonObject ) {
            msg = message
        }
        else {
            msg = message.toString()
        }

        msg
    }

    /**
     * For received messages with Vertx String and JsonObject pass through.
     *
     * @param message The received Vertx compatible message.
     *
     * @return The possibly APS type message.
     */
    static Object vertxToAps( Object message ) {
        Object msg = null

        if ( message instanceof String ) {
            String mess2 = (String) message

            if ( mess2.trim().startsWith( "{" ) ) {
                msg = JSON.stringToMap( mess2 )
            }
            else if ( mess2.trim().startsWith( "JSONObject:" ) ) {
                msg = JSON.stringToJson( mess2.substring( 11 ) )
            }
            else if ( mess2.trim().startsWith( "JSONArray:" ) ) {
                msg = JSON.stringToJson( mess2.substring( 10 ) )
            }
            else {
                msg = message
            }
        }
        else if ( message instanceof JsonObject ) {
            msg = message
        }
        else {
            msg = message.toString()
        }

        msg
    }
}
