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
import se.natusoft.osgi.aps.core.lib.ValidTargetTrait
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
class APSLocalInMemoryBus implements APSBusRouter, ValidTargetTrait {

    private static final String TARGET_ID = "local"

    //
    // Private Members
    //

    private Map<String/*target*/, Map<ID, List<APSHandler<Map<String, Object>>>>> subscribers =
            new ConcurrentHashMap<>()

    @Managed(loggingFor = "APSLocalInMemoryBus")
    private APSLogger logger

    APSLocalInMemoryBus() {
        this.vttTargetId = TARGET_ID
        this.vttSupportsAll = true
    }

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

        validTarget( target ) { String realTarget ->

            Map<ID, List<APSHandler<Map<String, Object>>>> subs =
                    this.subscribers.computeIfAbsent( realTarget ) { Map<String, Object> byId -> new ConcurrentHashMap<>() }

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

        validTarget( target ) { String realTarget ->

            this.subscribers.computeIfAbsent( realTarget ) { new ConcurrentHashMap<>() }
                    .computeIfAbsent( id ) { new LinkedList<>() }
                    .add( messageHandler )

            if ( resultHandler != null ) {

                resultHandler.handle( APSResult.success( null ) )
            }
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

        // Note that we don't have a target here, and thus tries to remove from all targets.
        // The subscriberId should be unique so only one will be removed.
        this.subscribers.each { String target, Map<ID, List<APSHandler<Map<String, Object>>>> value ->

            value.remove( subscriberId )
        }
    }

    /**
     * @return true if the implementation is a required, non optional provider.
     */
    @Override
    boolean required() {
        return true
    }
}
