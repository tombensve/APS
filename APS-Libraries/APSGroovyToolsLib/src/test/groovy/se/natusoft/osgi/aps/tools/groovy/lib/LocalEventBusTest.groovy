package se.natusoft.osgi.aps.tools.groovy.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test

@CompileStatic
@TypeChecked
class LocalEventBusTest {

    @Test
    void lebTest() throws Exception {
        int res = 0
        boolean warning = false, error = false

        new LocalEventBus()
                .subscribe("nisse") { Map<String, Object> event ->
                    res += event["number"] as Integer
                    throw new RuntimeException("Should not effect anything!")
                }.subscribe("nisse") { Map<String, Object> event ->
                    println "number: ${event["number"]}"
                }
                .publish("nisse", [action: 'number', number: 5 as Object])
                .publish("nisse", [action: 'number', number: 8 as Object])

                .onError { Exception e -> assert e.message == "Should not effect anything!"; error = true }

                .publish("nisse", [action: 'number', number: 12 as Object])

                .onWarning { String message -> assert message == "There are no subscribers for 'misse'!"; warning = true }

                .publish("misse", [action: 'number', number: 24 as Object])

        assert res == 25
        assert warning
        assert error
    }

}
