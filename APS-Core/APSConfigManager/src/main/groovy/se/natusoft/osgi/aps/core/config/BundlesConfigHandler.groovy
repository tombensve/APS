/*
 *
 * PROJECT
 *     Name
 *         APSConfigManager
 *     
 *     Code Version
 *         1.0.0
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
 *         2018-05-25: Created!
 *
 */
package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.BundleEvent
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.activator.annotation.BundleListener
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.util.APSLogger

/**
 * This listens to bundles and manages configurations.
 *
 * This  is how it works:
 * - Bundles MANIFEST.MF is checked for 'APS-Config-Id:', 'APS-Config-Schema:' and 'APS-Config-Default-Resource:'.
 *
 * - 'APS-Config-Id' is a key used to identify the config.
 *
 * - The 'APS-Config-Default-Resource' is used the first time a config is seen to provide default values.
 *   It is also checked for default values when managed config does not provide a value. This will happen
 *   when a new value have been added to a config at a later time.
 *
 * - An APSConfig implementation will be published as an OSGi service for each configuration, also with
 *   'APS-Config-Id=<id>' in the properties for the published service. This is used by config owner to
 *   lookup the correct APSConfig instance to use.
 */
// Nothing other than APSActivator will be referencing this, and it does it via reflection. Thereby the IDE
// cannot tell that this is actually used.
@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@TypeChecked
class BundlesConfigHandler {

    //
    // Private Members
    //

    @Managed( loggingFor = "aps-config-provider:bundle-config-handler" )
    APSLogger logger

    @Managed
    private BundleContext context

    @Managed
    private ConfigManager configManager

    //
    // Methods
    //

    /**
     * Handles bundles already running when this bundle starts.
     */
    @Initializer
    void handleAlreadyRunningBundles() {

        this.context.bundles.each { Bundle bundle ->

            this.logger.info("Already deployed: Checking bundle: ${bundle.symbolicName}")

            handleNewBundle( bundle )
        }
    }

    /**
     * This receives events from other bundles and determines if there are any new configurations to manage.
     *
     * @param event A received bundle event.
     */
    @BundleListener
    void handleEvent( @NotNull BundleEvent event ) {

        if ( event.type == BundleEvent.STARTED ) {

            handleNewBundle( event.bundle )
        }
        else if ( event.type == BundleEvent.STOPPED ) {

            handleLeavingBundle( event.bundle )
        }
    }

    /**
     * Manages config for new bundle.
     *
     * @param bundle The new bundle to manage config for.
     */
    private void handleNewBundle( @NotNull Bundle bundle ) {
        if (!(bundle.symbolicName == "aps-config-manager")) {
            this.logger.info( "New bundle: Checking bundle: ${ bundle.symbolicName }" )

            String configId = ( String ) bundle.getHeaders().get( "APS-Config-Id" )

            if ( configId != null ) {
                this.logger.info( "Found bundle with configuration id: " + configId )

                String schemaResourcePath = bundle.headers.get( "APS-Config-Schema" ) as String

                String defaultResourcePath = bundle.headers.get( "APS-Config-Default-Resource" ) as String

                if ( schemaResourcePath != null ) {
                    try {

                        this.configManager.addManagedConfig( configId, bundle, schemaResourcePath, defaultResourcePath )
                    }
                    catch ( Exception e ) {

                        this.logger.error( "Failed to loadMapJson config from: ${ schemaResourcePath } / ${ defaultResourcePath } " +
                                "for bundle '${ bundle.symbolicName }'!", e )
                    }
                }
                else {
                    this.logger.error( "Bad bundle ('${ bundle.symbolicName }')! Configuration with id '${ configId }' is " +
                            "available, but no APS-Config-Schema found!" )
                }
            }
        }
    }

    /**
     * Handles a bundle leaving.
     *
     * @param bundle The leaving bundle.
     */
    private void handleLeavingBundle( @NotNull Bundle bundle ) {

        String configId = bundle.getHeaders().get( "APS-Config-Id" )
        this.configManager.removeManagedConfig( configId )
    }
}
