package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.api.messaging.APSMessage
import se.natusoft.osgi.aps.api.messaging.APSMessageSender
import se.natusoft.osgi.aps.api.messaging.APSMessageSubscriber
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.APSUUID
import se.natusoft.osgi.aps.util.APSJson
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.web.models.APSComponent

// @formatter:off
/**
 * Provides alternatives for letting the backend provide the frontend GUI. This requires that the
 * frontend uses the APSWebManager.jsx component in App.js like this:
 *
 *     render() {
 *       return (
 *         <div className="App">
 *           <APSWebManager app={"my-app-name"} />
 *         </div>
 *       );
 *     }
 *
 * To use, make your own class, inject this, and register handlers in @Initializer.
 */
// @formatter:on
@SuppressWarnings( "unused" )
@CompileStatic
@TypeChecked
class APSWebManager {

    @Managed( loggingFor = "APSWebManager" )
    private APSLogger logger

    @OSGiService( additionalSearchCriteria = "(aps-messaging-protocol=vertx-eventbus)",
            timeout = "10 seconds", nonBlocking = true )
    private APSMessageSubscriber subscriber

    @OSGiService( additionalSearchCriteria = "(aps-messaging-protocol=vertx-eventbus)",
            timeout = "10 seconds", nonBlocking = true )
    private APSMessageSender sender

    private APSUUID subscriberId = new APSUUID()

    private APSHandler<String> newGUIHandler

    private APSHandler<Map<String, Object>> guiEventHandler

    private APSHandler<Map<String, Object>> allBackendHandler

    @SuppressWarnings( "unused" )
    @Initializer
    void init() {

        this.subscriber.subscribe( "aps:${ Config.APP_NAME }:backend", this.subscriberId ) { APSResult res ->

            res.onFailure { Exception e ->
                this.logger.error( e.message, e )
            }
        } { APSMessage<Map<String, Object>> message ->

            try {
                Map<String, Object> aps = message.content().aps as Map<String, Object>

                String type = aps.type as String

                switch ( type ) {

                    case "avail":

                        if ( this.newGUIHandler != null ) {
                            this.newGUIHandler.handle( aps.origin as String )
                        }

                        break

                    case "gui-event":

                        if ( this.guiEventHandler != null ) {
                            this.guiEventHandler.handle( message.content() as Map<String, Object> )
                        }

                        break
                }
            }
            catch ( NullPointerException npe ) {
                this.logger.error( "Bad message! => ${ message.content() }", npe )
            }
        }

        this.subscriber.subscribe( "aps:${ Config.APP_NAME }:backend:all", this.subscriberId ) { APSResult res ->

            res.onFailure { Exception e ->
                this.logger.error( e.message, e )
            }
        } { APSMessage<Map<String, Object>> message ->

            if ( this.allBackendHandler != null ) {
                this.allBackendHandler.handle( message.content() )
            }
        }
    }

    @BundleStop
    void cleanup() {
        this.subscriber.unsubscribe( this.subscriberId ) { APSResult res ->

            res.onFailure(  ) { Exception e ->
                this.logger.error( "Failed to unsubscribe!", e )
            }
        }
    }

    /**
     * Registers a handler for when a new web client requests a GUI.
     *
     * @param handler The handler to call in this case. It will get the client address as parameter.
     */
    void registerNewClientHandler( APSHandler<String> handler ) {
        this.newGUIHandler = handler
    }

    /**
     * Registers a handler for when an event from a web client comes in.
     *
     * @param handler When there is a message of type "gui-event" then this gets called
     *                with the full message, both 'aps' and 'content' parts.
     */
    void registerGUIEventHandler( APSHandler<Map<String, Object>> handler ) {
        this.guiEventHandler = handler
    }

    /**
     * This gets called when there is a broadcast message to all backend from a client. This type of
     * message should be avoided if possible!
     *
     * @param handler The handler to call when this happens.
     */
    void registerAllBackendsBroadcastHandler( APSHandler<Map<String, Object>> handler ) {
        this.allBackendHandler = handler
    }

    /**
     * Helper to send a GUI spec back to web client.
     *
     * @param origin The client address
     * @param gui The GUI spec to send.
     */
    private void sendGUIToOrigin( String origin, Map<String, Object> gui ) {

//        this.logger.debug( "gui: ${ gui }" )

        Map<String, Object> reply = [
                aps    : [
                        origin: "aps:${ Config.APP_NAME }:backend",
                        app   : "${ Config.APP_NAME }",
                        type  : "gui"
                ],
                content: gui
        ] as Map<String, Object>


        println "Thread: ${ Thread.currentThread().getName() }"

        this.sender.send( origin, reply ) { APSResult result ->

            result.onFailure() { Exception e ->
                this.logger.error( "Failed to send GUI to client!", e )
            }
        }

//        this.logger.debug( "Sent: ${ reply } to ${ origin }" )
    }

    /**
     * This takes the guijson/gui.json file on the classpath and delivers to the client. This is one
     * of several options for providing a GUI. This requires that the frontend uses the APSWebManager.jsx
     * component.
     *
     * @param origin The unique client address.
     */
    void renderGUIJSON( String origin ) {
//        this.logger.debug( "In renderGUIJSON!" )

        InputStream jsonStream =
                new BufferedInputStream( this.class.classLoader.getResourceAsStream( "guijson/gui.json" ) )

        Map<String, Object> gui = null

        try {
            gui = APSJson.readObject( jsonStream )
        }
        finally {
            jsonStream.close()
        }

        sendGUIToOrigin( origin, gui )
    }

    /**
     * Creates a JSON GUI specification from the supplied component hierarchy and sends it to
     * the specified client origin address. This is one way of producing a JSON GUI spec. This
     * requires that the frontend uses the APSWebManager.jsx component.
     *
     * @param origin The client address to send JSON GUI message to.
     * @param apsComponent The components to use for creating the JSON message.
     */
    void renderGUI( String origin, APSComponent apsComponent ) {

        sendGUIToOrigin( origin, apsComponent.componentProperties )
    }

    /**
     * Creates a JSON component value update message from the supplied component hierarchy and
     * sends it to the specified client address.
     *
     * @param origin The client address to send value updates to.
     * @param values A Map where the component id is the key and the value is the new value for
     * the component.
     */
    void updateGUIValues( String origin, Map<String, Object> values ) {

//        this.logger.debug( "updated values: ${ values }" )

        Map<String, Object> updatedValues = [
                aps    : [
                        origin: "aps:${ Config.APP_NAME }:backend",
                        app   : "${ Config.APP_NAME }",
                        type  : "updated-values"
                ],
                content: values
        ] as Map<String, Object>


        println "Thread: ${ Thread.currentThread().getName() }"

        this.sender.send( origin, updatedValues ) { APSResult result ->

            result.onFailure() { Exception e ->
                this.logger.error( "Failed to send updated values to client!", e )
            }
        }

//        this.logger.debug( "Sent: ${ updatedValues } to ${ origin }" )

    }
}
