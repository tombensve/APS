package se.natusoft.osgi.aps.core.msg

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.exceptions.APSValidationException

@CompileStatic
@TypeChecked
class ServiceAnnounceMessage extends BaseMessage {

    private static final String TYPE = "platform/service/announcement"

    private static Map<String, Object> schema = schemaDef(
            "1.0", TYPE,
            [ serviceId_1: "?service/.*" ] as Map<String, Object>
    )

    ServiceAnnounceMessage( String serviceId ) {
        super(
                schema,
                messageVal( "1.0", TYPE, serviceId,
                        [
                                serviceId: serviceId.startsWith( "service/" ) ? serviceId : "service/${serviceId}"
                        ] as Map<String, Object>
                )
        )

        validate()
    }

    ServiceAnnounceMessage( Map<String, Object> message ) throws APSValidationException {
        super( schema, message )

        validate()
    }

    String getFrom() {
        apsFrom
    }

    String getServiceId() {
        content[ "serviceId" ]
    }

}
