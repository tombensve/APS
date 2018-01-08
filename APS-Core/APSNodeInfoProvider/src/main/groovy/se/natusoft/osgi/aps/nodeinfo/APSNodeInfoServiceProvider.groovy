package se.natusoft.osgi.aps.nodeinfo

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.Lock
import org.osgi.framework.ServiceReference
import se.natusoft.docutations.Issue
import se.natusoft.docutations.NotUsed
import se.natusoft.osgi.aps.api.core.platform.model.NodeInfo
import se.natusoft.osgi.aps.api.core.platform.service.APSNodeInfoService
import se.natusoft.osgi.aps.api.reactive.APSAsyncValue
import se.natusoft.osgi.aps.api.reactive.APSHandler
import se.natusoft.osgi.aps.core.lib.DelayedExecutionHandler
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.*
import se.natusoft.osgi.aps.tools.models.UUID

// This is only for internal use! Due to the issue I'm currently declaring it here.
@Issue(id = "IDEA-184624", url = "https://youtrack.jetbrains.com/issue/IDEA-184624")
enum Actions {

    PublishNodeInfo,
    GetNodeInfo,
    GoodbyeNodeInfo
}

/**
 * Provides an implementation of the APSPlatformService.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
@OSGiServiceProvider
class APSNodeInfoServiceProvider implements APSNodeInfoService {

    private static final String THRONE_ADDRESS = "aps-node-info-throne"
    private static final String COMMON_ADDRESS = "aps-node-info-common"
    private static final String ABDICATION = "ABDICATED"

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-node-info:provider")
    private APSLogger logger

    @OSGiService(additionalSearchCriteria = "(vertx-object=Vertx)", timeout = "forever")
    private APSServiceTracker<Vertx> vertxTracker

    private NodeInfo localNode = new NodeInfo()
            .setLocalNode( true )
            .setMaster( false )
            .setAddress( System.getProperty( "aps-node-address", new UUID().toString() ) )
            .setName( System.getProperty( "aps-node-id", new UUID().toString() ) )
            .setPurpose( System.getProperty( "aps-node-purpose", "development" ) )
            .setDescription( System.getProperty( "aps-node-description", "No descriptoin provided!" ) )

    private Vertx vertx = null

    /** localNode gets added to this as does all received nodes. This always hold the current known nodes. */
    private Map<String, NodeInfo> nodes = [ : ]

    /**
     * If Vertx have not been received yet when a service method is called then the result callback is added to
     * this so that the callbacks can be executed later when Vertx is available and we have more information
     * to deliver.
     */
    private DelayedExecutionHandler<Closure<Void>> delayedExecutionHandler = new DelayedExecutionHandler<>()

    //
    // Constructors
    //

    /**
     * Creates a new APSPlatformServiceProvider instance.
     */
    APSNodeInfoServiceProvider() {}

    //
    // Enums
    //

    //
    // Methods
    //

    /**
     * This gets called after dependency injections are done.
     */
    @Initializer
    void init() {
        this.vertxTracker.onActiveServiceAvailable = this.&startup
        this.vertxTracker.onActiveServiceLeaving = this.&layLow
        nodes[ localNode.address ] = localNode
    }

    /**
     * Returns a description of the platform instance / installation.
     */
    @Override
    void nodeDescriptions( APSHandler<APSAsyncValue<List<NodeInfo>>> handler ) {

        List<NodeInfo> nodeList = [ ]
        nodes.each { String key, NodeInfo node ->
            nodeList << node
        }

        // Lesson learned:
        //    Closure<void> is not legal.
        //    Closure<Void> expects a return object of type Void!
        //    Closure without generics declaration is the way to go for no return value.
        //
        //    I always though that 'Closure' and 'Closure<Object>' was the same thing! Apparently not.
        //    But this is not Java ...

        Closure handle = {
            handler.handle( new APSAsyncValue.Provider( nodeList ) )
        }

        if ( this.vertx != null ) {
            handle()
        }
        else {
            this.delayedExecutionHandler << handle
        }
    }

    /**
     * @return Info about the local node.
     */
    @Override
    void localNode( APSHandler<APSAsyncValue<NodeInfo>> handler ) {

        Closure handle = {
            handler.handle( new APSAsyncValue.Provider<NodeInfo>( this.localNode ) )
        }

        if ( this.vertx != null ) {
            handle()
        }
        else {
            this.delayedExecutionHandler << handle
        }
    }

    /**
     * @return The master node.
     */
    @Override
    void masterNode( APSHandler<APSAsyncValue<NodeInfo>> handler ) {

        Closure handle = {
            handler.handle(
                    new APSAsyncValue.Provider<NodeInfo>(
                            this.nodes.find { Map.Entry<String, NodeInfo> entry -> entry.value.master }.value
                    )
            )
        }

        if ( this.vertx != null ) {
            handle()
        }
        else {
            this.delayedExecutionHandler << handle
        }
    }

    @BundleStop
    void shutdown() {
        if ( this.localNode.isMaster() ) {
            abdicate()
        }
    }

    /**
     * We have lost Vertx, but is still ready to do our job if it comes back.
     */
    private void layLow( @NotUsed ServiceReference sr, @NotUsed Class api ) {
        if ( this.localNode.isMaster() ) {
            abdicate() // Will most probably fail since this means that Vertx is down ...
                       // TODO: Must add callback to APSVertxProvider to get notified before Vertx goes away!
        }
    }

    /**
     * This gets called every time a new Vertx instance is available. There should be only one at a time,
     * so if this is called more than once then the current have left and a new arrived.
     *
     * @param vertx The Vertx instance.
     * @param sr Vertx OSGi published ServiceReference. Not used.
     */
    private void startup( Vertx vertx, @NotUsed ServiceReference sr ) {
        this.vertx = vertx

        // Gets notification of abdication from current king. Will trigger a new fight for the throne.
        this.vertx.eventBus().consumer( THRONE_ADDRESS ) { Message message ->
            if ( message.body().toString() == ABDICATION ) {
                fightForThrone()
            }
        }

        // Since we are starting up we should take the fight now also.
        fightForThrone()

        // Listen for published messages on the node info common address. This will receive
        // information about other nodes in the cluster.
        this.vertx.eventBus().consumer( COMMON_ADDRESS ) { Message message ->
            Map<String, Object> msg = message.body() as Map<String, Object>

            String action = msg[ 'action' ] as String

            if ( action == Actions.PublishNodeInfo.name() ) {
                NodeInfo ni = new NodeInfo()
                Map<String, String> nodeInfo = msg[ 'node-info' ] as Map<String, String>

                ni.master = ( nodeInfo as Map<String, Boolean> )[ 'master' ]
                ni.masterAddress = nodeInfo[ 'masterAddress' ]
                ni.address = nodeInfo[ 'address' ]
                ni.description = nodeInfo[ 'description' ]
                ni.name = nodeInfo[ 'name' ]
                ni.purpose = nodeInfo[ 'purpose' ]

                this.nodes.put( ni.address, ni )
                this.logger.info( "Just received new node with address: ${ni.address}" )
            }
            else if ( action == Actions.GoodbyeNodeInfo.name() ) {
                Map<String, String> nodeInfo = msg[ 'node-info' ] as Map<String, String>
                String address = nodeInfo[ 'address' ]
                this.nodes.remove( address )
                this.logger.info( "Node with address '${address}' left!" )
            }
            else if ( action == Actions.GetNodeInfo.name() ) {
                publishLocalNode()
                this.logger.info( "Responded to 'get-node-info' message." )
            }
            else {
                this.logger.error( "Received unknown message:${msg}" )
            }
        }

        publishLocalNode()
        askForOtherNodes()

        Thread.sleep( 1000 ) // We can allow one second to catch up with other nodes.
        this.delayedExecutionHandler.execute { Closure<Void> handler -> handler() }

    }

    private void publishLocalNode() {
        this.vertx.eventBus().publish( COMMON_ADDRESS, [
                action     : Actions.PublishNodeInfo.name(),
                'node-info': [
                        master       : this.localNode.master,
                        masterAddress: this.localNode.masterAddress,
                        address      : this.localNode.address,
                        description  : this.localNode.description,
                        name         : this.localNode.name,
                        purpose      : this.localNode.purpose
                ]
        ] )
    }

    private void sayGoodBye() {
        this.vertx.eventBus().publish( COMMON_ADDRESS, [
                action     : Actions.GoodbyeNodeInfo.name(),
                'node-info': [
                        master       : this.localNode.master,
                        masterAddress: this.localNode.masterAddress,
                        address      : this.localNode.address,
                        description  : this.localNode.description,
                        name         : this.localNode.name,
                        purpose      : this.localNode.purpose
                ]
        ] )
    }

    private void askForOtherNodes() {
        this.vertx.eventBus().publish( COMMON_ADDRESS, [
                action: Actions.GetNodeInfo.name()
        ] )
    }

    private void fightForThrone() {
        vertx.sharedData().getLock( "aps-node-mgr" ) { AsyncResult<Lock> lres ->

            vertx.sharedData().getClusterWideMap( "aps-node-mgr" ) { AsyncResult<AsyncMap<String, Object>> mres ->

                mres.result().get( "master" ) { AsyncResult<Object> gres ->

                    if ( gres.result() != null ) {

                        this.localNode.setMaster( false )
                        this.localNode.setMasterAddress( gres.result().toString() )
                        this.logger.info( "Available with local address ${this.localNode.getAddress()}." )
                    }
                    else {

                        // Set our address as master address.
                        mres.result().put( "master", this.localNode.getAddress() ) { AsyncResult<Void> pres ->

                            this.localNode
                                .setMaster( true )
                                .setMasterAddress( this.localNode.getAddress() )
                        }

                        this.logger.info( "Available with local address ${this.localNode.getAddress()}. Is master!" )
                    }
                }
            }

            // Release the lock.
            lres.result().release()
        }
    }

    private void abdicate() {

        if ( this.localNode.isMaster() ) {
            this.localNode.setMaster( false )

            vertx.sharedData().getLock( "aps-node-mgr" ) { AsyncResult<Lock> lres ->

                vertx.sharedData().getClusterWideMap( "aps-node-mgr" ) { AsyncResult<AsyncMap<String, Object>> mres ->

                    mres.result().remove( "master" ) { AsyncResult<Object> rres ->
                        this.logger.info( "[${this.localNode.address}]: Abdicated!" )
                    }

                    // Release the lock.
                    lres.result().release()
                }
            }

            this.vertx.eventBus().publish( THRONE_ADDRESS, "ABDICATED" )

            this.vertx = null

        }
    }
}

