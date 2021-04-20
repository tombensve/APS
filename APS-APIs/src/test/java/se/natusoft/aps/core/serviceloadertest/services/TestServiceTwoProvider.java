package se.natusoft.aps.core.serviceloadertest.services;

import com.google.auto.service.AutoService;
import se.natusoft.aps.core.APSServiceLoader;
import se.natusoft.aps.core.annotation.APSService;
import se.natusoft.aps.core.serviceloadertest.api.TestServiceOne;
import se.natusoft.aps.core.serviceloadertest.api.TestServiceTwo;

@AutoService(TestServiceTwo.class)
public class TestServiceTwoProvider implements TestServiceTwo {

    @APSService
    private TestServiceOne testServiceOne;


    public TestServiceTwoProvider() {
        APSServiceLoader.getInstance().injectServices( this );
    }

    @Override
    public void showMessage() {

        String message = testServiceOne.getMessage();
        System.out.println( message );

    }
}
