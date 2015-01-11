package se.natusoft.osgi.aps.tools;

import org.junit.Test;
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools;
import se.natusoft.osgi.aps.test.tools.TestBundle;
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStart;
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop;
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer;

import static org.junit.Assert.assertEquals;

public class APSActivatorNonServiceTest {

    @Test
    public void nonServiceInstanceTest() throws Exception {
        OSGIServiceTestTools testTools = new OSGIServiceTestTools();
        TestBundle testBundle = testTools.createBundle("test-bundle");
        testBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/tools/APSActivatorNonServiceTest.class"
        );

        APSActivator activator = new APSActivator();
        activator.start(testBundle.getBundleContext());

        try {
            assertEquals("bundleStart() was never called!", "Jeehaa!", System.getProperty("test.bundle.start"));
            assertEquals("init() was never called!", "init", System.getProperty("test.bundle.init"));
        }
        finally {
            activator.stop(testBundle.getBundleContext());
        }
    }

    // APSActivator should identify this class as a "managed" class, instantiate it, and call all of the
    // annotated methods below.

    @BundleStart
    public void bundleStart() {
        System.out.println("In bundleStart()!");
        System.setProperty("test.bundle.start", "Jeehaa!");
    }

    @BundleStop
    public void bundleStop() {
        System.out.println("In bundleStop()!");
    }

    @Initializer
    public void init() {
        System.out.println("In init()!");
        if (!System.getProperty("test.bundle.start").equals("Jeehaa!")) throw new RuntimeException("bundleStart() should have been called before the init() method!");
        System.setProperty("test.bundle.init", "init");
    }
}
