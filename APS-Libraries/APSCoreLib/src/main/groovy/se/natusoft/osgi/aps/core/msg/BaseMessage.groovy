package se.natusoft.osgi.aps.core.msg

import groovy.transform.CompileStatic
import se.natusoft.aps.api.messaging.APSBaseMessage
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
@CompileStatic
class BaseMessage implements APSBaseMessage {

    static final Map<String, Object> schemaDef(float version, String type, Map<String, Object> content) {
        [
                aps_1: [
                        version_1: "#<=${version}",
                        type_1: type
                ],
                content_1: content
        ] as Map<String, Object>
    }

    /** Provides the Map<String, Object> implementation. */
    @Delegate
    private LinkedHashMap<String, Object> message

    /** For validating the message. */
    private MapJsonSchemaValidator validator

    /**
     * Creates a new empty (and invalid) message.
     */
    BaseMessage() {

        this.message = [ : ]
    }

    /**
     * Creates a new BaseMessage with a schema.
     *
     * @param schema The schema for this message.
     */
    BaseMessage( Map<String, Object> schema ) {
        this()

        if ( schema != null ) {
            this.validator = new MapJsonSchemaValidator( validStructure: schema )
        }
    }

    /**
     * Constructor for services where a message has been received.
     *
     * @param schema The message schema.
     * @param message The received message.
     */
    BaseMessage( Map<String, Object> schema, Map<String, Object> message) {
        this(schema)
        if ( message instanceof LinkedHashMap ) {
            this.message = message as LinkedHashMap<String, Object>
        }
        else {
            this.message.putAll( message )
        }
    }

    /**
     * Validates the content of the message.
     */
    void validate() throws APSValidationException {

        if ( this.validator != null ) {
            this.validator.validate( this.message )
        }
    }

    // Remember, this is Groovy, aps["..."] references are calling getAps()! In subclasses content["..."] can be used.

    /**
     * @return 'aps.version'.
     */
    float getApsVersion() {
        aps[ "version" ] as float
    }

    /**
     * Sets the 'aps.version' value.
     *
     * @param version The version value to set.
     */
    @Override
    void setApsVersion( float version ) {
        aps[ "version" ] = version
    }

    /**
     * @return The 'aps.from' value.
     */
    @Override
    String getApsFrom() {
        aps[ "from" ]
    }

    /**
     * Sets the 'aps.from' value.
     *
     * @param from The from value to set.
     */
    @Override
    void setApsFrom( String from ) {
        aps[ "from" ] = from
    }

    /**
     * @return 'aps.type'
     */
    String getApsType() {
        aps[ "type" ] as String
    }

    /**
     * Sets the 'aps.type' id
     *
     * @param type The type to set.
     */
    @Override
    void setApsType( String type ) {
        aps[ "type" ] = type
    }

    /**
     * @return The 'aps' sub object.
     */
    Map<String, Object> getAps() {
        this.message[ "aps" ] as Map<String, Object>
    }

    /**
     * @return The 'content' sub object.
     */
    Map<String, Object> getContent() {
        this.message[ "content" ] as Map<String, Object>
    }
}
