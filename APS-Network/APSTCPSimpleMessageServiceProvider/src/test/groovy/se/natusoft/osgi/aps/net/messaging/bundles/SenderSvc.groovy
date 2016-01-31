package se.natusoft.osgi.aps.net.messaging.bundles

import se.natusoft.osgi.aps.api.net.messaging.service.APSSimpleMessageService
import se.natusoft.osgi.aps.api.net.util.TypedData
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStart
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

class SenderSvc {

    public static final CONTENT_TYPE = "fish"
    // I was listening to Scooters "How much is the fish" when I got the idea for test data :-)
    public static final CONTENT = "><>"

    @OSGiService(timeout = "2 seconds")
    private APSSimpleMessageService msgService

    @BundleStart
    public void start() throws Exception {
        TypedData message = new TypedData.Provider(
                contentType: CONTENT_TYPE,
                content: CONTENT.getBytes()
        )

        this.msgService.sendMessage("test", message)
    }
}
