package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.eventbus.Message
import se.natusoft.docutations.IDEAFail
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.api.messaging.APSMessage

@CompileStatic
@TypeChecked
class APSMessageProvider<M> implements APSMessage<M> {

    Message vertxMsg
    M message


    /**
     * @return the message content.
     */
    @Override
    @IDEAFail("The 'M' cast is due to IDEA not understanding that the M type of message is the same M type of return value.")
    M content() {
        return (M)this.message
    }

    /**
     * @return true if the message is replyable.
     */
    @Override
    boolean isReplyable() {
        return true
    }

    /**
     * Replies to message.
     *
     * @param reply The message to reply with.
     */
    @Override
    void reply( @NotNull M reply ) {
        this.vertxMsg.reply( TypeConv.apsToVertx( reply ) )
    }
}
