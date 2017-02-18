package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
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
     */
    PublishSubject<Map<String, Object>> bus = PublishSubject.create()

}
