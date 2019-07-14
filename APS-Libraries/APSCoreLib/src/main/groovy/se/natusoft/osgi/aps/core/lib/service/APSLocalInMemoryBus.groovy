package se.natusoft.osgi.aps.core.lib.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.docutations.Reactive
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.api.messaging.APSBusRouter
import se.natusoft.osgi.aps.api.messaging.APSTargetSpec
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSLogger

import java.util.concurrent.ConcurrentHashMap

/**
 * A very simple little bus that will call handler of any subscriber subscribing to same target
 * as sending to. No threading is done by this class, but subscription handlers that does not
 * handle the call quickly should submit their handling to an ExecutionService backed by a thread
 * pool.
 */
@CompileStatic
@TypeChecked
@SuppressWarnings( [ "UnnecessaryQualifiedReference", "unused" ] )
@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name = APS.Service.Provider, value = "aps-local-in-memory-bus"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Communication),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.Messaging),
        ]
)
class APSLocalInMemoryBus implements APSBusRouter {


    /** We only support targets with id "local"! */
    private static final APSTargetSpec targetSpec = new APSTargetSpec( id: "local" )

    //
    // Private Members
    //

    private Map<String/*target*/, Map<ID, List<APSHandler<Map<String, Object>>>>> subscribers = new ConcurrentHashMap<>()

    @Managed(loggingFor = "APSLocalInMemoryBus")
    private APSLogger logger

    //
    // Methods
    //

    /**
     * Sends a message.
     *
     * @param target The target to send to. How to interpret this is up to implementation.
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler The handler to call with result of operation. Can be null!
     */
    @Reactive
    @Override
    void send( @NotNull String target, @NotNull Map<String, Object> message,
               @Nullable APSHandler<APSResult> resultHandler ) {

        if ( this.targetSpec.valid( target ) ) {

            APSTargetSpec sendTargetSpec = new APSTargetSpec( target )

            Map<ID, List<APSHandler<Map<String, Object>>>> subs =
                    this.subscribers.computeIfAbsent( sendTargetSpec.address ) { Map<String, Object> byId -> new ConcurrentHashMap<>() }

            if ( !subs.isEmpty() ) {

                subs.each { ID id, List<APSHandler<Map<String, Object>>> handlers ->

                    handlers.each { APSHandler<Map<String, Object>> handler ->

                        try {
                            handler.handle( message )
                        } catch ( Exception e ) {
                            this.logger.error( "Message handler threw illegal exception!", e )
                        }
                    }
                }

                if ( resultHandler != null ) {
                    resultHandler.handle( APSResult.success( null ) )
                }
            }
            else if ( resultHandler != null ) {

                resultHandler.handle( APSResult.failure( new APSValidationException( "No subscribers!" ) ) )
            }
        }
        else if ( resultHandler != null ) {

            resultHandler.handle(
                    APSResult.failure( new APSValidationException( "'target' does not start with '${this.targetSpec.id}'!" ) )
            )
        }
    }

    /**
     * Subscribes to messages to a target.
     *
     * @param id A unique ID to associate subscription with. Also used to unsubscribe.
     * @param target The target to subscribe to.
     * @param resultHandler The result of the subscription.
     * @param messageHandler The handler to call with messages sent to target.
     */
    @Reactive
    @Override
    void subscribe( @NotNull ID id, @NotNull String target,
                    @Nullable @Optional APSHandler<APSResult> resultHandler,
                    @NotNull APSHandler<Map<String, Object>> messageHandler ) {

        if ( this.targetSpec.valid( target ) ) {

            APSTargetSpec subscribeTargetSpec = new APSTargetSpec( target )

            this.subscribers.computeIfAbsent( subscribeTargetSpec.address ) { new ConcurrentHashMap<>() }
                    .computeIfAbsent( id ) { new LinkedList<>() }
                    .add( messageHandler )

            if ( resultHandler != null ) {

                resultHandler.handle( APSResult.success( null ) )
            }
        }
        else if ( resultHandler != null ) {

            resultHandler.handle(
                    APSResult.failure(
                            new APSValidationException( "'target' does not start with '${this.targetSpec.id}'!" )
                    )
            )
        }
    }

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    @Reactive
    @Override
    void unsubscribe( @NotNull ID subscriberId ) {

        this.subscribers.each { String key, Map<ID, List<APSHandler<Map<String, Object>>>> value ->

            value.remove( subscriberId )
        }
    }

}
