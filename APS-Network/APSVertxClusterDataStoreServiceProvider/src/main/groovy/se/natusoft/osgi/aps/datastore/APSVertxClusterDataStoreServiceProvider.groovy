package se.natusoft.osgi.aps.datastore

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.Lock
import io.vertx.core.shareddata.SharedData
import se.natusoft.osgi.aps.api.core.store.APSDataStoreService
import se.natusoft.osgi.aps.api.reactive.APSHandler
import se.natusoft.osgi.aps.api.reactive.APSResult
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

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
class APSVertxClusterDataStoreServiceProvider implements APSDataStoreService {

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

        this.sharedData.getLock( keyParts[ 0 ] ) { AsyncResult<Lock> lres ->
            this.sharedData.getClusterWideMap( keyParts[ 0 ] ) { AsyncResult<AsyncMap<String, Object>> mres ->
                mres.result().put( keyParts[ 1 ], value ) { AsyncResult pres ->
                    try {
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
                    finally {
                        lres.result().release()
                    }
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

        this.sharedData.getLock( keyParts[ 0 ] ) { AsyncResult<Lock> mapLock ->
            this.sharedData.getClusterWideMap( keyParts[ 0 ] ) { AsyncResult<AsyncMap<String, Object>> mres ->
                mres.result().get( keyParts[ 1 ] ) { AsyncResult<Object> valueRes ->
                    handleValueResult( key, valueRes, result, mapLock )
                }
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

        this.sharedData.getLock( keyParts[ 0 ] ) { AsyncResult<Lock> mapLock ->
            this.sharedData.getClusterWideMap( keyParts[ 0 ] ) { AsyncResult<AsyncMap<String, Object>> mres ->
                mres.result().remove( keyParts[ 1 ] ) { AsyncResult<Object> valueRes ->
                    handleValueResult( key, valueRes, result, mapLock )
                }
            }
        }
    }

    /**
     * Common handling of result value from get() and remove().
     *
     * @param key The original key passed.
     * @param valueRes The value result.
     * @param result The final result handler to call with result.
     * @param mapLock The map lock.
     */
    private void handleValueResult( String key, AsyncResult<Object> valueRes, APSHandler<APSResult<Object>> result,
                                    AsyncResult<Lock> mapLock ) {
        try {
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
        finally {
            mapLock.result().release()
        }
    }
}
