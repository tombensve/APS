package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.docutations.Reactive;
import se.natusoft.osgi.aps.exceptions.APSValidationException;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;
import se.natusoft.osgi.aps.types.ID;
import se.natusoft.osgi.aps.util.APSLogger;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A very simple little bus that will call handler of any subscriber subscribing to same target
 * as sending to. No threading is done by this class, but subscription handlers that does not
 * handle the call quickly should submit their handling to an ExecutionService backed by a thread
 * pool.
 */
public class APSLocalInMemoryBus implements APSBusRouter {

    @SuppressWarnings( "WeakerAccess" )
    public static final APSBusRouter ROUTER = new APSLocalInMemoryBus();

    private static final String FILTER = "local:";

    //
    // Private Members
    //

    private Map<String, Map<ID, List<APSHandler<Map<String, Object>>>>> subscribers = new ConcurrentHashMap<>();

    private APSLogger logger = new APSLogger().setLoggingFor( "APSLocalInMemoryBus" );

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
    public void send( @NotNull String target, @NotNull Map<String, Object> message,
                      @Nullable APSHandler<APSResult<Void>> resultHandler ) {

        if ( target.startsWith( FILTER ) ) {

            target = target.substring( FILTER.length() );

            Map<ID, List<APSHandler<Map<String, Object>>>> subscribers =
                    this.subscribers.computeIfAbsent( target, byId -> new LinkedHashMap<>() );

            if ( !subscribers.isEmpty() ) {

                subscribers.forEach( ( ignore, handlers ) ->
                        handlers.forEach( handler -> {
                                    try {
                                        handler.handle( message );
                                    } catch ( Exception e ) {
                                        this.logger.error( "Message handler threw illegal exception!", e );
                                    }
                                }
                        )
                );

                if ( resultHandler != null ) {
                    resultHandler.handle( APSResult.success( null ) );
                }
            }
            else if ( resultHandler != null ) {

                resultHandler.handle( APSResult.failure( new APSValidationException( "No subscribers!" ) ) );
            }
        }
        else if ( resultHandler != null ) {

            resultHandler.handle(
                    APSResult.failure( new APSValidationException( "'target' does not start with 'local:'!" ) )
            );
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
    public void subscribe( @NotNull ID id, @NotNull String target, @Nullable APSHandler<APSResult> resultHandler,
                           @NotNull APSHandler<Map<String, Object>> messageHandler ) {

        if ( target.startsWith( FILTER ) ) {

            target = target.substring( FILTER.length() );

            this.subscribers.computeIfAbsent( target, byId -> new LinkedHashMap<>() )
                    .computeIfAbsent( id, handlers -> new LinkedList<>() ).add( messageHandler );

            if ( resultHandler != null ) {

                resultHandler.handle( APSResult.success( null ) );
            }
        }
        else if ( resultHandler != null ) {

            resultHandler.handle(
                    APSResult.failure(
                            new APSValidationException( "'target' does not start with 'local:'!" )
                    )
            );
        }
    }

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    @Reactive
    @Override
    public void unsubscribe( @NotNull ID subscriberId ) {

        this.subscribers.forEach( ( key, value ) -> value.remove( subscriberId ) );
    }
}
