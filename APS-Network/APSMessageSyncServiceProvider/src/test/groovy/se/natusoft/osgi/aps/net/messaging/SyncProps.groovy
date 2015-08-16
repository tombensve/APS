package se.natusoft.osgi.aps.net.messaging

import se.natusoft.osgi.aps.api.net.messaging.service.APSSyncService
import se.natusoft.osgi.aps.api.net.messaging.types.APSSyncDataEvent
import se.natusoft.osgi.aps.api.net.messaging.types.APSSyncEvent

class SyncProps extends Properties implements APSSyncService.APSSyncListener {

    private APSSyncService syncService

    public SyncProps(APSSyncService syncService) {
        this.syncService = syncService
        this.syncService.addSyncListener(this)
    }

    @Override
    public synchronized Object setProperty(String name, String value) {
        Object obj = super.setProperty(name, value)

        this.syncService.syncData(
                new APSSyncDataEvent.Default().key(name).now().content(value.getBytes("UTF-8"))
        )

        return obj
    }

    /**
     * Called to deliver a sync event. This can currently be one of:
     *
     * * APSSyncDataEvent
     * * APSReSyncEvent
     *
     * @param syncEvent The received sync event.
     */
    @Override
    void syncDataReceived(APSSyncEvent syncEvent) {
        if (syncEvent instanceof APSSyncDataEvent) {
            String value = new String(((APSSyncDataEvent)syncEvent).content.content)
            String key = syncEvent.key
            setProperty(key, value)
        }
    }
}
