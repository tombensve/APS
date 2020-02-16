package se.natusoft.osgi.aps.core.msg

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.messaging.APSBus
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.APSUUID
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSExecutor
import se.natusoft.osgi.aps.util.APSLogger

/**
 * This provides support for service announcements and service discovery.
 *
 * The principles of APS message driven services are:
 *
 * - 'serviceId' has the format "service/(name)".
 * - Announcements are done on APSBus target cluster:all:(serviceId)/announce.
 * - Check for service availability are done on APSBus target cluster:(serviceId)/check. This class will listen
 *   to this target and do a new announcement when a message comes in.
 *
 * The first thing to do after creating an instance of this is to call start().
 *
 * When shutting down stop() should be called to cleanup.
 *
 * Note that what this class does are things that need doing for services and clients. It of course must
 * not be done by this class, a service can handle all this by itself. This is just a helper to make it
 * easier, and provides a bit of reusable functionality.
 *
 * ## Flow
 *     - Service
 *         - Listen to check messages
 *             - On check message.
 *                 - Send service announcement.
 *         - Listen to service messages.
 *         - Send service announcement.
 *
 *     - Client
 *         - Register announcement listener.
 *         - Send service check message.
 */
@CompileStatic
@TypeChecked
class MessageTypeServiceHelper {

    // --- Service use only --- //

    /** The id of the service this helper is helping. */
    String helperForServiceId

    // --- Common use --- //

    /** The APS platform bus. */
    APSBus bus

    /** Logger to use. */
    APSLogger logger

    /** The helper startup result handler. */
    APSHandler<APSResult<?>> resultHandler

    private List<ID> subscriptions = [ ]

    private ID checkSub = new APSUUID()


    /**
     * Groovy constructor for using property constructor. If this is used from other languages
     * the bus property must be set before this can be used.
     */
    MessageTypeServiceHelper() {}

    /**
     * This is for calling from Java and possibly other JVM languages not supporting property constructors.
     *
     * @param bus The APS platform bus.
     * @param resultHandler Return the result of the helper startup. If this return a failure result then
     *                      the helper is not working!
     */
    MessageTypeServiceHelper( APSBus bus, APSHandler<APSResult<?>> resultHandler ) {
        this.bus = bus
        this.resultHandler = resultHandler
    }

    /**
     * For java code to provide a serviceId for service use. This is only relevant for services, not clients.
     *
     * @param serviceId The id to set.
     */
    MessageTypeServiceHelper serviceId( String serviceId ) {
        this.helperForServiceId = serviceId

        this
    }

    /**
     * This needs to be called on shutdown.
     */
    void stop() {
        this.subscriptions.each { ID id ->
            this.bus.unsubscribe( id )
        }
        this.subscriptions = null
    }

    //
    // Client APIs
    //

    /**
     * This looks for a service.
     *
     * @param serviceId The service to look for.
     * @param resultHandler This will be called in 2 cases: 1) The subscription to the announce target of the service
     *                      failed, in which case you have a problem! 2) A reply from the service have been received.
     *                      This means that the service is available to be used. In this case result.success() is true.
     */
    MessageTypeServiceHelper onServiceAvailable( String serviceId, APSHandler<APSResult<?>> resultHandler ) {
        ID subscription = new APSUUID()
        this.subscriptions << subscription
        this.bus.subscribe( subscription, "cluster:${serviceId}/announce" ) { APSResult<?> res ->

            if ( !res.success() ) {

                if ( resultHandler != null ) {
                    APSExecutor.submit { resultHandler.handle( res ) }
                }
            }
            else {
                checkService( serviceId, null )
            }

        } { Map<String, Object> message ->

            this.logger.debug "OnServiceAbvailable: Message: ${message}"

            // That we get this message means that the service is available!
            this.subscriptions.remove( subscription )
            this.bus.unsubscribe( subscription )

            resultHandler.handle( APSResult.success( null ) )
        }

        this
    }

    /**
     * This will result in an announcement being done again for the service if it is available.
     *
     * @param serviceId The id of the service to look for.
     * @param resultHandler The result of the call.
     */
    MessageTypeServiceHelper checkService( String serviceId, APSHandler<APSResult<?>> resultHandler ) {

        this.bus.send( "cluster:${serviceId}/check", new ServiceCheckMessage( serviceId ) ) { APSResult<?> res ->
            this.logger.info "@@@@ Send service announcement success: ${res.success()}"
            if ( !res.success() && this.logger != null ) {

                this.logger.error( "Failed to send check message to service '${serviceId}'!", res.failure() )
            }

            if ( resultHandler != null ) {

                APSExecutor.submit { resultHandler.handle( res ) }
            }
        }

        this
    }

    /**
     * Utility to send message to service. This to complement listenToServiceMessages(...). That one makes more
     * sense, this one not as much, but it would look strange to use the bus directly only for this.
     *
     * @param serviceId The id of the service to call.
     * @param message The message to the service.
     * @param resultHandler The result of the call.
     */
    MessageTypeServiceHelper callService( String serviceId, Map<String, Object> message, APSHandler<APSResult<?>>
            resultHandler ) {
        this.bus.send( "cluster:${serviceId}" as String, message as Map<String, Object>, resultHandler as APSHandler<APSResult<?>> )

        this
    }

    //
    // Service APIs
    //

    /**
     * This should be called after creation.
     *
     * @param resultHandler
     */
    MessageTypeServiceHelper provideCheckService() {

        ID checkSub = new APSUUID()

        this.bus.subscribe( checkSub, "cluster:${this.helperForServiceId}/check" ) { APSResult<?> res ->

            if ( res.success() ) {
                this.subscriptions << checkSub
            }
            APSExecutor.submit { this.resultHandler.handle( res ) }

        } { Map<String, Object> message ->
            // We don't really care about the content of the message here, only that we got it!
            announceService() { APSResult<?> annres ->
                this.resultHandler.handle( annres )
            }
        }

        this
    }

    /**
     * Utility to listen to messages to the service. This just handles the subscription id and sets up
     * a subscription to the already provided serviceId.
     *
     * @param resultHandler Passed on to from bus.subscribe(..., ..., resultHandler, ...).
     * @param messageHandler Passed on to from bus.subscribe(..., ..., ..., messageHandler).
     */
    MessageTypeServiceHelper listenToServiceMessages( APSHandler<APSResult<?>> resultHandler, APSHandler<Map<String,
            Object>>
            messageHandler ) {

        ID subID = new APSUUID()
        this.bus.subscribe( subID, "cluster:${this.helperForServiceId}" ) { APSResult<?> res ->

            if ( res.success() ) {
                this.subscriptions << subID
            }
            APSExecutor.submit { resultHandler.handle( res ) }

        } { Map<String, Object> message ->

            messageHandler.handle( message )
        }

        this
    }

    /**
     * Sends an announcement of the service being available. This is always done to 'cluster:all:(serviceId)/announce'.
     * This means that there must be an APSBusRouter deployed that supports the 'cluster:all' part of the target.
     * APSVertxProvider supplies such.
     *
     * @param resultHandler
     */
    MessageTypeServiceHelper announceService( APSHandler<APSResult<?>> resultHandler ) {
        this.bus.send(
                "cluster:all:${this.helperForServiceId}/announce" as String,
                new ServiceAnnounceMessage( this.helperForServiceId ) as Map<String, Object>,
                resultHandler as APSHandler<APSResult<?>>
        )

        this
    }

}
