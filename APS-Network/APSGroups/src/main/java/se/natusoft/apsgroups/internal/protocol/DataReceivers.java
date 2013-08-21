package se.natusoft.apsgroups.internal.protocol;

import se.natusoft.apsgroups.internal.protocol.message.MessagePacketListener;

import java.util.LinkedList;

/**
 * Manages a collection of DataReceivers.
 */
public class DataReceivers extends LinkedList<DataReceiver> implements DataReceiver {

    /**
     * Adds a MessagePacket listener.
     *
     * @param listener The listener to add.
     */
    @Override
    public void addMessagePacketListener(MessagePacketListener listener) {
        for (DataReceiver receiver : this) {
            receiver.addMessagePacketListener(listener);
        }
    }

    /**
     * Removes a MessagePacket listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeMessagePacketListener(MessagePacketListener listener) {
        for (DataReceiver receiver : this) {
            receiver.removeMessagePacketListener(listener);
        }
    }
}
