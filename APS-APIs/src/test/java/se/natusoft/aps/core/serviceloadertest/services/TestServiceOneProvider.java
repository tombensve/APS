package se.natusoft.aps.core.serviceloadertest.services;

import com.google.auto.service.AutoService;
import se.natusoft.aps.core.serviceloadertest.api.TestServiceOne;

@AutoService(TestServiceOne.class)
public class TestServiceOneProvider implements TestServiceOne {
    @Override
    public String getMessage() {
        return "This is a test";
    }
}
