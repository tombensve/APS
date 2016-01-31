package se.natusoft.osgi.aps.net.messaging.bundles

import se.natusoft.osgi.aps.api.net.messaging.service.APSSimpleMessageService
import se.natusoft.osgi.aps.api.net.util.TypedData
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStart
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

class ReceiverSvc implements APSSimpleMessageService.MessageListener {

    public static final String TYPE = "tcp.msg.svc.msg.type"
    public static final String MSG = "tcp.msg.svc.msg"

    @OSGiService(timeout = "2 seconds")
    APSSimpleMessageService msgService

    @BundleStart
    public void start() throws Exception {
        this.msgService.addMessageListener("test", this)
    }

    @BundleStop
    public void stop() throws Exception {
        this.msgService.removeMessageListener("test", this)
    }

    @Override
    void messageReceived(String topic, TypedData message) {
        System.setProperty(TYPE, message.contentType)
        System.setProperty(MSG, new String(message.content))
    }
}
