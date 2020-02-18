package se.natusoft.osgi.aps.core.msg

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
@TypeChecked
class ServiceCheckMessage extends BaseMessage {

    private static final String TYPE = "platform/service/check"

    private static Map<String, Object> schema = schemaDef( "1.0", TYPE, [ : ] )

    ServiceCheckMessage( String from ) {
        super(
                schema,
                messageVal( "1.0", TYPE, from, [ : ] )
        )

        validate()
    }

    ServiceCheckMessage( Map<String, Object> message ) {
        super( schema, message )

        validate()
    }
}
