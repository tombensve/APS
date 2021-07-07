/*
 *
 * PROJECT
 *     Name
 *         APS Filesystem Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides access to a service/application private filesystem that remains until the
 *         service/application specifically deletes it. This is independent of the OSGi server
 *         it is running in (if configured).
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
 *         2011-08-03: Created!
 *
 */
package se.natusoft.osgi.aps.core.filesystem.service;

import org.osgi.framework.BundleContext;
import se.natusoft.aps.activator.annotation.Initializer;
import se.natusoft.aps.activator.annotation.Managed;
import se.natusoft.aps.activator.annotation.APSPlatformServiceProperty;
import se.natusoft.aps.activator.annotation.APSPlatformServiceProvider;
import se.natusoft.aps.api.core.filesystem.model.APSFilesystem;
import se.natusoft.aps.api.core.filesystem.service.APSFilesystemService;
import se.natusoft.aps.constants.APS;
import se.natusoft.osgi.aps.core.filesystem.model.APSFilesystemImpl;
import se.natusoft.osgi.aps.exceptions.APSIOException;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;
import se.natusoft.osgi.aps.util.APSLogger;

import java.io.File;
import java.io.IOException;

/**
 * Provides an implementation of APSFilesystemService.
 */
@SuppressWarnings( "Duplicates" )
@APSPlatformServiceProvider(
        properties = {
                @APSPlatformServiceProperty( name = APS.Service.Provider, value = "aps-filesystem-service-provider" ),
                @APSPlatformServiceProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Storage ),
                @APSPlatformServiceProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Storage ),
                @APSPlatformServiceProperty( name = APS.Service.PersistenceScope, value = APS.Value.Service.PersistenceScope.Permanent )
        }
)
public class APSFilesystemServiceProvider implements APSFilesystemService {
    //
    // Private Members
    //

    /**
     * Our logger.
     */
    @Managed
    private APSLogger logger = null;

    @Managed
    private BundleContext context;

    /**
     * The filesystem root catalog.
     */
    private String apsFSRoot = null;

    //
    // Init
    //

    @Initializer
    public void init() {
        this.apsFSRoot = getFSRoot( this.context );
    }

    //
    // Methods
    //

    /**
     * Returns the file system root.
     *
     * @param bcontext The bundle context
     */
    private String getFSRoot( BundleContext bcontext ) {
        String fsRoot = System.getProperty( APSFilesystemService.CONF_APS_FILESYSTEM_ROOT );
        if ( fsRoot == null ) {
            String userHome = System.getProperty( "user.home" );
            if ( userHome != null ) {
                fsRoot = userHome + File.separator + ".apsHome" + File.separator + "filesystems";
                this.logger.info( "The system property '" + APSFilesystemService.CONF_APS_FILESYSTEM_ROOT +
                        "' was not found so we look in '" + fsRoot + "' instead!" );
            }
            else if ( bcontext != null ) {
                fsRoot = bcontext.getDataFile( "." ).getAbsolutePath();
                this.logger.error( "The '" + APSFilesystemService.CONF_APS_FILESYSTEM_ROOT + "' system property " +
                        "was not found and system property 'user.home' was not found either so I default to " +
                        "the file path provided by the bundle context: '" + fsRoot + "'! PLEASE NOTE THAT THIS " +
                        "PATH IS NOT VERY PERSISTENT!" );
            }
        }

        File fsRootFile = new File( fsRoot );
        if ( !fsRootFile.exists() ) {
            if ( !fsRootFile.mkdirs() ) {
                this.logger.error( "Failed to create filesystem root path: '" + fsRootFile.getAbsolutePath() + "'!" );
            }
        }

        return fsRoot;
    }


    /**
     * Returns the filesystem for the specified owner. If the filesystem does not exist it is created.
     *
     * @param owner   The owner of the filesystem or rather a unique identifier of it.
     * @param handler Called with the filesystem.
     * @throws APSIOException on failure.
     */
    @Override
    public void getFilesystem( String owner, APSHandler<APSResult<APSFilesystem>> handler ) {
        if (this.apsFSRoot == null) {
            this.apsFSRoot = getFSRoot( null );
        }
        try {
            APSFilesystemImpl fs = new APSFilesystemImpl( this.apsFSRoot, owner );
            handler.handle( APSResult.success( fs ) );
        } catch ( APSIOException ioe ) {
            handler.handle( APSResult.failure( ioe ) );
        }
    }

    /**
     * Removes the filesystem and all files in it.
     *
     * @param owner   The owner of the filesystem to delete.
     * @param handler
     * @throws APSIOException on any failure.
     */
    @Override
    public void deleteFilesystem( String owner, APSHandler<APSResult<Void>> handler ) {

        getFilesystem( owner, ( result ) -> {
            if ( result.success() ) {
                try {
                    result.result().content().getRootDirectory().recursiveDelete();
                    handler.handle( APSResult.success( null ) );
                } catch ( IOException ioe ) {
                    handler.handle( APSResult.failure( ioe ) );
                }
            }
            else {
                handler.handle( APSResult.failure( result.failure() ) );
            }
        } );

    }
}
