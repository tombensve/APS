/* 
 * 
 * PROJECT
 *     Name
 *         APS Vertx Cluster DataStore Service Provider
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
 *         2019-08-17: Created!
 *         
 */
package se.natusoft.osgi.aps.datastore

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.Lock
import io.vertx.core.shareddata.SharedData
import se.natusoft.osgi.aps.types.APSLockable
import se.natusoft.osgi.aps.api.core.store.APSLockableDataStoreService
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider

@SuppressWarnings("GroovyUnusedDeclaration")
@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name = APS.Service.Provider, value = "aps-vertx-cluster-datastore-service-provider"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Network),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.Storage),
                @OSGiProperty(name = APS.Service.PersistenceScope, value = APS.Value.Service.PersistenceScope.Clustered),
        ]
)
@CompileStatic
@TypeChecked
class APSVertxClusterDataStoreServiceProvider implements APSLockableDataStoreService {

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-vertx-cluster-datastore-service-provider")
    private APSLogger logger

    @OSGiService(additionalSearchCriteria = "(vertx-object=SharedData)", timeout = "15 sec", nonBlocking = true)
    private SharedData sharedData

    //
    // Methods
    //

    private void showISEMessage() {
        this.logger.info("#####################################################################")
        this.logger.info("IllegalStateException was thrown! This is OK. Its because our Vert.x ")
        this.logger.info("instance is non clustered! This is done by many tests to not cause")
        this.logger.info("problems during tests in building since multiple simultaneous jobs")
        this.logger.info("might find each other and thus cause behavior changes for the test.")
        this.logger.info("APSConfigProvider is using the cluster data store for config data.")
        this.logger.info("The tests however provide their own default config values, and if ")
        this.logger.info("these cannot be stored in the cluster, that is OK. It will not affect")
        this.logger.info("the test.")
        this.logger.info("#####################################################################")
    }

    private void handleISE(IllegalStateException ise) {
        String clusterProp = System.getProperty( "aps.vertx.clustered" )
        if (clusterProp.trim().equals( "false" )) {
            showISEMessage(  )
        }
        else {
            throw ise
        }
    }

    private <K, V> void getClusterWideMap( String name, Handler<AsyncResult<AsyncMap<K, V>>> resultHandler) {
        try {
            this.sharedData.getClusterWideMap(  name, resultHandler )
        }
        catch ( IllegalStateException ise ) {
            handleISE( ise )
        }
    }

    /**
     * Stores a value in the store.
     *
     * The key must be in 2 parts separated by a dot! The first is the client specific id, and the second is
     * the key of a map returned for the client id. Use the name of the calling service as the first part
     * for example.
     *
     * @param key The value key. See comment on key above! Must be in 2 parts.
     * @param value The value to store.
     * @param result Indicates the success or failure of the operation.
     *
     * @throws APSValidationException on bad key format. This is validated and thrown immediately before
     *                                any async operations!
     */
    @Override
    void store( String key, Object value, APSHandler<APSResult> result ) {

        String[] keyParts = key.split( "\\." )
        if ( keyParts.length < 2 ) {
            throw new APSValidationException( "Bad key value! Must be in store-client-key.map-key format!" )
        }

        this.sharedData.getClusterWideMap( keyParts[ 0 ] ) { AsyncResult<AsyncMap<String, Object>> mres ->
            mres.result().put( keyParts[ 1 ], value ) { AsyncResult pres ->
                if ( pres.failed() ) {
                    try {
                        result.handle( APSResult.failure( pres.cause() as Exception ) )
                    }
                    finally {
                        this.logger.error( "Store of value with key '${key}' failed!", pres.cause() )
                    }
                }

                else if ( pres.succeeded() ) {
                    result.handle( APSResult.success( null ) )
                }
                else {
                    this.logger.error( "Apparently Vertx SharedData operation can neither fail nor succeed!" )
                }
            }
        }
    }

