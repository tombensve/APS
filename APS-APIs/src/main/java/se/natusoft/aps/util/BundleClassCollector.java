/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2017-01-05: Created!
 *
 */
package se.natusoft.aps.util;

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
     * @param bundle    The bundle to get class entries for.
     * @param startPath The start path to look for entries.
     * @param logger    An optional logger, can be null.
     */
    public static List<Class> collectClassEntries( Bundle bundle, String startPath, APSLogger logger ) {

        List<Class> bundleClasses = new LinkedList<>();

        collectClassEntries( bundle, bundleClasses, startPath, logger );

        return bundleClasses;
    }

    /**
     * Populates the entries list with recursively found class entries.
     *
     * @param bundle    The bundle to get class entries for.
     * @param entries   The list to add the class entries to.
     * @param startPath The start path to look for entries.
     * @param logger    An optional logger, can be null.
     */
    public static void collectClassEntries( Bundle bundle, List<Class> entries, String startPath, APSLogger logger ) {

        Enumeration entryPathEnumeration = bundle.getEntryPaths( startPath );

        while ( entryPathEnumeration != null && entryPathEnumeration.hasMoreElements() ) {

            String entryPath = entryPathEnumeration.nextElement().toString();
            if ( entryPath.endsWith( "/" ) ) {

                collectClassEntries( bundle, entries, entryPath, logger );
            }
            else if ( entryPath.endsWith( ".class" ) ) {

                try {
                    String classQName = entryPath.substring( 0, entryPath.length() - 6 )
                            .replace( File.separatorChar, '.' );

                    if ( classQName.startsWith( "WEB-INF.classes." ) ) {

                        classQName = classQName.substring( 16 );
                    }

                    // We skip log for Groovy closures. They are embedded in parent class and not separate classes.
                    if ( !classQName.contains( "closure" ) ) {

                        try {

                            Class entryClass = bundle.loadClass( classQName );

                            // If not activatorMode is true then there will be classes in this list already on the first
                            // call to this method. Therefore we skip duplicates.
                            if ( !entries.contains( entryClass ) ) {

                                entries.add( entryClass );
                            }
                        } catch ( NullPointerException npe ) {

                            if ( logger != null ) logger.error( npe.getMessage(), npe );
                        }
                    }
                } catch ( ClassNotFoundException | NoClassDefFoundError cnfe ) {

                    if ( logger != null ) logger.warn( "Failed to load bundle class!", cnfe );
                }
            }
        }
    }
}
