/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2018-05-26: Created!
 *
 */
package se.natusoft.osgi.aps.tools;

import org.junit.Assert;
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
                .publish("nisse", MapBuilder.map("action:", "number", "number:", 5))
                .publish("nisse", MapBuilder.map("action:", "number", "number:", 8))
                .onError(e -> {
                    Assert.assertEquals(e.getMessage(), "Should not effect anything!");
                    error = true;
                })
                .publish("nisse", MapBuilder.map("action:", "number", "number:", 12))
                .onWarning(message -> {
                    Assert.assertEquals("There are no subscribers for 'misse'!", message);
                    warning = true;
                })
                .publish("misse", MapBuilder.map("action:", "number", "number:", 24));

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
