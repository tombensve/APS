package se.natusoft.aps.core.serviceloadertest.services;

import com.google.auto.service.AutoService;
import se.natusoft.aps.core.serviceloadertest.api.TestServiceOne;
import se.natusoft.aps.core.serviceloadertest.api.TestServiceTwo;

import java.util.List;

import static se.natusoft.aps.core.APSServiceLocator.*;

@AutoService( TestServiceTwo.class )
public class TestServiceTwoProvider implements TestServiceTwo {

    private final TestServiceOne oneService = apsService( TestServiceOne.class );
    private final List<TestServiceOne> oneServices = apsServices( TestServiceOne.class );
    // TODO: Missing test for annotation filtering!

    public TestServiceTwoProvider() {
        System.out.println( "---> tso: " + oneService );
    }

    @Override
    public void showMessage() {

        final String valid = "This is a test This is TestServiceOnePointTwo!";

        assert this.oneServices.size() == 2;

        // Make sure we get the same instance!
        assert this.oneServices.get( 0 ) == this.oneService;

        System.out.println( "#### Test #1 ####" );
        System.out.println( this.oneService.getMessage() );
        assert valid.contains( this.oneService.getMessage() );

        System.out.println( "#### Test #2 ###" );
        this.oneServices.forEach( service -> {
            System.out.println( service.getMessage() );
            assert valid.contains( service.getMessage() );
        } );
    }
}
