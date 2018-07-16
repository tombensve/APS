package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.json.JsonObject
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.core.lib.MapJsonDocSchemaValidator
import se.natusoft.osgi.aps.json.JSONObject

/**
 * This class defines a generic event structure in JSONish Map structure as can be defined and validated
 * by MapJsonDocSchemaValidator.
 */
@CompileStatic
@TypeChecked
class EventDefs {

    //
    // Message keys
    //

    public static final String _data_ = "data"
    public static final String _header_ = "header"
    public static final String _address_ = "address"
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    public static final String _classifier_ = "classifier"
    public static final String _eventType_ = "eventType"
    public static final String _error_ = "error"
    public static final String _code_ = "code"
    public static final String _message_ = "message"
    public static final String _webs_ = "webs"

    //
    // Misc Constants
    //

    @SuppressWarnings( "GroovyUnusedDeclaration" )
    public static final String VERTX_EVENT_MESSAGE_SRC = "eventMessageSrc"

    /** For hanging up an original message in the root of a forwarded local message. */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    public static final String ORIG_EVENT = "origMessage"

    /** Validates an event structure. */
    public static final MapJsonDocSchemaValidator EVENT_VALIDATOR = new MapJsonDocSchemaValidator( validStructure: EVENT_SCHEMA )

    //
    // Event Schemas
    //

    /**
     * A schema defining valid event structure.
     *
     * ## Structure
     *
     * - __sender__ (required) Contains information about the sender.
     *    - __address__ (required) The address of the sender.
     * - __eventType__ (required) What type of event. The rest of the body depends on this.
     * - __data__ (optional) The event data section.
     * - __error__ (optional) An error message when things go wrong.
     *    - __code__ (required) An error code (int).
     *    - __message__ (required) An error message.
     */
    public static final Map<String, Object> EVENT_SCHEMA = [
            header_1   : [
                    address_1: "?[A-Z,a-z,0-9,\\.,-]*",
            ],
            eventType_1: "?req-webs|provide-webs",
            data_0     : [
                    webs_0: WEBS,
            ],
            error_0    : [
                    code_1   : "?[0-9]*",
                    message_1: "?.*"
            ]
    ] as Map<String, Object>

    /**
     * Schema part for the body/webs part for when eventType is "provide-webs".
     */
    public static final List<Map<String, Object>> WEBS = [
            [
                    name_1: "?.*",
                    url_1 : "?^https?://.*:?/.*"
            ]
    ] as List<Map<String, Object>> // Groovy compiler has problems accepting the LinkedList is a List!

}

/**
 * Utility wrapper.
 */
@TypeChecked
@CompileStatic
class Event {

    private Map<String, Object> content = [:]

    Event( @NotNull sender ) {
        this.content[ EventDefs._header_ ] = [adress: sender] as Object
    }

    Event( @NotNull JSONObject json ) {
        this.content = json.toMap()
    }

    Event( @NotNull Map<String, Object> jsonmap) {
        this.content = jsonmap
    }

    Event eventType( @NotNull String eventType ) {
        this.content[ EventDefs._eventType_ ] = eventType
        this
    }

    Event data( Map<String, Object> data ) {
        this.content[ EventDefs._data_ ] = data
        this
    }

    Event error( int code, String message ) {
        this.content[ EventDefs._error_ ] = [
                code   : code,
                message: message
        ] as Object
        this
    }

    JsonObject toJson() {
        new JsonObject( this.content )
    }

    String getEventType() {
        this.content[ EventDefs._eventType_ ] as String
    }

    String getAddress() {
        this.content[ EventDefs._header_ ][ EventDefs._address_ ] as String
    }

    @SuppressWarnings( "GroovyUnusedDeclaration" )
    List<Map<String, Object>> getWebs() {
        this.content[ EventDefs._data_ ][ EventDefs._webs_ ] as List<Map<String, Object>>
    }

    @SuppressWarnings( "GroovyUnusedDeclaration" )
    int getErrorCode() {
        this.content[ EventDefs._error_ ][ EventDefs._code_ ] as Integer
    }

    @SuppressWarnings( "GroovyUnusedDeclaration" )
    String getErrorMessage() {
        this.content[ EventDefs._error_ ][ EventDefs._message_ ] as String
    }

    @SuppressWarnings( "GroovyUnusedDeclaration" )
    void validate() {
        EventDefs.EVENT_VALIDATOR.validate( this.content )
    }
}

