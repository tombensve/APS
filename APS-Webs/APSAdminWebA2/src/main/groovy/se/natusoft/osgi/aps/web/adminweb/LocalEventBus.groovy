package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

/**
 * Contains one and only property. One and the same instance of this will be injected into both publishers and consumers. So
 * it is basically a wrap of the instance to make it injectable and reusable.
 */
@CompileStatic
@TypeChecked
class LocalEventBus {

    /**
     * We use RXJava's Observable subclass Subject to provide simple local messaging. We also use Map<String, Object> structure
     * as Groovy fakeish JSON. This will be converted to real JSON before going up to client.
     *
     * Do note that this is Groovy and this is thus a java bean property, so the bus instance can be accessed directly.
     */
    PublishSubject<Map<String, Object>> bus = PublishSubject.create()

    // Utilities

    /**
     * Sends an event on the local event bus.
     *
     * @param event The event to send.
     */
    void send(Map<String, Object> event) {
        this.bus.onNext(event)
    }

    /**
     * Consumes sent events.
     *
     * @param consumer A consumer of subscriptions.
     */
    Disposable consume(Consumer<? super Map<String, Object>> consumer) {
        this.bus.subscribe(consumer)
    }
}
