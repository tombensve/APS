package se.natusoft.osgi.aps.tools;

import org.osgi.framework.Bundle;

import java.io.File;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * This only contains static support methods for getting a list of the classes in a bundle.
 */
public class BundleClassCollector {

    /**
     * Creates and populates a list with recursively found class entries.
     *
     * @param bundle The bundle to get class entries for.
     * @param startPath The start path to look for entries.
     * @param logger An optional logger, can be null.
     */
    public static List<Class> collectClassEntries(Bundle bundle, String startPath, APSLogger logger) {
        List<Class> bundleClasses = new LinkedList<>();
        collectClassEntries(bundle, bundleClasses, startPath, logger);
        return bundleClasses;
    }

    /**
     * Populates the entries list with recursively found class entries.
     *
     * @param bundle The bundle to get class entries for.
     * @param entries The list to add the class entries to.
     * @param startPath The start path to look for entries.
     * @param logger An optional logger, can be null.
     */
    public static void collectClassEntries(Bundle bundle, List<Class> entries, String startPath, APSLogger logger) {
        Enumeration entryPathEnumeration = bundle.getEntryPaths(startPath);
        while (entryPathEnumeration.hasMoreElements()) {
            String entryPath = entryPathEnumeration.nextElement().toString();
            if (entryPath.endsWith("/")) {
                collectClassEntries(bundle, entries, entryPath, logger);
            } else if (entryPath.endsWith(".class")) {
                try {
                    String classQName = entryPath.substring(0, entryPath.length() - 6).replace(File.separatorChar, '.');
                    if (classQName.startsWith("WEB-INF.classes.")) {
                        classQName = classQName.substring(16);
                    }
                    try {
                        Class entryClass = bundle.loadClass(classQName);
                        // If not activatorMode is true then there will be classes in this list already on the first
                        // call to this method. Therefore we skip duplicates.
                        if (!entries.contains(entryClass)) {
                            entries.add(entryClass);
                        }
                    } catch (NullPointerException npe) {
                        if (logger != null) logger.error(npe.getMessage(), npe);
                        // Felix (when used by Karaf) seems to have problems here for a perfectly good classQName!
                        // No, it is not null, if it where null it would explain this!
                        //
                        // It is:
                        //
                        //     se.natusoft.osgi.aps.userservice.config.UserServiceInstConfig.UserServiceInstance
                        //
                        // that cause this NPE. It belongs to APSSimpleUserServiceProvider and is a perfectly OK
                        // class that compiles flawlessly. The classQName also contains a perfectly correct reference
                        // to the class.
                        //
                        // This is the exception thrown:
                        //
                        //   Caused by: java.lang.NullPointerException
                        //   at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.findClass(BundleWiringImpl.java:2015)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.findClassOrResourceByDelegation(BundleWiringImpl.java:1501)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.access$400(BundleWiringImpl.java:75)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.loadClass(BundleWiringImpl.java:1955)[org.apache.felix.framework-4.2.1.jar:]
                        //   at java.lang.ClassLoader.loadClass(ClassLoader.java:358)[:1.7.0_60]
                        //   at org.apache.felix.framework.BundleWiringImpl.getClassByDelegation(BundleWiringImpl.java:1374)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.searchImports(BundleWiringImpl.java:1553)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.findClassOrResourceByDelegation(BundleWiringImpl.java:1484)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.access$400(BundleWiringImpl.java:75)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.loadClass(BundleWiringImpl.java:1955)[org.apache.felix.framework-4.2.1.jar:]
                        //   at java.lang.ClassLoader.loadClass(ClassLoader.java:358)[:1.7.0_60]
                        //   at java.lang.ClassLoader.defineClass1(Native Method)[:1.7.0_60]
                        //   at java.lang.ClassLoader.defineClass(ClassLoader.java:800)[:1.7.0_60]
                        //   at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.findClass(BundleWiringImpl.java:2279)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl.findClassOrResourceByDelegation(BundleWiringImpl.java:1501)[org.apache.felix.framework-4.2.1.jar:]
                        //    at org.apache.felix.framework.BundleWiringImpl.access$400(BundleWiringImpl.java:75)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleWiringImpl$BundleClassLoader.loadClass(BundleWiringImpl.java:1955)[org.apache.felix.framework-4.2.1.jar:]
                        //   at java.lang.ClassLoader.loadClass(ClassLoader.java:358)[:1.7.0_60]
                        //   at org.apache.felix.framework.Felix.loadBundleClass(Felix.java:1844)[org.apache.felix.framework-4.2.1.jar:]
                        //   at org.apache.felix.framework.BundleImpl.loadClass(BundleImpl.java:937)[org.apache.felix.framework-4.2.1.jar:]
                        //   at se.natusoft.osgi.aps.tools.APSActivator.collectClassEntries(APSActivator.java:962)[104:aps-tools-lib:1.0.0]

                    }
                } catch (ClassNotFoundException | NoClassDefFoundError cnfe) {
                    if (logger != null) logger.warn("Failed to load bundle class!", cnfe);
                }
            }
        }
    }
}
