package se.natusoft.aps.core.serviceloadertest.services;

import com.google.auto.service.AutoService;
import se.natusoft.aps.core.serviceloadertest.api.TestServiceOne;
import se.natusoft.aps.core.serviceloadertest.api.TestServiceTwo;

import static se.natusoft.aps.core.APSServiceLocator.*;

@AutoService(TestServiceTwo.class)
public class TestServiceTwoProvider implements TestServiceTwo {

    private final TestServiceOne tso = locateService( TestServiceOne.class );

    public TestServiceTwoProvider() {
        System.out.println("---> tso: " + tso);
    }

    @Override
    public void showMessage() {

        String message = this.tso.getMessage();
        System.out.println( message );

    }
}
