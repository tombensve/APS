package se.natusoft.osgi.aps.tools;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LocalEventBusTest extends MapBuilder {

    private int res = 0;
    private boolean warning = false, error = false;

    @Test
    public void lebTest() throws Exception {

        new LocalEventBus()
                .subscribe("nisse", (Map<String, Object> event) -> {
                    res += (int) event.get("number");
                    throw new RuntimeException("Should not effect anything!");
                }).subscribe("nisse", (Map<String, Object> event) -> System.out.println("number: " + event.get("number")))
                .publish("nisse", map("action::", "number", "number:", 5))
                .publish("nisse", map("action:", "number", "number:", 8))
                .onError(e -> {
                    assertEquals(e.getMessage(), "Should not effect anything!");
                    error = true;
                })
                .publish("nisse", map("action:", "number", "number:", 12))
                .onWarning(message -> {
                    assertEquals("There are no subscribers for 'misse'!", message);
                    warning = true;
                })
                .publish("misse", map("action:", "number", "number:", 24));
//        .publish("nisse", MapBuilder.map().k("action").v("number").k("number").v(5).toMap());

        assert res == 25;
        assert warning;
        assert error;
    }

}
