package se.natusoft.osgi.aps.tools;

import org.junit.Test;
import se.natusoft.osgi.aps.util.LocalEventBus;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static se.natusoft.osgi.aps.util.MapBuilder.map;

public class LocalEventBusTest {

    private int res = 0;
    private boolean warning = false, error = false;

    @Test
    public void lebTest() throws Exception {

        new LocalEventBus()
                .subscribe("nisse", (Map<String, Object> event) -> {
                    NumberEvent ne = new NumberEvent(event);
                    if (ne.getAction().equals("number")) {
                        res += ne.getNumber();
                    }
                    throw new RuntimeException("Should not effect anything!");
                }).subscribe("nisse", (Map<String, Object> event) -> System.out.println("number: " + event.get("number")))
                .subscribe("tomte", (Map<String, Object> event) -> {
                    throw new RuntimeException("This should not be called!");
                })
                .publish("nisse", map("action:", "number", "number:", 5))
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

        assert res == 25;
        assert warning;
        assert error;
    }

    private static final class NumberEvent {
        private Map<String, Object> event;

        public NumberEvent(Map<String, Object> event) {
            this.event = event;
        }

        public String getAction() {
            return (String)event.get("action");
        }

        public int getNumber() {
            return (int)event.get("number");
        }
    }
}
