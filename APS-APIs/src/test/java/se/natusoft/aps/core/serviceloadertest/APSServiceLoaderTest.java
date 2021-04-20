package se.natusoft.aps.core.serviceloadertest;

import org.junit.Test;
import se.natusoft.aps.core.serviceloadertest.services.TestServiceTwoProvider;

public class APSServiceLoaderTest {


    @Test
    public void simpleTest() {
        new TestServiceTwoProvider().showMessage();
    }
}
