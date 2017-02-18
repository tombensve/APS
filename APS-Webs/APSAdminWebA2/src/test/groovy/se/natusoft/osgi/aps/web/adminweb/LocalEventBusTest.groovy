package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test

@CompileStatic
@TypeChecked
class LocalEventBusTest {

    @Test
    void lebTest() throws Exception {
        LocalEventBus leb = new LocalEventBus();

        int res = 0

        leb.bus.subscribe { Map<String, Object> event ->
            res += event [ "number"] as Integer
        }

        leb.bus.onNext([ action: 'number', number:  5 as Object ])
        leb.bus.onNext([ action: 'number', number:  8 as Object ])
        leb.bus.onNext([ action: 'number', number: 12 as Object ])

        assert res == 25
    }

}
