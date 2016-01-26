package se.natusoft.osgi.aps.test.tools;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

/**
 * Tests of TestBundle.
 */
public class TestBundleTest {

    @Test
    public void testLoadFromMaven() throws IOException {

        OSGIServiceTestTools testTools = new OSGIServiceTestTools();
        TestBundle testBundle = testTools.createBundle("test-bundle");
        testBundle.loadEntryPathsFromMaven("se.natusoft.osgi.aps", "aps-apis", "1.0.0");

        assertNotNull(testBundle.getEntry("se/natusoft/osgi/aps/annotations/APSServiceAPI.class"));
    }
}
