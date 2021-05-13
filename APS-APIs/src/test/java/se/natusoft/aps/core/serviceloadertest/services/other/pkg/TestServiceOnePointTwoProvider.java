package se.natusoft.aps.core.serviceloadertest.services.other.pkg;

import com.google.auto.service.AutoService;
import se.natusoft.aps.core.serviceloadertest.api.TestServiceOne;

@AutoService(TestServiceOne.class)
public class TestServiceOnePointTwoProvider implements TestServiceOne {
    @Override
    public String getMessage() {
        return "This is TestServiceOnePointTwo!";
    }
}
