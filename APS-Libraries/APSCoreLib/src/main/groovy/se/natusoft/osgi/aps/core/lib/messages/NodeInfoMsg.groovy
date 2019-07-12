package se.natusoft.osgi.aps.core.lib.messages

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.IDEAFail
import se.natusoft.osgi.aps.exceptions.APSValidationException

import static se.natusoft.osgi.aps.core.lib.messages.SchemaConstants.*

@CompileStatic
@TypeChecked
/**
 * This serves as an example of defining a well defined message.
 *
 * To have message validated on set do:
 *
 * Groovy:
 *
 *     new NodeInfoMsg(validate: true, message: message)
 *
 * Java:
 *
 *     new NodeInfoMsg().enableValidation().setMessage(message)
 */
class NodeInfoMsg extends WellDefinedMessage<NodeInfoMsg> {

    Map<String, Object> getSchema() {
        Map<String, Object> schema = super.getSchema()
        ( schema.headers_1 as Map<String, Object> ) << (
                [
                        type_1   : "NodeInfo",
                        version_1: "#<=1.0"
                ] as Map<String, Object> )
        ( schema.content_1 as Map<String, Object> ) << (
                [
                        id_1        : _UUID,
                        name_0      : TEXT_NUM_DOT,
                        address_0   : IP_ADDRESS,
                        busAddress_0: BUS_ADDRESS,
                        uptime_0    : NUM_DOT
                ] as Map<String, Object> )

        schema
    }

    /**
     * Creates a new NodeInfoMsg.
     *
     * @param message The Map JSON message setContent.
     *
     * @throws APSValidationException on invalid message.
     */
    @IDEAFail( "IDEA sees this.message as a Map key rather than JB property in base class. The compiler treats this correctly." )
    NodeInfoMsg( Map<String, Object> message ) {
        this.setMessage( message )
    }
}
