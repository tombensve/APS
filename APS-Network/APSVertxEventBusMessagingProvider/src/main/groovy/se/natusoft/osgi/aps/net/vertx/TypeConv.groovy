/* 
 * 
 * PROJECT
 *     Name
 *         APS Vertx Event Bus Messaging Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides an implementation of APSMessageService using Vert.x event bus.
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
 *         2018-05-28: Created!
 *         
 */
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
 **/
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
        Object msg

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
