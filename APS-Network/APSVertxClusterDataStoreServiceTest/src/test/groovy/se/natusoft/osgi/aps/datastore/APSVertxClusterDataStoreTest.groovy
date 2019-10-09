package se.natusoft.osgi.aps.datastore

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.types.APSLockable
import se.natusoft.osgi.aps.api.core.store.APSLockableDataStoreService
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.test.tools.APSOSGIServiceTestTools
import se.natusoft.osgi.aps.util.APSLogger

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@TypeChecked
class APSVertxClusterDataStoreTest extends APSOSGIServiceTestTools {

    // Note that since we use a clustered vertx there is a possibility of another build running
    // at the same time on the same subnet, joining the same cluster. That is why we store a
    // static value. If we overwrite the value for another concurrent test execution we will
    // store the same value so nothing should fail due to that.
    public static String id = "test-value"
    public static boolean stored = false
    public static String retrieved = null

    // For clarity, not pointless!!!
    @SuppressWarnings( "GroovyPointlessBoolean" )
    @Test
    void clusterStoreTest() throws Exception {
        // Most of the unfamiliar constructs here are provided by OSGiServiceTestTools and groovy DSL features.

        // For now we have to make sure we are running Vert.x clustered. This will not work with
        // unclustered vertx instance.
        String vertxClustered = System.getProperty( "aps.vertx.clustered" )
        if (vertxClustered != null && vertxClustered == "false") {
            return
        }

        println "============================================================================"
        println "DO NOTE: All the RED colored output comes from Vertx! It is not something "
        println "that have failed! Vertx have just chosen this color for their log output!"
        println "============================================================================"

        deployConfigAndVertxPlusDeps(
                dataStoreServiceDeployer( null ) {
                    deploy 'aps-vertx-cluster-data-store-provider' with new APSActivator() from 'se.natusoft.osgi.aps', 'aps-vertx-cluster-datastore-service-provider', '1.0.0'
                }
        )

        // As of version 3.8.0 of Vert.x something have happened. When the ClusterStoreTestClient class
        // gets deployed and run for some very, very weird reason the read lock and read of value gets
        // done before the write lock and write of value! And a sleep between the write and read do solve
        // the problem, but the really, really super weird thing is that this sleep also solves the problem!
        // Note that this is before "ClusterStoreTestClient" is even deployed! The only explanation I can
        // come up with is that Vert.x now threads off and returns before everything is upp and running.

        hold() maxTime 6 unit SECONDS go() // Support really slow machines.

        // This do actually fail every now and then!! Should be failsafe!!
        //hold() whilst { ClusterStoreTestClient.stored == true } maxTime 8L unit SECONDS go()

        deploy 'producer' with new APSActivator() using '/se/natusoft/osgi/aps/datastore/ClusterStoreTestClient.class'

        try {
            println ">>>>> " + new Date()
            hold() whilst { stored == false } maxTime 3L unit SECONDS go()
            hold() whilst { retrieved == null } maxTime 3L unit SECONDS go()
            println "<<<<< " + new Date()

            assert stored == true
            assert retrieved == id

        }
        finally {
            shutdown()
            hold() maxTime 500 unit MILLISECONDS go() // Give Vertx time to shut down.
        }
    }

}

@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@TypeChecked
class ClusterStoreTestClient {

    public static boolean stored = false

    @OSGiService( additionalSearchCriteria = "(service-persistence-scope=clustered)",
            nonBlocking = true )
    private APSLockableDataStoreService dataStoreService

    @Managed( loggingFor = "ClusterStoreTestClient" )
    APSLogger logger

    @Initializer
    void init() {

        this.dataStoreService.lock( "test.someId" ) { APSResult<APSLockable.APSLock> lockRes ->

            if ( lockRes.success() ) {

                this.dataStoreService.store( "test.someId", APSVertxClusterDataStoreTest.id ) { APSResult<Void> storeRes ->

                    APSVertxClusterDataStoreTest.stored = storeRes.success()
                    this.logger.info( "Stored value with result: ${ storeRes.success() }" )

                    this.logger.info( "Current lock for writing: ${ lockRes.result().content() }" )

                    stored = true
                }
            }
        }


        // Note that this retrieval directly after the store which is done in a callback on a Vertx event loop thread
        // works due to that the service always performs a lock on the value stored or fetched.

        this.dataStoreService.lock( "test.someId" ) { APSResult<APSLockable.APSLock> lockRes ->

            this.logger.info( "Current lock for reading: ${ lockRes.result().content() }" )

            this.dataStoreService.retrieve( "test.someId" ) { APSResult<UUID> result ->

                if ( result.success() ) {

                    APSVertxClusterDataStoreTest.retrieved = result.result().content()
                    this.logger.info( "Retrieved stored value: ${ result.result().content() }" )
                }
                else {

                    this.logger.error( "Retrieval failed!", result.failure() )
                }
            }
        }


    }

}
