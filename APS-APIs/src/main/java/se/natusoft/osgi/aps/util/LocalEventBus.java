/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *         2018-05-26: Created!
 *         
 */
package se.natusoft.osgi.aps.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * This is an in local VM event bus.
 */
public class LocalEventBus {

    public interface Caller<T> {
        void call( T arg );
    }

    //
    // Private Members
    //

    /**
     * The subscribes per address.
     */
    private Map<String/*subject*/, List<Caller<Map<String, Object>/*event*/>>/*Subscriber*/> subscribers = new LinkedHashMap<>();

    /**
     * This error handler will be called on exceptions during publish.
     */
    private Caller<Exception> errorHandler;

    /**
     * This handler will be called on warnings.
     */
    private Caller<String> warningHandler;

    /**
     * For threading.
     */
    private ExecutorService executorService;

    //
    // Methods
    //

    /**
     * Supplies an ExecutorService for calling subscribers.
     *
     * @param executorService The executor service to supply.
     */
    @SuppressWarnings( "unused" ) // Optional method!
    public void setExecutorService( ExecutorService executorService ) {
        this.executorService = executorService;
    }

    /**
     * Subscribes to a subject.
     *
     * @param subject    The subject to subscribe to.
     * @param subscriber The subscriber to call with messages to the address.
     */
    public LocalEventBus subscribe( String subject, Caller<Map<String, Object>> subscriber ) {

        this.subscribers.computeIfAbsent( subject, key -> new LinkedList<>() ).add( subscriber );

        return this;
    }

    /**
     * Publish a message on the bus.
     *
     * @param subject The subject to publish to.
     * @param message The message to publish.
     */
    public LocalEventBus publish( String subject, Map<String, Object> message ) {

        List<Caller<Map<String, Object>>> subs = this.subscribers.get( subject );

        if ( subs != null ) {

            Runnable runable = () -> sendMessageToSubscribers( subs, message );

            if ( this.executorService != null ) {

                this.executorService.submit( runable );
            }
            else {
                runable.run();
            }
        }
        else {
            if ( this.warningHandler != null ) {

                this.warningHandler.call( "There are no subscribers for '" + subject + "'!" );
            }
        }

        return this;
    }

    /**
     * Actualy sends the messages to the subscribers.
     *
     * @param subscribers The subscribers to send to.
     */
    private void sendMessageToSubscribers(
            List<Caller<Map<String, Object>>> subscribers,
            Map<String, Object> message
    ) {
        subscribers.forEach( subscriber -> {

            try {
                subscriber.call( message );

            } catch ( Exception e ) {

                if ( this.errorHandler != null ) {

                    this.errorHandler.call( e );
                }
            }
        } );
    }

    /**
     * Sets an error handler.
     *
     * @param errorHandler The error handler to set.
     */
    public LocalEventBus onError( Caller<Exception> errorHandler ) {

        this.errorHandler = errorHandler;
        return this;
    }

    /**
     * Sets a warning handler.
     *
     * @param warningHandler The warning handler to set.
     */
    public LocalEventBus onWarning( Caller<String> warningHandler ) {

        this.warningHandler = warningHandler;
        return this;
    }

    /**
     * Clears all subscribers.
     */
    @SuppressWarnings( "unused" ) // Optional method.
    public void clearSubscribers() {

        this.subscribers.clear();
    }
}
