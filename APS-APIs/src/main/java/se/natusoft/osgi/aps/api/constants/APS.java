package se.natusoft.osgi.aps.api.constants;

/**
 * General constants for the whole of APS.
 */
public interface APS {

    interface Messaging {

        /** Specifies a topic to publish or listen to. */
        String Topic = "aps.messaging.topic";

        /** The messaging protocol name that an APSMessageProtocol implementation implements. */
        String Protocol = "aps.messaging.protocol";

        interface ConnectionPoint {

            /** The name of the APSConnectionPoint to provide as service or to consume. */
            String Name = "aps.connection-point.name";

        }
    }
}
