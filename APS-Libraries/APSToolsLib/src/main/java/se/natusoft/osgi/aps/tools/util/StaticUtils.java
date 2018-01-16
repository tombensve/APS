package se.natusoft.osgi.aps.tools.util;

/**
 * This contains a set of static utility functions.
 */
public class StaticUtils {

    private StaticUtils() {}

    /**
     * Runs a block of code with the specified classloader as context classloader.
     *
     * @param classLoader The classloader to run with.
     * @param toRun The code to run.
     */
    public static void runWithContextClassLoader(ClassLoader classLoader, Runnable toRun) {
        ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            toRun.run();
        }
        finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }
}
