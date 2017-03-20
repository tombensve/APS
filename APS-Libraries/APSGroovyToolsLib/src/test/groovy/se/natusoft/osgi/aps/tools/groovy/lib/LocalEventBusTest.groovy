package se.natusoft.osgi.aps.tools.groovy.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test

@CompileStatic
@TypeChecked
class LocalEventBusTest {

    @Test
    void lebTest() throws Exception {
        LocalEventBus eventBus = new LocalEventBus();

        int res = 0

        eventBus.subscribe("nisse") { Map<String, Object> event ->
            res += event["number"] as Integer
            throw new RuntimeException("Should not effect anything!")
        }

        eventBus.subscribe("nisse") { Map<String, Object> event ->
            println "number: ${event["number"]}"
        }

        boolean warning = false, error = false

        eventBus.publish("nisse", [action: 'number', number: 5 as Object])
        eventBus.publish("nisse", [action: 'number', number: 8 as Object])

        eventBus.onError { Exception e -> assert e.message == "Should not effect anything!"; error = true }

        eventBus.publish("nisse", [action: 'number', number: 12 as Object])

        eventBus.onWarning { String message -> assert message == "There are no subscribers for 'misse'!"; warning = true }

        eventBus.publish("misse", [action: 'number', number: 24 as Object])

        assert res == 25
        assert warning
        assert error
    }

}
