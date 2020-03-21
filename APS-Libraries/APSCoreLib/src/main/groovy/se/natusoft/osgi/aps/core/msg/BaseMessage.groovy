package se.natusoft.osgi.aps.core.msg

import groovy.transform.CompileStatic
import se.natusoft.osgi.aps.core.lib.MapJsonSchemaValidator
import se.natusoft.osgi.aps.exceptions.APSValidationException

/**
 * Base class for validating messages. Note that subclasses will also be a Map<String, Object>!
 *
 * The following base applies to all messages:
 *
 *         private static Map<String, Object> schema = [
 *             aps_1 : [
 *                     version_1: "?#<=1.0",
 *                     type_1   : TYPE,
 *             ],
 *             content_1: [
 *
 *             ]
 *         ] as Map<String, Object>
 */
@SuppressWarnings( "unused" )
@CompileStatic
class BaseMessage implements Map<String, Object> {

    static final Map<String, Object> schemaDef(String version, String type, Map<String, Object> content) {
        [
                aps_1: [
                        version_1: "?<=${version}",
                        type_1: type
                ],
                content_1: content
        ] as Map<String, Object>
    }

    static final Map<String, Object> messageVal(String version, String type, String from, Map<String, Object> content) {
        [
                aps: [
                        version: version,
                        type: type
                ],
                content: content
        ] as Map<String, Object>
    }

    @Delegate
    private LinkedHashMap<String, Object> _message

    private MapJsonSchemaValidator validator

    BaseMessage() {

        this._message = [ : ]
    }

    BaseMessage( Map<String, Object> schema ) {

        if ( schema != null ) {
            this.validator = new MapJsonSchemaValidator( validStructure: schema )
        }

        this._message = [ : ]
    }

    BaseMessage( Map<String, Object> schema, Map<String, Object> message ) {

        this( schema )

        if ( !message instanceof LinkedHashMap ) {
            this._message = [ : ]
            this._message.putAll( message )
        }
        else {
            this._message = message as LinkedHashMap<String, Object>
        }
    }

    Map<String, Object> getMessage() {
        return this._message
    }

    /**
     * Validates the content of the message.
     */
    void validate() throws APSValidationException {

        if ( this.validator != null ) {
            this.validator.validate( this._message )
        }
    }

    float getApsVersion() {
        aps["version"] as float
    }

    String getApsType() {
        aps["type"] as String
    }

    Map<String, Object> getAps() {
        this["aps"] as Map<String, Object>
    }

    Map<String, Object> getContent() {
        this["content"] as Map<String, Object>
    }
}