    /**
     * Retrieves a value from the store.
     *
     * @param key The key for the value.
     * @param result The handler to receive the value.
     */
    @Override
    void retrieve( String key, APSHandler<APSResult<Object>> result ) {
        String[] keyParts = key.split( "\\." )
        if ( keyParts.length < 2 ) {
            throw new APSValidationException( "Bad key value! Must be in store-client-key.map-key format!" )
        }

        this.sharedData.getClusterWideMap( keyParts[ 0 ] ) { AsyncResult<AsyncMap<String, Object>> mres ->
            mres.result().get( keyParts[ 1 ] ) { AsyncResult<Object> valueRes ->
                handleValueResult( key, valueRes, result )
            }
        }
    }

    /**
     * Removes a value from the store.
     *
     * @param key The key for the value.
     * @param result The handler to receive the value.
     */
    @Override
    void remove( String key, APSHandler<APSResult<Object>> result ) {
        String[] keyParts = key.split( "\\." )

        if ( keyParts.length < 2 ) {
            throw new APSValidationException( "Bad key value! Must be in store-client-key.map-key format!" )
        }

        this.sharedData.getClusterWideMap( keyParts[ 0 ] ) { AsyncResult<AsyncMap<String, Object>> mres ->

            mres.result().remove( keyParts[ 1 ] ) { AsyncResult<Object> valueRes ->

                handleValueResult( key, valueRes, result )
            }
        }
    }

    /**
     * Common handling of result value from get() and remove().
     *
     * @param key The original key passed.
     * @param valueRes The value result.
     * @param result The final result handler to call with result.
     */
    private void handleValueResult( String key, AsyncResult valueRes, APSHandler<APSResult> result ) {
        if ( valueRes.failed() ) {

            try {
                result.handle( APSResult.failure( valueRes.cause() as Exception ) )
            }
            finally {
                this.logger.error( "Failed to fetch value for key '${key}'!", valueRes.cause() )
            }
        }

        else if ( valueRes.succeeded() ) {

            result.handle( APSResult.success( valueRes.result() ) )
        }
        else {
            this.logger.error( "Apparently Vertx SharedData operation can neither fail nor succeed!" )
        }
    }

    /**
     * Acquires a lock, and on success also provides an APSLock instance. Do note that even if the APSLock instance
     * is not used to release the lock, the lock should be released before the return of this method call!
     *
     * @param lockId Something that identifies what to be locked.
     * @param resultHandler A handler in which whatever is locked can be used if result indicates success.
     */
    @Override
    void lock( Object lockId, APSHandler<APSResult<APSLock>> resultHandler ) {
        String[] keyParts = lockId.toString().split( "\\." )

        if ( keyParts.length < 2 ) {
            throw new APSValidationException( "Bad key value! Must be in store-client-key.map-key format!" )
        }

        this.sharedData.getLock( keyParts[ 0 ] ) { AsyncResult<Lock> mapLock ->

            if ( mapLock.succeeded() ) {

                Lock lock = mapLock.result()

                try {

                    resultHandler.handle( APSResult.success( (APSLock) new VxLock( lock: lock ) ) )
                }
                catch ( IllegalStateException ise ) {
                    showISEMessage(  )
                    handleISE( ise )
                }
                finally {

                    // Note that there is no way to check if this has already been released! I'm assuming
                    // that the release() call can be made multiple times without throwing an exception, or
                    // otherwise failing.
                    lock.release()
                }

            }

            else {
                resultHandler.handle( APSResult.failure( mapLock.cause(  ) ))
            }
        }

    }

}

/**
 * Provides an implementation of APSLock.
 */
class VxLock implements APSLockable.APSLock {

    Lock lock

    @Override
    void release( APSHandler<APSResult> resultHandler ) {
        this.lock.release()
        if ( resultHandler != null ) {
            resultHandler.handle( APSResult.success( null ) )
        }
    }

    String toString() {
        "{ lock: ${this.lock} }"
    }
}
